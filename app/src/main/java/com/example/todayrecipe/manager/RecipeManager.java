package com.example.todayrecipe.manager;

import com.example.todayrecipe.model.Recipe;
import com.example.todayrecipe.model.Ingredient;
import com.example.todayrecipe.model.CookingStep;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RecipeManager {
    private static RecipeManager instance;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private RecipeManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static RecipeManager getInstance() {
        if (instance == null) {
            instance = new RecipeManager();
        }
        return instance;
    }

    public interface RecipeCallback {
        void onSuccess(Recipe recipe);
        void onFailure(String error);
    }

    public interface RecipeListCallback {
        void onSuccess(List<Recipe> recipes);
        void onFailure(String error);
    }

    public interface StringCallback {
        void onSuccess(String result);
        void onFailure(String error);
    }

    public void createRecipe(Recipe recipe, List<Ingredient> ingredients,
                             List<CookingStep> steps, StringCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onFailure("로그인이 필요합니다");
            return;
        }

        recipe.setUserId(auth.getCurrentUser().getUid());
        recipe.setIngredients(ingredients);
        recipe.setCookingSteps(steps);

        DocumentReference docRef = db.collection("recipes").document();
        recipe.setRecipeId(docRef.getId());

        docRef.set(recipe)
                .addOnSuccessListener(aVoid -> callback.onSuccess(recipe.getRecipeId()))
                .addOnFailureListener(e -> callback.onFailure("레시피 등록 실패: " + e.getMessage()));
    }

    public void getRecipeById(String recipeId, RecipeCallback callback) {
        db.collection("recipes")
                .document(recipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Recipe recipe = documentSnapshot.toObject(Recipe.class);
                    if (recipe != null) {
                        // Increment view count
                        recipe.incrementViewCount();
                        updateRecipe(recipe, success -> {
                            if (success) {
                                callback.onSuccess(recipe);
                            }
                        });
                    } else {
                        callback.onFailure("레시피를 찾을 수 없습니다");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getRecipesByUserId(String userId, RecipeListCallback callback) {
        db.collection("recipes")
                .whereEqualTo("userId", userId)
                .orderBy("creationDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = queryDocumentSnapshots.toObjects(Recipe.class);
                    callback.onSuccess(recipes);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void searchRecipes(String keyword, RecipeListCallback callback) {
        // Simple search by title
        db.collection("recipes")
                .whereGreaterThanOrEqualTo("title", keyword)
                .whereLessThanOrEqualTo("title", keyword + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = queryDocumentSnapshots.toObjects(Recipe.class);
                    callback.onSuccess(recipes);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getRecipesByCategory(String category, RecipeListCallback callback) {
        db.collection("recipes")
                .whereEqualTo("category", category)
                .orderBy("creationDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = queryDocumentSnapshots.toObjects(Recipe.class);
                    callback.onSuccess(recipes);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getTodayRecommendations(RecipeListCallback callback) {
        // Get popular recipes as recommendations
        db.collection("recipes")
                .orderBy("averageRating", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = queryDocumentSnapshots.toObjects(Recipe.class);
                    callback.onSuccess(recipes);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public interface BooleanCallback {
        void onComplete(boolean success);
    }

    public void updateRecipe(Recipe recipe, BooleanCallback callback) {
        db.collection("recipes")
                .document(recipe.getRecipeId())
                .set(recipe)
                .addOnSuccessListener(aVoid -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    public void deleteRecipe(String recipeId, BooleanCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onComplete(false);
            return;
        }

        // Verify ownership first
        db.collection("recipes")
                .document(recipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Recipe recipe = documentSnapshot.toObject(Recipe.class);
                    if (recipe != null && recipe.getUserId().equals(auth.getCurrentUser().getUid())) {
                        documentSnapshot.getReference().delete()
                                .addOnSuccessListener(aVoid -> callback.onComplete(true))
                                .addOnFailureListener(e -> callback.onComplete(false));
                    } else {
                        callback.onComplete(false);
                    }
                })
                .addOnFailureListener(e -> callback.onComplete(false));
    }
}