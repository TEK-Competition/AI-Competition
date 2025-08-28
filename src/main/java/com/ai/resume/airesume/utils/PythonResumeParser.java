package com.ai.resume.airesume.utils;

import com.ai.resume.airesume.dto.Education;
import com.ai.resume.airesume.dto.Language;
import com.ai.resume.airesume.dto.Resume;
import com.ai.resume.airesume.dto.Work;
import com.ai.resume.airesume.dto.InterviewRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * 通过Python脚本解析简历
 */
public class PythonResumeParser {

    private static final String SECRET_ID= "xxx";
    private static final String SECRET_KEY = "xxx";

    private static final String PYTHON_SCRIPT_NAME = "resume_parser.py";
    private static final String INSTALL_SCRIPT_NAME = "install_dependencies.py";
    private static final String INTERVIEW_ANALYZER_SCRIPT_NAME = "interview_analyzer.py";
    private static final String RESUME_COMPARE_SCRIPT_NAME = "resume_compare.py";
    private final ObjectMapper objectMapper;

    public PythonResumeParser() {
        objectMapper = new ObjectMapper();
        try {
            // 确保Python脚本可用
            ensurePythonScriptAvailable(PYTHON_SCRIPT_NAME);
            ensurePythonScriptAvailable(INSTALL_SCRIPT_NAME);
            ensurePythonScriptAvailable(INTERVIEW_ANALYZER_SCRIPT_NAME);
            ensurePythonScriptAvailable(RESUME_COMPARE_SCRIPT_NAME);
            
            // 安装依赖
            installDependencies();
        } catch (Exception e) {
            System.err.println("初始化Python解析器失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 确保Python脚本可用
     * @param scriptName 脚本名称
     * @throws IOException 如果发生I/O错误
     * @throws URISyntaxException 如果URI语法错误
     */
    private void ensurePythonScriptAvailable(String scriptName) throws IOException, URISyntaxException {
        // 检查当前目录是否存在脚本
        File scriptFile = new File(scriptName);
        if (!scriptFile.exists()) {
            // 从资源中提取脚本
            Path resourcePath = Paths.get(getClass().getClassLoader().getResource("").toURI())
                    .resolve("../resources/" + scriptName);
            
            if (Files.exists(resourcePath)) {
                Files.copy(resourcePath, scriptFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                // 尝试从JAR包中提取
                try {
                    Path tempFile = Files.createTempFile(scriptName, ".py");
                    Files.copy(getClass().getClassLoader().getResourceAsStream(scriptName), tempFile, StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(tempFile, scriptFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    System.err.println("无法提取Python脚本: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 安装Python依赖
     * @throws IOException 如果发生I/O错误
     */
    private void installDependencies() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        
        // 设置命令
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            processBuilder.command("python", INSTALL_SCRIPT_NAME);
        } else {
            processBuilder.command("python3", INSTALL_SCRIPT_NAME);
        }
        
        // 执行命令
        Process process = processBuilder.start();
        
        // 读取输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        
        // 等待命令完成
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("安装Python依赖失败，退出码: " + exitCode);
            }
        } catch (InterruptedException e) {
            System.err.println("安装Python依赖被中断: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 解析简历文件
     * @param file 简历文件
     * @return 解析后的简历对象
     * @throws Exception 如果解析失败
     */
    public Resume parseResume(File file) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();
        
        // 设置命令
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            processBuilder.command("python", PYTHON_SCRIPT_NAME, file.getAbsolutePath());
        } else {
            processBuilder.command("python3", PYTHON_SCRIPT_NAME, file.getAbsolutePath());
        }
        
        // 执行命令
        Process process = processBuilder.start();
        
        // 读取输出
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        // 检查错误
        StringBuilder error = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                error.append(line).append("\n");
            }
        }
        
        // 等待命令完成
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("解析简历失败: " + error.toString());
        }
        
        // 解析JSON
        String json = output.toString().trim();
        if (json.isEmpty()) {
            throw new Exception("解析结果为空");
        }
        
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            
            // 创建简历对象
            Resume resume = new Resume();
            resume.setFilePath(file.getAbsolutePath());
            resume.setCreateDate(new Date());
            
            // 设置基本信息
            resume.setName(getTextValue(rootNode, "name"));
            resume.setGender(getTextValue(rootNode, "gender"));
            resume.setAge(getIntValue(rootNode, "age", 0));
            resume.setHighestEducation(getTextValue(rootNode, "highestEducation"));
            
            // 设置教育经历
            JsonNode educationNode = rootNode.get("educationExperience");
            if (educationNode != null && educationNode.isArray()) {
                List<Education> educationList = new ArrayList<>();
                for (JsonNode node : educationNode) {
                    Education education = new Education();
                    education.setStartDate(getTextValue(node, "startDate"));
                    education.setEndDate(getTextValue(node, "endDate"));
                    education.setName(getTextValue(node, "name"));
                    education.setEducation(getTextValue(node, "education"));
                    education.setMajor(getTextValue(node, "major"));
                    educationList.add(education);
                }
                resume.setEducationExperience(educationList);
            }
            
            // 设置工作经历
            JsonNode workNode = rootNode.get("workExperience");
            if (workNode != null && workNode.isArray()) {
                List<Work> workList = new ArrayList<>();
                for (JsonNode node : workNode) {
                    Work work = new Work();
                    work.setStartDate(getTextValue(node, "startDate"));
                    work.setEndDate(getTextValue(node, "endDate"));
                    work.setCompanyName(getTextValue(node, "companyName"));
                    work.setPosition(getTextValue(node, "position"));
                    work.setResponsibility(getTextValue(node, "responsibility"));
                    workList.add(work);
                }
                resume.setWorkExperience(workList);
            }
            
            // 设置语言能力
            JsonNode languageNode = rootNode.get("languageSkills");
            if (languageNode != null && languageNode.isArray()) {
                List<Language> languageList = new ArrayList<>();
                for (JsonNode node : languageNode) {
                    Language language = new Language();
                    language.setLanguageName(getTextValue(node, "languageName"));
                    language.setLevel(getTextValue(node, "level"));
                    languageList.add(language);
                }
                resume.setLanguageSkills(languageList);
            }
            
            // 设置自我评价
            resume.setSelfEvaluation(getTextValue(rootNode, "selfEvaluation"));
            
            return resume;
        } catch (Exception e) {
            throw new Exception("解析JSON失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 分析面试录音
     * @param audioFilePath 录音文件路径
     * @param resume 简历对象
     * @return 分析结果
     * @throws Exception 如果分析失败
     */
    public String analyzeInterview(String audioFilePath, Resume resume) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();
        
        // 将简历对象转换为JSON
        String resumeJson = objectMapper.writeValueAsString(resume);
        
        // 设置命令
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            processBuilder.command("python", INTERVIEW_ANALYZER_SCRIPT_NAME, audioFilePath, resumeJson);
        } else {
            processBuilder.command("python3", INTERVIEW_ANALYZER_SCRIPT_NAME, audioFilePath, resumeJson);
        }
        
        // 执行命令
        Process process = processBuilder.start();
        
        // 读取输出
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        // 检查错误
        StringBuilder error = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                error.append(line).append("\n");
            }
        }
        
        // 等待命令完成
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("分析面试失败: " + error.toString());
        }
        
        // 返回分析结果
        String result = output.toString().trim();
        if (result.isEmpty()) {
            throw new Exception("分析结果为空");
        }
        
        return result;
    }
    
    /**
     * 比较多份简历
     * @param resumes 简历列表
     * @return 比较结果
     * @throws Exception 如果比较失败
     */
    public String compareResumes(List<Resume> resumes) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();
        
        // 将简历列表转换为JSON
        String resumesJson = objectMapper.writeValueAsString(resumes);
        
        // 设置命令
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            processBuilder.command("python", RESUME_COMPARE_SCRIPT_NAME, resumesJson);
        } else {
            processBuilder.command("python3", RESUME_COMPARE_SCRIPT_NAME, resumesJson);
        }
        
        // 执行命令
        Process process = processBuilder.start();
        
        // 读取输出
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        // 检查错误
        StringBuilder error = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                error.append(line).append("\n");
            }
        }
        
        // 等待命令完成
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("比较简历失败: " + error.toString());
        }
        
        // 返回比较结果
        String result = output.toString().trim();
        if (result.isEmpty()) {
            throw new Exception("比较结果为空");
        }
        
        return result;
    }
    
    /**
     * 获取文本值
     * @param node JSON节点
     * @param fieldName 字段名
     * @return 文本值，如果不存在则返回空字符串
     */
    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : "";
    }
    
    /**
     * 获取整数值
     * @param node JSON节点
     * @param fieldName 字段名
     * @param defaultValue 默认值
     * @return 整数值，如果不存在则返回默认值
     */
    private int getIntValue(JsonNode node, String fieldName, int defaultValue) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() && fieldNode.isInt() ? fieldNode.asInt() : defaultValue;
    }

    /**
     * 计算TC3-HMAC-SHA256签名
     * @param secretKey 密钥
     * @param httpRequestMethod HTTP请求方法（GET、POST等）
     * @param canonicalUri 规范URI
     * @param canonicalQueryString 规范查询字符串
     * @param canonicalHeaders 规范请求头
     * @param signedHeaders 参与签名的请求头
     * @param requestPayload 请求体
     * @param timestamp 时间戳
     * @param date 日期（格式：yyyy-MM-dd）
     * @param service 服务名
     * @return 计算出的签名
     * @throws Exception 如果计算签名失败
     */
    private String calculateSignature(String secretKey, String httpRequestMethod, String canonicalUri,
                                     String canonicalQueryString, String canonicalHeaders, String signedHeaders,
                                     String requestPayload, long timestamp, String date, String service) throws Exception {
        // 步骤1：计算请求字符串哈希值
        String hashedRequestPayload = sha256Hex(requestPayload);
        
        // 步骤2：构造规范请求字符串
        String canonicalRequest = httpRequestMethod + "\n" +
                canonicalUri + "\n" +
                canonicalQueryString + "\n" +
                canonicalHeaders + "\n" +
                signedHeaders + "\n" +
                hashedRequestPayload;
        
        // 步骤3：计算规范请求字符串的哈希值
        String hashedCanonicalRequest = sha256Hex(canonicalRequest);
        
        // 步骤4：构造签名字符串
        String algorithm = "TC3-HMAC-SHA256";
        String credentialScope = date + "/" + service + "/tc3_request";
        String stringToSign = algorithm + "\n" +
                timestamp + "\n" +
                credentialScope + "\n" +
                hashedCanonicalRequest;
        
        // 步骤5：计算签名
        byte[] secretDate = hmacSha256(("TC3" + secretKey).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmacSha256(secretDate, service);
        byte[] secretSigning = hmacSha256(secretService, "tc3_request");
        byte[] signatureBytes = hmacSha256(secretSigning, stringToSign);
        
        return bytesToHex(signatureBytes);
    }
    
    /**
     * 计算字符串的SHA-256哈希值，并返回十六进制字符串
     * @param data 输入字符串
     * @return 十六进制哈希值
     * @throws Exception 如果计算哈希失败
     */
    private String sha256Hex(String data) throws Exception {
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash).toLowerCase();
    }
    
    /**
     * 计算HMAC-SHA256
     * @param key 密钥
     * @param data 数据
     * @return HMAC-SHA256结果
     * @throws Exception 如果计算失败
     */
    private byte[] hmacSha256(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 将字节数组转换为十六进制字符串
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    /**
     * 验证签名计算过程
     * @param secretKey 密钥
     * @param httpRequestMethod HTTP请求方法
     * @param canonicalUri 规范URI
     * @param canonicalQueryString 规范查询字符串
     * @param canonicalHeaders 规范请求头
     * @param signedHeaders 参与签名的请求头
     * @param requestPayload 请求体
     * @param timestamp 时间戳
     * @param date 日期
     * @param service 服务名
     * @throws Exception 如果验证失败
     */
    private void verifySignatureProcess(String secretKey, String httpRequestMethod, String canonicalUri,
                                      String canonicalQueryString, String canonicalHeaders, String signedHeaders,
                                      String requestPayload, long timestamp, String date, String service) throws Exception {
        // 步骤1：计算请求字符串哈希值
        String hashedRequestPayload = sha256Hex(requestPayload);
        System.out.println("1. HashedRequestPayload: " + hashedRequestPayload);
        
        // 步骤2：构造规范请求字符串
        String canonicalRequest = httpRequestMethod + "\n" +
                canonicalUri + "\n" +
                canonicalQueryString + "\n" +
                canonicalHeaders + "\n" +
                signedHeaders + "\n" +
                hashedRequestPayload;
        System.out.println("2. CanonicalRequest: \n" + canonicalRequest);
        
        // 步骤3：计算规范请求字符串的哈希值
        String hashedCanonicalRequest = sha256Hex(canonicalRequest);
        System.out.println("3. HashedCanonicalRequest: " + hashedCanonicalRequest);
        
        // 步骤4：构造签名字符串
        String algorithm = "TC3-HMAC-SHA256";
        String credentialScope = date + "/" + service + "/tc3_request";
        String stringToSign = algorithm + "\n" +
                timestamp + "\n" +
                credentialScope + "\n" +
                hashedCanonicalRequest;
        System.out.println("4. StringToSign: \n" + stringToSign);
        
        // 步骤5：计算签名
        byte[] secretDate = hmacSha256(("TC3" + secretKey).getBytes(StandardCharsets.UTF_8), date);
        System.out.println("5.1. SecretDate: " + bytesToHex(secretDate));
        
        byte[] secretService = hmacSha256(secretDate, service);
        System.out.println("5.2. SecretService: " + bytesToHex(secretService));
        
        byte[] secretSigning = hmacSha256(secretService, "tc3_request");
        System.out.println("5.3. SecretSigning: " + bytesToHex(secretSigning));
        
        byte[] signatureBytes = hmacSha256(secretSigning, stringToSign);
        String signature = bytesToHex(signatureBytes);
        System.out.println("5.4. Signature: " + signature);
        
        // 步骤6：构造Authorization
        String authorization = "TC3-HMAC-SHA256 " +
                "Credential=" + "XXXX" + "/" + credentialScope + ", " +
                "SignedHeaders=" + signedHeaders + ", " +
                "Signature=" + signature;
        System.out.println("6. Authorization: " + authorization);
    }
    
    /**
     * 提交录音文件到腾讯云API进行识别
     *
     * @param audioFilePath 录音文件路径
     * @return 任务ID
     * @throws Exception 如果提交失败
     */
    public long submitAudioToTencentCloud(String audioFilePath) throws Exception {
        // 读取音频文件
        File audioFile = new File(audioFilePath);
        byte[] audioBytes = Files.readAllBytes(audioFile.toPath());
        String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);
        
        // 创建JSON请求体
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("EngineModelType", "16k_zh"); // 16k采样率中文模型
        requestBody.put("ChannelNum", 1); // 单声道
        requestBody.put("ResTextFormat", 1);
        requestBody.put("SourceType", 1); // 使用音频数据
        requestBody.put("SpeakerDiarization", 1);
        requestBody.put("SpeakerNumber", 2);
        requestBody.put("Data", audioBase64);

        // 腾讯云API密钥信息
        String service = "asr"; // 服务名
        String host = "asr.tencentcloudapi.com";
        String region = "ap-guangzhou"; // 地域
        String action = "CreateRecTask"; // 接口名称
        String version = "2019-06-14"; // API版本

        // 获取当前UTC时间
        long timestamp = System.currentTimeMillis() / 1000;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date = sdf.format(new Date(timestamp * 1000));
        
        // 请求体转为JSON字符串
        String requestPayloadJson = requestBody.toString();
        
        // ************* 步骤 1：拼接规范请求串 *************
        String httpRequestMethod = "POST";
        String canonicalUri = "/";
        String canonicalQueryString = "";
        String contentType = "application/json; charset=utf-8";
        
        // 注意：x-tc-action 值需要小写，但请求头里的 X-TC-Action 不能小写
        String canonicalHeaders = "content-type:" + contentType + "\n"
                + "host:" + host + "\n"
                + "x-tc-action:" + action.toLowerCase() + "\n";
        String signedHeaders = "content-type;host;x-tc-action";
        
        // 计算请求体的哈希值
        String hashedRequestPayload = sha256Hex(requestPayloadJson);
        
        // 拼接规范请求串
        String canonicalRequest = httpRequestMethod + "\n"
                + canonicalUri + "\n"
                + canonicalQueryString + "\n"
                + canonicalHeaders + "\n"
                + signedHeaders + "\n"
                + hashedRequestPayload;
        
        System.out.println("CanonicalRequest:\n" + canonicalRequest);
        
        // ************* 步骤 2：拼接待签名字符串 *************
        String algorithm = "TC3-HMAC-SHA256";
        String credentialScope = date + "/" + service + "/" + "tc3_request";
        String hashedCanonicalRequest = sha256Hex(canonicalRequest);
        String stringToSign = algorithm + "\n"
                + timestamp + "\n"
                + credentialScope + "\n"
                + hashedCanonicalRequest;
        
        System.out.println("StringToSign:\n" + stringToSign);
        
        // ************* 步骤 3：计算签名 *************
        byte[] secretDate = hmacSha256(("TC3" + SECRET_KEY).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmacSha256(secretDate, service);
        byte[] secretSigning = hmacSha256(secretService, "tc3_request");
        String signature = bytesToHex(hmacSha256(secretSigning, stringToSign)).toLowerCase();
        
        System.out.println("Signature: " + signature);
        
        // ************* 步骤 4：拼接 Authorization *************
        String authorization = algorithm + " "
                + "Credential=" + SECRET_ID + "/" + credentialScope + ", "
                + "SignedHeaders=" + signedHeaders + ", "
                + "Signature=" + signature;
        
        System.out.println("Authorization: " + authorization);
        
        // 创建HTTP请求
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://" + host);
        
        // 设置请求头
        httpPost.setHeader("Content-Type", contentType);
        httpPost.setHeader("Host", host);
        httpPost.setHeader("X-TC-Action", action);
        httpPost.setHeader("X-TC-Timestamp", String.valueOf(timestamp));
        httpPost.setHeader("X-TC-Version", version);
        httpPost.setHeader("X-TC-Region", region);
        httpPost.setHeader("Authorization", authorization);
        
        // 设置请求体
        httpPost.setEntity(new StringEntity(requestPayloadJson, ContentType.APPLICATION_JSON));
        System.out.println("Request Body: " + requestPayloadJson);
        // 发送请求
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            System.out.println("Response Status: " + response.getCode());
            System.out.println("Response Body: " + responseBody);
            // 解析响应
            if (response.getCode() == 200) {
                JsonNode responseJson = objectMapper.readTree(responseBody);
                JsonNode data = responseJson.path("Response").path("Data");
                if (!data.isMissingNode()) {
                    return Long.parseLong(data.path("TaskId").asText());
                }
            }
            
            // 如果响应中包含错误信息，提取并显示
            JsonNode responseJson = objectMapper.readTree(responseBody);
            JsonNode error = responseJson.path("Response").path("Error");
            if (!error.isMissingNode()) {
                String errorCode = error.path("Code").asText();
                String errorMessage = error.path("Message").asText();
                throw new Exception("API调用失败: " + errorCode + " - " + errorMessage);
            }
            
            throw new Exception("提交音频文件失败: " + responseBody);
        }
    }
    
    /**
     * 查询腾讯云语音识别任务结果
     * @param taskId 任务ID
     * @return 识别结果文本
     * @throws Exception 如果查询失败
     */
    public String queryTencentCloudTaskResult(Long taskId) throws Exception {
        // 创建JSON请求体
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("TaskId", taskId);
        
        // 腾讯云API密钥信息
        String service = "asr"; // 服务名
        String host = "asr.tencentcloudapi.com";
        String region = "ap-guangzhou"; // 地域
        String action = "DescribeTaskStatus"; // 接口名称
        String version = "2019-06-14"; // API版本

        // 获取当前UTC时间
        long timestamp = System.currentTimeMillis() / 1000;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date = sdf.format(new Date(timestamp * 1000));
        
        // 请求体转为JSON字符串
        String requestPayloadJson = requestBody.toString();
        
        // ************* 步骤 1：拼接规范请求串 *************
        String httpRequestMethod = "POST";
        String canonicalUri = "/";
        String canonicalQueryString = "";
        String contentType = "application/json; charset=utf-8";
        
        // 注意：x-tc-action 值需要小写，但请求头里的 X-TC-Action 不能小写
        String canonicalHeaders = "content-type:" + contentType + "\n"
                + "host:" + host + "\n"
                + "x-tc-action:" + action.toLowerCase() + "\n";
        String signedHeaders = "content-type;host;x-tc-action";
        
        // 计算请求体的哈希值
        String hashedRequestPayload = sha256Hex(requestPayloadJson);
        
        // 拼接规范请求串
        String canonicalRequest = httpRequestMethod + "\n"
                + canonicalUri + "\n"
                + canonicalQueryString + "\n"
                + canonicalHeaders + "\n"
                + signedHeaders + "\n"
                + hashedRequestPayload;
        
        System.out.println("CanonicalRequest:\n" + canonicalRequest);
        
        // ************* 步骤 2：拼接待签名字符串 *************
        String algorithm = "TC3-HMAC-SHA256";
        String credentialScope = date + "/" + service + "/" + "tc3_request";
        String hashedCanonicalRequest = sha256Hex(canonicalRequest);
        String stringToSign = algorithm + "\n"
                + timestamp + "\n"
                + credentialScope + "\n"
                + hashedCanonicalRequest;
        
        System.out.println("StringToSign:\n" + stringToSign);
        
        // ************* 步骤 3：计算签名 *************
        byte[] secretDate = hmacSha256(("TC3" + SECRET_KEY).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmacSha256(secretDate, service);
        byte[] secretSigning = hmacSha256(secretService, "tc3_request");
        String signature = bytesToHex(hmacSha256(secretSigning, stringToSign)).toLowerCase();
        
        System.out.println("Signature: " + signature);
        
        // ************* 步骤 4：拼接 Authorization *************
        String authorization = algorithm + " "
                + "Credential=" + SECRET_ID + "/" + credentialScope + ", "
                + "SignedHeaders=" + signedHeaders + ", "
                + "Signature=" + signature;
        
        System.out.println("Authorization: " + authorization);
        
        // 创建HTTP请求
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://" + host);
        
        // 设置请求头
        httpPost.setHeader("Content-Type", contentType);
        httpPost.setHeader("Host", host);
        httpPost.setHeader("X-TC-Action", action);
        httpPost.setHeader("X-TC-Timestamp", String.valueOf(timestamp));
        httpPost.setHeader("X-TC-Version", version);
        httpPost.setHeader("X-TC-Region", region);
        httpPost.setHeader("Authorization", authorization);
        
        // 设置请求体
        httpPost.setEntity(new StringEntity(requestPayloadJson, ContentType.APPLICATION_JSON));
        
        System.out.println("Request Headers:");
        for (org.apache.hc.core5.http.Header header : httpPost.getHeaders()) {
            System.out.println(header.getName() + ": " + header.getValue());
        }
        System.out.println("Request Body: " + requestPayloadJson);
        
        // 发送请求
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            System.out.println("Response Status: " + response.getCode());
            System.out.println("Response Body: " + responseBody);
            
            // 解析响应
            if (response.getCode() == 200) {
                JsonNode responseJson = objectMapper.readTree(responseBody);
                JsonNode data = responseJson.path("Response").path("Data");
                if (!data.isMissingNode()) {
                    int status = data.path("Status").asInt();
                    if (status == 2) { // 任务完成
                        // 获取ResultDetail中的信息并按照指定格式组装结果
                        StringBuilder result = new StringBuilder();
                        JsonNode resultDetail = data.path("ResultDetail");
                        
                        if (!resultDetail.isMissingNode() && resultDetail.isArray()) {
                            // 处理ResultDetail数组
                            for (JsonNode sentence : resultDetail) {
                                // 获取所需字段
                                int speakerId = sentence.path("SpeakerId").asInt(0);
                                String finalSentence = sentence.path("FinalSentence").asText("");
                                int startMs = sentence.path("StartMs").asInt(0);
                                int endMs = sentence.path("EndMs").asInt(0);
                                
                                // 跳过空句子
                                if (finalSentence.isEmpty()) {
                                    continue;
                                }
                                
                                // 转换时间格式（毫秒->秒，保留一位小数）
                                double startSec = startMs / 1000.0;
                                double endSec = endMs / 1000.0;
                                String startSecStr = String.format("%.1f", startSec);
                                String endSecStr = String.format("%.1f", endSec);
                                
                                // SpeakerId需要+1（因为API返回的ID从0开始）
                                int displaySpeakerId = speakerId + 1;
                                
                                // 按照指定格式组装：[StartMs-EndMs] 说话人X：FinalSentence
                                result.append("[")
                                      .append(startSecStr)
                                      .append("-")
                                      .append(endSecStr)
                                      .append("] 说话人")
                                      .append(displaySpeakerId)
                                      .append("：")
                                      .append(finalSentence)
                                      .append("\n");
                            }
                        } else {
                            // 如果没有ResultDetail字段，则使用原来的Result字段
                            System.out.println("警告：API返回结果中不包含ResultDetail字段，将使用Result字段");
                            result.append(data.path("Result").asText(""));
                        }
                        
                        return result.toString().trim();
                    } else if (status == 3) { // 任务失败
                        throw new Exception("语音识别任务失败: " + data.path("ErrorMsg").asText());
                    } else { // 任务进行中
                        return null;
                    }
                }
            }
            
            // 如果响应中包含错误信息，提取并显示
            JsonNode responseJson = objectMapper.readTree(responseBody);
            JsonNode error = responseJson.path("Response").path("Error");
            if (!error.isMissingNode()) {
                String errorCode = error.path("Code").asText();
                String errorMessage = error.path("Message").asText();
                throw new Exception("API调用失败: " + errorCode + " - " + errorMessage);
            }
            
            throw new Exception("查询任务结果失败: " + responseBody);
        }
    }
    
    /**
     * 异步处理面试录音识别和分析
     * @param record 面试记录
     * @param resume 简历对象
     * @param databaseManager 数据库管理器
     * @return CompletableFuture<String> 返回分析结果的Future
     */
    public CompletableFuture<String> processInterviewRecordingAsync(InterviewRecord record, Resume resume, DatabaseManager databaseManager) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 提交录音文件到腾讯云API
                Long taskId = submitAudioToTencentCloud(record.getFilePath());
                
                // 轮询查询结果，最多等待30分钟
                String recognitionResult = null;
                int maxRetries = 180; // 30分钟，每10秒查询一次
                int retryCount = 0;
                
                while (recognitionResult == null && retryCount < maxRetries) {
                    Thread.sleep(10000); // 等待10秒
                    recognitionResult = queryTencentCloudTaskResult(taskId);
                    retryCount++;
                }
                
                if (recognitionResult == null) {
                    throw new Exception("语音识别超时");
                }
                
                // 将识别结果存入数据库
                record.setInterviewContent(recognitionResult);
                databaseManager.updateInterviewRecord(record);
                
                // 调用Python脚本进行面试分析，传入语音识别结果
