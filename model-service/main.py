#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author: cealus
@time: 2025/06/15
@description: application
"""

import uvicorn
from fastapi import FastAPI

from service import conversion_service
from service import exegesis_service
from service import field_service
from service import graph_service
from service import process_service
from service import relation_service

app = FastAPI()


@app.get("/index")
def index():
    return {"code": 1, "message": "success"}


app.include_router(exegesis_service.router)
app.include_router(relation_service.router)
app.include_router(conversion_service.router)
app.include_router(graph_service.router)
app.include_router(process_service.router)
app.include_router(field_service.router)

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
