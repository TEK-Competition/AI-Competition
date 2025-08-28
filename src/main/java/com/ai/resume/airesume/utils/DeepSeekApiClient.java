package com.ai.resume.airesume.utils;

import com.ai.resume.airesume.dto.Education;
import com.ai.resume.airesume.dto.Language;
import com.ai.resume.airesume.dto.Resume;
import com.ai.resume.airesume.dto.Work;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * DeepSeek API客户端工具类
 * 用于调用DeepSeek的API服务解析简历
 */
public class DeepSeekApiClient {
    
    // API配置
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String API_KEY = "xxx"; // 使用提供的API密钥
    
    private final ObjectMapper objectMapper;
    
    public DeepSeekApiClient() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 解析简历文件
     * @param file 简历文件
     * @return 解析后的简历对象
     */
    public Resume parseResume(File file) {
        try {
            // 读取文件内容并进行Base64编码
            byte[] fileContent = Files.readAllBytes(file.toPath());
            String encodedContent = Base64.getEncoder().encodeToString(fileContent);
            
            // 构建请求JSON
            String promptTemplate = "请解析以下简历，并以JSON格式返回结果，格式为：\n" +
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
                    "}\n" +
                    "文件内容（Base64编码）：";
            
            // 创建安全的JSON请求
            String requestJson = createSafeJsonRequest(promptTemplate, encodedContent);
            
            // 发送请求到DeepSeek API
            String responseBody = sendRequest(requestJson);
            
            // 解析响应
            return parseResponse(responseBody, file);
            
        } catch (IOException e) {
            System.err.println("解析简历失败: " + e.getMessage());
            e.printStackTrace();
            return createEmptyResume(file);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 创建安全的JSON请求，处理可能的控制字符和特殊字符
     * @param promptTemplate 提示模板
     * @param encodedContent 编码后的文件内容
     * @return 安全的JSON请求字符串
     */
    private String createSafeJsonRequest(String promptTemplate, String encodedContent) {
        try {
            // 将Base64内容分成较小的块，避免过长的字符串
            int maxChunkSize = 1000; // 每个块的最大字符数
            List<String> chunks = new ArrayList<>();
            
            for (int i = 0; i < encodedContent.length(); i += maxChunkSize) {
                int end = Math.min(i + maxChunkSize, encodedContent.length());
                chunks.add(encodedContent.substring(i, end));
            }
            
            // 构建消息内容
            StringBuilder contentBuilder = new StringBuilder(promptTemplate);
            for (String chunk : chunks) {
                contentBuilder.append(chunk);
            }
            
            // 清理JSON字符串，移除不合法的控制字符
            String content = cleanJsonString(contentBuilder.toString());
            
            // 使用ObjectMapper创建安全的JSON
            return objectMapper.writeValueAsString(
                    objectMapper.createObjectNode()
                            .put("model", "deepseek-chat")
                            .put("temperature", 0.1)
                            .put("max_tokens", 2000)
                            .set("messages", objectMapper.createArrayNode()
                                    .add(objectMapper.createObjectNode()
                                            .put("role", "user")
                                            .put("content", content))));
            
        } catch (Exception e) {
            System.err.println("创建JSON请求失败: " + e.getMessage());
            e.printStackTrace();
            
            // 回退到简单的请求，不包含文件内容
            return "{\n" +
                    "  \"model\": \"deepseek-chat\",\n" +
                    "  \"messages\": [\n" +
                    "    {\n" +
                    "      \"role\": \"user\",\n" +
                    "      \"content\": \"请解析简历并返回JSON格式\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"temperature\": 0.1,\n" +
                    "  \"max_tokens\": 2000\n" +
                    "}";
        }
    }
    
    /**
     * 清理JSON字符串，移除不合法的控制字符
     * @param input 输入字符串
     * @return 清理后的字符串
     */
    private String cleanJsonString(String input) {
        if (input == null) {
            return "";
        }
        
        StringBuilder cleaned = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            // 移除ASCII控制字符 (0x00-0x1F)，但保留制表符、换行符和回车符
            if ((c >= 0x20 || c == 0x09 || c == 0x0A || c == 0x0D) && c != 0x7F) {
                cleaned.append(c);
            }
        }
        return cleaned.toString();
    }
    
    /**
     * 发送请求到DeepSeek API
     * @param requestJson 请求JSON
     * @return API响应
     * @throws IOException 如果请求失败
     */
    private String sendRequest(String requestJson) throws IOException, ParseException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_URL);

            // 设置请求头
            httpPost.setHeader("Authorization", "Bearer " + API_KEY);
            httpPost.setHeader("Content-Type", "application/json");

            // 设置请求体
            StringEntity entity = new StringEntity(requestJson, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);

            // 打印请求信息用于调试
            System.out.println("发送请求到: " + API_URL);

            // 发送请求并获取响应
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());

                if (response.getCode() == 200) {
                    return responseBody;
                } else {
                    System.err.println("API请求失败: " + response.getCode() + " - " + responseBody);
                    throw new IOException("API请求失败: " + response.getCode() + " - " + responseBody);
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