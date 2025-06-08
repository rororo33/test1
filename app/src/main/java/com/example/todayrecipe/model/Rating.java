package com.example.todayrecipe.model;

import java.util.Date;

public class Rating {
    private String ratingId;
    private String recipeId;
    private String userId;
    private float score;  // int에서 float로 변경
    private String comment;
    private Date ratingDate;

    public Rating() {
        // Firebase requires empty constructor
    }

    public Rating(String recipeId, String userId, float score, String comment) {  // int에서 float로 변경
        this.recipeId = recipeId;
        this.userId = userId;
        this.score = score;
        this.comment = comment;
        this.ratingDate = new Date();
    }

    // Getters and Setters
    public String getRatingId() {
        return ratingId;
    }

    public void setRatingId(String ratingId) {
        this.ratingId = ratingId;
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

    public float getScore() {  // int에서 float로 변경
        return score;
    }

    public void setScore(float score) {  // int에서 float로 변경
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getRatingDate() {
        return ratingDate;
    }

    public void setRatingDate(Date ratingDate) {
        this.ratingDate = ratingDate;
    }
}