package com.example.todayrecipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todayrecipe.adapter.RecipeAdapter;
import com.example.todayrecipe.manager.RecipeManager;
import com.example.todayrecipe.manager.UserManager;
import com.example.todayrecipe.model.Recipe;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {
    private RecyclerView recommendRecyclerView;
    private RecyclerView popularRecyclerView;
    private SearchView searchView;
    private TextView recommendTitle, popularTitle;
    private RecipeAdapter recommendAdapter;
    private RecipeAdapter popularAdapter;
    private RecipeManager recipeManager;
    private FloatingActionButton fabAddRecipe;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recipeManager = RecipeManager.getInstance();
        initViews();
        setupRecyclerViews();
        loadRecommendations();
        setupListeners();
    }

    private void initViews() {
        recommendRecyclerView = findViewById(R.id.recommendRecyclerView);
        popularRecyclerView = findViewById(R.id.popularRecyclerView);
        searchView = findViewById(R.id.searchView);
        recommendTitle = findViewById(R.id.recommendTitle);
        popularTitle = findViewById(R.id.popularTitle);
        fabAddRecipe = findViewById(R.id.fabAddRecipe);
        bottomNavigation = findViewById(R.id.bottomNavigation);

    }

    private void setupRecyclerViews() {
        recommendAdapter = new RecipeAdapter(new ArrayList<>(), this);
        popularAdapter = new RecipeAdapter(new ArrayList<>(), this);

        recommendRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendRecyclerView.setAdapter(recommendAdapter);

        popularRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        popularRecyclerView.setAdapter(popularAdapter);
    }

    private void loadRecommendations() {
        // Load today's recommendations
        recipeManager.getTodayRecommendations(new RecipeManager.RecipeListCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                recommendAdapter.updateRecipes(recipes);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(HomeActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Load popular recipes
        recipeManager.getTodayRecommendations(new RecipeManager.RecipeListCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                popularAdapter.updateRecipes(recipes);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(HomeActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(HomeActivity.this, SearchResultActivity.class);
                intent.putExtra("query", query);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        fabAddRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, RecipeEditActivity.class);
                startActivity(intent);
            }
        });

        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_home) {
                return true;
            } else if (itemId == R.id.menu_search) {
                searchView.setIconified(false);
                return true;
            } else if (itemId == R.id.menu_bookmark) {
                Intent intent = new Intent(HomeActivity.this, BookmarkActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_profile) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("recipeId", recipe.getRecipeId());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            UserManager.getInstance().logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}