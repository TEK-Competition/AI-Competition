#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author: cealus
@time: 2025/06/15
@description: load langchain
"""
from langchain_ollama import OllamaLLM
from openai import OpenAI
from pymilvus import MilvusClient, DataType

from config.setting import setting


class LangChainApplication(object):
    def __init__(self):
        self.llm_service = OllamaLLM(model=setting.OLLAMA_MODEL,
                                     base_url=setting.OLLAMA_URL)

    def get_llm_answer(self, query=''):
        if query:
            return self.llm_service.invoke(query)
        return ''

    async def get_llm_astream(self, query=''):
        if query:
            async for chunk in self.llm_service.astream(query):
                yield chunk


class DashScopeApplication(object):
    def __init__(self):
        self.client = OpenAI(
            api_key=setting.DASHSCOPE_API_KEY,
            base_url=setting.DASHSCOPE_URL
        )
        self.milvus_client = MilvusClient(uri=setting.MILVUS_DB + "milvus.db")
        self.collection = "rag_collection_"

    def emb_text(self, text):
        return (
            self.client.embeddings.create(input=text, model="text-embedding-3-small")
            .data[0]
            .embedding
        )

    def get_collection_name(self, item_id):
        return self.collection + item_id

    def create_knowledge(self, item_id, name):
        collection_name = self.collection + item_id
        milvus_client = application.milvus_client
        if milvus_client.has_collection(collection_name):
            milvus_client.drop_collection(collection_name)

        text_embedding = self.emb_text(name)
        embedding_dim = len(text_embedding)
        schema = self.milvus_client.create_schema(auto_id=False, enable_dynamic_field=False)
        # Add fields to schema
        schema.add_field(field_name="id", datatype=DataType.INT64, is_primary=True)
        schema.add_field(field_name="vector", datatype=DataType.FLOAT_VECTOR, dim=embedding_dim)
        schema.add_field(field_name="text", datatype=DataType.VARCHAR, max_length=65535)
        schema.add_field(field_name="metadata", datatype=DataType.JSON)
        index_params = MilvusClient.prepare_index_params()
        index_params.add_index(
            field_name="vector",
            metric_type="COSINE",
            index_type="AUTOINDEX",
        )
        self.milvus_client.create_collection(
            collection_name=collection_name,
            schema=schema,
            index_params=index_params,
            consistency_level="Bounded",
        )
        self.milvus_client.load_collection(collection_name=collection_name)
        return collection_name

    def get_llm_answer(self, query=''):
        print(query)
        if query:
            completion = self.client.chat.completions.create(
                # https://help.aliyun.com/zh/model-studio/getting-started/models
                model=setting.DEEPSEEK_MODEL,
                messages=[
                    {'role': 'system', 'content': '你是一个数据开发人员.'},
                    {'role': 'user', 'content': query}
                ]
            )
            return completion.choices[0].message.content
        return ''


print("init")
application = DashScopeApplication()
print("start")
