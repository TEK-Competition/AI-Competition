package com.ai.resume.airesume.dto;

/**
 * 面试记录实体类
 */
public class InterviewRecord {
    private long id;
    private long resumeId;
    private String fileName;
    private String displayName;
    private String interviewContent;
    private String filePath;
    private String recordingStartTime; // 录音开始时间，格式为yyyy-MM-dd HH:mm:ss

    public InterviewRecord() {
    }

    public InterviewRecord(long resumeId, String fileName, String displayName, String filePath) {
        this.resumeId = resumeId;
        this.fileName = fileName;
        this.displayName = displayName;
        this.filePath = filePath;
        // 默认设置为当前时间
        this.recordingStartTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
    }

    public InterviewRecord(long resumeId, String fileName, String displayName, String filePath, String recordingStartTime) {
        this.resumeId = resumeId;
        this.fileName = fileName;
        this.displayName = displayName;
        this.filePath = filePath;
        this.recordingStartTime = recordingStartTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getResumeId() {
        return resumeId;
    }

    public void setResumeId(long resumeId) {
        this.resumeId = resumeId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getInterviewContent() {
        return interviewContent;
    }

    public void setInterviewContent(String interviewContent) {
        this.interviewContent = interviewContent;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getRecordingStartTime() {
        return recordingStartTime;
    }

    public void setRecordingStartTime(String recordingStartTime) {
        this.recordingStartTime = recordingStartTime;
    }
} 