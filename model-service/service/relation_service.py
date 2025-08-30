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


class CodeRelationService(object):
    def __init__(self):
        self.prompt_template = '''请找出下列存储过程中使用的表和视图的关联关系。
            使用JSON格式[{"from":["table1","table2"],"to":"table3"}]输出,from表示来源,to表示目标。
            如SELECT FROM table1,table2表示[{"from":["table1","table2"],"to":"table3"}];
            INSERT table1 SELECT FROM table2,table3和UPDATE table1 FROM table2 join table3表示[{"from":["table3","table2"],"to":"table1"}]。
            其中不考虑DELETE,PRINT,DECLARE语句。如不存在关联关系则表示为{"from":"NONE","to":"table"}或{"from":"table","to":"NONE"}。
            输出使用原表名,不使用别名。不需要说明关联方式。请简短思考简洁回答,不需要解释过程.使用英文输出。下列为对应的SQL:
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


codeRelationService = CodeRelationService()

router = APIRouter(
    prefix='/relation',
    tags=['table relation']
)


@router.post("/answer")
def index(request: Request):
    return {"code": 1, "data": codeRelationService.get_llm_answer(request.sp)}
