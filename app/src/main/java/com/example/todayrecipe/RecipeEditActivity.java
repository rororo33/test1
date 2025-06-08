package com.example.todayrecipe;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.todayrecipe.manager.RecipeManager;
import com.example.todayrecipe.model.Recipe;
import com.example.todayrecipe.model.Ingredient;
import com.example.todayrecipe.model.CookingStep;
import java.util.ArrayList;
import java.util.List;

public class RecipeEditActivity extends AppCompatActivity {
    private EditText titleEditText, descriptionEditText, cookingTimeEditText, servingsEditText;
    private Spinner categorySpinner, difficultySpinner;
    private LinearLayout ingredientsContainer, stepsContainer;
    private Button addIngredientButton, addStepButton, saveButton;

    private RecipeManager recipeManager;
    private String recipeId;
    private Recipe editingRecipe;
    private List<View> ingredientViews = new ArrayList<>();
    private List<View> stepViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_edit);

        // 뒤로가기 버튼 활성화
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recipeManager = RecipeManager.getInstance();
        recipeId = getIntent().getStringExtra("recipeId");

        initViews();
        setupSpinners();

        if (recipeId != null) {
            loadRecipeForEdit();
        } else {
            addIngredientView();
            addStepView();
        }

        setupListeners();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // 현재 액티비티 종료
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        cookingTimeEditText = findViewById(R.id.cookingTimeEditText);
        servingsEditText = findViewById(R.id.servingsEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        difficultySpinner = findViewById(R.id.difficultySpinner);
        ingredientsContainer = findViewById(R.id.ingredientsContainer);
        stepsContainer = findViewById(R.id.stepsContainer);
        addIngredientButton = findViewById(R.id.addIngredientButton);
        addStepButton = findViewById(R.id.addStepButton);
        saveButton = findViewById(R.id.saveButton);
    }

    private void setupSpinners() {
        String[] categories = {"한식", "중식", "양식", "일식", "기타"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        String[] difficulties = {"쉬움", "보통", "어려움"};
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, difficulties);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(difficultyAdapter);
    }

    private void setupListeners() {
        addIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addIngredientView();
            }
        });

        addStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStepView();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecipe();
            }
        });
    }

    private void addIngredientView() {
        View ingredientView = getLayoutInflater().inflate(R.layout.item_ingredient_input, null);
        ingredientsContainer.addView(ingredientView);
        ingredientViews.add(ingredientView);

        Button removeButton = ingredientView.findViewById(R.id.removeIngredientButton);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ingredientsContainer.removeView(ingredientView);
                ingredientViews.remove(ingredientView);
            }
        });
    }

    private void addStepView() {
        View stepView = getLayoutInflater().inflate(R.layout.item_step_input, null);
        stepsContainer.addView(stepView);
        stepViews.add(stepView);

        EditText stepNumberText = stepView.findViewById(R.id.stepNumberEditText);
        stepNumberText.setText(String.valueOf(stepViews.size()));

        Button removeButton = stepView.findViewById(R.id.removeStepButton);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stepsContainer.removeView(stepView);
                stepViews.remove(stepView);
                updateStepNumbers();
            }
        });
    }

    private void updateStepNumbers() {
        for (int i = 0; i < stepViews.size(); i++) {
            EditText stepNumberText = stepViews.get(i).findViewById(R.id.stepNumberEditText);
            stepNumberText.setText(String.valueOf(i + 1));
        }
    }

    private void loadRecipeForEdit() {
        recipeManager.getRecipeById(recipeId, new RecipeManager.RecipeCallback() {
            @Override
            public void onSuccess(Recipe recipe) {
                editingRecipe = recipe;
                displayRecipeData();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(RecipeEditActivity.this, error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayRecipeData() {
        titleEditText.setText(editingRecipe.getTitle());
        descriptionEditText.setText(editingRecipe.getDescription());
        cookingTimeEditText.setText(String.valueOf(editingRecipe.getCookingTime()));
        servingsEditText.setText(String.valueOf(editingRecipe.getServings()));

        // Set spinners
        for (int i = 0; i < categorySpinner.getCount(); i++) {
            if (categorySpinner.getItemAtPosition(i).toString().equals(editingRecipe.getCategory())) {
                categorySpinner.setSelection(i);
                break;
            }
        }

        for (int i = 0; i < difficultySpinner.getCount(); i++) {
            if (difficultySpinner.getItemAtPosition(i).toString().equals(editingRecipe.getDifficulty())) {
                difficultySpinner.setSelection(i);
                break;
            }
        }

        // Load ingredients
        if (editingRecipe.getIngredients() != null) {
            for (Ingredient ingredient : editingRecipe.getIngredients()) {
                addIngredientView();
                View lastView = ingredientViews.get(ingredientViews.size() - 1);
                EditText nameText = lastView.findViewById(R.id.ingredientNameEditText);
                EditText amountText = lastView.findViewById(R.id.ingredientAmountEditText);
                EditText unitText = lastView.findViewById(R.id.ingredientUnitEditText);

                nameText.setText(ingredient.getName());
                amountText.setText(ingredient.getAmount());
                unitText.setText(ingredient.getUnit());
            }
        }

        // Load steps
        if (editingRecipe.getCookingSteps() != null) {
            for (CookingStep step : editingRecipe.getCookingSteps()) {
                addStepView();
                View lastView = stepViews.get(stepViews.size() - 1);
                EditText descText = lastView.findViewById(R.id.stepDescriptionEditText);
                descText.setText(step.getDescription());
            }
        }
    }

    private void saveRecipe() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String cookingTimeStr = cookingTimeEditText.getText().toString().trim();
        String servingsStr = servingsEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        String difficulty = difficultySpinner.getSelectedItem().toString();

        if (title.isEmpty() || description.isEmpty() || cookingTimeStr.isEmpty() || servingsStr.isEmpty()) {
            Toast.makeText(this, "모든 필수 항목을 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        int cookingTime = Integer.parseInt(cookingTimeStr);
        int servings = Integer.parseInt(servingsStr);

        Recipe recipe;
        if (editingRecipe != null) {
            recipe = editingRecipe;
            recipe.setTitle(title);
            recipe.setDescription(description);
            recipe.setCookingTime(cookingTime);
            recipe.setServings(servings);
            recipe.setCategory(category);
            recipe.setDifficulty(difficulty);
        } else {
            recipe = new Recipe(title, description, category, cookingTime, difficulty, servings);
        }

        List<Ingredient> ingredients = new ArrayList<>();
        for (View view : ingredientViews) {
            EditText nameText = view.findViewById(R.id.ingredientNameEditText);
            EditText amountText = view.findViewById(R.id.ingredientAmountEditText);
            EditText unitText = view.findViewById(R.id.ingredientUnitEditText);

            String name = nameText.getText().toString().trim();
            String amount = amountText.getText().toString().trim();
            String unit = unitText.getText().toString().trim();

            if (!name.isEmpty()) {
                ingredients.add(new Ingredient(name, amount, unit, false));
            }
        }

        List<CookingStep> steps = new ArrayList<>();
        for (int i = 0; i < stepViews.size(); i++) {
            View view = stepViews.get(i);
            EditText descText = view.findViewById(R.id.stepDescriptionEditText);
            String desc = descText.getText().toString().trim();

            if (!desc.isEmpty()) {
                steps.add(new CookingStep(i + 1, desc));
            }
        }

        if (ingredients.isEmpty() || steps.isEmpty()) {
            Toast.makeText(this, "재료와 조리 단계를 최소 1개 이상 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        saveButton.setEnabled(false);
        recipeManager.createRecipe(recipe, ingredients, steps, new RecipeManager.StringCallback() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(RecipeEditActivity.this, "레시피가 저장되었습니다", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                saveButton.setEnabled(true);
                Toast.makeText(RecipeEditActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}