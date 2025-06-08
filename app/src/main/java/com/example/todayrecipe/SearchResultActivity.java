package com.example.todayrecipe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todayrecipe.adapter.RecipeAdapter;
import com.example.todayrecipe.manager.RecipeManager;
import com.example.todayrecipe.model.Recipe;
import java.util.ArrayList;
import java.util.List;

public class SearchResultActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {
    private RecyclerView recyclerView;
    private TextView resultCountText;
    private RecipeAdapter adapter;
    private RecipeManager recipeManager;
    private String searchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        searchQuery = getIntent().getStringExtra("query");
        if (searchQuery == null) {
            finish();
            return;
        }

        setTitle("검색: " + searchQuery);

        recipeManager = RecipeManager.getInstance();
        initViews();
        performSearch();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.searchResultRecyclerView);
        resultCountText = findViewById(R.id.resultCountText);

        adapter = new RecipeAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void performSearch() {
        recipeManager.searchRecipes(searchQuery, new RecipeManager.RecipeListCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                adapter.updateRecipes(recipes);
                resultCountText.setText(recipes.size() + "개의 레시피를 찾았습니다");

                if (recipes.isEmpty()) {
                    Toast.makeText(SearchResultActivity.this, "검색 결과가 없습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(SearchResultActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("recipeId", recipe.getRecipeId());
        startActivity(intent);
    }
}