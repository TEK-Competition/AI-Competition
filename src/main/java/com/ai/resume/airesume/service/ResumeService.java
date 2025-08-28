package com.ai.resume.airesume.service;

import com.ai.resume.airesume.dto.Resume;
import com.ai.resume.airesume.utils.DatabaseManager;
import com.ai.resume.airesume.utils.PythonResumeParser;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 简历服务类，处理简历相关的业务逻辑
 */
public class ResumeService {
    
    private final DatabaseManager databaseManager;
    private final PythonResumeParser resumeParser;
    
    /**
     * 构造函数
     */
    public ResumeService() {
        this.databaseManager = DatabaseManager.getInstance();
        this.resumeParser = new PythonResumeParser();
    }

    /**
     * 获取所有简历
     * @return 简历列表
     */
    public List<Resume> getAllResumes() {
        return databaseManager.getAllResumes();
    }
    
    /**
     * 获取简历详情
     * @param id 简历ID
     * @return 简历对象
     */
    public Resume getResumeById(long id) {
        return databaseManager.getResumeById(id);
    }
    
    /**
     * 保存简历
     * @param resume 简历对象
     * @return 保存的简历ID
     */
    public long saveResume(Resume resume) {
        return databaseManager.saveResume(resume);
    }
    
    /**
     * 更新简历
     * @param resume 简历对象
     * @return 更新是否成功
     */
    public boolean updateResume(Resume resume) {
        return databaseManager.updateResume(resume);
    }
    
    /**
     * 删除简历
     * @param resumeId 简历ID
     * @return 删除是否成功
     */
    public boolean deleteResume(long resumeId) {
        return databaseManager.deleteResume(resumeId);
    }
    
    /**
     * 异步解析简历文件
     * @param file 简历文件
     * @return 包含解析结果的CompletableFuture
     */
    public CompletableFuture<Resume> parseResumeAsync(File file) {
        // 检查文件是否存在
        if (file == null || !file.exists()) {
            return CompletableFuture.completedFuture(null);
        }
        
        // 异步解析简历
        return CompletableFuture.supplyAsync(() -> {
            try {
                return resumeParser.parseResume(file);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }
    
    /**
     * 比较多份简历
     * @param resumeList 简历列表，至少包含两份简历
     * @return 比较结果
     */
    public CompletableFuture<String> compareResumesAsync(List<Resume> resumeList) {
        if (resumeList == null || resumeList.size() < 2) {
            return CompletableFuture.completedFuture("需要至少两份简历进行比较");
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return resumeParser.compareResumes(resumeList);
            } catch (Exception e) {
                e.printStackTrace();
                return "比较失败: " + e.getMessage();
            }
        });
    }
} 