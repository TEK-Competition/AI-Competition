#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author: cealus
@time: 2025/07/2
@description: code conversion request
"""

import unittest
from unittest.mock import patch

from fastapi.testclient import TestClient

from service.conversion_service import ConversionService, conversionService, router, Request


class TestConversionService(unittest.TestCase):

    def setUp(self):
        self.service = ConversionService()

    def test_init(self):
        expected_prompt = '请将下列存储过程转换成SparkSQL。 '
        self.assertEqual(self.service.prompt_template, expected_prompt)

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_with_query(self, mock_application):
        mock_response = '<think>一些思考</think>最终的SparkSQL代码'
        mock_application.get_llm_answer.return_value = mock_response

        query = '存储过程代码示例'
        result = self.service.get_llm_answer(query)

        expected_prompt = '请将下列存储过程转换成SparkSQL。 存储过程代码示例'
        mock_application.get_llm_answer.assert_called_once_with(expected_prompt)

        self.assertEqual(result, '最终的SparkSQL代码')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_no_think_tag(self, mock_application):
        """测试 get_llm_answer 方法没有 </think> 标签时"""
        # 设置mock
        mock_response = '直接的SparkSQL代码'
        mock_application.get_llm_answer.return_value = mock_response

        query = '存储过程代码示例'
        result = self.service.get_llm_answer(query)

        # 验证结果处理（没有找到 </think> 标签，返回完整响应）
        self.assertEqual(result, '直接的SparkSQL代码')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_empty_query(self, mock_application):
        """测试 get_llm_answer 方法空查询时"""
        # 设置mock
        mock_application.get_llm_answer.return_value = '响应内容'

        result = self.service.get_llm_answer('')

        # 验证调用
        expected_prompt = '请将下列存储过程转换成SparkSQL。 '
        mock_application.get_llm_answer.assert_called_once_with(expected_prompt)

        self.assertEqual(result, '响应内容')

    @patch('app.langchain_appliaction.application')
    def test_get_llm_answer_multiple_think_tags(self, mock_application):
        mock_response = '<think>思考1</think>中间内容<think>思考2</think>最终代码'
        mock_application.get_llm_answer.return_value = mock_response

        result = self.service.get_llm_answer('查询')

        # 验证只处理第一个 </think> 标签
        self.assertEqual(result, '中间内容<think>思考2</think>最终代码')


class TestConversionServiceGlobal(unittest.TestCase):

    def test_global_instance(self):
        self.assertIsInstance(conversionService, ConversionService)
        self.assertEqual(conversionService.prompt_template,
                         '请将下列存储过程转换成SparkSQL。 ')


class TestRouter(unittest.TestCase):

    def setUp(self):
        self.client = TestClient(router)

    @patch('service.conversion_service.conversionService')
    def test_index_endpoint(self, mock_conversion_service):
        mock_conversion_service.get_llm_answer.return_value = '转换后的SparkSQL代码'

        request_data = {"sp": "存储过程代码"}

        response = self.client.post("/answer", json=request_data)

        # 验证响应
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {
            "code": 1,
            "data": "转换后的SparkSQL代码"
        })

        mock_conversion_service.get_llm_answer.assert_called_once_with("存储过程代码")

    @patch('service.conversion_service.conversionService')
    def test_index_endpoint_empty_sp(self, mock_conversion_service):
        mock_conversion_service.get_llm_answer.return_value = ''

        request_data = {"sp": ""}

        response = self.client.post("/answer", json=request_data)

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {
            "code": 1,
            "data": ""
        })

        mock_conversion_service.get_llm_answer.assert_called_once_with("")

    def test_router_configuration(self):
        self.assertEqual(router.prefix, '/conversion')
        self.assertEqual(router.tags, ['Stored procedures conversion'])


class TestRequestModel(unittest.TestCase):

    def test_request_model(self):
        request = Request(sp="存储过程代码")

        self.assertEqual(request.sp, "存储过程代码")

    def test_request_model_validation(self):
        with self.assertRaises(ValueError):
            Request(sp=None)

        request = Request(sp="")
        self.assertEqual(request.sp, "")


class TestIntegration(unittest.TestCase):

    @patch('app.application.get_llm_answer')
    def test_integration_flow(self, mock_get_llm_answer):
        mock_get_llm_answer.return_value = '<think>思考过程</think>SELECT * FROM table'

        service = ConversionService()

        result = service.get_llm_answer('存储过程代码')

        self.assertEqual(result, 'SELECT * FROM table')

        expected_prompt = '请将下列存储过程转换成SparkSQL。 存储过程代码'
        mock_get_llm_answer.assert_called_once_with(expected_prompt)


if __name__ == '__main__':
    unittest.main()
