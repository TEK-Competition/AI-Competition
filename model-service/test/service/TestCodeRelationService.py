#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author: cealus
@time: 2025/07/2
@description: get code relation
"""
from fastapi import APIRouter

from app.langchain_appliaction import application
from modules.Question import Request
import unittest
from unittest.mock import Mock, patch, MagicMock
from fastapi import APIRouter
from fastapi.testclient import TestClient

# 导入被测试的模块和类
from service.relation_service import CodeRelationService, codeRelationService, router, Request


class TestCodeRelationService(unittest.TestCase):

    def setUp(self):
        self.service = CodeRelationService()

    def test_init(self):
        expected_prompt = '''请找出下列存储过程中使用的表和视图的关联关系。
            使用JSON格式[{"from":["table1","table2"],"to":"table3"}]输出,from表示来源,to表示目标。
            如SELECT FROM table1,table2表示[{"from":["table1","table2"],"to":"table3"}];
            INSERT table1 SELECT FROM table2,table3和UPDATE table1 FROM table2 join table3表示[{"from":["table3","table2"],"to":"table1"}]。
            其中不考虑DELETE,PRINT,DECLARE语句。如不存在关联关系则表示为{"from":"NONE","to":"table"}或{"from":"table","to":"NONE"}。
            输出使用原表名,不使用别名。不需要说明关联方式。请简短思考简洁回答,不需要解释过程.使用英文输出。下列为对应的SQL:
        '''
        self.assertEqual(self.service.prompt_template, expected_prompt)

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_with_query(self, mock_application):
        mock_response = '<think>思考过程</think>[{"from": ["users", "orders"], "to": "order_details"}]'
        mock_application.get_llm_answer.return_value = mock_response

        query = 'SELECT * FROM users JOIN orders ON users.id = orders.user_id;'
        result = self.service.get_llm_answer(query)

        expected_prompt = self.service.prompt_template + 'SELECT * FROM users JOIN orders ON users.id = orders.user_id;'
        mock_application.get_llm_answer.assert_called_once_with(expected_prompt)

        self.assertEqual(result, '[{"from": ["users", "orders"], "to": "order_details"}]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_no_think_tag(self, mock_application):
        mock_response = '[{"from": ["customers", "orders"], "to": "order_items"}]'
        mock_application.get_llm_answer.return_value = mock_response

        query = 'SELECT * FROM customers JOIN orders;'
        result = self.service.get_llm_answer(query)

        self.assertEqual(result, '[{"from": ["customers", "orders"], "to": "order_items"}]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_empty_query(self, mock_application):
        mock_application.get_llm_answer.return_value = '响应内容'

        result = self.service.get_llm_answer('')

        expected_prompt = self.service.prompt_template + ''
        mock_application.get_llm_answer.assert_called_once_with(expected_prompt)

        self.assertEqual(result, '响应内容')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_json_extraction(self, mock_application):
        mock_response = '前置文本<think>思考</think>[{"from": ["table1", "table2"], "to": "table3"}]后置文本'
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('SELECT * FROM table1, table2;')

        self.assertEqual(result, '[{"from": ["table1", "table2"], "to": "table3"}]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_multiple_think_tags(self, mock_application):
        mock_response = '<think>思考1</think>中间内容<think>思考2</think>[{"from": ["src1", "src2"], "to": "dest"}]'
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('查询')

        self.assertEqual(result, '中间内容<think>思考2</think>[{"from": ["src1", "src2"], "to": "dest"}]')

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
            [{"from": ["users", "orders"], "to": "order_details"}, {"from": ["products"], "to": "order_details"}]
            更多文本
        '''
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('复杂查询')

        expected_result = '[{"from": ["users", "orders"], "to": "order_details"}, {"from": ["products"], "to": "order_details"}]'
        self.assertEqual(result.strip(), expected_result)

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_incomplete_json(self, mock_application):
        mock_response = '<think>思考</think>[{"from": ["table1", "table2"], "to": "table3"'
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('查询')

        self.assertEqual(result, '[{"from": ["table1", "table2"], "to": "table3"')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_multiple_relations(self, mock_application):
        mock_response = '''
        <think>分析表关系</think>
        [
            {"from": ["customers"], "to": "orders"},
            {"from": ["orders", "products"], "to": "order_items"},
            {"from": ["suppliers"], "to": "products"}
        ]
        '''
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('多表关联查询')

        expected_result = '[{"from": ["customers"], "to": "orders"}, {"from": ["orders", "products"], "to": "order_items"}, {"from": ["suppliers"], "to": "products"}]'
        self.assertEqual(result.strip(), expected_result)

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_single_relation_object(self, mock_application):
        mock_response = '<think>思考</think>{"from": ["table_a"], "to": "table_b"}'
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('简单查询')

        self.assertEqual(result, '{"from": ["table_a"], "to": "table_b"}')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_with_none_relations(self, mock_application):
        mock_response = '''
        <think>思考</think>
        [
            {"from": "NONE", "to": "config_table"},
            {"from": ["users"], "to": "NONE"},
            {"from": ["table1", "table2"], "to": "result_table"}
        ]
        '''
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('包含NONE关系的查询')

        expected_result = '[{"from": "NONE", "to": "config_table"}, {"from": ["users"], "to": "NONE"}, {"from": ["table1", "table2"], "to": "result_table"}]'
        self.assertEqual(result.strip(), expected_result)

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_insert_statement(self, mock_application):
        mock_response = '<think>思考</think>[{"from": ["source_table"], "to": "target_table"}]'
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('INSERT INTO target_table SELECT * FROM source_table')

        self.assertEqual(result, '[{"from": ["source_table"], "to": "target_table"}]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_update_statement(self, mock_application):
        mock_response = '<think>思考</think>[{"from": ["joined_table"], "to": "target_table"}]'
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('UPDATE target_table FROM joined_table')

        self.assertEqual(result, '[{"from": ["joined_table"], "to": "target_table"}]')


class TestCodeRelationServiceGlobal(unittest.TestCase):

    def test_global_instance(self):

        self.assertIsInstance(codeRelationService, CodeRelationService)
        self.assertEqual(codeRelationService.prompt_template,
                         '''请找出下列存储过程中使用的表和视图的关联关系。
             使用JSON格式[{"from":["table1","table2"],"to":"table3"}]输出,from表示来源,to表示目标。
             如SELECT FROM table1,table2表示[{"from":["table1","table2"],"to":"table3"}];
             INSERT table1 SELECT FROM table2,table3和UPDATE table1 FROM table2 join table3表示[{"from":["table3","table2"],"to":"table1"}]。
             其中不考虑DELETE,PRINT,DECLARE语句。如不存在关联关系则表示为{"from":"NONE","to":"table"}或{"from":"table","to":"NONE"}。
             输出使用原表名,不使用别名。不需要说明关联方式。请简短思考简洁回答,不需要解释过程.使用英文输出。下列为对应的SQL:
         ''')


class TestRouter(unittest.TestCase):

    def setUp(self):
        self.client = TestClient(router)

    @patch('service.relation_service.codeRelationService')
    def test_index_endpoint(self, mock_service):
        mock_service.get_llm_answer.return_value = '[{"from": ["users", "orders"], "to": "order_details"}]'

        request_data = {"sp": "SELECT * FROM users JOIN orders ON users.id = orders.user_id;"}

        response = self.client.post("/answer", json=request_data)

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {
            "code": 1,
            "data": '[{"from": ["users", "orders"], "to": "order_details"}]'
        })

        mock_service.get_llm_answer.assert_called_once_with(
            "SELECT * FROM users JOIN orders ON users.id = orders.user_id;")

    @patch('service.relation_service.codeRelationService')
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

    @patch('service.relation_service.codeRelationService')
    def test_index_endpoint_multiple_relations(self, mock_service):
        complex_json = '''
        [
            {"from": ["customers"], "to": "orders"},
            {"from": ["orders", "products"], "to": "order_items"},
            {"from": ["suppliers"], "to": "products"}
        ]
        '''
        mock_service.get_llm_answer.return_value = complex_json

        complex_sp = """
        SELECT c.name, o.order_date, oi.quantity, p.product_name, s.supplier_name
        FROM customers c
        JOIN orders o ON c.id = o.customer_id
        JOIN order_items oi ON o.id = oi.order_id
        JOIN products p ON oi.product_id = p.id
        JOIN suppliers s ON p.supplier_id = s.id
        """
        request_data = {"sp": complex_sp}

        response = self.client.post("/answer", json=request_data)

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {
            "code": 1,
            "data": complex_json
        })

    def test_router_configuration(self):
        self.assertEqual(router.prefix, '/relation')
        self.assertEqual(router.tags, ['table relation'])


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
        service = CodeRelationService()

        mock_response = '<think>思考</think>[{"from": ["table1", "table2"], "to": "table3"'
        mock_application.get_llm_answer.return_value = mock_response

        result = service.get_llm_answer('查询')

        self.assertEqual(result, '[{"from": ["table1", "table2"], "to": "table3"')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_only_closing_bracket(self, mock_application):
        service = CodeRelationService()

        mock_response = '<think>思考</think>{"from": ["table1"], "to": "table2"}]'
        mock_application.get_llm_answer.return_value = mock_response

        result = service.get_llm_answer('查询')

        self.assertEqual(result, '{"from": ["table1"], "to": "table2"}]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_no_brackets(self, mock_application):
        service = CodeRelationService()

        mock_response = '<think>思考</think>普通文本响应'
        mock_application.get_llm_answer.return_value = mock_response

        result = service.get_llm_answer('查询')

        self.assertEqual(result, '普通文本响应')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_malformed_json(self, mock_application):
        service = CodeRelationService()

        mock_response = '<think>思考</think>[{"from": ["table1"], "to": "table2"}, malformed]'
        mock_application.get_llm_answer.return_value = mock_response

        result = service.get_llm_answer('查询')

        self.assertEqual(result, '[{"from": ["table1"], "to": "table2"}, malformed]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_complex_array_relations(self, mock_application):
        service = CodeRelationService()

        mock_response = '''
        <think>思考</think>
        [
            {"from": ["table1", "table2", "table3"], "to": "result_table"},
            {"from": ["table4"], "to": "NONE"},
            {"from": "NONE", "to": "config_table"}
        ]
        '''
        mock_application.get_llm_answer.return_value = mock_response

        result = service.get_llm_answer('复杂数组关系查询')

        expected_result = '[{"from": ["table1", "table2", "table3"], "to": "result_table"}, {"from": ["table4"], "to": "NONE"}, {"from": "NONE", "to": "config_table"}]'
        self.assertEqual(result.strip(), expected_result)


if __name__ == '__main__':
    unittest.main()