#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author: cealus
@time: 2025/06/15
@description: load langchain
"""
from langchain_ollama import OllamaLLM
from openai import OpenAI

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
