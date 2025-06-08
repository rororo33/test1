package com.example.todayrecipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todayrecipe.adapter.UserAdapter;
import com.example.todayrecipe.manager.InteractionManager;
import com.example.todayrecipe.manager.UserManager;
import com.example.todayrecipe.model.User;
import java.util.ArrayList;
import java.util.List;

public class FollowListActivity extends AppCompatActivity implements UserAdapter.OnUserClickListener {
    private RecyclerView recyclerView;
    private TextView emptyText;
    private UserAdapter adapter;

    private InteractionManager interactionManager;
    private UserManager userManager;

    private String userId;
    private String type; // "followers" or "following"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_list);

        userId = getIntent().getStringExtra("userId");
        type = getIntent().getStringExtra("type");

        if (userId == null || type == null) {
            finish();
            return;
        }

        // 타이틀 설정
        if ("followers".equals(type)) {
            setTitle("팔로워");
        } else {
            setTitle("팔로잉");
        }

        // 뒤로가기 버튼 활성화
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        interactionManager = InteractionManager.getInstance();
        userManager = UserManager.getInstance();

        initViews();
        loadFollowList();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.followListRecyclerView);
        emptyText = findViewById(R.id.emptyText);

        adapter = new UserAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadFollowList() {
        if ("followers".equals(type)) {
            loadFollowers();
        } else {
            loadFollowing();
        }
    }

    private void loadFollowers() {
        interactionManager.getFollowers(userId, new InteractionManager.ListCallback() {
            @Override
            public void onSuccess(List<String> userIds) {
                if (userIds.isEmpty()) {
                    emptyText.setText("팔로워가 없습니다");
                    emptyText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    loadUsers(userIds);
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(FollowListActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFollowing() {
        interactionManager.getFollowing(userId, new InteractionManager.ListCallback() {
            @Override
            public void onSuccess(List<String> userIds) {
                if (userIds.isEmpty()) {
                    emptyText.setText("팔로잉하는 사용자가 없습니다");
                    emptyText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    loadUsers(userIds);
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(FollowListActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUsers(List<String> userIds) {
        List<User> users = new ArrayList<>();
        int[] loadedCount = {0};

        for (String id : userIds) {
            userManager.getUserById(id, new UserManager.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    users.add(user);
                    loadedCount[0]++;

                    if (loadedCount[0] == userIds.size()) {
                        emptyText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.updateUsers(users);
                    }
                }

                @Override
                public void onFailure(String error) {
                    loadedCount[0]++;
                    if (loadedCount[0] == userIds.size()) {
                        if (users.isEmpty()) {
                            emptyText.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyText.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter.updateUsers(users);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onUserClick(User user) {
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra("userId", user.getUserId());
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
}