//                String analysisResult = analyzeInterviewWithText(record.getFilePath(), resume, recognitionResult);
//
//                return analysisResult;
                return recognitionResult;
            } catch (Exception e) {
                e.printStackTrace();
                return "处理失败: " + e.getMessage();
            }
        });
    }
    
    /**
     * 使用文本内容进行面试分析
     * @param audioPath 音频文件路径
     * @param resume 简历对象
     * @param recognitionText 识别文本
     * @return 分析结果
     * @throws Exception 如果分析失败
     */
    public String analyzeInterviewWithText(String audioPath, Resume resume, String recognitionText) throws Exception {
        // 这里调用Python脚本进行面试分析
        String pythonPath = getProjectPath() + "/src/main/resources/interview_analyzer.py";
        
        ProcessBuilder pb = new ProcessBuilder(
                "python3",
                pythonPath,
                "--text", recognitionText,
                "--resume", objectMapper.writeValueAsString(resume)
        );
        
        Process process = pb.start();
        
        // 读取脚本输出
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        
        // 等待进程完成
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            // 读取错误输出
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
            throw new Exception("Python脚本执行失败: " + errorOutput.toString());
        }
        
        return output.toString().trim();
    }

    /**
     * 获取项目路径
     * @return 项目根路径
     */
    private String getProjectPath() {
        try {
            return new File(".").getCanonicalPath();
        } catch (IOException e) {
            return System.getProperty("user.dir");
        }
    }
} 