#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author: cealus
@time: 2025/08/12
@description:  generate table relation graph
"""

import unittest
from unittest.mock import patch

from fastapi.testclient import TestClient

from service.graph_service import GraphService, graphService, router, Request


class TestGraphService(unittest.TestCase):

    def setUp(self):
        self.service = GraphService()

    def test_init(self):
        expected_prompt = '''有以下来自多个存储过程文件的表关系信息,from表示来源表,to表示目标表.
            请把下列关联信息整理并生成表流程图,使用JSON输出为{"from": "table1", "to": "table2"}.
            不需要说明关联方式。请简短思考简洁回答,不需要解释过程.使用英文输出.
            下列为对应的SQL:
        '''
        self.assertEqual(self.service.prompt_template, expected_prompt)

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_with_query(self, mock_application):
        mock_response = '<think>思考过程</think>[{"from": "users", "to": "orders"}]'
        mock_application.get_llm_answer.return_value = mock_response

        query = 'SELECT * FROM users JOIN orders ON users.id = orders.user_id;'
        result = self.service.get_llm_answer(query)

        expected_prompt = self.service.prompt_template + 'SELECT * FROM users JOIN orders ON users.id = orders.user_id;'
        mock_application.get_llm_answer.assert_called_once_with(expected_prompt)

        self.assertEqual(result, '[{"from": "users", "to": "orders"}]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_no_think_tag(self, mock_application):
        mock_response = '[{"from": "customers", "to": "orders"}]'
        mock_application.get_llm_answer.return_value = mock_response

        query = 'SELECT * FROM customers JOIN orders;'
        result = self.service.get_llm_answer(query)

        self.assertEqual(result, '[{"from": "customers", "to": "orders"}]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_empty_query(self, mock_application):
        mock_application.get_llm_answer.return_value = '响应内容'

        result = self.service.get_llm_answer('')

        expected_prompt = self.service.prompt_template + ''
        mock_application.get_llm_answer.assert_called_once_with(expected_prompt)

        self.assertEqual(result, '响应内容')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_json_extraction(self, mock_application):
        mock_response = '前置文本<think>思考</think>[{"from": "table1", "to": "table2"}]后置文本'
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('SELECT * FROM table1 JOIN table2;')

        self.assertEqual(result, '[{"from": "table1", "to": "table2"}]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_multiple_think_tags(self, mock_application):
        mock_response = '<think>思考1</think>中间内容<think>思考2</think>[{"from": "src", "to": "dest"}]'
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('查询')

        self.assertEqual(result, '中间内容<think>思考2</think>[{"from": "src", "to": "dest"}]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_only_think_tag_no_json(self, mock_application):
        mock_response = '<think>思考过程</think>只有文本没有JSON'
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('查询')

        self.assertEqual(result, '只有文本没有JSON')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_complex_json_extraction(self, mock_application):
        mock_response = '''
            一些说明文本
            [{"from": "users", "to": "orders"}, {"from": "orders", "to": "order_items"}]
            更多文本
        '''
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('复杂查询')

        expected_result = '[{"from": "users", "to": "orders"}, {"from": "orders", "to": "order_items"}]'
        self.assertEqual(result.strip(), expected_result)

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_incomplete_json(self, mock_application):
        mock_response = '<think>思考</think>[{"from": "table1", "to": "table2"'
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('查询')

        self.assertEqual(result, '[{"from": "table1", "to": "table2"')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_multiple_relations(self, mock_application):
        mock_response = '''
        <think>分析表关系</think>
        [
            {"from": "customers", "to": "orders"},
            {"from": "orders", "to": "order_items"},
            {"from": "products", "to": "order_items"}
        ]
        '''
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('多表关联查询')

        expected_result = '[{"from": "customers", "to": "orders"}, {"from": "orders", "to": "order_items"}, {"from": "products", "to": "order_items"}]'
        self.assertEqual(result.strip(), expected_result)

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_single_relation_object(self, mock_application):
        mock_response = '<think>思考</think>{"from": "table_a", "to": "table_b"}'
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('简单查询')

        self.assertEqual(result, '{"from": "table_a", "to": "table_b"}')


class TestGraphServiceGlobal(unittest.TestCase):

    def test_global_instance(self):
        self.assertIsInstance(graphService, GraphService)
        self.assertEqual(graphService.prompt_template,
                         '''有以下来自多个存储过程文件的表关系信息,from表示来源表,to表示目标表.
             请把下列关联信息整理并生成表流程图,使用JSON输出为{"from": "table1", "to": "table2"}.
             不需要说明关联方式。请简短思考简洁回答,不需要解释过程.使用英文输出.
             下列为对应的SQL:
         ''')


class TestRouter(unittest.TestCase):

    def setUp(self):
        self.client = TestClient(router)

    @patch('service.graph_service.graphService')
    def test_index_endpoint(self, mock_service):
        mock_service.get_llm_answer.return_value = '[{"from": "users", "to": "orders"}]'
        request_data = {"sp": "SELECT * FROM users JOIN orders ON users.id = orders.user_id;"}

        response = self.client.post("/answer", json=request_data)

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {
            "code": 1,
            "data": '[{"from": "users", "to": "orders"}]'
        })

        mock_service.get_llm_answer.assert_called_once_with(
            "SELECT * FROM users JOIN orders ON users.id = orders.user_id;")

    @patch('service.graph_service.graphService')
    def test_index_endpoint_empty_sp(self, mock_service):
        mock_service.get_llm_answer.return_value = ''

        request_data = {"sp": ""}

        response = self.client.post("/answer", json=request_data)

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {
            "code": 1,
            "data": ""
        })

        mock_service.get_llm_answer.assert_called_once_with("")

    @patch('service.graph_service.graphService')
    def test_index_endpoint_multiple_relations(self, mock_service):
        complex_json = '''
        [
            {"from": "customers", "to": "orders"},
            {"from": "orders", "to": "order_items"},
            {"from": "products", "to": "order_items"}
        ]
        '''
        mock_service.get_llm_answer.return_value = complex_json

        complex_sp = """
        SELECT c.name, o.order_date, oi.quantity, p.product_name
        FROM customers c
        JOIN orders o ON c.id = o.customer_id
        JOIN order_items oi ON o.id = oi.order_id
        JOIN products p ON oi.product_id = p.id
        """
        request_data = {"sp": complex_sp}

        response = self.client.post("/answer", json=request_data)

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {
            "code": 1,
            "data": complex_json
        })

    def test_router_configuration(self):
        self.assertEqual(router.prefix, '/graph')
        self.assertEqual(router.tags, ['table relation graph'])


class TestRequestModel(unittest.TestCase):

    def test_request_model(self):
        request = Request(sp="SELECT * FROM users JOIN orders;")

        self.assertEqual(request.sp, "SELECT * FROM users JOIN orders;")

    def test_request_model_validation(self):
        with self.assertRaises(ValueError):
            Request(sp=None)

        request = Request(sp="")
        self.assertEqual(request.sp, "")


class TestEdgeCases(unittest.TestCase):

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_only_opening_bracket(self, mock_application):
        service = GraphService()

        mock_response = '<think>思考</think>[{"from": "table1", "to": "table2"'
        mock_application.get_llm_answer.return_value = mock_response

        result = service.get_llm_answer('查询')

        self.assertEqual(result, '[{"from": "table1", "to": "table2"')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_only_closing_bracket(self, mock_application):
        service = GraphService()

        mock_response = '<think>思考</think>{"from": "table1", "to": "table2"}]'
        mock_application.get_llm_answer.return_value = mock_response

        result = service.get_llm_answer('查询')

        self.assertEqual(result, '{"from": "table1", "to": "table2"}]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_no_brackets(self, mock_application):
        service = GraphService()

        mock_response = '<think>思考</think>普通文本响应'
        mock_application.get_llm_answer.return_value = mock_response

        result = service.get_llm_answer('查询')

        self.assertEqual(result, '普通文本响应')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_malformed_json(self, mock_application):
        service = GraphService()

        mock_response = '<think>思考</think>[{"from": "table1", "to": "table2"}, malformed]'
        mock_application.get_llm_answer.return_value = mock_response

        result = service.get_llm_answer('查询')

        self.assertEqual(result, '[{"from": "table1", "to": "table2"}, malformed]')


if __name__ == '__main__':
    unittest.main()
