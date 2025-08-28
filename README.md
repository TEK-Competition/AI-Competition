# AI简历系统技术文档

## 1. 项目名称

AI简历系统（AiResume）

## 2. 项目功能介绍

AI简历系统是一款基于Java和Python的混合应用程序，主要提供以下功能：

1. **简历解析**
   - 支持PDF、DOC、DOCX等多种格式简历文件上传
   - 使用AI技术自动提取简历中的个人信息、教育经历、工作经验、语言能力等关键信息
   - 支持简历信息的编辑和保存

2. **简历比较**
   - 支持多份简历的对比分析
   - 从学历背景、工作经历、语言能力等多维度进行评估
   - 提供候选人优缺点分析和推荐意见

3. **面试分析**
   - 支持面试录音功能
   - 使用腾讯云语音识别API将录音转换为文本
   - 基于AI分析面试内容，评估候选人的沟通能力、专业度、工作经历等
   - 生成面试评估报告

4. **简历管理**
   - 简历列表查看
   - 简历详情查看和编辑
   - 面试记录管理

## 3. 项目使用开发技术

### 后端技术
- **Java 21**：核心编程语言
- **JavaFX**：GUI界面开发框架
- **SQLite**：轻量级关系型数据库
- **Maven**：项目构建和依赖管理工具
- **Python**：AI模型调用和数据处理

### 前端技术
- **JavaFX FXML**：界面布局
- **BootstrapFX**：美化界面样式
- **CSS**：自定义样式

### 工具库
- **Jackson**：JSON处理
- **Hutool**：Java工具类库
- **Apache HttpClient**：HTTP请求处理

### AI与机器学习
- **OpenAI Python SDK**：调用AI模型进行简历解析和分析

## 4. 项目使用的外部API

1. **腾讯云语音识别API**
   - 用于将面试录音转换为文本
   - 支持中文语音识别
   - 支持说话人分离功能

2. **MoonshotAI API**
   - 用于简历解析
   - 用于简历比较分析
   - 用于面试内容分析
   - 模型：moonshot-v1-8k

3. **DeepSeek API**
   - 作为备选的AI模型
   - 用于简历解析
   - 模型：deepseek-chat

## 5. 项目结构分层介绍

### 整体架构
项目采用标准的MVC架构模式，清晰分离各层职责：

### 控制器层 (Controller)
处理用户界面交互和事件响应：
- `ResumeController.java`：简历列表管理
- `ResumeDetailController.java`：简历详情查看和编辑
- `ResumeCompareController.java`：简历比较功能
- `InterviewRecordsController.java`：面试记录管理

### 服务层 (Service)
封装核心业务逻辑：
- `ResumeService.java`：简历相关业务处理
- `ResumeDetailService.java`：简历详情业务处理
- `CompareService.java`：简历比较业务处理
- `InterviewService.java`：面试记录和分析业务处理

### 数据传输对象 (DTO)
定义系统中的数据结构：
- `Resume.java`：简历信息
- `Education.java`：教育经历
- `Work.java`：工作经历
- `Language.java`：语言能力
- `InterviewRecord.java`：面试记录

### 工具类 (Utils)
提供通用功能和外部API调用：
- `DatabaseManager.java`：数据库操作
- `PythonResumeParser.java`：Python脚本调用
- `AudioRecorder.java`：音频录制
- `MoonshotApiClient.java`：MoonshotAI API调用
- `DeepSeekApiClient.java`：DeepSeek API调用
- `TencentCloudAPITC3D.java`：腾讯云API调用
- `CommonUtils.java`：通用工具方法
- `AudioDeviceDiagnostic.java`：音频设备诊断

### Python脚本
处理AI相关功能：
- `resume_parser.py`：简历解析
- `resume_compare.py`：简历比较
- `interview_analyzer.py`：面试分析
- `install_dependencies.py`：安装Python依赖

### 资源文件
- FXML文件：定义界面布局
- CSS文件：定义界面样式

## 6. 数据库设计

系统使用SQLite数据库存储数据，主要包含以下表：

1. **resume表**：存储简历基本信息
   - id：简历ID
   - name：姓名
   - gender：性别
   - age：年龄
   - highest_education：最高学历
   - self_evaluation：自我评价
   - file_path：简历文件路径

2. **education表**：存储教育经历
   - id：教育经历ID
   - resume_id：关联的简历ID
   - start_date：开始日期
   - end_date：结束日期
   - school_name：学校名称
   - education：学历

3. **work表**：存储工作经历
   - id：工作经历ID
   - resume_id：关联的简历ID
   - start_date：开始日期
   - end_date：结束日期
   - company_name：公司名称
   - position：职位
   - responsibility：工作职责

4. **language表**：存储语言能力
   - id：语言能力ID
   - resume_id：关联的简历ID
   - language_name：语言名称
   - level：熟练程度

5. **interview_record表**：存储面试记录
   - id：面试记录ID
   - resume_id：关联的简历ID
   - file_path：录音文件路径
   - interview_date：面试日期
   - interview_content：面试内容文本
   - analysis_result：分析结果
   - recording_start_time：录音开始时间

## 7. 系统流程

### 简历解析流程
1. 用户上传简历文件
2. 系统调用Python脚本解析简历
3. Python脚本调用MoonshotAI或DeepSeek API进行解析
4. 解析结果返回给Java程序
5. 系统将解析结果保存到数据库

### 面试分析流程
1. 用户选择简历并开始录音
2. 系统记录面试音频
3. 用户结束录音后，系统调用腾讯云语音识别API转写文本
4. 系统调用Python脚本分析面试内容
5. 分析结果显示给用户并保存到数据库

### 简历比较流程
1. 用户选择多份简历进行比较
2. 系统调用Python脚本进行比较分析
3. Python脚本调用MoonshotAI API进行多维度分析
4. 分析结果返回给用户

## 8. 部署要求

### 环境要求
- JDK 21
- Python 3.8+
- Maven 3.8+

### 依赖安装
- Java依赖通过Maven自动安装
- Python依赖通过`install_dependencies.py`脚本安装

### 运行方式
```bash
# 使用Maven Wrapper运行
./mvnw clean javafx:run
```