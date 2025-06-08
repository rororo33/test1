package com.example.todayrecipe.model;

import java.util.Date;

public class Comment {
    private String commentId;
    private String recipeId;
    private String userId;
    private String content;
    private Date commentDate;
    private String userNickname; // 표시용

    public Comment() {
        // Firebase requires empty constructor
    }

    public Comment(String recipeId, String userId, String content) {
        this.recipeId = recipeId;
        this.userId = userId;
        this.content = content;
        this.commentDate = new Date();
    }

    // Getters and Setters
    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(Date commentDate) {
        this.commentDate = commentDate;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }
}