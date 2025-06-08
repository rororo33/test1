package com.example.todayrecipe;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todayrecipe.adapter.RecipeAdapter;
import com.example.todayrecipe.manager.RecipeManager;
import com.example.todayrecipe.manager.UserManager;
import com.example.todayrecipe.model.Recipe;
import com.example.todayrecipe.model.User;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {
    private TextView emailText, nameText, nicknameText;
    private Button changePasswordButton, logoutButton;
    private RecyclerView myRecipesRecyclerView;
    private RecipeAdapter adapter;
    private UserManager userManager;
    private RecipeManager recipeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setTitle("마이페이지");

        // 뒤로가기 버튼 활성화
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        userManager = UserManager.getInstance();
        recipeManager = RecipeManager.getInstance();

        initViews();
        loadUserInfo();
        loadMyRecipes();
        setupListeners();

        // 팔로워/팔로잉 수 표시 추가
        TextView followerCountText = findViewById(R.id.followerCountText);
        TextView followingCountText = findViewById(R.id.followingCountText);

        // 팔로워 수 클릭 시 팔로워 목록 보기
        if (followerCountText != null) {
            followerCountText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProfileActivity.this, FollowListActivity.class);
                    intent.putExtra("userId", userManager.getCurrentUser().getUid());
                    intent.putExtra("type", "followers");
                    startActivity(intent);
                }
            });
        }

        // 팔로잉 수 클릭 시 팔로잉 목록 보기
        if (followingCountText != null) {
            followingCountText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProfileActivity.this, FollowListActivity.class);
                    intent.putExtra("userId", userManager.getCurrentUser().getUid());
                    intent.putExtra("type", "following");
                    startActivity(intent);
                }
            });
        }
    }

    private void initViews() {
        emailText = findViewById(R.id.emailText);
        nameText = findViewById(R.id.nameText);
        nicknameText = findViewById(R.id.nicknameText);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        logoutButton = findViewById(R.id.logoutButton);
        myRecipesRecyclerView = findViewById(R.id.myRecipesRecyclerView);

        adapter = new RecipeAdapter(new ArrayList<>(), this);
        myRecipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        myRecipesRecyclerView.setAdapter(adapter);
    }

    private void loadUserInfo() {
        FirebaseUser currentUser = userManager.getCurrentUser();
        if (currentUser != null) {
            emailText.setText("이메일: " + currentUser.getEmail());

            userManager.getUserById(currentUser.getUid(), new UserManager.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    nameText.setText("이름: " + user.getName());
                    nicknameText.setText("닉네임: " + user.getNickname());

                    // 팔로워/팔로잉 수 표시
                    TextView followerCountText = findViewById(R.id.followerCountText);
                    TextView followingCountText = findViewById(R.id.followingCountText);
                    if (followerCountText != null) {
                        followerCountText.setText("팔로워 " + user.getFollowerCount());
                    }
                    if (followingCountText != null) {
                        followingCountText.setText("팔로잉 " + user.getFollowingCount());
                    }
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadMyRecipes() {
        FirebaseUser currentUser = userManager.getCurrentUser();
        if (currentUser != null) {
            recipeManager.getRecipesByUserId(currentUser.getUid(), new RecipeManager.RecipeListCallback() {
                @Override
                public void onSuccess(List<Recipe> recipes) {
                    adapter.updateRecipes(recipes);
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupListeners() {
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordDialog();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmation();
            }
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);

        EditText oldPasswordEdit = dialogView.findViewById(R.id.oldPasswordEdit);
        EditText newPasswordEdit = dialogView.findViewById(R.id.newPasswordEdit);
        EditText confirmPasswordEdit = dialogView.findViewById(R.id.confirmPasswordEdit);

        builder.setView(dialogView)
                .setTitle("비밀번호 변경")
                .setPositiveButton("변경", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String oldPassword = oldPasswordEdit.getText().toString();
                        String newPassword = newPasswordEdit.getText().toString();
                        String confirmPassword = confirmPasswordEdit.getText().toString();

                        if (!newPassword.equals(confirmPassword)) {
                            Toast.makeText(ProfileActivity.this, "새 비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (newPassword.length() < 6) {
                            Toast.makeText(ProfileActivity.this, "비밀번호는 6자 이상이어야 합니다", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        userManager.changePassword(oldPassword, newPassword, new UserManager.AuthCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(ProfileActivity.this, "비밀번호가 변경되었습니다", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("로그아웃")
                .setMessage("정말 로그아웃 하시겠습니까?")
                .setPositiveButton("로그아웃", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userManager.logout();
                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
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
            finish(); // 현재 액티비티 종료 (이전 화면으로 돌아감)
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 팔로워/팔로잉 수가 변경되었을 수 있으므로 다시 로드
        loadUserInfo();
    }
}