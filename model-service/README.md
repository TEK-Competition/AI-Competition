# code-looms

模型应用平台 version 1.0.0

# 环境配置

python3 -m venv codelooms
source codelooms/bin/activate
pip install -r requirements.txt

启动方式：uvicorn main:app --reload --timeout-keep-alive 100000
API文档：http://127.0.0.1:8000/docs

