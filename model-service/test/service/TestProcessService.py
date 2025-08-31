#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author: cealus
@time: 2025/08/12
@description:  generate sp relation graph
"""

import unittest
from unittest.mock import patch

from fastapi.testclient import TestClient

from service.process_service import ProcessService, processService, router, Request


class TestProcessService(unittest.TestCase):

    def setUp(self):
        self.service = ProcessService()

    def test_init(self):
        expected_prompt = '''有以下来自多个存储过程文件的表关系信息,file表示文件,from表示来源表,to表示目标表.
            请把下列关联信息整理并生成文件流程图,使用JSON输出为{"from": "file1", "to": "file2"}.
            不需要说明关联方式。请简短思考简洁回答,不需要解释过程.使用英文输出.
            下列为对应的SQL:
        '''
        self.assertEqual(self.service.prompt_template, expected_prompt)

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_with_query(self, mock_application):
        mock_response = '<think>思考过程</think>[{"from": "sp_file1.sql", "to": "sp_file2.sql"}]'
        mock_application.get_llm_answer.return_value = mock_response

        query = 'EXEC sp_file1; EXEC sp_file2;'
        result = self.service.get_llm_answer(query)

        expected_prompt = self.service.prompt_template + 'EXEC sp_file1; EXEC sp_file2;'
        mock_application.get_llm_answer.assert_called_once_with(expected_prompt)

        self.assertEqual(result, '[{"from": "sp_file1.sql", "to": "sp_file2.sql"}]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_no_think_tag(self, mock_application):
        mock_response = '[{"from": "proc_a.sql", "to": "proc_b.sql"}]'
        mock_application.get_llm_answer.return_value = mock_response

        query = 'CALL proc_a(); CALL proc_b();'
        result = self.service.get_llm_answer(query)

        self.assertEqual(result, '[{"from": "proc_a.sql", "to": "proc_b.sql"}]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_empty_query(self, mock_application):
        mock_application.get_llm_answer.return_value = '响应内容'

        result = self.service.get_llm_answer('')

        expected_prompt = self.service.prompt_template + ''
        mock_application.get_llm_answer.assert_called_once_with(expected_prompt)

        self.assertEqual(result, '响应内容')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_json_extraction(self, mock_application):
        mock_response = '前置文本<think>思考</think>[{"from": "file1.sql", "to": "file2.sql"}]后置文本'
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('EXEC file1_procedure;')

        self.assertEqual(result, '[{"from": "file1.sql", "to": "file2.sql"}]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_multiple_think_tags(self, mock_application):
        mock_response = '<think>思考1</think>中间内容<think>思考2</think>[{"from": "src_file.sql", "to": "dest_file.sql"}]'
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('查询')

        self.assertEqual(result, '中间内容<think>思考2</think>[{"from": "src_file.sql", "to": "dest_file.sql"}]')

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
            [{"from": "sp_main.sql", "to": "sp_utils.sql"}, {"from": "sp_utils.sql", "to": "sp_helpers.sql"}]
            更多文本
        '''
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('复杂存储过程调用')

        expected_result = '[{"from": "sp_main.sql", "to": "sp_utils.sql"}, {"from": "sp_utils.sql", "to": "sp_helpers.sql"}]'
        self.assertEqual(result.strip(), expected_result)

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_incomplete_json(self, mock_application):
        mock_response = '<think>思考</think>[{"from": "proc1.sql", "to": "proc2.sql"'
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('查询')

        self.assertEqual(result, '[{"from": "proc1.sql", "to": "proc2.sql"')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_multiple_file_relations(self, mock_application):
        mock_response = '''
        <think>分析文件关系</think>
        [
            {"from": "main_procedure.sql", "to": "validation.sql"},
            {"from": "main_procedure.sql", "to": "calculation.sql"},
            {"from": "calculation.sql", "to": "math_utils.sql"}
        ]
        '''
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('多文件存储过程调用')

        expected_result = '[{"from": "main_procedure.sql", "to": "validation.sql"}, {"from": "main_procedure.sql", "to": "calculation.sql"}, {"from": "calculation.sql", "to": "math_utils.sql"}]'
        self.assertEqual(result.strip(), expected_result)

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_single_relation_object(self, mock_application):
        mock_response = '<think>思考</think>{"from": "file_a.sql", "to": "file_b.sql"}'
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('简单查询')

        self.assertEqual(result, '{"from": "file_a.sql", "to": "file_b.sql"}')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_with_file_extensions(self, mock_application):
        mock_response = '''
        <think>思考</think>
        [
            {"from": "sp_main.prc", "to": "sp_helpers.fnc"},
            {"from": "sp_helpers.fnc", "to": "sp_utils.pkg"},
            {"from": "sp_main.prc", "to": "sp_config.sql"}
        ]
        '''
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('多种文件类型调用')

        expected_result = '[{"from": "sp_main.prc", "to": "sp_helpers.fnc"}, {"from": "sp_helpers.fnc", "to": "sp_utils.pkg"}, {"from": "sp_main.prc", "to": "sp_config.sql"}]'
        self.assertEqual(result.strip(), expected_result)


class TestProcessServiceGlobal(unittest.TestCase):

    def test_global_instance(self):
        self.assertIsInstance(processService, ProcessService)
        self.assertEqual(processService.prompt_template,
                         '''有以下来自多个存储过程文件的表关系信息,file表示文件,from表示来源表,to表示目标表.
             请把下列关联信息整理并生成文件流程图,使用JSON输出为{"from": "file1", "to": "file2"}.
             不需要说明关联方式。请简短思考简洁回答,不需要解释过程.使用英文输出.
             下列为对应的SQL:
         ''')


class TestRouter(unittest.TestCase):

    def setUp(self):
        self.client = TestClient(router)

    @patch('service.process_service.processService')
    def test_index_endpoint(self, mock_service):
        mock_service.get_llm_answer.return_value = '[{"from": "sp_main.sql", "to": "sp_helpers.sql"}]'

        request_data = {"sp": "EXEC sp_main; EXEC sp_helpers;"}

        response = self.client.post("/answer", json=request_data)

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {
            "code": 1,
            "data": '[{"from": "sp_main.sql", "to": "sp_helpers.sql"}]'
        })

        mock_service.get_llm_answer.assert_called_once_with("EXEC sp_main; EXEC sp_helpers;")

    @patch('service.process_service.processService')
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

    @patch('service.process_service.processService')
    def test_index_endpoint_multiple_relations(self, mock_service):
        complex_json = '''
        [
            {"from": "main_proc.sql", "to": "validation.sql"},
            {"from": "main_proc.sql", "to": "calculation.sql"},
            {"from": "calculation.sql", "to": "math_lib.sql"}
        ]
        '''
        mock_service.get_llm_answer.return_value = complex_json

        complex_sp = """
        CREATE PROCEDURE main_proc
        AS
        BEGIN
            EXEC validation_proc;
            EXEC calculation_proc;
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
        self.assertEqual(router.prefix, '/process')
        self.assertEqual(router.tags, ['sp relation graph'])


