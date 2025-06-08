package com.example.todayrecipe.manager;

import com.example.todayrecipe.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InteractionManager {
    private static InteractionManager instance;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private InteractionManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static InteractionManager getInstance() {
        if (instance == null) {
            instance = new InteractionManager();
        }
        return instance;
    }

    public interface BooleanCallback {
        void onComplete(boolean success);
    }

    public interface ListCallback {
        void onSuccess(List<String> items);
        void onFailure(String error);
    }

    // Rating functionality
    public void addRating(String recipeId, float rating, String comment, BooleanCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onComplete(false);
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("userId", userId);
        ratingData.put("recipeId", recipeId);
        ratingData.put("score", rating);
        ratingData.put("comment", comment);
        ratingData.put("ratingDate", FieldValue.serverTimestamp());

        android.util.Log.d("InteractionManager", "Adding rating: " + rating + " for recipe: " + recipeId);

        // Check if user already rated
        db.collection("ratings")
                .whereEqualTo("userId", userId)
                .whereEqualTo("recipeId", recipeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Add new rating
                        db.collection("ratings").add(ratingData)
                                .addOnSuccessListener(documentReference -> {
                                    android.util.Log.d("InteractionManager", "Rating added successfully");
                                    updateRecipeRating(recipeId);
                                    callback.onComplete(true);
                                })
                                .addOnFailureListener(e -> {
                                    android.util.Log.e("InteractionManager", "Failed to add rating: " + e.getMessage());
                                    callback.onComplete(false);
                                });
                    } else {
                        callback.onComplete(false); // Already rated
                    }
                })
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    private void updateRecipeRating(String recipeId) {
        db.collection("ratings")
                .whereEqualTo("recipeId", recipeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        float totalRating = 0;
                        int count = queryDocumentSnapshots.size();

                        for (var doc : queryDocumentSnapshots.getDocuments()) {
                            // 중요: "score" 필드 사용
                            Number score = (Number) doc.get("score");
                            if (score != null) {
                                totalRating += score.floatValue();
                            }
                        }

                        float averageRating = totalRating / count;

                        android.util.Log.d("InteractionManager",
                                "Updating recipe " + recipeId +
                                        ": count=" + count +
                                        ", average=" + averageRating);

                        // Update recipe with new average rating
                        db.collection("recipes").document(recipeId)
                                .update(
                                        "averageRating", averageRating,
                                        "ratingCount", count
                                )
                                .addOnSuccessListener(aVoid -> {
                                    android.util.Log.d("InteractionManager",
                                            "Recipe rating updated successfully");
                                })
                                .addOnFailureListener(e -> {
                                    android.util.Log.e("InteractionManager",
                                            "Failed to update recipe rating: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("InteractionManager",
                            "Failed to fetch ratings: " + e.getMessage());
                });
    }

    // Bookmark functionality
    public void addBookmark(String recipeId, BooleanCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onComplete(false);
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> bookmarkData = new HashMap<>();
        bookmarkData.put("userId", userId);
        bookmarkData.put("recipeId", recipeId);
        bookmarkData.put("timestamp", FieldValue.serverTimestamp());

        String bookmarkId = userId + "_" + recipeId;
        db.collection("bookmarks")
                .document(bookmarkId)
                .set(bookmarkData)
                .addOnSuccessListener(aVoid -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    public void removeBookmark(String recipeId, BooleanCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onComplete(false);
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String bookmarkId = userId + "_" + recipeId;

        db.collection("bookmarks")
                .document(bookmarkId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    public void isBookmarked(String recipeId, BooleanCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onComplete(false);
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String bookmarkId = userId + "_" + recipeId;

        db.collection("bookmarks")
                .document(bookmarkId)
                .get()
                .addOnSuccessListener(documentSnapshot -> callback.onComplete(documentSnapshot.exists()))
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    // Report functionality
    public void reportRecipe(String recipeId, String reason, String description, BooleanCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onComplete(false);
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("userId", userId);
        reportData.put("recipeId", recipeId);
        reportData.put("reason", reason);
        reportData.put("description", description);
        reportData.put("status", "pending");
        reportData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("reports").add(reportData)
                .addOnSuccessListener(documentReference -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    // Follow functionality
    public void followUser(String targetUserId, BooleanCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onComplete(false);
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();
        if (currentUserId.equals(targetUserId)) {
            callback.onComplete(false); // Can't follow yourself
            return;
        }

        Map<String, Object> followData = new HashMap<>();
        followData.put("followerId", currentUserId);
        followData.put("followingId", targetUserId);
        followData.put("timestamp", FieldValue.serverTimestamp());

        String followId = currentUserId + "_" + targetUserId;
        db.collection("follows")
                .document(followId)
                .set(followData)
                .addOnSuccessListener(aVoid -> {
                    updateFollowCounts(currentUserId, targetUserId, true);
                    callback.onComplete(true);
                })
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    public void unfollowUser(String targetUserId, BooleanCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onComplete(false);
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();
        String followId = currentUserId + "_" + targetUserId;

        db.collection("follows")
                .document(followId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    updateFollowCounts(currentUserId, targetUserId, false);
                    callback.onComplete(true);
                })
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    public void isFollowing(String targetUserId, BooleanCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onComplete(false);
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();
        String followId = currentUserId + "_" + targetUserId;

        db.collection("follows")
                .document(followId)
                .get()
                .addOnSuccessListener(documentSnapshot -> callback.onComplete(documentSnapshot.exists()))
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    private void updateFollowCounts(String followerId, String followingId, boolean isFollowing) {
        int increment = isFollowing ? 1 : -1;

        // 팔로워의 팔로잉 수 업데이트
        db.collection("users").document(followerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Long currentFollowing = documentSnapshot.getLong("followingCount");
                    if (currentFollowing == null) currentFollowing = 0L;

                    long newCount = Math.max(0, currentFollowing + increment);
                    db.collection("users").document(followerId)
                            .update("followingCount", newCount)
                            .addOnFailureListener(e -> {
                                android.util.Log.e("InteractionManager",
                                        "Failed to update followingCount: " + e.getMessage());
                            });
                });

        // 대상 사용자의 팔로워 수 업데이트
        db.collection("users").document(followingId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Long currentFollowers = documentSnapshot.getLong("followerCount");
                    if (currentFollowers == null) currentFollowers = 0L;

                    long newCount = Math.max(0, currentFollowers + increment);
                    db.collection("users").document(followingId)
                            .update("followerCount", newCount)
                            .addOnSuccessListener(aVoid -> {
                                android.util.Log.d("InteractionManager",
                                        "Updated followerCount to: " + newCount);
                            })
                            .addOnFailureListener(e -> {
                                android.util.Log.e("InteractionManager",
                                        "Failed to update followerCount: " + e.getMessage());
                            });
                });
    }

    // Get user's bookmarked recipes
    public void getBookmarkedRecipeIds(String userId, ListCallback callback) {
        db.collection("bookmarks")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> recipeIds = new java.util.ArrayList<>();
                    queryDocumentSnapshots.forEach(doc -> {
                        String recipeId = doc.getString("recipeId");
                        if (recipeId != null) {
                            recipeIds.add(recipeId);
                        }
                    });
                    callback.onSuccess(recipeIds);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Get user's followers
    public void getFollowers(String userId, ListCallback callback) {
        db.collection("follows")
                .whereEqualTo("followingId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> followerIds = new java.util.ArrayList<>();
                    queryDocumentSnapshots.forEach(doc -> {
                        String followerId = doc.getString("followerId");
                        if (followerId != null) {
                            followerIds.add(followerId);
                        }
                    });
                    callback.onSuccess(followerIds);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Get user's following
    public void getFollowing(String userId, ListCallback callback) {
        db.collection("follows")
                .whereEqualTo("followerId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> followingIds = new java.util.ArrayList<>();
                    queryDocumentSnapshots.forEach(doc -> {
                        String followingId = doc.getString("followingId");
                        if (followingId != null) {
                            followingIds.add(followingId);
                        }
                    });
                    callback.onSuccess(followingIds);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}