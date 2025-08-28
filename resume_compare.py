#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import json
import os
from pathlib import Path
from openai import OpenAI

# API密钥
API_KEY = "xxx"

def compare_resumes(resumes_json):
    """
    比较多份简历并推荐最佳候选人
    :param resumes_json: 简历JSON字符串数组
    :return: 分析结果的JSON字符串
    """
    try:
        # 解析简历JSON
        resumes = json.loads(resumes_json)
        
        # 初始化客户端
        client = OpenAI(
            api_key=API_KEY,
            base_url="https://api.moonshot.cn/v1",
        )
        
        # 构建提示信息
        system_prompt = """你是一位专业的HR招聘专家，需要对多份简历进行对比分析，并推荐最佳候选人。
请根据以下维度进行分析：

1. 学历背景：评估候选人的学历层次（如博士、硕士、本科等）、毕业院校水平（如985、211、QS200等）、专业相关性等。

2. 工作经历：评估候选人的工作年限、公司背景（如世界500强、中国500强、国企等）、职位级别、工作职责与应聘岗位的匹配度等。

3. 语言能力：评估候选人的语言技能水平，包括普通话、英语或其他外语能力等。

请在分析后给出以下内容：
1. 对每位候选人的优缺点简要分析
2. 最终推荐的候选人及推荐理由
3. 总结性对比分析（300字以内）

请以客观、专业的口吻进行分析，避免主观臆断。"""

        # 构建用户提示
        user_prompt = f"""请对以下{len(resumes)}份简历进行对比分析，并推荐最佳候选人：

"""
        
        # 添加简历信息
        for i, resume in enumerate(resumes):
            user_prompt += f"""候选人{i+1}：{resume.get('name', '未提供')}
性别：{resume.get('gender', '未提供')}
年龄：{resume.get('age', '未提供')}
最高学历：{resume.get('highestEducation', '未提供')}

教育经历：
"""
            if 'educationExperience' in resume and resume['educationExperience']:
                for edu in resume['educationExperience']:
                    user_prompt += f"- {edu.get('startDate', '')} 至 {edu.get('endDate', '')}, {edu.get('name', '')}, {edu.get('major', '')}, {edu.get('education', '')}\n"
            else:
                user_prompt += "无教育经历信息\n"
            
            user_prompt += "\n工作经历：\n"
            if 'workExperience' in resume and resume['workExperience']:
                for work in resume['workExperience']:
                    user_prompt += f"- {work.get('startDate', '')} 至 {work.get('endDate', '')}, {work.get('companyName', '')}, {work.get('position', '')}\n  职责：{work.get('responsibility', '')}\n"
            else:
                user_prompt += "无工作经历信息\n"
            
            user_prompt += "\n语言能力：\n"
            if 'languageSkills' in resume and resume['languageSkills']:
                for lang in resume['languageSkills']:
                    user_prompt += f"- {lang.get('languageName', '')}: {lang.get('level', '')}\n"
            else:
                user_prompt += "无语言能力信息\n"
            
            user_prompt += "\n自我评价：\n"
            user_prompt += f"{resume.get('selfEvaluation', '无自我评价')}\n\n"
            
            user_prompt += "----------\n\n"
        
        # 调用API获取分析结果
        completion = client.chat.completions.create(
            model="moonshot-v1-8k",
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            temperature=0.1,
        )
        
        # 获取分析结果
        analysis_result = completion.choices[0].message.content
        
        return analysis_result
        
    except Exception as e:
        print(f"分析简历时出错: {str(e)}")
        return f"分析失败: {str(e)}"

def main():
    """
    主函数
    """
    if len(sys.argv) < 2:
        print("用法: python resume_compare.py <简历JSON数组>")
        sys.exit(1)
    
    resumes_json = sys.argv[1]
    
    result = compare_resumes(resumes_json)
    print(result)

if __name__ == "__main__":
    main() 