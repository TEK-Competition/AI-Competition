#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author: cealus
@time: 2025/08/2
@description: field exegesis service
"""

import unittest
from unittest.mock import Mock, patch

from fastapi.testclient import TestClient

from service.exegesis_service import ExegesisService, exegesisService, router, ExegesisRequest


class TestExegesisService(unittest.TestCase):

    def setUp(self):
        with patch('app.langchain_appliaction.application') as mock_app:
            mock_app.milvus_client = Mock()
            mock_app.get_collection_name = Mock(return_value="test_collection")
            mock_app.emb_text = Mock(return_value=[0.1, 0.2, 0.3])
            mock_app.get_llm_answer = Mock()
            mock_app.create_knowledge = Mock(return_value="test_collection")

            self.service = ExegesisService()
            self.service.milvus_client = Mock()

    def test_init(self):
        expected_prompt = """现在有一个表'{}',它包含以下字段：{}.
            请根据以下内容为每一个字段添加注释,并使用JSON输出为[{name:字段1,comment :字段2}]。
            Context:"""
        expected_translation = """有下列数据库字段'{}'.请对字段翻译成中文,并使用JSON输出为[{"column":"字段","translation":"翻译"}]"""

        self.assertEqual(self.service.prompt_template, expected_prompt)
        self.assertEqual(self.service.prompt_translation, expected_translation)
        self.assertIsNotNone(self.service.milvus_client)

    @patch('app.langchain_appliaction.application')
    def test_retrieve_documents(self, mock_application):
        mock_application.get_collection_name.return_value = "test_collection"
        mock_application.emb_text.return_value = [0.1, 0.2, 0.3]

        mock_search_result = [
            {
                "entity": {"text": "文档内容1"},
                "distance": 0.95
            },
            {
                "entity": {"text": "文档内容2"},
                "distance": 0.92
            }
        ]
        self.service.milvus_client.search.return_value = [mock_search_result]

        result = self.service.retrieve_documents("item123", "测试问题", 2)

        mock_application.get_collection_name.assert_called_once_with("item123")
        mock_application.emb_text.assert_called_once_with("测试问题")
        self.service.milvus_client.search.assert_called_once_with(
            collection_name="test_collection",
            data=[[0.1, 0.2, 0.3]],
            limit=2,
            output_fields=["text"]
        )

        expected_result = [("文档内容1", 0.95), ("文档内容2", 0.92)]
        self.assertEqual(result, expected_result)

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer(self, mock_application):
        mock_application.get_llm_answer.return_value = '<think>思考过程</think>[{"name": "field1", "comment": "注释1"}]'

        with patch.object(self.service, 'retrieve_documents') as mock_retrieve:
            mock_retrieve.return_value = [("文档内容1", 0.95), ("文档内容2", 0.92)]

            result = self.service.get_llm_answer("item123", "test_table", "field1,field2")

            expected_translation = self.service.prompt_translation.format("field1,field2")
            mock_retrieve.assert_called_once_with("item123", expected_translation, 3)

            expected_prompt = self.service.prompt_template.format("test_table", "field1,field2")
            expected_context = "Text: 文档内容1\nText: 文档内容2\n"
            mock_application.get_llm_answer.assert_called_once_with(
                expected_prompt + expected_translation + expected_context
            )

            self.assertEqual(result, '[{"name": "field1", "comment": "注释1"}]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_no_think_tag(self, mock_application):
        mock_application.get_llm_answer.return_value = '[{"name": "field1", "comment": "注释1"}]'

        with patch.object(self.service, 'retrieve_documents') as mock_retrieve:
            mock_retrieve.return_value = [("文档内容", 0.95)]

            result = self.service.get_llm_answer("item123", "test_table", "field1")

            self.assertEqual(result, '[{"name": "field1", "comment": "注释1"}]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_json_extraction(self, mock_application):
        mock_application.get_llm_answer.return_value = '一些前置文本[{"name": "field1", "comment": "注释1"}]一些后置文本'

        with patch.object(self.service, 'retrieve_documents') as mock_retrieve:
            mock_retrieve.return_value = [("文档内容", 0.95)]

            result = self.service.get_llm_answer("item123", "test_table", "field1")

            self.assertEqual(result, '[{"name": "field1", "comment": "注释1"}]')

    @patch('unstructured.partition.auto.partition')
    @patch('app.langchain_appliaction.application')
    def test_add_knowledge(self, mock_application, mock_partition):
        mock_application.create_knowledge.return_value = "test_collection"
        mock_application.emb_text.return_value = [0.1, 0.2, 0.3]

        mock_element1 = Mock()
        mock_element1.text = "文本内容1"
        mock_element1.metadata.to_dict.return_value = {"key": "value1"}

        mock_element2 = Mock()
        mock_element2.text = "文本内容2"
        mock_element2.metadata.to_dict.return_value = {"key": "value2"}

        mock_partition.return_value = [mock_element1, mock_element2]

        self.service.add_knowledge("item123", "test_name", "file1.pdf,file2.docx")

        mock_application.create_knowledge.assert_called_once_with("item123", "test_name")

        mock_partition.assert_any_call(
            filename="file1.pdf",
            strategy="hi_res",
            chunking_strategy="by_title"
        )
        mock_partition.assert_any_call(
            filename="file2.docx",
            strategy="hi_res",
            chunking_strategy="by_title"
        )

        self.assertEqual(mock_application.emb_text.call_count, 2)
        mock_application.emb_text.assert_any_call("文本内容1")
        mock_application.emb_text.assert_any_call("文本内容2")

        expected_data = [
            {
                "id": 0,
                "vector": [0.1, 0.2, 0.3],
                "text": "文本内容1",
                "metadata": {"key": "value1"}
            },
            {
                "id": 1,
                "vector": [0.1, 0.2, 0.3],
                "text": "文本内容2",
                "metadata": {"key": "value2"}
            }
        ]
        self.service.milvus_client.insert.assert_called_once_with(
            collection_name="test_collection",
            data=expected_data
        )

    @patch('unstructured.partition.auto.partition')
    @patch('app.langchain_appliaction.application')
    def test_add_knowledge_single_file(self, mock_application, mock_partition):
        mock_application.create_knowledge.return_value = "test_collection"
        mock_application.emb_text.return_value = [0.1, 0.2, 0.3]

        mock_element = Mock()
        mock_element.text = "文本内容"
        mock_element.metadata.to_dict.return_value = {"key": "value"}
        mock_partition.return_value = [mock_element]

        self.service.add_knowledge("item123", "test_name", "file1.pdf")

        mock_partition.assert_called_once_with(
            filename="file1.pdf",
            strategy="hi_res",
            chunking_strategy="by_title"
        )


class TestExegesisServiceGlobal(unittest.TestCase):

    @patch('app.langchain_appliaction.application')
    def test_global_instance(self, mock_application):
        mock_application.milvus_client = Mock()

        self.assertIsInstance(exegesisService, ExegesisService)
        self.assertEqual(exegesisService.prompt_template,
                         """现在有一个表'{}',它包含以下字段：{}.
             请根据以下内容为每一个字段添加注释,并使用JSON输出为[{name:字段1,comment :字段2}]。
             Context:""")


class TestRouter(unittest.TestCase):

    def setUp(self):
        self.client = TestClient(router)

    @patch('service.exegesis_service.exegesisService')
    def test_answer_endpoint(self, mock_service):
        mock_service.get_llm_answer.return_value = '[{"name": "field1", "comment": "注释1"}]'

        request_data = {"id": "item123", "name": "test_table", "list": "field1,field2"}

        response = self.client.post("/answer", json=request_data)

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {
            "code": 1,
            "data": '[{"name": "field1", "comment": "注释1"}]'
        })

        # 验证服务调用
        mock_service.get_llm_answer.assert_called_once_with("item123", "test_table", "field1,field2")

    @patch('service.exegesis_service.exegesisService')
    def test_knowledge_endpoint(self, mock_service):
        mock_service.add_knowledge.return_value = None

        request_data = {"id": "item123", "name": "test_name", "list": "file1.pdf,file2.docx"}

        response = self.client.post("/knowledge", json=request_data)

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {
            "code": 1,
            "data": None
        })

        mock_service.add_knowledge.assert_called_once_with("item123", "test_name", "file1.pdf,file2.docx")

    def test_router_configuration(self):
        self.assertEqual(router.prefix, '/exegesis')
        self.assertEqual(router.tags, ['exegesis'])


class TestExegesisRequestModel(unittest.TestCase):

    def test_request_model(self):
        request = ExegesisRequest(id="item123", name="test_table", list="field1,field2")

        self.assertEqual(request.id, "item123")
        self.assertEqual(request.name, "test_table")
        self.assertEqual(request.list, "field1,field2")

    def test_request_model_validation(self):
        with self.assertRaises(ValueError):
            ExegesisRequest(id=None, name="test", list="fields")

        with self.assertRaises(ValueError):
            ExegesisRequest(id="item123", name=None, list="fields")

        with self.assertRaises(ValueError):
            ExegesisRequest(id="item123", name="test", list=None)


class TestEdgeCases(unittest.TestCase):

    @patch('app.langchain_appliaction.application')
    def test_retrieve_documents_empty_results(self, mock_application):
        service = ExegesisService()
        service.milvus_client = Mock()

        mock_application.get_collection_name.return_value = "test_collection"
        mock_application.emb_text.return_value = [0.1, 0.2, 0.3]
        service.milvus_client.search.return_value = [[]]  # 空结果

        result = service.retrieve_documents("item123", "测试问题")

        self.assertEqual(result, [])

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_no_json_found(self, mock_application):
        service = ExegesisService()

        mock_application.get_llm_answer.return_value = "没有JSON的响应"

        with patch.object(service, 'retrieve_documents') as mock_retrieve:
            mock_retrieve.return_value = [("文档内容", 0.95)]

            result = service.get_llm_answer("item123", "test_table", "field1")

            # 验证返回原始响应
            self.assertEqual(result, "没有JSON的响应")


if __name__ == '__main__':
    unittest.main()
