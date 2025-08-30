#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author:
@time:
@description: knowledge service
"""
from fastapi import APIRouter

from app.langchain_appliaction import application
from modules.Question import ExegesisRequest


class ExegesisService(object):

    def __init__(self):
        self.prompt_template = """现在有一个表'{}',它包含以下字段：{}"""

    def get_llm_answer(self, table_name, field_list):
        prompt = self.prompt_template.format(table_name,
                                             field_list) + "请为每一个字段添加注释,并使用JSON输出为[{name:字段1,comment :字段2}]。"
        data = application.get_llm_answer(prompt)
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


exegesisService = ExegesisService()

router = APIRouter(
    prefix='/exegesis',
    tags=['exegesis']
)


@router.post("/answer")
def index(request: ExegesisRequest):
    return {"code": 1, "data": exegesisService.get_llm_answer(request.tableName, request.fields)}
