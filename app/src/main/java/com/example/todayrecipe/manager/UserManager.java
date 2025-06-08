package com.example.todayrecipe.manager;

import com.example.todayrecipe.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private static UserManager instance;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private UserManager() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(String error);
    }

    public interface AuthCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public void register(String email, String password, String name, String nickname, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {
                                User user = new User(firebaseUser.getUid(), email, name, nickname);
                                saveUserToFirestore(user, callback);
                            }
                        } else {
                            callback.onFailure("회원가입 실패: " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void saveUserToFirestore(User user, AuthCallback callback) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", user.getUserId());
        userData.put("email", user.getEmail());
        userData.put("name", user.getName());
        userData.put("nickname", user.getNickname());
        userData.put("registrationDate", user.getRegistrationDate());
        userData.put("followerCount", 0);
        userData.put("followingCount", 0);

        db.collection("users")
                .document(user.getUserId())
                .set(userData)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("사용자 정보 저장 실패: " + e.getMessage()));
    }

    public void login(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure("로그인 실패: " + task.getException().getMessage());
                        }
                    }
                });
    }

    public void changePassword(String oldPassword, String newPassword, AuthCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            // Re-authenticate first
            auth.signInWithEmailAndPassword(user.getEmail(), oldPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            callback.onSuccess();
                                        } else {
                                            callback.onFailure("비밀번호 변경 실패");
                                        }
                                    });
                        } else {
                            callback.onFailure("현재 비밀번호가 일치하지 않습니다");
                        }
                    });
        }
    }

    public void getUserById(String userId, UserCallback callback) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        // 팔로워/팔로잉 카운트가 null인 경우 0으로 초기화
                        boolean needsUpdate = false;
                        Map<String, Object> updates = new HashMap<>();

                        if (documentSnapshot.getLong("followerCount") == null) {
                            updates.put("followerCount", 0);
                            user.setFollowerCount(0);
                            needsUpdate = true;
                        }

                        if (documentSnapshot.getLong("followingCount") == null) {
                            updates.put("followingCount", 0);
                            user.setFollowingCount(0);
                            needsUpdate = true;
                        }

                        if (needsUpdate) {
                            db.collection("users").document(userId).update(updates);
                        }

                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("사용자를 찾을 수 없습니다");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void logout() {
        auth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }
}