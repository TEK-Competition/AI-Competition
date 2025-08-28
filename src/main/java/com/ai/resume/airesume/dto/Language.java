package com.ai.resume.airesume.dto;

public class Language {
    private String languageName;
    private String level;
    
    public Language() {
    }
    
    public Language(String languageName, String level) {
        this.languageName = languageName;
        this.level = level;
    }
    
    public String getLanguageName() {
        return languageName;
    }
    
    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }
    
    public String getLevel() {
        return level;
    }
    
    public void setLevel(String level) {
        this.level = level;
    }
    
    @Override
    public String toString() {
        return languageName + "（" + level + "）";
    }
} 