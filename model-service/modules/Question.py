#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author: Cealus
@time:
@description:
"""
from pydantic import BaseModel


class Request(BaseModel):
    sp: str
    id: int


class ExegesisRequest(BaseModel):
    id: int
    tableName: str
    fields: str
