#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author:
@time:
@description: knowledge service
"""
import warnings

from fastapi import APIRouter
from unstructured.partition.auto import partition

from app.langchain_appliaction import application
from modules.Question import ExegesisRequest

warnings.filterwarnings("ignore")


class ExegesisService(object):

    def __init__(self):
        self.prompt_template = """现在有一个表'{}',它包含以下字段：{}.
            请根据以下内容为每一个字段添加注释,并使用JSON输出为[{name:字段1,comment :字段2}]。
            Context:"""
        self.prompt_translation = """有下列数据库字段'{}'.请对字段翻译成中文,并使用JSON输出为[{"column":"字段","translation":"翻译"}]"""

        self.milvus_client = application.milvus_client

    def retrieve_documents(self, item_id, question, top_k=3):
        collection_name = application.get_collection_name(item_id)
        search_res = self.milvus_client.search(
            collection_name=collection_name,
            data=[application.emb_text(question)],
            limit=top_k,
            # search_params={"metric_type": "IP", "params": {}},
            output_fields=["text"],
        )
        return [(res["entity"]["text"], res["distance"]) for res in search_res[0]]

    def get_llm_answer(self, item_id, table_name, field_list):
        translation = self.prompt_template.format(field_list)
        retrieved_docs = self.retrieve_documents(item_id, translation)
        context = "\n".join([f"Text: {doc[0]}\n" for doc in retrieved_docs])

        prompt = self.prompt_template.format(table_name, field_list)

        data = application.get_llm_answer(prompt + translation + context)
        index = data.find('</think>')
        if index > 0:
            data = data[index + 8:]
        index = data.find('[')
        if index > 0:
            data = data[index:]
        index = data.rfind(']')
        if index > 0:
            data = data[:index + 1]
        return data

    def add_knowledge(self, item_id, name, files):
        collection_name = application.create_knowledge(item_id, name)

        data = []
        for file in files.split(","):
            elements = partition(
                filename=file,
                strategy="hi_res",
                chunking_strategy="by_title",
            )

            for i, element in enumerate(elements):
                data.append(
                    {
                        "id": i,
                        "vector": application.emb_text(element.text),
                        "text": element.text,
                        "metadata": element.metadata.to_dict(),
                    }
                )
        self.milvus_client.insert(collection_name=collection_name, data=data)


exegesisService = ExegesisService()

router = APIRouter(
    prefix='/exegesis',
    tags=['exegesis']
)


@router.post("/answer")
def answer(request: ExegesisRequest):
    return {"code": 1, "data": exegesisService.get_llm_answer(request.id, request.name, request.list)}


@router.post("/knowledge")
def knowledge(request: ExegesisRequest):
    return {"code": 1, "data": exegesisService.add_knowledge(request.id, request.name, request.list)}
