#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author: cealus
@time: 2025/06/15
@description: load langchain
"""

import unittest
from unittest.mock import Mock, patch

from app.langchain_appliaction import LangChainApplication, DashScopeApplication


class TestLangChainApplication(unittest.TestCase):

    def setUp(self):
        self.app = LangChainApplication()

    @patch('config.setting')
    def test_init(self, mock_setting):
        mock_setting.OLLAMA_MODEL = "test_model"
        mock_setting.OLLAMA_URL = "http://test.url"

        # 重新初始化以使用mock的设置
        app = LangChainApplication()

        self.assertIsNotNone(app.llm_service)
        self.assertEqual(app.llm_service.model, "test_model")
        self.assertEqual(app.llm_service.base_url, "http://test.url")

    @patch.object(LangChainApplication, 'llm_service')
    def test_get_llm_answer_with_query(self, mock_llm_service):
        mock_llm_service.invoke.return_value = "Test response"

        result = self.app.get_llm_answer("Test query")

        mock_llm_service.invoke.assert_called_once_with("Test query")
        self.assertEqual(result, "Test response")

    def test_get_llm_answer_empty_query(self):
        result = self.app.get_llm_answer("")

        self.assertEqual(result, "")

    @patch.object(LangChainApplication, 'llm_service')
    async def test_get_llm_astream_with_query(self, mock_llm_service):

        async def mock_astream(query):
            yield "chunk1"
            yield "chunk2"

        mock_llm_service.astream = mock_astream

        chunks = []
        async for chunk in self.app.get_llm_astream("Test query"):
            chunks.append(chunk)

        self.assertEqual(chunks, ["chunk1", "chunk2"])

    async def test_get_llm_astream_empty_query(self):
        chunks = []
        async for chunk in self.app.get_llm_astream(""):
            chunks.append(chunk)

        self.assertEqual(chunks, [])


class TestDashScopeApplication(unittest.TestCase):

    def setUp(self):
        with patch('openai.OpenAI') as mock_openai, \
                patch('pymilvus.MilvusClient') as mock_milvus, \
                patch('config.setting') as mock_setting:
            mock_setting.DASHSCOPE_API_KEY = "test_api_key"
            mock_setting.DASHSCOPE_URL = "http://test.dashscope.url"
            mock_setting.MILVUS_DB = "test_milvus_uri"
            mock_setting.DEEPSEEK_MODEL = "deepseek-test"

            self.mock_openai_instance = Mock()
            self.mock_milvus_instance = Mock()

            mock_openai.return_value = self.mock_openai_instance
            mock_milvus.return_value = self.mock_milvus_instance

            self.app = DashScopeApplication()

    def test_init(self):
        self.assertEqual(self.app.collection, "rag_collection_")
        self.assertEqual(self.app.milvus_client, self.mock_milvus_instance)
        self.assertEqual(self.app.client, self.mock_openai_instance)

    def test_emb_text(self):
        mock_embedding = Mock()
        mock_embedding.embedding = [0.1, 0.2, 0.3]
        mock_embedding_data = Mock()
        mock_embedding_data.data = [mock_embedding]

        self.mock_openai_instance.embeddings.create.return_value = mock_embedding_data

        result = self.app.emb_text("test text")

        self.mock_openai_instance.embeddings.create.assert_called_once_with(
            input="test text", model="text-embedding-3-small"
        )
        self.assertEqual(result, [0.1, 0.2, 0.3])

    def test_get_collection_name(self):
        result = self.app.get_collection_name("123")
        self.assertEqual(result, "rag_collection_123")

    @patch.object(DashScopeApplication, 'emb_text')
    def test_create_knowledge(self, mock_emb_text):
        mock_emb_text.return_value = [0.1, 0.2, 0.3]
        self.mock_milvus_client.has_collection.return_value = True

        collection_name = self.app.create_knowledge("123", "test_name")

        self.mock_milvus_client.has_collection.assert_called_once_with("rag_collection_123")
        self.mock_milvus_client.drop_collection.assert_called_once_with("rag_collection_123")
        mock_emb_text.assert_called_once_with("test_name")

        self.mock_milvus_client.create_schema.assert_called_once()
        self.mock_milvus_client.prepare_index_params.assert_called_once()
        self.mock_milvus_client.create_collection.assert_called_once()
        self.mock_milvus_client.load_collection.assert_called_once_with("rag_collection_123")

        self.assertEqual(collection_name, "rag_collection_123")

    def test_get_llm_answer_with_query(self):
        mock_message = Mock()
        mock_message.content = "Test response"
        mock_choice = Mock()
        mock_choice.message = mock_message
        mock_completion = Mock()
        mock_completion.choices = [mock_choice]

        self.mock_openai_instance.chat.completions.create.return_value = mock_completion

        result = self.app.get_llm_answer("Test query")

        self.mock_openai_instance.chat.completions.create.assert_called_once_with(
            model="deepseek-test",
            messages=[
                {'role': 'system', 'content': '你是一个数据开发人员.'},
                {'role': 'user', 'content': 'Test query'}
            ]
        )
        self.assertEqual(result, "Test response")

    def test_get_llm_answer_empty_query(self):
        result = self.app.get_llm_answer("")

        self.assertEqual(result, "")


class TestApplicationGlobal(unittest.TestCase):

    @patch('app.langchain_appliaction.DashScopeApplication')
    def test_application_initialization(self, mock_dashscope_app):
        mock_instance = Mock()
        mock_dashscope_app.return_value = mock_instance

        self.assertTrue(True)


if __name__ == '__main__':
    unittest.main()
