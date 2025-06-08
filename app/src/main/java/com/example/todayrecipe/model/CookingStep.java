package com.example.todayrecipe.model;

public class CookingStep {
    private String stepId;
    private String recipeId;
    private int stepNumber;
    private String description;
    private String imageUrl;

    public CookingStep() {
        // Firebase requires empty constructor
    }

    public CookingStep(int stepNumber, String description) {
        this.stepNumber = stepNumber;
        this.description = description;
    }

    // Getters and Setters
    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}