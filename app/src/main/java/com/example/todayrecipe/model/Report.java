package com.example.todayrecipe.model;

import java.util.Date;

public class Report {
    private String reportId;
    private String recipeId;
    private String userId;
    private String reason;
    private String description;
    private Date reportDate;
    private String status;

    public Report() {
        // Firebase requires empty constructor
    }

    public Report(String recipeId, String userId, String reason, String description) {
        this.recipeId = recipeId;
        this.userId = userId;
        this.reason = reason;
        this.description = description;
        this.reportDate = new Date();
        this.status = "pending";
    }

    // Getters and Setters
    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}