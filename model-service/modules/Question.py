#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author: cealus
@time:
@description: LLM request model
"""
from pydantic import BaseModel


# common LLM request
class Request(BaseModel):
    sp: str
    id: int


# field exegesis  LLM request
class ExegesisRequest(BaseModel):
    id: int
    name: str
    list: str
