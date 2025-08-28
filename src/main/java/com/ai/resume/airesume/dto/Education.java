package com.ai.resume.airesume.dto;

public class Education {
    private String startDate;
    private String endDate;
    private String name;
    private String education;
    private String major;
    
    public Education() {
    }
    
    public Education(String startDate, String endDate, String name, String education, String major) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.name = name;
        this.education = education;
        this.major = major;
    }
    
    public String getStartDate() {
        return startDate;
    }
    
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    
    public String getEndDate() {
        return endDate;
    }
    
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEducation() {
        return education;
    }
    
    public void setEducation(String education) {
        this.education = education;
    }
    
    public String getMajor() {
        return major;
    }
    
    public void setMajor(String major) {
        this.major = major;
    }
    
    @Override
    public String toString() {
        return startDate + " - " + endDate + " " + name + " " + education + (major != null ? " " + major : "");
    }
} 