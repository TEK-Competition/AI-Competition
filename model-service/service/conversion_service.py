#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author: cealus
@time: 2025/07/2
@description: code conversion
"""
from fastapi import APIRouter

from app.langchain_appliaction import application
from modules.Question import Request


class ConversionService(object):
    def __init__(self):
        self.prompt_template = '''请将下列存储过程转换成SparkSQL。
            其中删除CREATE,PRINT,DECLARE语句,删除SELECT语句中没有FROM子句的语句。
            将INSERT和UPDATE语句转换成查询语句,并保证INSERT语句中字段映射到查询语句,
            如INSERT INTO tar_table(tar_field1,tar_field2) SELECT src_field1,src_field2 FROM src_table的语句,
            转换后为WITH tar_table AS (SELECT src_field1 AS tar_field1,src_field2 AS tar_field2 FROM src_table);
            UPDATE tar_table set tar_field1=src_field1,tar_field2=src_field2 FROM tar_table,src_table的语句,
            转换后为WITH tar_table AS (SELECT *,tar_field1 AS src_field1,tar_field2 AS src_field2 FROM tar_table,src_table);
            DELETE语句转换为查询语句,where条件取反,如DELETE FROM table where date='2025' and status=0 的语句,
            转换后为WITH table AS (SELECT * FROM table where not (date='2025' and status=0));
            其中已知CONVERT(numeric,field)转换为double(field);ltrim(rtrim(field))转换为trim(field);
            CONVERT(date,field)转换为to_date(field,yyyy/MM/dd);CONVERT(varchar,field)转换为string(field);
            NULLIF(),NVL(),COALESCE()函数转换为IFNULL();直接去除ROUND(),CAST()方法保留字段。
            输出使用原表名,不适用别名。请简短思考简洁回答,不需要解释过程.使用英文输出。下列为对应的SQL:
        '''

    def get_llm_answer(self, query=''):
        data = application.get_llm_answer(self.prompt_template + query)
        index = data.find('</think>')
        if index > 0:
            data = data[index + 8:]
        return data


conversionService = ConversionService()

router = APIRouter(
    prefix='/conversion',
    tags=['Stored procedures conversion']
)


@router.post("/answer")
def index(request: Request):
    return {"code": 1, "data": conversionService.get_llm_answer(request.sp)}
