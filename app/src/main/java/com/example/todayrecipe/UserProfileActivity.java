package com.example.todayrecipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todayrecipe.adapter.RecipeAdapter;
import com.example.todayrecipe.manager.InteractionManager;
import com.example.todayrecipe.manager.RecipeManager;
import com.example.todayrecipe.manager.UserManager;
import com.example.todayrecipe.model.Recipe;
import com.example.todayrecipe.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {
    private TextView nameText, nicknameText, followerCountText, followingCountText, recipeCountText;
    private Button followButton;
    private RecyclerView userRecipesRecyclerView;
    private RecipeAdapter adapter;

    private UserManager userManager;
    private RecipeManager recipeManager;
    private InteractionManager interactionManager;

    private String targetUserId;
    private boolean isFollowing = false;
    private boolean isOwnProfile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        targetUserId = getIntent().getStringExtra("userId");
        if (targetUserId == null) {
            finish();
            return;
        }

        // 뒤로가기 버튼 활성화
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        userManager = UserManager.getInstance();
        recipeManager = RecipeManager.getInstance();
        interactionManager = InteractionManager.getInstance();

        // 본인 프로필인지 확인
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getUid().equals(targetUserId)) {
            isOwnProfile = true;
        }

        initViews();
        loadUserInfo();
        loadUserRecipes();

        if (!isOwnProfile && currentUser != null) {
            checkFollowStatus();
        }
    }

    private void initViews() {
        nameText = findViewById(R.id.nameText);
        nicknameText = findViewById(R.id.nicknameText);
        followerCountText = findViewById(R.id.followerCountText);
        followingCountText = findViewById(R.id.followingCountText);
        recipeCountText = findViewById(R.id.recipeCountText);
        followButton = findViewById(R.id.followButton);
        userRecipesRecyclerView = findViewById(R.id.userRecipesRecyclerView);

        adapter = new RecipeAdapter(new ArrayList<>(), this);
        userRecipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userRecipesRecyclerView.setAdapter(adapter);

        // 본인 프로필이면 팔로우 버튼 숨기기
        if (isOwnProfile) {
            followButton.setVisibility(View.GONE);
        } else {
            followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleFollow();
                }
            });
        }

        // 팔로워 수 클릭 시 팔로워 목록 보기
        followerCountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, FollowListActivity.class);
                intent.putExtra("userId", targetUserId);
                intent.putExtra("type", "followers");
                startActivity(intent);
            }
        });

        // 팔로잉 수 클릭 시 팔로잉 목록 보기
        followingCountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, FollowListActivity.class);
                intent.putExtra("userId", targetUserId);
                intent.putExtra("type", "following");
                startActivity(intent);
            }
        });
    }

    private void loadUserInfo() {
        userManager.getUserById(targetUserId, new UserManager.UserCallback() {
            @Override
            public void onSuccess(User user) {
                setTitle(user.getNickname() + "님의 프로필");
                nameText.setText(user.getName());
                nicknameText.setText("@" + user.getNickname());

                // Firestore에서 직접 최신 팔로워/팔로잉 수 가져오기
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(targetUserId)
                        .get()
                        .addOnSuccessListener(doc -> {
                            Long followerCount = doc.getLong("followerCount");
                            Long followingCount = doc.getLong("followingCount");

                            followerCountText.setText("팔로워 " + (followerCount != null ? followerCount : 0));
                            followingCountText.setText("팔로잉 " + (followingCount != null ? followingCount : 0));
                        });
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(UserProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadUserRecipes() {
        recipeManager.getRecipesByUserId(targetUserId, new RecipeManager.RecipeListCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                adapter.updateRecipes(recipes);
                recipeCountText.setText("레시피 " + recipes.size() + "개");
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(UserProfileActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkFollowStatus() {
        interactionManager.isFollowing(targetUserId, new InteractionManager.BooleanCallback() {
            @Override
            public void onComplete(boolean following) {
                isFollowing = following;
                updateFollowButton();
            }
        });
    }

    private void updateFollowButton() {
        if (isFollowing) {
            followButton.setText("팔로우 취소");
            followButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray));
        } else {
            followButton.setText("팔로우");
            followButton.setBackgroundTintList(getResources().getColorStateList(R.color.purple_500));
        }
    }

    private void toggleFollow() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        followButton.setEnabled(false);

        if (isFollowing) {
            interactionManager.unfollowUser(targetUserId, new InteractionManager.BooleanCallback() {
                @Override
                public void onComplete(boolean success) {
                    followButton.setEnabled(true);
                    if (success) {
                        isFollowing = false;
                        updateFollowButton();
                        Toast.makeText(UserProfileActivity.this, "팔로우를 취소했습니다", Toast.LENGTH_SHORT).show();
                        // 팔로워 수 업데이트 - 약간의 지연 후 실행
                        followButton.postDelayed(() -> loadUserInfo(), 500);
                    } else {
                        Toast.makeText(UserProfileActivity.this, "팔로우 취소에 실패했습니다", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            interactionManager.followUser(targetUserId, new InteractionManager.BooleanCallback() {
                @Override
                public void onComplete(boolean success) {
                    followButton.setEnabled(true);
                    if (success) {
                        isFollowing = true;
                        updateFollowButton();
                        Toast.makeText(UserProfileActivity.this, "팔로우했습니다", Toast.LENGTH_SHORT).show();
                        // 팔로워 수 업데이트 - 약간의 지연 후 실행
                        followButton.postDelayed(() -> loadUserInfo(), 500);
                    } else {
                        Toast.makeText(UserProfileActivity.this, "팔로우에 실패했습니다", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("recipeId", recipe.getRecipeId());
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 팔로우 상태가 변경되었을 수 있으므로 다시 확인
        if (!isOwnProfile && FirebaseAuth.getInstance().getCurrentUser() != null) {
            checkFollowStatus();
            loadUserInfo();
        }
        // 실제 팔로워/팔로잉 수 다시 계산
        recalculateFollowCounts();
    }

    private void recalculateFollowCounts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 팔로워 수 계산
        db.collection("follows")
                .whereEqualTo("followingId", targetUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int followerCount = querySnapshot.size();
                    db.collection("users").document(targetUserId)
                            .update("followerCount", followerCount)
                            .addOnSuccessListener(aVoid -> {
                                followerCountText.setText("팔로워 " + followerCount);
                            });
                });

        // 팔로잉 수 계산
        db.collection("follows")
                .whereEqualTo("followerId", targetUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int followingCount = querySnapshot.size();
                    db.collection("users").document(targetUserId)
                            .update("followingCount", followingCount)
                            .addOnSuccessListener(aVoid -> {
                                followingCountText.setText("팔로잉 " + followingCount);
                            });
                });
    }
}