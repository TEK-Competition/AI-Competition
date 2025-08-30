#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author: cealus
@time: 2025/06/15
@description: project configuration
"""

import os


class Setting:
    OLLAMA_MODEL = "deepseek-r1:32b"
    OLLAMA_URL = "127.0.0.1:11434"
    # DEEPSEEK_MODEL = "deepseek-r1-distill-llama-70b"
    DEEPSEEK_MODEL = "deepseek-r1-distill-qwen-32b"
    DEEPSEEK_KEY = os.getenv("DEEPSEEK_KEY")
    DEEPSEEK_URL = 'https://api.deepseek.com'
    DASHSCOPE_URL = 'https://dashscope.aliyuncs.com/compatible-mode/v1'
    DASHSCOPE_API_ID = os.getenv("DASHSCOPE_API_ID")
    DASHSCOPE_API_KEY = os.getenv("DASHSCOPE_API_KEY")

    DOCS_PATH = "docs"
    VECTOR_STORE = "cache"

    SOURCE_DATA_DIR = "." + os.sep + "data" + os.sep + "sp" + os.sep
    PRE_DATA_DIR = "." + os.sep + "data" + os.sep + "pre" + os.sep
    SQL_DATA_DIR = "." + os.sep + "data" + os.sep + "pre" + os.sep
    MILVUS_DB = "." + os.sep + "db" + os.sep + "milvus"


setting = Setting()
