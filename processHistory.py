from analyseIngredient import classify_product_category
from textPostprocess import parse_top_level_sections
from userRecord import connectDB

coll = connectDB()

model = "gpt-4.1-2025-04-14"

cursor = coll.find({"category": {"$exists": False}})  # 只处理还没有分类的

for doc in cursor:
    _id = doc["_id"]
    sections = parse_top_level_sections(doc.get("content"))
    if len(sections)==0:
        category = classify_product_category(model,doc.get("content"))
    else:
        category = classify_product_category(model,sections[0]["content"])
    # 5. 写回MongoDB
    coll.update_one({"_id": _id}, {"$set": {"category": category}})