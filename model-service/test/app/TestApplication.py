#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author: cealus
@time: 2025/07/2
@description: get code relation
"""
from app.langchain_appliaction import application

content = application.get_llm_answer('你好啊')

print(content)