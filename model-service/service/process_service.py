#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author: cealus
@time: 2025/08/12
@description:  generate sp relation graph
"""
from fastapi import APIRouter

from app.langchain_appliaction import application
from modules.Question import Request


class ProcessService(object):
    def __init__(self):
        self.prompt_template = '''有以下来自多个存储过程文件的表关系信息,file表示文件,from表示来源表,to表示目标表.
            请把下列关联信息整理并生成文件流程图,使用JSON输出为{"from": "file1", "to": "file2"}.
            不需要说明关联方式。请简短思考简洁回答,不需要解释过程.使用英文输出.
            下列为对应的SQL:
        '''

    def get_llm_answer(self, query=''):
        data = application.get_llm_answer(self.prompt_template + query)
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


processService = ProcessService()

router = APIRouter(
    prefix='/process',
    tags=['sp relation graph']
)


@router.post("/answer")
def index(request: Request):
    return {"code": 1, "data": processService.get_llm_answer(request.sp)}
