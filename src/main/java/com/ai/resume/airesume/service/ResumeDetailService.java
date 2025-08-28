package com.ai.resume.airesume.service;

import com.ai.resume.airesume.dto.Resume;
import com.ai.resume.airesume.utils.DatabaseManager;

/**
 * 简历详情服务类，处理简历详情相关的业务逻辑
 */
public class ResumeDetailService {
    
    private final DatabaseManager databaseManager;
    
    /**
     * 构造函数
     */
    public ResumeDetailService() {
        this.databaseManager = DatabaseManager.getInstance();
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
     * 获取简历详情
     * @param id 简历ID
     * @return 简历对象
     */
    public Resume getResumeById(long id) {
        return databaseManager.getResumeById(id);
    }
} 