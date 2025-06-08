package com.example.todayrecipe.model;

public class Ingredient {
    private String ingredientId;
    private String recipeId;
    private String name;
    private String amount;
    private String unit;
    private boolean optional;

    public Ingredient() {
        // Firebase requires empty constructor
    }

    public Ingredient(String name, String amount, String unit, boolean optional) {
        this.name = name;
        this.amount = amount;
        this.unit = unit;
        this.optional = optional;
    }

    // Getters and Setters
    public String getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(String ingredientId) {
        this.ingredientId = ingredientId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }
}