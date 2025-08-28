#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import json
import os
import argparse
from pathlib import Path
from openai import OpenAI

# MoonshotAI API密钥
API_KEY = "xxx"

def analyze_interview(audio_file_path, resume_json, speech_text=None):
    """
    分析面试录音
    :param audio_file_path: 录音文件路径
    :param resume_json: 简历JSON字符串
    :param speech_text: 腾讯云API返回的语音识别结果，如果为None则直接处理音频文件
    :return: 分析结果的JSON字符串
    """
    try:
        # 解析简历JSON
        resume = json.loads(resume_json)
        
        # 初始化客户端
        client = OpenAI(
            api_key=API_KEY,
            base_url="https://api.moonshot.cn/v1",
        )
        
        # 如果没有提供语音识别结果，则直接处理音频文件
        if speech_text is None:
            print(f"上传文件: {audio_file_path}")
            file_object = client.files.create(file=Path(audio_file_path), purpose="file-extract")
            
            # 获取文件内容
            file_content = client.files.content(file_id=file_object.id).text
        else:
            print("使用腾讯云API返回的语音识别结果")
            file_content = f"面试录音文字转写结果：\n{speech_text}"
        
        # 构建提示信息
        system_prompt = """你是一位专业的面试评估专家，需要对面试录音进行多维度分析。
请根据录音内容，对候选人进行以下维度的评估，并给出100字以内的总结：

1. 沟通能力：评估候选人的表达清晰度、逻辑性、是否善于倾听，以及是否能够准确理解问题。可能的评价包括：沟通能力强，沟通能力不足，内向，外向，反应灵敏等。

2. 专业度：评估候选人对所申请职位相关知识的掌握程度，以及是否能够将理论知识应用到实际问题中。

3. 工作经历：评估候选人的工作背景，例如是否有世界500强、中国500强、国企等经历，以及这些经历与应聘职位的相关性。

4. 教育经历：评估候选人的学术背景，例如是否毕业于985、211、QS200、双一流等高校，以及专业与应聘职位的匹配度。

5. 语言能力：评估候选人的语言表达能力，包括普通话水平、英语或其他外语能力等。

请根据以上维度，给出一个全面、客观、专业的评估，帮助招聘方做出决策。"""

        # 构建用户提示
        user_prompt = f"""请分析这段面试录音，并给出多维度评估。
候选人信息：
姓名：{resume.get('name', '未提供')}
性别：{resume.get('gender', '未提供')}
年龄：{resume.get('age', '未提供')}
最高学历：{resume.get('highestEducation', '未提供')}

请提供100字以内的分析总结。"""
        
        # 调用API获取分析结果
        completion = client.chat.completions.create(
            model="moonshot-v1-8k",
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "system", "content": file_content},
                {"role": "user", "content": user_prompt}
            ],
            temperature=0.1,
        )
        
        # 获取分析结果
        analysis_result = completion.choices[0].message.content
        
        return analysis_result
        
    except Exception as e:
        print(f"分析面试录音时出错: {str(e)}")
        return f"分析失败: {str(e)}"

def main():
    """
    主函数
    """
    parser = argparse.ArgumentParser(description='面试录音分析工具')
    
    # 新旧两种命令行参数方式都支持
    parser.add_argument('--text', help='语音识别文本', default=None)
    parser.add_argument('--resume', help='简历JSON字符串', default=None)
    
    # 支持旧的位置参数方式
    parser.add_argument('args', nargs='*', help='旧格式的位置参数')
    
    args = parser.parse_args()
    
    # 处理旧版本的命令行参数格式
    if args.args and len(args.args) >= 2:
        audio_file_path = args.args[0]
        resume_json = args.args[1]
        speech_text = args.args[2] if len(args.args) > 2 else None
    else:
        # 处理新的命令行参数格式
        if args.text is None or args.resume is None:
            print("错误: 必须提供--text和--resume参数")
            sys.exit(1)
        
        audio_file_path = None  # 新格式不需要音频文件路径
        resume_json = args.resume
        speech_text = args.text
    
    result = analyze_interview(audio_file_path, resume_json, speech_text)
    if result:
        print(result)
    else:
        print("分析失败")
        sys.exit(1)

if __name__ == "__main__":
    main() 