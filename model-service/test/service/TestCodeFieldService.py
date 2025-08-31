#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author: cealus
@time: 2025/07/22
@description: get sp field
"""

import unittest
from unittest.mock import patch

from fastapi.testclient import TestClient

from service.field_service import CodeFieldService, codeFieldService, router, Request


class TestCodeFieldService(unittest.TestCase):

    def setUp(self):
        self.service = CodeFieldService()

    def test_init(self):
        expected_prompt = '''请找出下列存储过程中使用的表和视图,以及对应字段。
            使用->表示关联关系,->表示一个表数据来源或关联于前一个表。
            其中不考虑DELETE,PRINT,DECLARE语句。
            请输出JSON格式[{table:'表名',fields:[{name:'字段1',dataType:'数据类型'},{name:'字段2',dataType:'数据类型'}]}]
            输出使用原表名,不适用别名。不需要说明关联方式。请简短思考简洁回答,不需要解释过程.使用英文输出。下列为对应的SQL:
        '''
        self.assertEqual(self.service.prompt_template, expected_prompt)

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_with_query(self, mock_application):
        mock_response = '<think>思考过程</think>[{"table": "users", "fields": [{"name": "id", "dataType": "int"}]}]'
        mock_application.get_llm_answer.return_value = mock_response

        query = 'SELECT * FROM users;'
        result = self.service.get_llm_answer(query)

        expected_prompt = self.service.prompt_template + 'SELECT * FROM users;'
        mock_application.get_llm_answer.assert_called_once_with(expected_prompt)

        self.assertEqual(result, '[{"table": "users", "fields": [{"name": "id", "dataType": "int"}]}]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_no_think_tag(self, mock_application):
        mock_response = '[{"table": "products", "fields": [{"name": "name", "dataType": "varchar"}]}]'
        mock_application.get_llm_answer.return_value = mock_response

        query = 'SELECT name FROM products;'
        result = self.service.get_llm_answer(query)

        self.assertEqual(result, '[{"table": "products", "fields": [{"name": "name", "dataType": "varchar"}]}]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_empty_query(self, mock_application):
        mock_application.get_llm_answer.return_value = '响应内容'

        result = self.service.get_llm_answer('')

        expected_prompt = self.service.prompt_template + ''
        mock_application.get_llm_answer.assert_called_once_with(expected_prompt)

        self.assertEqual(result, '响应内容')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_json_extraction(self, mock_application):
        mock_response = '前置文本<think>思考</think>[{"table": "orders", "fields": [{"name": "amount", "dataType": "decimal"}]}]后置文本'
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('SELECT amount FROM orders;')

        self.assertEqual(result, '[{"table": "orders", "fields": [{"name": "amount", "dataType": "decimal"}]}]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_multiple_think_tags(self, mock_application):
        mock_response = '<think>思考1</think>中间内容<think>思考2</think>[{"table": "test", "fields": []}]'
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('查询')

        self.assertEqual(result, '中间内容<think>思考2</think>[{"table": "test", "fields": []}]')

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
            [{"table": "table1", "fields": [{"name": "col1", "dataType": "int"}]}]
            更多文本
            [{"table": "table2", "fields": [{"name": "col2", "dataType": "varchar"}]}]
        '''
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('复杂查询')

        expected_result = '[{"table": "table1", "fields": [{"name": "col1", "dataType": "int"}]}]'
        self.assertEqual(result.strip(), expected_result)

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_incomplete_json(self, mock_application):
        mock_response = '<think>思考</think>[{"table": "test", "fields": [{"name": "col"'
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('查询')

        self.assertEqual(result, '[{"table": "test", "fields": [{"name": "col"')


class TestCodeFieldServiceGlobal(unittest.TestCase):

    def test_global_instance(self):
        self.assertIsInstance(codeFieldService, CodeFieldService)
        self.assertEqual(codeFieldService.prompt_template,
                         '''请找出下列存储过程中使用的表和视图,以及对应字段。
             使用->表示关联关系,->表示一个表数据来源或关联于前一个表。
             其中不考虑DELETE,PRINT,DECLARE语句。
             请输出JSON格式[{table:'表名',fields:[{name:'字段1',dataType:'数据类型'},{name:'字段2',dataType:'数据类型'}]}]
             输出使用原表名,不适用别名。不需要说明关联方式。请简短思考简洁回答,不需要解释过程.使用英文输出。下列为对应的SQL:
         ''')


class TestRouter(unittest.TestCase):

    def setUp(self):
        self.client = TestClient(router)

    @patch('service.field_service.codeFieldService')
    def test_index_endpoint(self, mock_service):
        mock_service.get_llm_answer.return_value = '[{"table": "users", "fields": [{"name": "id", "dataType": "int"}]}]'
        request_data = {"sp": "SELECT id FROM users;"}
        response = self.client.post("/answer", json=request_data)
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {
            "code": 1,
            "data": '[{"table": "users", "fields": [{"name": "id", "dataType": "int"}]}]'
        })

        mock_service.get_llm_answer.assert_called_once_with("SELECT id FROM users;")

    @patch('service.field_service.codeFieldService')
    def test_index_endpoint_empty_sp(self, mock_service):
        mock_service.get_llm_answer.return_value = ''
        request_data = {"sp": ""}
        response = self.client.post("/answer", json=request_data)

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {
            "code": 1,
            "data": ""
        })

        # 验证服务调用
        mock_service.get_llm_answer.assert_called_once_with("")

    @patch('service.field_service.codeFieldService')
    def test_index_endpoint_complex_sp(self, mock_service):
        complex_json = '''
        [
            {"table": "orders", "fields": [{"name": "order_id", "dataType": "int"}, {"name": "amount", "dataType": "decimal"}]},
            {"table": "customers", "fields": [{"name": "customer_id", "dataType": "int"}, {"name": "name", "dataType": "varchar"}]}
        ]
        '''
        mock_service.get_llm_answer.return_value = complex_json

        complex_sp = """
        CREATE PROCEDURE GetOrderDetails
        AS
        BEGIN
            SELECT o.order_id, o.amount, c.name 
            FROM orders o 
            INNER JOIN customers c ON o.customer_id = c.customer_id
        END
        """
        request_data = {"sp": complex_sp}

        response = self.client.post("/answer", json=request_data)

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {
            "code": 1,
            "data": complex_json
        })

    def test_router_configuration(self):
        self.assertEqual(router.prefix, '/field')
        self.assertEqual(router.tags, ['table field'])


class TestRequestModel(unittest.TestCase):

    def test_request_model(self):
        request = Request(sp="SELECT * FROM users;")

        self.assertEqual(request.sp, "SELECT * FROM users;")

    def test_request_model_validation(self):
        with self.assertRaises(ValueError):
            Request(sp=None)

        request = Request(sp="")
        self.assertEqual(request.sp, "")


class TestEdgeCases(unittest.TestCase):

    @patch('your_module.application')
    def test_get_llm_answer_only_opening_bracket(self, mock_application):
        service = CodeFieldService()

        mock_response = '<think>思考</think>[{"table": "test", "fields": [{"name": "col"'
        mock_application.get_llm_answer.return_value = mock_response

        result = service.get_llm_answer('查询')

        self.assertEqual(result, '[{"table": "test", "fields": [{"name": "col"')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_only_closing_bracket(self, mock_application):
        service = CodeFieldService()

        mock_response = '<think>思考</think>{"table": "test", "fields": []}]'
        mock_application.get_llm_answer.return_value = mock_response

        result = service.get_llm_answer('查询')

        self.assertEqual(result, '{"table": "test", "fields": []}]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_no_brackets(self, mock_application):
        service = CodeFieldService()

        mock_response = '<think>思考</think>普通文本响应'
        mock_application.get_llm_answer.return_value = mock_response

        result = service.get_llm_answer('查询')

        self.assertEqual(result, '普通文本响应')


if __name__ == '__main__':
    unittest.main()
