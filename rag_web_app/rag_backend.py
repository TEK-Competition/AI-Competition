from flask import Flask, request, render_template, jsonify
import pickle
from sentence_transformers import SentenceTransformer
import faiss
from mlx_lm import load, generate
#import os
#os.environ["HF_HUB_OFFLINE"] = "1"

model = SentenceTransformer('BAAI/bge-small-zh', local_files_only=True)  # 或者换为 'BAAI/bge-small-zh'
#model = SentenceTransformer('BAAI/bge-m3', local_files_only=True)
local_path = "/Users/junli/.cache/huggingface/hub/models--Qwen--Qwen3-4B-MLX-4bit" \
"/snapshots/96690e1ccfeefd2b6405e6516e964771ade49e3a" 
llm_model,tokenizer = load(
        path_or_hf_repo=local_path
    )
loaded_index = faiss.read_index("vector_index.faiss")
with open("knowledge_texts.pkl","rb") as f:
    knowledge_texts = pickle.load(f)

def generate_prompt(question, knowledge):
    prompt = f"""
    你是一个专业、严谨、友好的 AI 助手，擅长根据提供的资料回答用户问题。

    请遵循以下规则：
    1. 你只能根据下面提供的【参考资料】回答问题，不要编造任何额外信息。
    2. 请仅选择【参考资料】中最相关的一条的内容。
    3. 如果【参考资料】中没有直接回答问题的内容，回答有相关性的内容
    3. 如果【参考资料】中不包含与用户问题相关的内容，请明确说明：“根据现有资料无法回答该问题”。
    3. 回答请尽量简洁，不要额外总结延展。

    【参考资料】
    {knowledge}

    【用户问题】
    {question}

    请根据上述【参考资料】，回答用户的【用户问题】。
    """
    return prompt

def generate_response(model, tokenizer, prompt):
    if tokenizer.chat_template is not None:
        messages=[{"role":"user", "content":prompt}]
        prompt = tokenizer.apply_chat_template(
            messages,
            add_generation_prompt=True,
            enable_thinking=False
        )
    response = generate(
        model,
        tokenizer,
        prompt=prompt,
        verbose=False,
        max_tokens=1024
     )
    return response

def get_eng_kword(input:str):
    for i in range(len(input)):
        c = input[i]
        if (c>='a' and c<='z') or (c>='A' and c<='Z'):
            key_word=''+c
            while i<len(input)-1:
                i+=1
                c=input[i]
                if (c>='a' and c<='z') or (c>='A' and c<='Z') or c==' ':
                    key_word+=c
                else:
                    break
            if len(key_word)>1:
                return key_word.strip()
    
    return ''

rag_backend = Flask(__name__)

@rag_backend.route("/")
def index():
    return render_template("index.html")  # 渲染前端页面

@rag_backend.route("/chat", methods=["POST"])
def chat():
    #print(request.json)
    user_input = request.json.get("message", "")
    mode = request.json.get("mode","")
    #print(f'mode is {mode}')
    if user_input and len(user_input.strip())>0:     
        #Check for quit command
        key_word = get_eng_kword(user_input)
        top_k_texts = set()
        if len(key_word)>0:
            for text in knowledge_texts:
                if key_word in text:
                    top_k_texts.add(text)
        # 将用户问题转为向量
        query_embedding = model.encode(user_input)  # shape: (1, d) 如果是 1D，请 reshape
        # 确保是 2D 向量，Faiss 要求形状为 (n_queries, d)，一般就是 (1, d)
        query_vector = query_embedding.reshape(1, -1)  # shape: (1, 384) 或 (1, 768)
        k = 2
        D, I = loaded_index.search(query_vector, k)  # D: 距离，I: 索引编号

        #print("检索到的索引编号:", I)
        #print("对应的距离（越小越相似）:", D)
        top_k_texts.update([knowledge_texts[i] for i in I[0]])
        '''
        print("最相关的知识片段：")
        for i, text in enumerate(top_k_texts):
            print(f"[{i+1}] {text}")
        '''
        prompt = generate_prompt(question=user_input,knowledge=top_k_texts)
        
        response = generate_response(model=llm_model,tokenizer=tokenizer,prompt=prompt)
        
        if mode=='normal':
            if '<think>' in response and '</think>' in response \
                and response.find('<think>')<response.find('</think>'):
                start = response.find('</think>')+len('</think>')
                response = response[start:]

        return jsonify({"reply": response})
    else:
        return jsonify({"reply":''})
       

if __name__ == "__main__":
    rag_backend.run(debug=True)