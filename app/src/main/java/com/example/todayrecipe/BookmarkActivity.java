package com.example.todayrecipe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todayrecipe.adapter.RecipeAdapter;
import com.example.todayrecipe.manager.InteractionManager;
import com.example.todayrecipe.manager.RecipeManager;
import com.example.todayrecipe.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BookmarkActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {
    private RecyclerView recyclerView;
    private TextView emptyText;
    private RecipeAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        setTitle("북마크한 레시피");

        // 뒤로가기 버튼
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        loadBookmarkedRecipes();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.bookmarkRecyclerView);
        emptyText = findViewById(R.id.emptyText);

        adapter = new RecipeAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadBookmarkedRecipes() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("bookmarks")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> recipeIds = new ArrayList<>();
                    queryDocumentSnapshots.forEach(doc -> {
                        String recipeId = doc.getString("recipeId");
                        if (recipeId != null) {
                            recipeIds.add(recipeId);
                        }
                    });

                    if (recipeIds.isEmpty()) {
                        emptyText.setVisibility(android.view.View.VISIBLE);
                        recyclerView.setVisibility(android.view.View.GONE);
                    } else {
                        emptyText.setVisibility(android.view.View.GONE);
                        recyclerView.setVisibility(android.view.View.VISIBLE);
                        loadRecipes(recipeIds);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "북마크 로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadRecipes(List<String> recipeIds) {
        List<Recipe> recipes = new ArrayList<>();
        AtomicInteger loadedCount = new AtomicInteger(0);

        for (String recipeId : recipeIds) {
            db.collection("recipes")
                    .document(recipeId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Recipe recipe = documentSnapshot.toObject(Recipe.class);
                        if (recipe != null) {
                            recipe.setRecipeId(documentSnapshot.getId()); // ID 설정
                            recipes.add(recipe);
                        }

                        // 모든 레시피를 로드했는지 확인
                        if (loadedCount.incrementAndGet() == recipeIds.size()) {
                            // 모든 레시피가 로드되면 한 번에 업데이트
                            adapter.updateRecipes(recipes);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // 실패해도 카운트 증가
                        if (loadedCount.incrementAndGet() == recipeIds.size()) {
                            adapter.updateRecipes(recipes);
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
            finish(); // 현재 액티비티 종료 (이전 화면으로 돌아감)
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookmarkedRecipes(); // 화면 복귀 시 새로고침
    }
}