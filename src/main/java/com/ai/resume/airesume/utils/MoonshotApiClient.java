package com.ai.resume.airesume.utils;

import com.ai.resume.airesume.dto.Education;
import com.ai.resume.airesume.dto.Language;
import com.ai.resume.airesume.dto.Resume;
import com.ai.resume.airesume.dto.Work;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Moonshot API客户端，用于解析简历文件
 */
public class MoonshotApiClient {
    
    // API配置
    private static final String API_FILES_URL = "https://api.moonshot.cn/v1/files";
    private static final String API_CHAT_URL = "https://api.moonshot.cn/v1/chat/completions";
    private static final String API_KEY = "sk-r16PKjOiiZrioa3XpiqCigW3hprjLlfbULcDmWLGxmNSoe9p";
    
    private final ObjectMapper objectMapper;
    
    public MoonshotApiClient() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 解析简历文件
     * @param file 简历文件
     * @return 解析后的简历对象
     */
    public Resume parseResume(File file) {
        try {
            System.out.println("开始上传文件到Moonshot API: " + file.getName());
            
            // 第一步：上传文件获取文件ID
            String fileId = uploadFile(file);
            if (fileId == null || fileId.isEmpty()) {
                System.err.println("文件上传失败，无法获取文件ID");
                return createEmptyResume(file);
            }
            
            System.out.println("文件上传成功，文件ID: " + fileId);
            
            // 第二步：使用文件ID进行简历解析
            return extractResumeInfo(fileId, file);
            
        } catch (Exception e) {
            System.err.println("解析简历失败: " + e.getMessage());
            e.printStackTrace();
            return createEmptyResume(file);
        }
    }
    
