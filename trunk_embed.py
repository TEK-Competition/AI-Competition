from sentence_transformers import SentenceTransformer
import faiss
import os
import pickle

# 1. 加载 Embedding 模型（推荐中文场景使用 BGE，这里先用一个通用多语言模型）
model = SentenceTransformer('BAAI/bge-small-zh')  # 或者换为 'BAAI/bge-small-zh'
#model = SentenceTransformer('BAAI/bge-m3')
# 2. 读取知识库文件夹中的所有 .md 文件
knowledge_dir = "./knowledge_base"
docs = []
chunks = []

for filename in os.listdir(knowledge_dir):
    if filename.endswith(".md"):
        with open(os.path.join(knowledge_dir, filename), "r", encoding="utf-8") as f:
            text = f.read()
            # 简单切块：按双换行符分割段落（可改进）
            text_chunks = text.split("\n\n")  # 按段落切，你可优化为按句子/长度
            for i, chunk in enumerate(text_chunks):
                chunk = chunk.strip()
                if len(chunk) > 10:  # 过滤掉太短的块
                    docs.append(chunk)
                    chunks.append(chunk)

with open("knowledge_texts.pkl","wb") as f:
    pickle.dump(chunks,f)
    
# 3. 对每个文本块生成向量
print(f"正在为 {len(chunks)} 个文本块生成向量...")
embeddings = model.encode(chunks, show_progress_bar=True)  # shape: [n_chunks, 384 或 768]

# 4. 构建 Faiss 向量索引
dimension = embeddings.shape[1]  # 向量维度，比如 384
print(f'the dimension of embeddings is {dimension}')
index = faiss.IndexFlatL2(dimension)  # 使用 L2 距离（欧几里得距离）
print(f"正在构建索引，向量维度：{dimension}")
index.add(embeddings)  # 把所有向量加入索引
print("向量索引构建完成！")

# 可将 index 保存到磁盘，后续直接加载使用
faiss.write_index(index, "vector_index.faiss")
print("向量索引已保存为 vector_index.faiss")