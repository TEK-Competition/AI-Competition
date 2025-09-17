# AI-Competition

 - 所有参赛作品知识产权归公司所有，需符合隐私保护法规， 确保符合信息安全； 
 - 开发周期位6月到8月，截止日前按规定上传代码到指定的repo中，以个人或者团队名字命名，后面跟上location缩写和作品名称，比如: feature/YourName-SH-ProjectName 
 - 不限开发语言和平台，IOS/安卓/WEB端任选一端，后端语言不限，数据库关系型/NoSQL自选； 
 - 开发工具必须选用开源IDE以及开源框架或者社区版工具，数据库以及DB工具也只能选用开源或者社区版, AI工具也请选用开源免费的AI框架； 
- 开发中期7月的两周会有一个中期成果展示会，邀请参赛队来进行中期展示，可线上拨入；只需展示进度即可，不需要展示成品或能运行的代码; 每个地区会有一位DL的Owner，去检查每个地区的进度 
 - 单元测试代码可选，有完整的单元测试代码为加分项 
 - 参赛项目必须围绕竞赛主题，且具有一定的创新性、实用性和可行性。 
 - 参赛者需独立完成项目开发，不得抄袭或盗用他人成果。如有发现，将取消参赛资格。 
 - 展示环节需要先展示需求文档或链接，先展示文档里所设计的功能，再演示作品是否实现 
 - 在竞赛过程中，各团队应遵守公司的各项规章制度，确保项目开发过程的安全和合规。 
 - 参赛团队不得使用客户电脑，只能用自己电脑来参赛。代码中不能有任何的客户信息。不能复制粘贴任何的客户现有代码。尽量不要在客户的办公室或者客户的网络里上传代码。 
 - 所有代码将上传到公司共享盘上存档 
 - 在最后展示环节，如果报名的团队所在城市有我们能线下举办展示环节的办公室，可以邀请团队来办公室线下一起参与；如果是临近城市的，可以支持报销高铁票等。如果是报名城市太远无法线下的团队，我们也支持线上拨入 


## startup

### ollama
1. 下载ollama
[官网](https://ollama.com/)
下载并安装ollama,运行ollama，打开powershell
```bash
#pull qwen3:8b 模型
ollama.exe pull qwen3:8b
```

### mcp 服务器
1. 下载/克隆本项目
2. 使用uv
```bash
# 同步项目依赖
uv sync
# 运行
uvx ollama-mcp-bridge
```

### web 
```bash
# 进入web服务器根目录
cd .\web\ai-chart\
# 启动http server
python -m http.server 8080
```

运行效果
```bash
(ai-competition) PS C:\Users\13533\VscodeProject\AI-Competition> cd .\web\ai-chart\
(ai-competition) PS C:\Users\13533\VscodeProject\AI-Competition\web\ai-chart> python -m http.server 8080
Serving HTTP on :: port 8080 (http://[::]:8080/) ...
```