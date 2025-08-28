#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import json
import os
from pathlib import Path
from openai import OpenAI

# MoonshotAI API密钥
API_KEY = "xxx"

def parse_resume(file_path):
    """
    解析简历文件
    :param file_path: 简历文件路径
    :return: 解析结果的JSON字符串
    """
    try:
        # 初始化客户端
        client = OpenAI(
            api_key=API_KEY,
            base_url="https://api.moonshot.cn/v1",
        )
        
        # 上传文件
#         print(f"上传文件: {file_path}")
        file_object = client.files.create(file=Path(file_path), purpose="file-extract")
        
        # 获取文件内容
        file_content = client.files.content(file_id=file_object.id).text
        
        # 构建提示信息
        prompt = """请解析这份简历，并以JSON格式返回以下信息：
        {
            "name": "姓名",
            "gender": "性别",
            "age": 年龄,
            "highestEducation": "最高学历",
            "educationExperience": [
                {"startDate": "开始日期", "endDate": "结束日期", "name": "学校名称", "education": "学历"}
            ],
            "workExperience": [
                {"startDate": "开始日期", "endDate": "结束日期", "companyName": "公司名称", "position": "职位"}
            ],
            "languageSkills": [
                {"languageName": "语言名称", "level": "熟练程度"}
            ],
            "selfEvaluation": "自我评价"
        }
        请确保返回的是有效的JSON格式，不要添加额外的解释或标记。
        """
        
        # 构建消息
        messages = [
            {
                "role": "system",
                "content": "你是一个专业的简历解析助手，能够从简历中提取关键信息并按照指定格式输出。",
            },
            {
                "role": "system",
                "content": file_content,
            },
            {"role": "user", "content": prompt},
        ]
        
        # 调用API获取解析结果
#         print("调用MoonshotAI API解析简历...")
        completion = client.chat.completions.create(
            model="moonshot-v1-8k",
            messages=messages,
            temperature=0.1,
        )
        
        # 提取响应内容
        response_content = completion.choices[0].message.content
        
        # 提取JSON部分
        json_content = extract_json(response_content)
        
        # 返回解析结果
        return json_content
        
    except Exception as e:
        error_message = {
            "error": True,
            "message": str(e)
        }
        return json.dumps(error_message, ensure_ascii=False)

def extract_json(text):
    """
    从文本中提取JSON部分
    :param text: 包含JSON的文本
    :return: JSON字符串
    """
    # 尝试从代码块中提取JSON
    if "```json" in text:
        start = text.find("```json") + 7
        end = text.find("```", start)
        if end > start:
            return text[start:end].strip()
    
    # 尝试从普通代码块中提取JSON
    if "```" in text:
        start = text.find("```") + 3
        end = text.find("```", start)
        if end > start:
            return text[start:end].strip()
    
    # 尝试直接查找JSON对象
    if "{" in text and "}" in text:
        start = text.find("{")
        end = text.rfind("}") + 1
        if end > start:
            return text[start:end].strip()
    
    # 如果无法提取，返回原文本
    return text

if __name__ == "__main__":
    # 检查命令行参数
    if len(sys.argv) != 2:
        print(json.dumps({"error": True, "message": "使用方法: python resume_parser.py <文件路径>"}, ensure_ascii=False))
        sys.exit(1)
    
    # 获取文件路径参数
    file_path = sys.argv[1]
    
    # 检查文件是否存在
    if not os.path.exists(file_path):
        print(json.dumps({"error": True, "message": f"文件不存在: {file_path}"}, ensure_ascii=False))
        sys.exit(1)
    
    # 解析简历并打印结果
    result = parse_resume(file_path)
    print(result) 