package com.ai.resume.airesume.service;

import com.ai.resume.airesume.dto.Resume;
import com.ai.resume.airesume.utils.DatabaseManager;
import com.ai.resume.airesume.utils.PythonResumeParser;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 简历比较服务类，处理简历比较相关的业务逻辑
 */
public class CompareService {
    
    private final DatabaseManager databaseManager;
    private final PythonResumeParser resumeParser;
    
    /**
     * 构造函数
     */
    public CompareService() {
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
     * 异步比较多份简历
     * @param selectedIds 选择的简历ID列表
     * @return 比较结果的CompletableFuture
     */
    public CompletableFuture<String> compareResumesAsync(List<Long> selectedIds) {
        if (selectedIds == null || selectedIds.size() < 2) {
            return CompletableFuture.completedFuture("需要至少选择两份简历进行比较");
        }
        
        // 获取选择的简历
        List<Resume> selectedResumes = selectedIds.stream()
                .map(databaseManager::getResumeById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        if (selectedResumes.size() < 2) {
            return CompletableFuture.completedFuture("找不到足够的简历进行比较");
        }
        
        // 异步执行比较
        return CompletableFuture.supplyAsync(() -> {
            try {
                return resumeParser.compareResumes(selectedResumes);
            } catch (Exception e) {
                e.printStackTrace();
                return "比较失败: " + e.getMessage();
            }
        });
    }
} 