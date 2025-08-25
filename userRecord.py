import yaml
from datetime import datetime

from pymongo import MongoClient

with open('config.yaml', 'r', encoding='utf-8') as file:
    config = yaml.safe_load(file)  # 安全读取 YAML 数据

def connectDB():
    client = MongoClient(config["database_uri"])
    db = client[config["database_name"]]
    coll = db[config["database_collection"]]

    return coll


def insertRecord(coll,user_id,category,content):
    record = {
        "user_id": user_id,
        "timestamp": datetime.now(),
        "category":category,
        "content": content,
    }
    coll.insert_one(record)