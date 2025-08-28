package com.ai.resume.airesume.service;

import com.ai.resume.airesume.dto.InterviewRecord;
import com.ai.resume.airesume.dto.Resume;
import com.ai.resume.airesume.utils.AudioRecorder;
import com.ai.resume.airesume.utils.DatabaseManager;
import com.ai.resume.airesume.utils.PythonResumeParser;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 面试服务类，处理面试录音和分析相关的业务逻辑
 */
public class InterviewService {
    
    private final DatabaseManager databaseManager;
    private final PythonResumeParser resumeParser;
    private final AudioRecorder audioRecorder;

    /**
     * 构造函数
     */
    public InterviewService() {
        this.databaseManager = DatabaseManager.getInstance();
        this.resumeParser = new PythonResumeParser();
        String RECORDING_FOLDER = System.getProperty("user.home") + "/AiResume/recordings";
        this.audioRecorder = new AudioRecorder(RECORDING_FOLDER);
        
        // 确保录音目录存在
        File recordingDir = new File(RECORDING_FOLDER);
        if (!recordingDir.exists()) {
            recordingDir.mkdirs();
        }
    }
    
    /**
     * 开始录音
     * @param resumeId 简历ID
     * @return 是否成功开始录音
     */
    public boolean startRecording(long resumeId) {
        // 构建录音文件名（简历ID_时间戳）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        String fileName = resumeId + "_" + timestamp;
        audioRecorder.setOutputFileName(fileName);
        
        return audioRecorder.startRecording();
    }
    
    /**
     * 停止录音并保存面试记录
     * @param resumeId 简历ID
     * @return 新创建的面试记录ID，如果失败则返回-1
     */
    public long stopRecordingAndSave(long resumeId) {
        String recordingPath = audioRecorder.stopRecording();
        
        if (recordingPath != null) {
            // 创建面试记录
            File recordFile = new File(recordingPath);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String displayName = "面试录音 " + sdf.format(new Date());
            String recordingStartTime = sdf.format(new Date());
            
            InterviewRecord record = new InterviewRecord(
                    resumeId,
                    recordFile.getName(),
                    displayName,
                    recordingPath,
                    recordingStartTime
            );
            
            // 保存到数据库
            return databaseManager.insertInterviewRecord(record);
        }
        
        return -1;
    }
    
    /**
     * 根据简历ID获取所有面试记录
     * @param resumeId 简历ID
     * @return 面试记录列表
     */
    public List<InterviewRecord> getInterviewRecordsByResumeId(long resumeId) {
        return databaseManager.getInterviewRecordsByResumeId(resumeId);
    }
    
    /**
     * 根据ID获取面试记录
     * @param recordId 面试记录ID
     * @return 面试记录
     */
    public InterviewRecord getInterviewRecordById(long recordId) {
        return databaseManager.getInterviewRecordById(recordId);
    }
    
    /**
     * 删除面试记录
     * @param recordId 面试记录ID
     * @return 是否删除成功
     */
    public boolean deleteInterviewRecord(long recordId) {
        return databaseManager.deleteInterviewRecord(recordId);
    }
    
    /**
     * 异步处理面试录音识别
     * @param record 面试记录
     * @param resume 简历对象
     * @return 处理结果的CompletableFuture
     */
    public CompletableFuture<String> processInterviewRecordingAsync(InterviewRecord record, Resume resume) {
        return resumeParser.processInterviewRecordingAsync(record, resume, databaseManager);
    }
    
    /**
     * 异步进行面试分析
     * @param record 面试记录
     * @param resume 简历对象
     * @return 分析结果的CompletableFuture
     */
    public CompletableFuture<String> analyzeInterviewAsync(InterviewRecord record, Resume resume) {
        if (record.getInterviewContent() == null || record.getInterviewContent().isEmpty()) {
            return CompletableFuture.completedFuture("请先进行录音识别");
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return resumeParser.analyzeInterviewWithText(
                        record.getFilePath(), 
                        resume, 
                        record.getInterviewContent()
                );
            } catch (Exception e) {
                e.printStackTrace();
                return "分析失败: " + e.getMessage();
            }
        });
    }
    
    /**
     * 获取AudioRecorder实例
     * @return AudioRecorder实例
     */
    public AudioRecorder getAudioRecorder() {
        return audioRecorder;
    }
} 