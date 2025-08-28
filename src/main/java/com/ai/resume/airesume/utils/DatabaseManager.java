package com.ai.resume.airesume.utils;

import com.ai.resume.airesume.dto.Education;
import com.ai.resume.airesume.dto.InterviewRecord;
import com.ai.resume.airesume.dto.Language;
import com.ai.resume.airesume.dto.Resume;
import com.ai.resume.airesume.dto.Work;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseManager {
    private static final String DB_PATH = System.getProperty("user.home") + "/AiResume/database/";
    private static final String DB_NAME = "airesume.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH + DB_NAME;
    
    private static DatabaseManager instance;
    
    private final ObjectMapper objectMapper;
    
    private DatabaseManager() {
        objectMapper = new ObjectMapper();
        initDatabase();
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * 初始化数据库，创建必要的表
     */
    private void initDatabase() {
        // 确保数据库目录存在
        File dbDir = new File(DB_PATH);
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 创建简历表
            stmt.execute("CREATE TABLE IF NOT EXISTS t_resume (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "gender TEXT," +
                    "age INTEGER," +
                    "highest_education TEXT," +
                    "education_experience TEXT," +
                    "work_experience TEXT," +
                    "language_skills TEXT," +
                    "self_evaluation TEXT," +
                    "interview_attachments TEXT," +
                    "interview_summary TEXT," +
                    "file_path TEXT," +
                    "create_date TEXT" +
                    ")");
            
            // 创建面试记录表
            stmt.execute("CREATE TABLE IF NOT EXISTS interview_record (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "resume_id INTEGER," +
                    "file_name TEXT," +
                    "display_name TEXT," +
                    "interview_content TEXT," +
                    "file_path TEXT," +
                    "FOREIGN KEY(resume_id) REFERENCES t_resume(id)" +
                    ")");
            
            // 检查interview_record表是否有recording_start_time列，如果没有则添加
            try {
                ResultSet rs = conn.getMetaData().getColumns(null, null, "interview_record", "recording_start_time");
                if (!rs.next()) {
                    // 列不存在，添加它
                    stmt.execute("ALTER TABLE interview_record ADD COLUMN recording_start_time TEXT");
                    System.out.println("已添加recording_start_time列到interview_record表");
                }
                rs.close();
            } catch (SQLException e) {
                // 如果检查失败，尝试直接添加列，可能会失败但不阻止程序运行
                try {
                    stmt.execute("ALTER TABLE interview_record ADD COLUMN recording_start_time TEXT");
                } catch (SQLException e2) {
                    System.err.println("添加recording_start_time列失败: " + e2.getMessage());
                }
            }
            
        } catch (SQLException e) {
            System.err.println("初始化数据库失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 获取数据库连接
     * @return 数据库连接
     * @throws SQLException 如果连接失败
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
    
    /**
     * 保存简历到数据库
     * @param resume 简历对象
     * @return 保存后的简历ID
     */
    public long saveResume(Resume resume) {
        String sql = "INSERT INTO t_resume (name, gender, age, highestEducation, " +
                "educationExperience, workExperience, languageSkills, " +
                "selfEvaluation, interviewSummary, filePath, createDate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, resume.getName());
            pstmt.setString(2, resume.getGender());
            pstmt.setInt(3, resume.getAge());
            pstmt.setString(4, resume.getHighestEducation());
            
            // 将对象列表转换为JSON字符串
            pstmt.setString(5, objectMapper.writeValueAsString(resume.getEducationExperience()));
            pstmt.setString(6, objectMapper.writeValueAsString(resume.getWorkExperience()));
            pstmt.setString(7, objectMapper.writeValueAsString(resume.getLanguageSkills()));
            
            pstmt.setString(8, resume.getSelfEvaluation());
            pstmt.setString(9, resume.getInterviewSummary());
            pstmt.setString(10, resume.getFilePath());
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pstmt.setString(11, sdf.format(resume.getCreateDate()));
            
            pstmt.executeUpdate();
            
            // 获取生成的ID
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    resume.setId(id);
                    return id;
                }
            }
            
        } catch (SQLException | JsonProcessingException e) {
            System.err.println("保存简历失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * 更新简历
     * @param resume 简历对象
     * @return 是否更新成功
     */
    public boolean updateResume(Resume resume) {
        String sql = "UPDATE t_resume SET name = ?, gender = ?, age = ?, " +
                "highestEducation = ?, educationExperience = ?, workExperience = ?, " +
                "languageSkills = ?, selfEvaluation = ?, interviewSummary = ? " +
                "WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, resume.getName());
            pstmt.setString(2, resume.getGender());
            pstmt.setInt(3, resume.getAge());
            pstmt.setString(4, resume.getHighestEducation());
            
            // 将对象列表转换为JSON字符串
            pstmt.setString(5, objectMapper.writeValueAsString(resume.getEducationExperience()));
            pstmt.setString(6, objectMapper.writeValueAsString(resume.getWorkExperience()));
            pstmt.setString(7, objectMapper.writeValueAsString(resume.getLanguageSkills()));
            
            pstmt.setString(8, resume.getSelfEvaluation());
            pstmt.setString(9, resume.getInterviewSummary());
            pstmt.setLong(10, resume.getId());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException | JsonProcessingException e) {
            System.err.println("更新简历失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * 根据ID获取简历
     * @param id 简历ID
     * @return 简历对象，如果不存在则返回null
     */
    public Resume getResumeById(long id) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT * FROM t_resume WHERE id = ?")) {
            
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToResume(rs);
                }
            }
        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 根据ID获取简历
     * @param id 简历ID
     * @return 简历对象，如果不存在则返回null
     * @deprecated 使用 {@link #getResumeById(long)} 代替
     */
    @Deprecated
    public Resume getResume(long id) {
        return getResumeById(id);
    }
    
    /**
     * 获取所有简历
     * @return 简历列表
     */
    public List<Resume> getAllResumes() {
        String sql = "SELECT * FROM t_resume ORDER BY createDate DESC";
        List<Resume> resumes = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                resumes.add(mapResultSetToResume(rs));
            }
            
        } catch (SQLException | JsonProcessingException e) {
            System.err.println("获取所有简历失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return resumes;
    }
    
    /**
     * 删除简历
     * @param id 简历ID
     * @return 是否删除成功
     */
    public boolean deleteResume(long id) {
        String sql = "DELETE FROM t_resume WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("删除简历失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * 将ResultSet映射为Resume对象
     * @param rs ResultSet
     * @return Resume对象
     */
    private Resume mapResultSetToResume(ResultSet rs) throws SQLException, JsonProcessingException {
        Resume resume = new Resume();
        
        resume.setId(rs.getLong("id"));
        resume.setName(rs.getString("name"));
        resume.setGender(rs.getString("gender"));
        resume.setAge(rs.getInt("age"));
        resume.setHighestEducation(rs.getString("highestEducation"));
        
        // 将JSON字符串转换为对象列表
        String educationJson = rs.getString("educationExperience");
        if (educationJson != null && !educationJson.isEmpty()) {
            List<Education> educationList = objectMapper.readValue(educationJson, 
                    new TypeReference<List<Education>>() {});
            resume.setEducationExperience(educationList);
        }
        
        String workJson = rs.getString("workExperience");
        if (workJson != null && !workJson.isEmpty()) {
            List<Work> workList = objectMapper.readValue(workJson, 
                    new TypeReference<List<Work>>() {});
            resume.setWorkExperience(workList);
        }
        
        String languageJson = rs.getString("languageSkills");
        if (languageJson != null && !languageJson.isEmpty()) {
            List<Language> languageList = objectMapper.readValue(languageJson, 
                    new TypeReference<List<Language>>() {});
            resume.setLanguageSkills(languageList);
        }
        
        resume.setSelfEvaluation(rs.getString("selfEvaluation"));
        resume.setInterviewSummary(rs.getString("interviewSummary"));
        resume.setFilePath(rs.getString("filePath"));
        
        String createDateStr = rs.getString("createDate");
        if (createDateStr != null && !createDateStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                resume.setCreateDate(sdf.parse(createDateStr));
            } catch (Exception e) {
                resume.setCreateDate(new Date());
            }
        }
        
        return resume;
    }
    
    /**
     * 插入一条面试记录
     * @param record 面试记录对象
     * @return 插入的面试记录ID
     */
    public long insertInterviewRecord(InterviewRecord record) {
        long id = -1;
        try (Connection conn = getConnection()) {
            // 先检查表中是否有recording_start_time列
            boolean hasRecordingStartTimeColumn = false;
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "interview_record", "recording_start_time")) {
                hasRecordingStartTimeColumn = rs.next();
            }
            
            String sql;
            PreparedStatement pstmt;
            
            if (hasRecordingStartTimeColumn) {
                // 如果有recording_start_time列，使用包含该列的SQL
                sql = "INSERT INTO interview_record (resume_id, file_name, display_name, file_path, recording_start_time) " +
                      "VALUES (?, ?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                
                pstmt.setLong(1, record.getResumeId());
                pstmt.setString(2, record.getFileName());
                pstmt.setString(3, record.getDisplayName());
                pstmt.setString(4, record.getFilePath());
                pstmt.setString(5, record.getRecordingStartTime());
            } else {
                // 如果没有recording_start_time列，使用不包含该列的SQL
                sql = "INSERT INTO interview_record (resume_id, file_name, display_name, file_path) " +
                      "VALUES (?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                
                pstmt.setLong(1, record.getResumeId());
                pstmt.setString(2, record.getFileName());
                pstmt.setString(3, record.getDisplayName());
                pstmt.setString(4, record.getFilePath());
            }
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        id = rs.getLong(1);
                        record.setId(id);
                    }
                }
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }
    
    /**
     * 保存面试记录
     * @param record 面试记录对象
     * @return 保存的记录ID，如果失败则返回-1
     * @deprecated 使用 {@link #insertInterviewRecord(InterviewRecord)} 代替
     */
    @Deprecated
    public long saveInterviewRecord(InterviewRecord record) {
        return insertInterviewRecord(record);
    }
    
    /**
     * 更新面试记录
     * @param record 面试记录对象
     * @return 是否更新成功
     */
    public boolean updateInterviewRecord(InterviewRecord record) {
        try (Connection conn = getConnection()) {
            // 先检查表中是否有recording_start_time列
            boolean hasRecordingStartTimeColumn = false;
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "interview_record", "recording_start_time")) {
                hasRecordingStartTimeColumn = rs.next();
            }
            
            String sql;
            PreparedStatement pstmt;
            
            if (hasRecordingStartTimeColumn) {
                // 如果有recording_start_time列，使用包含该列的SQL
                sql = "UPDATE interview_record SET resume_id = ?, file_name = ?, display_name = ?, " +
                        "interview_content = ?, file_path = ?, recording_start_time = ? WHERE id = ?";
                pstmt = conn.prepareStatement(sql);
                
                pstmt.setLong(1, record.getResumeId());
                pstmt.setString(2, record.getFileName());
                pstmt.setString(3, record.getDisplayName());
                pstmt.setString(4, record.getInterviewContent());
                pstmt.setString(5, record.getFilePath());
                pstmt.setString(6, record.getRecordingStartTime());
                pstmt.setLong(7, record.getId());
            } else {
                // 如果没有recording_start_time列，使用不包含该列的SQL
                sql = "UPDATE interview_record SET resume_id = ?, file_name = ?, display_name = ?, " +
                        "interview_content = ?, file_path = ? WHERE id = ?";
                pstmt = conn.prepareStatement(sql);
                
                pstmt.setLong(1, record.getResumeId());
                pstmt.setString(2, record.getFileName());
                pstmt.setString(3, record.getDisplayName());
                pstmt.setString(4, record.getInterviewContent());
                pstmt.setString(5, record.getFilePath());
                pstmt.setLong(6, record.getId());
            }
            
            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 删除面试记录
     * @param recordId 面试记录ID
     * @return 是否删除成功
     */
    public boolean deleteInterviewRecord(long recordId) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "DELETE FROM interview_record WHERE id = ?")) {
            
            pstmt.setLong(1, recordId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 根据简历ID删除面试记录
     * @param resumeId 简历ID
     * @return 是否删除成功
     */
    public boolean deleteInterviewRecordsByResumeId(long resumeId) {
        try (Connection conn = getConnection()) {
            String sql = "DELETE FROM interview_record WHERE resume_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, resumeId);
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("删除面试记录失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * 根据ID获取面试记录
     * @param recordId 面试记录ID
     * @return 面试记录对象，如果不存在则返回null
     */
    public InterviewRecord getInterviewRecordById(long recordId) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT * FROM interview_record WHERE id = ?")) {
            
            pstmt.setLong(1, recordId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createInterviewRecordFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 根据ID获取面试记录
     * @param id 面试记录ID
     * @return 面试记录对象，如果不存在则返回null
     * @deprecated 使用 {@link #getInterviewRecordById(long)} 代替
     */
    @Deprecated
    public InterviewRecord getInterviewRecord(long id) {
        return getInterviewRecordById(id);
    }
    
    /**
     * 根据简历ID获取面试记录列表
     * @param resumeId 简历ID
     * @return 面试记录列表
     */
    public List<InterviewRecord> getInterviewRecordsByResumeId(long resumeId) {
        List<InterviewRecord> records = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT * FROM interview_record WHERE resume_id = ?")) {
            
            pstmt.setLong(1, resumeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(createInterviewRecordFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return records;
    }
    
    /**
     * 从ResultSet创建InterviewRecord对象
     * @param rs ResultSet对象
     * @return InterviewRecord对象
     * @throws SQLException 如果读取ResultSet出错
     */
    private InterviewRecord createInterviewRecordFromResultSet(ResultSet rs) throws SQLException {
        InterviewRecord record = new InterviewRecord();
        record.setId(rs.getLong("id"));
        record.setResumeId(rs.getLong("resume_id"));
        record.setFileName(rs.getString("file_name"));
        record.setDisplayName(rs.getString("display_name"));
        record.setFilePath(rs.getString("file_path"));
        
        // 获取interview_content，可能为null
        String interviewContent = rs.getString("interview_content");
        if (interviewContent != null) {
            record.setInterviewContent(interviewContent);
        }
        
        // 尝试读取recording_start_time列，这个列可能不存在
        try {
            String recordingStartTime = rs.getString("recording_start_time");
            if (recordingStartTime != null) {
                record.setRecordingStartTime(recordingStartTime);
            }
        } catch (SQLException e) {
            // 忽略这个异常，表示列不存在
        }
        
        return record;
    }
} 