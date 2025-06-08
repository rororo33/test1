package com.example.todayrecipe.manager;

import com.example.todayrecipe.model.Recipe;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class SearchEngine {
    private static SearchEngine instance;
    private FirebaseFirestore db;

    private SearchEngine() {
        db = FirebaseFirestore.getInstance();
    }

    public static SearchEngine getInstance() {
        if (instance == null) {
            instance = new SearchEngine();
        }
        return instance;
    }

    public interface SearchCallback {
        void onSuccess(List<Recipe> recipes);
        void onFailure(String error);
    }

    // 키워드로 검색
    public void searchByKeyword(String keyword, SearchCallback callback) {
        if (keyword == null || keyword.trim().isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        String searchKey = keyword.toLowerCase().trim();

        // 제목으로 검색
        db.collection("recipes")
                .orderBy("title")
                .startAt(searchKey)
                .endAt(searchKey + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = queryDocumentSnapshots.toObjects(Recipe.class);

                    // 추가로 설명에서도 검색 (클라이언트 사이드)
                    List<Recipe> additionalRecipes = new ArrayList<>();
                    db.collection("recipes")
                            .get()
                            .addOnSuccessListener(allRecipes -> {
                                allRecipes.forEach(doc -> {
                                    Recipe recipe = doc.toObject(Recipe.class);
                                    if (recipe != null &&
                                            recipe.getDescription().toLowerCase().contains(searchKey) &&
                                            !containsRecipe(recipes, recipe.getRecipeId())) {
                                        additionalRecipes.add(recipe);
                                    }
                                });

                                recipes.addAll(additionalRecipes);
                                callback.onSuccess(recipes);
                            });
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // 카테고리로 검색
    public void searchByCategory(String category, SearchCallback callback) {
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

    // 난이도로 검색
    public void searchByDifficulty(String difficulty, SearchCallback callback) {
        db.collection("recipes")
                .whereEqualTo("difficulty", difficulty)
                .orderBy("averageRating", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = queryDocumentSnapshots.toObjects(Recipe.class);
                    callback.onSuccess(recipes);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // 조리 시간으로 검색
    public void searchByCookingTime(int maxTime, SearchCallback callback) {
        db.collection("recipes")
                .whereLessThanOrEqualTo("cookingTime", maxTime)
                .orderBy("cookingTime")
                .orderBy("averageRating", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = queryDocumentSnapshots.toObjects(Recipe.class);
                    callback.onSuccess(recipes);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // 고급 검색 (여러 조건 조합)
    public void advancedSearch(String keyword, String category, String difficulty,
                               Integer maxCookingTime, Float minRating, SearchCallback callback) {
        Query query = db.collection("recipes");

        // 카테고리 필터
        if (category != null && !category.isEmpty()) {
            query = query.whereEqualTo("category", category);
        }

        // 난이도 필터
        if (difficulty != null && !difficulty.isEmpty()) {
            query = query.whereEqualTo("difficulty", difficulty);
        }

        // 조리 시간 필터
        if (maxCookingTime != null) {
            query = query.whereLessThanOrEqualTo("cookingTime", maxCookingTime);
        }

        // 최소 평점 필터
        if (minRating != null) {
            query = query.whereGreaterThanOrEqualTo("averageRating", minRating);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = new ArrayList<>();

                    queryDocumentSnapshots.forEach(doc -> {
                        Recipe recipe = doc.toObject(Recipe.class);
                        if (recipe != null) {
                            // 키워드 필터 (클라이언트 사이드)
                            if (keyword == null || keyword.isEmpty() ||
                                    recipe.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                                    recipe.getDescription().toLowerCase().contains(keyword.toLowerCase())) {
                                recipes.add(recipe);
                            }
                        }
                    });

                    callback.onSuccess(recipes);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // 재료로 검색
    public void searchByIngredients(List<String> ingredients, SearchCallback callback) {
        if (ingredients == null || ingredients.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        // Firestore는 배열 내용 검색이 제한적이므로 모든 레시피를 가져와서 필터링
        db.collection("recipes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> matchingRecipes = new ArrayList<>();

                    queryDocumentSnapshots.forEach(doc -> {
                        Recipe recipe = doc.toObject(Recipe.class);
                        if (recipe != null && recipe.getIngredients() != null) {
                            boolean hasAllIngredients = true;

                            for (String searchIngredient : ingredients) {
                                boolean found = false;
                                for (var ingredient : recipe.getIngredients()) {
                                    if (ingredient.getName().toLowerCase()
                                            .contains(searchIngredient.toLowerCase())) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    hasAllIngredients = false;
                                    break;
                                }
                            }

                            if (hasAllIngredients) {
                                matchingRecipes.add(recipe);
                            }
                        }
                    });

                    callback.onSuccess(matchingRecipes);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private boolean containsRecipe(List<Recipe> recipes, String recipeId) {
        for (Recipe recipe : recipes) {
            if (recipe.getRecipeId().equals(recipeId)) {
                return true;
            }
        }
        return false;
    }
}