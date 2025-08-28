package com.ai.resume.airesume.dto;

public class Work {
    private String startDate;
    private String endDate;
    private String companyName;
    private String position;
    private String responsibility;
    
    public Work() {
    }
    
    public Work(String startDate, String endDate, String companyName, String position, String responsibility) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.companyName = companyName;
        this.position = position;
        this.responsibility = responsibility;
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
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public String getResponsibility() {
        return responsibility;
    }
    
    public void setResponsibility(String responsibility) {
        this.responsibility = responsibility;
    }
    
    @Override
    public String toString() {
        return startDate + " - " + endDate + " " + companyName + (position != null ? " " + position : "");
    }
} 