class TestRequestModel(unittest.TestCase):

    def test_request_model(self):
        request = Request(sp="EXEC sp_test;")

        self.assertEqual(request.sp, "EXEC sp_test;")

    def test_request_model_validation(self):
        with self.assertRaises(ValueError):
            Request(sp=None)

        request = Request(sp="")
        self.assertEqual(request.sp, "")


class TestEdgeCases(unittest.TestCase):

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_only_opening_bracket(self, mock_application):
        service = ProcessService()

        mock_response = '<think>思考</think>[{"from": "file1.sql", "to": "file2.sql"'
        mock_application.get_llm_answer.return_value = mock_response

        result = service.get_llm_answer('查询')

        self.assertEqual(result, '[{"from": "file1.sql", "to": "file2.sql"')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_only_closing_bracket(self, mock_application):
        service = ProcessService()

        # 设置mock
        mock_response = '<think>思考</think>{"from": "file1.sql", "to": "file2.sql"}]'
        mock_application.get_llm_answer.return_value = mock_response

        result = service.get_llm_answer('查询')

        self.assertEqual(result, '{"from": "file1.sql", "to": "file2.sql"}]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_no_brackets(self, mock_application):
        service = ProcessService()

        # 设置mock
        mock_response = '<think>思考</think>普通文本响应'
        mock_application.get_llm_answer.return_value = mock_response

        result = service.get_llm_answer('查询')

        self.assertEqual(result, '普通文本响应')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_malformed_json(self, mock_application):
        service = ProcessService()

        mock_response = '<think>思考</think>[{"from": "file1.sql", "to": "file2.sql"}, malformed]'
        mock_application.get_llm_answer.return_value = mock_response

        result = service.get_llm_answer('查询')

        self.assertEqual(result, '[{"from": "file1.sql", "to": "file2.sql"}, malformed]')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_with_complex_file_names(self, mock_application):
        service = ProcessService()

        mock_response = '''
        <think>思考</think>
        [
            {"from": "sp_main-v1.0.sql", "to": "sp_helpers_module.sql"},
            {"from": "sp_main-v1.0.sql", "to": "sp_config_prod.sql"}
        ]
        '''
        mock_application.get_llm_answer.return_value = mock_response

        result = service.get_llm_answer('复杂文件名调用')

        expected_result = '[{"from": "sp_main-v1.0.sql", "to": "sp_helpers_module.sql"}, {"from": "sp_main-v1.0.sql", "to": "sp_config_prod.sql"}]'
        self.assertEqual(result.strip(), expected_result)


if __name__ == '__main__':
    unittest.main()
