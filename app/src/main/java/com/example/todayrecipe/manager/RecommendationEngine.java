package com.example.todayrecipe.manager;

import java.util.concurrent.CopyOnWriteArrayList;
import com.example.todayrecipe.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RecommendationEngine {
    private static RecommendationEngine instance;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private RecommendationEngine() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static RecommendationEngine getInstance() {
        if (instance == null) {
            instance = new RecommendationEngine();
        }
        return instance;
    }

    public interface RecommendationCallback {
        void onSuccess(List<Recipe> recipes);
        void onFailure(String error);
    }

    // 오늘의 추천 레시피
    public void getTodayRecommendations(RecommendationCallback callback) {
        if (auth.getCurrentUser() != null) {
            // 로그인 사용자: 개인화된 추천
            getPersonalizedRecommendations(callback);
        } else {
            // 비로그인 사용자: 인기 레시피
            getPopularRecipes(callback);
        }
    }

    // 개인화된 추천 (사용자의 평가 기록 기반)
    private void getPersonalizedRecommendations(RecommendationCallback callback) {
        String userId = auth.getCurrentUser().getUid();

        // 1. 사용자가 높게 평가한 레시피의 카테고리 파악
        db.collection("ratings")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("score", 4)
                .get()
                .addOnSuccessListener(ratingSnapshots -> {
                    List<String> preferredCategories = new ArrayList<>();
                    List<String> ratedRecipeIds = new ArrayList<>();

                    ratingSnapshots.forEach(doc -> {
                        ratedRecipeIds.add(doc.getString("recipeId"));
                    });

                    // 평가한 레시피들의 카테고리 수집
                    for (String recipeId : ratedRecipeIds) {
                        db.collection("recipes")
                                .document(recipeId)
                                .get()
                                .addOnSuccessListener(recipeDoc -> {
                                    Recipe recipe = recipeDoc.toObject(Recipe.class);
                                    if (recipe != null) {
                                        preferredCategories.add(recipe.getCategory());
                                    }
                                });
                    }

                    // 2. 선호 카테고리의 다른 레시피 추천
                    if (!preferredCategories.isEmpty()) {
                        String topCategory = getMostFrequent(preferredCategories);
                        getRecipesByCategory(topCategory, ratedRecipeIds, callback);
                    } else {
                        // 평가 기록이 없으면 인기 레시피 추천
                        getPopularRecipes(callback);
                    }
                })
                .addOnFailureListener(e -> getPopularRecipes(callback));
    }

    // 카테고리별 레시피 가져오기 (이미 평가한 레시피 제외)
    private void getRecipesByCategory(String category, List<String> excludeIds, RecommendationCallback callback) {
        db.collection("recipes")
                .whereEqualTo("category", category)
                .orderBy("averageRating", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // CopyOnWriteArrayList 사용하여 thread-safe 보장
                    List<Recipe> recipes = new CopyOnWriteArrayList<>();

                    queryDocumentSnapshots.forEach(doc -> {
                        Recipe recipe = doc.toObject(Recipe.class);
                        if (recipe != null && !excludeIds.contains(recipe.getRecipeId())) {
                            recipes.add(recipe);
                        }
                    });

                    // 랜덤하게 섞어서 5개만 반환
                    List<Recipe> finalRecipes = new ArrayList<>(recipes);
                    Collections.shuffle(finalRecipes);
                    if (finalRecipes.size() > 5) {
                        finalRecipes = finalRecipes.subList(0, 5);
                    }

                    callback.onSuccess(finalRecipes);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // 인기 레시피 추천
    private void getPopularRecipes(RecommendationCallback callback) {
        db.collection("recipes")
                .orderBy("averageRating", Query.Direction.DESCENDING)
                .orderBy("ratingCount", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = queryDocumentSnapshots.toObjects(Recipe.class);

                    // 상위 10개 중 랜덤하게 5개 선택
                    Collections.shuffle(recipes);
                    if (recipes.size() > 5) {
                        recipes = recipes.subList(0, 5);
                    }

                    callback.onSuccess(recipes);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // 유사한 레시피 추천
    public void getSimilarRecipes(String recipeId, RecommendationCallback callback) {
        db.collection("recipes")
                .document(recipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Recipe targetRecipe = documentSnapshot.toObject(Recipe.class);
                    if (targetRecipe != null) {
                        // 같은 카테고리의 다른 레시피 추천
                        db.collection("recipes")
                                .whereEqualTo("category", targetRecipe.getCategory())
                                .whereNotEqualTo("recipeId", recipeId)
                                .orderBy("recipeId")
                                .orderBy("averageRating", Query.Direction.DESCENDING)
                                .limit(5)
                                .get()
                                .addOnSuccessListener(querySnapshots -> {
                                    List<Recipe> recipes = querySnapshots.toObjects(Recipe.class);
                                    callback.onSuccess(recipes);
                                })
                                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                    } else {
                        callback.onFailure("레시피를 찾을 수 없습니다");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // 가장 빈번한 요소 찾기
    private String getMostFrequent(List<String> list) {
        if (list.isEmpty()) return null;

        Collections.sort(list);
        String maxItem = list.get(0);
        int maxCount = 1;
        int currentCount = 1;

        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).equals(list.get(i-1))) {
                currentCount++;
            } else {
                if (currentCount > maxCount) {
                    maxCount = currentCount;
                    maxItem = list.get(i-1);
                }
                currentCount = 1;
            }
        }

        if (currentCount > maxCount) {
            maxItem = list.get(list.size()-1);
        }

        return maxItem;
    }
}