    /**
     * 上传文件到Moonshot API
     * @param file 要上传的文件
     * @return 文件ID
     * @throws IOException 如果上传失败
     */
    private String uploadFile(File file) throws IOException, ParseException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_FILES_URL);
            
            // 设置请求头
            httpPost.setHeader("Authorization", "Bearer " + API_KEY);
            
            // 创建multipart请求体
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("purpose", "file-extract", ContentType.TEXT_PLAIN);
            builder.addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, file.getName());
            
            httpPost.setEntity(builder.build());
            
            // 发送请求并获取响应
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("文件上传响应: " + responseBody);
                
                if (response.getCode() == 200) {
                    // 解析响应获取文件ID
                    JsonNode rootNode = objectMapper.readTree(responseBody);
                    return rootNode.path("id").asText();
                } else {
                    System.err.println("文件上传失败: " + response.getCode() + " - " + responseBody);
                    throw new IOException("文件上传失败: " + response.getCode() + " - " + responseBody);
                }
            }
        }
    }
    
    /**
     * 使用文件ID提取简历信息
     * @param fileId 文件ID
     * @param originalFile 原始文件
     * @return 解析后的简历对象
     * @throws IOException 如果提取失败
     */
    private Resume extractResumeInfo(String fileId, File originalFile) throws IOException, ParseException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_CHAT_URL);
            
            // 设置请求头
            httpPost.setHeader("Authorization", "Bearer " + API_KEY);
            httpPost.setHeader("Content-Type", "application/json");
            
            // 创建请求体
            String promptTemplate = "请解析以下简历文件，并以JSON格式返回结果，格式为：\n" +
                    "{\n" +
                    "\"name\":\"姓名\",\n" +
                    "\"gender\":\"性别\",\n" +
                    "\"age\":年龄,\n" +
                    "\"highestEducation\":\"最高学历\",\n" +
                    "\"educationExperience\":[\n" +
                    "{\"startDate\":\"开始日期\",\"endDate\":\"结束日期\",\"name\":\"学校名称\",\"education\":\"学历\"},\n" +
                    "],\n" +
                    "\"workExperience\":[\n" +
                    "{\"startDate\":\"开始日期\",\"endDate\":\"结束日期\",\"companyName\":\"公司名称\",\"position\":\"职位\"}\n" +
                    "],\n" +
                    "\"languageSkills\":[\n" +
                    "{\"languageName\":\"语言名称\",\"level\":\"熟练程度\"}\n" +
                    "],\n" +
                    "\"selfEvaluation\":\"自我评价\"\n" +
                    "}";
            
            // 创建请求JSON
            ObjectNode requestNode = objectMapper.createObjectNode();
            requestNode.put("model", "moonshot-v1-8k");
            requestNode.put("temperature", 0.1);
            requestNode.put("max_tokens", 2000);
            
            ArrayNode messagesNode = objectMapper.createArrayNode();
            
            // 系统消息
            ObjectNode systemMsgNode = objectMapper.createObjectNode();
            systemMsgNode.put("role", "system");
            systemMsgNode.put("content", "你是一个专业的简历解析助手，能够从简历中提取关键信息并按照指定格式输出。");
            messagesNode.add(systemMsgNode);
            
            // 用户消息
            ObjectNode userMsgNode = objectMapper.createObjectNode();
            userMsgNode.put("role", "user");
            userMsgNode.put("content", promptTemplate);
            
            // 添加文件引用
            ArrayNode fileRefsNode = objectMapper.createArrayNode();
            ObjectNode fileRefNode = objectMapper.createObjectNode();
            fileRefNode.put("file_id", fileId);
            fileRefNode.put("type", "file");
            fileRefsNode.add(fileRefNode);
            
            // 将文件引用添加到用户消息中
            userMsgNode.set("file_ids", fileRefsNode);
            messagesNode.add(userMsgNode);
            
            // 将消息数组添加到请求中
            requestNode.set("messages", messagesNode);
            
            String requestJson = objectMapper.writeValueAsString(requestNode);
            System.out.println("发送解析请求: " + requestJson);
            
            StringEntity entity = new StringEntity(requestJson, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            
            // 发送请求并获取响应
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("简历解析响应: " + responseBody);
                
                if (response.getCode() == 200) {
                    // 解析响应获取简历信息
                    return parseResponse(responseBody, originalFile);
                } else {
                    System.err.println("简历解析失败: " + response.getCode() + " - " + responseBody);
                    throw new IOException("简历解析失败: " + response.getCode() + " - " + responseBody);
                }
            }
        }
    }
    
    /**
     * 解析API响应
     * @param responseBody API响应体
     * @param file 原始文件
     * @return 解析后的简历对象
     */
    private Resume parseResponse(String responseBody, File file) {
        try {
            // 解析API响应JSON
            JsonNode rootNode = objectMapper.readTree(responseBody);
            
            // 从响应中提取简历JSON字符串
            String resumeJsonStr = rootNode.path("choices").path(0).path("message").path("content").asText();
            
            // 提取JSON部分（可能包含在代码块中）
            resumeJsonStr = extractJsonFromText(resumeJsonStr);
            System.out.println("提取的简历JSON: " + resumeJsonStr);
            
            // 解析简历JSON
            JsonNode resumeNode = objectMapper.readTree(resumeJsonStr);
            
            // 创建简历对象
            Resume resume = new Resume();
            resume.setFilePath(file.getAbsolutePath());
            resume.setCreateDate(new Date());
            
            // 设置基本信息
            if (resumeNode.has("name")) {
                resume.setName(resumeNode.get("name").asText(""));
            }
            
            if (resumeNode.has("gender")) {
                resume.setGender(resumeNode.get("gender").asText(""));
            }
            
            if (resumeNode.has("age")) {
                try {
                    resume.setAge(resumeNode.get("age").asInt(0));
                } catch (Exception e) {
                    resume.setAge(0);
                }
            }
            
            if (resumeNode.has("highestEducation")) {
                resume.setHighestEducation(resumeNode.get("highestEducation").asText(""));
            }
            
            if (resumeNode.has("selfEvaluation")) {
                resume.setSelfEvaluation(resumeNode.get("selfEvaluation").asText(""));
            }
            
            // 设置教育经历
            List<Education> educationList = new ArrayList<>();
            if (resumeNode.has("educationExperience") && resumeNode.get("educationExperience").isArray()) {
                JsonNode educationArray = resumeNode.get("educationExperience");
                for (JsonNode eduNode : educationArray) {
                    Education education = new Education();
                    education.setStartDate(eduNode.path("startDate").asText(""));
                    education.setEndDate(eduNode.path("endDate").asText(""));
                    education.setName(eduNode.path("name").asText(""));
                    education.setEducation(eduNode.path("education").asText(""));
                    educationList.add(education);
                }
            }
            resume.setEducationExperience(educationList);
            
            // 设置工作经历
            List<Work> workList = new ArrayList<>();
            if (resumeNode.has("workExperience") && resumeNode.get("workExperience").isArray()) {
                JsonNode workArray = resumeNode.get("workExperience");
                for (JsonNode workNode : workArray) {
                    Work work = new Work();
                    work.setStartDate(workNode.path("startDate").asText(""));
                    work.setEndDate(workNode.path("endDate").asText(""));
                    work.setCompanyName(workNode.path("companyName").asText(""));
                    work.setPosition(workNode.path("position").asText(""));
                    workList.add(work);
                }
            }
            resume.setWorkExperience(workList);
            
            // 设置语言能力
            List<Language> languageList = new ArrayList<>();
            if (resumeNode.has("languageSkills") && resumeNode.get("languageSkills").isArray()) {
                JsonNode languageArray = resumeNode.get("languageSkills");
                for (JsonNode langNode : languageArray) {
                    Language language = new Language();
                    language.setLanguageName(langNode.path("languageName").asText(""));
                    language.setLevel(langNode.path("level").asText(""));
                    languageList.add(language);
                }
            }
            resume.setLanguageSkills(languageList);
            
            return resume;
            
        } catch (Exception e) {
            System.err.println("解析API响应失败: " + e.getMessage());
            e.printStackTrace();
            return createEmptyResume(file);
        }
    }
    
    /**
     * 从文本中提取JSON部分
     * @param text 包含JSON的文本
     * @return JSON字符串
     */
    private String extractJsonFromText(String text) {
        // 尝试从代码块中提取JSON
        if (text.contains("```json")) {
            int start = text.indexOf("```json") + 7;
            int end = text.indexOf("```", start);
            if (end > start) {
                return text.substring(start, end).trim();
            }
        }
        
        // 尝试从普通代码块中提取JSON
        if (text.contains("```")) {
            int start = text.indexOf("```") + 3;
            int end = text.indexOf("```", start);
            if (end > start) {
                return text.substring(start, end).trim();
            }
        }
        
        // 尝试直接查找JSON对象
        if (text.contains("{") && text.contains("}")) {
            int start = text.indexOf("{");
            int end = text.lastIndexOf("}") + 1;
            if (end > start) {
                return text.substring(start, end).trim();
            }
        }
        
        // 如果无法提取，返回原文本
        return text;
    }
    
    /**
     * 创建空的简历对象（API调用失败时使用）
     * @param file 原始文件
     * @return 空的简历对象
     */
    private Resume createEmptyResume(File file) {
        Resume resume = new Resume();
        resume.setName("未解析");
        resume.setFilePath(file.getAbsolutePath());
        resume.setCreateDate(new Date());
        return resume;
    }
} 