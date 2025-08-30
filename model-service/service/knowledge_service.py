#!/usr/bin/env python
# -*- coding:utf-8 _*-
"""
@author:
@time:
@description: knowledge service
"""
from langchain.document_loaders import UnstructuredFileLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter


def add_knowledge(item_id, text):
    text_splitter = RecursiveCharacterTextSplitter(chunk_size=2000, chunk_overlap=200)
    loader = UnstructuredFileLoader(text)
    docs = loader.load()
    split_docs = text_splitter.split_documents(docs)


class KnowledgeService(object):
    pass
