#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author: cealus
@time: 2025/06/15
@description: project configuration
"""
import os


class Setting:
    # use ollama
    OLLAMA_MODEL = "deepseek-r1:32b"
    OLLAMA_URL = "127.0.0.1:11434"
    # DEEPSEEK_MODEL = "deepseek-r1-distill-llama-70b"
    # MODEL config
    DEEPSEEK_MODEL = "deepseek-r1-distill-qwen-32b"
    DEEPSEEK_KEY = os.getenv("DEEPSEEK_KEY")
    DEEPSEEK_URL = 'https://api.deepseek.com'
    # DASHSCOPE config
    DASHSCOPE_URL = 'https://dashscope.aliyuncs.com/compatible-mode/v1'
    DASHSCOPE_API_ID = os.getenv("DASHSCOPE_API_ID")
    DASHSCOPE_API_KEY = os.getenv("DASHSCOPE_API_KEY")

    # milvus save path
    MILVUS_DB = "." + os.sep + "db" + os.sep + "milvus"


setting = Setting()
