#!/bin/bash

# 安装项目
npm install


# 构建项目
npm run build

# 切换到 github_pages 分支
git checkout github_pages

# 删除旧文件（保留 .git 目录）
find . -maxdepth 1 ! -name '.git' ! -name 'dist' ! -name '.' ! -name '..' -exec rm -rf {} +

# 复制 dist 内容到根目录
cp -r dist/* .

# 删除旧文件（保留 .git 目录）
find .  -name 'dist' -exec rm -rf {} +

# 提交并推送
git add .
git commit -m "Update deployment"
git push origin github_pages
