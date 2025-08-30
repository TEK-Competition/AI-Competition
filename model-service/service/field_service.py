#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author:
@time:
@description: get field
"""
from fastapi import APIRouter

from app.langchain_appliaction import application
from modules.Question import Request


class CodeFieldService(object):

    def __init__(self):
        self.prompt_template = '''请找出下列存储过程中使用的表和视图,以及对应字段。
            使用->表示关联关系,->表示一个表数据来源或关联于前一个表。
            其中不考虑DELETE,PRINT,DECLARE语句。
            请输出JSON格式[{table:'表名',fields:[{name:'字段1',dataType:'数据类型'},{name:'字段2',dataType:'数据类型'}]}]
            输出使用原表名,不适用别名。不需要说明关联方式。请简短思考简洁回答,不需要解释过程.使用英文输出。下列为对应的SQL:
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


codeFieldService = CodeFieldService()

router = APIRouter(
    prefix='/field',
    tags=['table field']
)


@router.post("/answer")
def index(request: Request):
    return {"code": 1, "data": codeFieldService.get_llm_answer(request.sp)}
