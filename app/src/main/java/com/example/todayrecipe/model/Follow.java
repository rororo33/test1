package com.example.todayrecipe.model;

import java.util.Date;

public class Follow {
    private String followId;
    private String followerId;
    private String followingId;
    private Date followDate;

    public Follow() {
        // Firebase requires empty constructor
    }

    public Follow(String followerId, String followingId) {
        this.followerId = followerId;
        this.followingId = followingId;
        this.followDate = new Date();
    }

    // Getters and Setters
    public String getFollowId() {
        return followId;
    }

    public void setFollowId(String followId) {
        this.followId = followId;
    }

    public String getFollowerId() {
        return followerId;
    }

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }

    public String getFollowingId() {
        return followingId;
    }

    public void setFollowingId(String followingId) {
        this.followingId = followingId;
    }

    public Date getFollowDate() {
        return followDate;
    }

    public void setFollowDate(Date followDate) {
        this.followDate = followDate;
    }
}