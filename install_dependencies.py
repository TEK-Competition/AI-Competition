#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import subprocess
import os
import platform

def main():
    """
    安装所需的Python依赖
    """
    print("开始安装Python依赖...")
    
    # 获取requirements.txt文件路径
    script_dir = os.path.dirname(os.path.abspath(__file__))
    req_path = os.path.join(script_dir, "requirements.txt")
    
    # 如果在JAR包中运行，需要提取requirements.txt
    if not os.path.exists(req_path):
        # 尝试在当前目录查找
        if os.path.exists("requirements.txt"):
            req_path = "requirements.txt"
        else:
            # 创建临时requirements.txt
            req_path = os.path.join(os.path.expanduser("~"), "resume_requirements.txt")
            with open(req_path, "w") as f:
                f.write("openai>=1.0.0\npathlib>=1.0.1\n")
            print(f"已创建临时依赖文件: {req_path}")
    
    # 确定pip命令
    pip_cmd = "pip"
    if platform.system() != "Windows":
        pip_cmd = "pip3"
    
    # 安装依赖
    try:
        print(f"正在使用 {pip_cmd} 安装依赖...")
        subprocess.check_call([pip_cmd, "install", "-r", req_path])
        print("依赖安装成功!")
        return True
    except subprocess.CalledProcessError as e:
        print(f"安装依赖失败: {e}")
        print("尝试使用--user选项...")
        try:
            subprocess.check_call([pip_cmd, "install", "--user", "-r", req_path])
            print("依赖安装成功!")
            return True
        except subprocess.CalledProcessError as e2:
            print(f"安装依赖失败: {e2}")
            return False

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1) 