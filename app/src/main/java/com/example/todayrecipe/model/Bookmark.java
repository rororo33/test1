package com.example.todayrecipe.model;

import java.util.Date;

public class Bookmark {
    private String bookmarkId;
    private String recipeId;
    private String userId;
    private Date bookmarkDate;

    public Bookmark() {
        // Firebase requires empty constructor
    }

    public Bookmark(String recipeId, String userId) {
        this.recipeId = recipeId;
        this.userId = userId;
        this.bookmarkDate = new Date();
    }

    // Getters and Setters
    public String getBookmarkId() {
        return bookmarkId;
    }

    public void setBookmarkId(String bookmarkId) {
        this.bookmarkId = bookmarkId;
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

    public Date getBookmarkDate() {
        return bookmarkDate;
    }

    public void setBookmarkDate(Date bookmarkDate) {
        this.bookmarkDate = bookmarkDate;
    }
}