package com.example.todayrecipe.model;

import java.util.Date;

public class User {
    private String userId;
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String profileImage;
    private Date registrationDate;
    private int followerCount = 0;  // 추가
    private int followingCount = 0; // 추가

    public User() {
        // Firebase requires empty constructor
    }

    public User(String userId, String email, String name, String nickname) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.registrationDate = new Date();
        this.followerCount = 0;  // 초기화
        this.followingCount = 0; // 초기화
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }
}