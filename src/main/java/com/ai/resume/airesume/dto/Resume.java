package com.ai.resume.airesume.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Resume {
    private Long id;
    private String name;
    private String gender;
    private int age;
    private String highestEducation;
    private List<Education> educationExperience;
    private List<Work> workExperience;
    private List<Language> languageSkills;
    private String selfEvaluation;
    private List<String> interviewAttachments;
    private String interviewSummary;
    private String filePath;
    private Date createDate;

    public Resume() {
        this.educationExperience = new ArrayList<>();
        this.workExperience = new ArrayList<>();
        this.languageSkills = new ArrayList<>();
        this.interviewAttachments = new ArrayList<>();
        this.createDate = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getHighestEducation() {
        return highestEducation;
    }

    public void setHighestEducation(String highestEducation) {
        this.highestEducation = highestEducation;
    }

    public List<Education> getEducationExperience() {
        return educationExperience;
    }

    public void setEducationExperience(List<Education> educationExperience) {
        this.educationExperience = educationExperience;
    }

    public List<Work> getWorkExperience() {
        return workExperience;
    }

    public void setWorkExperience(List<Work> workExperience) {
        this.workExperience = workExperience;
    }

    public List<Language> getLanguageSkills() {
        return languageSkills;
    }

    public void setLanguageSkills(List<Language> languageSkills) {
        this.languageSkills = languageSkills;
    }

    public String getSelfEvaluation() {
        return selfEvaluation;
    }

    public void setSelfEvaluation(String selfEvaluation) {
        this.selfEvaluation = selfEvaluation;
    }

    public List<String> getInterviewAttachments() {
        return interviewAttachments;
    }

    public void setInterviewAttachments(List<String> interviewAttachments) {
        this.interviewAttachments = interviewAttachments;
    }

    public String getInterviewSummary() {
        return interviewSummary;
    }

    public void setInterviewSummary(String interviewSummary) {
        this.interviewSummary = interviewSummary;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
} 