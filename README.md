一 项目目标
该项目代码实现基于特定知识点的AI智能问答，目前的知识库里仅有TekSystems数据安全培训的内容，但是很容易扩展。

二 主要界面与功能
目前实现了基于网页的访问http://127.0.0.1:5000，用户输入TekSystems数据安全培训的内容相关的问题，点击“发送”或回车键，把问题发送给后台，后台通过匹配，LLM推理，返回答案。
1. "Enter+Shift"可以多行输入问题
2. 点击“深度思考”，该框变绿时，进入深度思考模式，返回答案里有思考过程。再次点击变灰时，没有思考过程，只有最终答案。
3. AI回答的框支持滚动条。

三 模块与文件介绍
1. rag_web_app: 该目录即可以实现完整的项目功能
（1）templates 目录里有index.html,网页的脚本，包含静态，CSS，和javascript
 (2) knowledge_texts.pkl, python脚本存储的知识库文件，用于加载
（3） vector_index.faiss, 知识库向量存储文件，用于加载匹配
（4） rag_backend.py, 后端代码
2. knowledge_base 目录，里面有所有知识库的md原始文件，当前仅有TekSystems数据安全培训的内容，可扩展
3. trunk_embed.py, 读取knowledge_base目录中的文件，生成knowledge_texts.pkl和vector_index.faiss。当knowledge_base目录中的文件更新时，需要运行python3 trunk_embed.py来生成新的knowledge_texts.pkl和vector_index.faiss，并拷贝至rag_web_app目录

四 运行要求
1. 硬件：目前的代码要求运行在Apple M3以上芯片的电脑，16G以上内存
2. 软件环境：OS: MacOS 15以上
需要安装 python3.10以上，最新版本的transformers, PyTorch, Flask, sentence_transformers, faiss, pickle, 

