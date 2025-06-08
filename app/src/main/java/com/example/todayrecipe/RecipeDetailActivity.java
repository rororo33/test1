package com.example.todayrecipe;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.todayrecipe.adapter.IngredientAdapter;
import com.example.todayrecipe.adapter.CookingStepAdapter;
import com.example.todayrecipe.adapter.CommentAdapter;
import com.example.todayrecipe.manager.RecipeManager;
import com.example.todayrecipe.manager.InteractionManager;
import com.example.todayrecipe.manager.UserManager;
import com.example.todayrecipe.model.Recipe;
import com.example.todayrecipe.model.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {
    private ImageView recipeImage;
    private TextView titleText, categoryText, cookingTimeText, difficultyText, servingsText;
    private TextView descriptionText;
    private RatingBar ratingBar;
    private TextView ratingText;
    private RecyclerView ingredientsRecyclerView, stepsRecyclerView, commentsRecyclerView;
    private Button bookmarkButton, ratingButton, reportButton, commentSubmitButton;
    private EditText commentEditText;

    private RecipeManager recipeManager;
    private InteractionManager interactionManager;
    private UserManager userManager;
    private String recipeId;
    private Recipe currentRecipe;
    private boolean isBookmarked = false;
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        recipeId = getIntent().getStringExtra("recipeId");
        if (recipeId == null) {
            finish();
            return;
        }

        // 뒤로가기 버튼 활성화
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recipeManager = RecipeManager.getInstance();
        interactionManager = InteractionManager.getInstance();
        userManager = UserManager.getInstance();

        initViews();
        loadRecipeDetails();
        checkBookmarkStatus();
        loadComments();
    }

    private void initViews() {
        recipeImage = findViewById(R.id.recipeImage);
        titleText = findViewById(R.id.titleText);
        categoryText = findViewById(R.id.categoryText);
        cookingTimeText = findViewById(R.id.cookingTimeText);
        difficultyText = findViewById(R.id.difficultyText);
        servingsText = findViewById(R.id.servingsText);
        descriptionText = findViewById(R.id.descriptionText);
        ratingBar = findViewById(R.id.ratingBar);
        ratingText = findViewById(R.id.ratingText);
        ingredientsRecyclerView = findViewById(R.id.ingredientsRecyclerView);
        stepsRecyclerView = findViewById(R.id.stepsRecyclerView);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        bookmarkButton = findViewById(R.id.bookmarkButton);
        ratingButton = findViewById(R.id.ratingButton);
        reportButton = findViewById(R.id.reportButton);
        commentEditText = findViewById(R.id.commentEditText);
        commentSubmitButton = findViewById(R.id.commentSubmitButton);

        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        stepsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        commentAdapter = new CommentAdapter(new ArrayList<>());
        commentsRecyclerView.setAdapter(commentAdapter);

        setupListeners();
    }

    private void setupListeners() {
        bookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBookmark();
            }
        });

        ratingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });

        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReportDialog();
            }
        });

        commentSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitComment();
            }
        });
    }

    private void loadRecipeDetails() {
        recipeManager.getRecipeById(recipeId, new RecipeManager.RecipeCallback() {
            @Override
            public void onSuccess(Recipe recipe) {
                currentRecipe = recipe;
                displayRecipe();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(RecipeDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayRecipe() {
        titleText.setText(currentRecipe.getTitle());
        categoryText.setText(currentRecipe.getCategory());
        cookingTimeText.setText(currentRecipe.getCookingTime() + "분");
        difficultyText.setText("난이도: " + currentRecipe.getDifficulty());
        servingsText.setText(currentRecipe.getServings() + "인분");
        descriptionText.setText(currentRecipe.getDescription());

        ratingBar.setRating(currentRecipe.getAverageRating());
        ratingText.setText(String.format("%.1f (%d)", currentRecipe.getAverageRating(), currentRecipe.getRatingCount()));

        if (currentRecipe.getMainImage() != null && !currentRecipe.getMainImage().isEmpty()) {
            Glide.with(this)
                    .load(currentRecipe.getMainImage())
                    .into(recipeImage);
        }

        if (currentRecipe.getIngredients() != null) {
            IngredientAdapter ingredientAdapter = new IngredientAdapter(currentRecipe.getIngredients());
            ingredientsRecyclerView.setAdapter(ingredientAdapter);
        }

        if (currentRecipe.getCookingSteps() != null) {
            CookingStepAdapter stepAdapter = new CookingStepAdapter(currentRecipe.getCookingSteps());
            stepsRecyclerView.setAdapter(stepAdapter);
        }
    }

    private void checkBookmarkStatus() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        interactionManager.isBookmarked(recipeId, new InteractionManager.BooleanCallback() {
            @Override
            public void onComplete(boolean success) {
                isBookmarked = success;
                updateBookmarkButton();
            }
        });
    }

    private void updateBookmarkButton() {
        bookmarkButton.setText(isBookmarked ? "북마크 해제" : "북마크");
    }

    private void toggleBookmark() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isBookmarked) {
            interactionManager.removeBookmark(recipeId, new InteractionManager.BooleanCallback() {
                @Override
                public void onComplete(boolean success) {
                    if (success) {
                        isBookmarked = false;
                        updateBookmarkButton();
                        Toast.makeText(RecipeDetailActivity.this, "북마크가 해제되었습니다", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            interactionManager.addBookmark(recipeId, new InteractionManager.BooleanCallback() {
                @Override
                public void onComplete(boolean success) {
                    if (success) {
                        isBookmarked = true;
                        updateBookmarkButton();
                        Toast.makeText(RecipeDetailActivity.this, "북마크에 추가되었습니다", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // 현재 액티비티 종료 (이전 화면으로 돌아감)
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showRatingDialog() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rating, null);

        RatingBar dialogRatingBar = dialogView.findViewById(R.id.dialogRatingBar);
        EditText commentEditText = dialogView.findViewById(R.id.commentEditText);

        builder.setView(dialogView)
                .setTitle("별점 주기")
                .setPositiveButton("등록", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        float rating = dialogRatingBar.getRating();
                        String comment = commentEditText.getText().toString();

                        interactionManager.addRating(recipeId, rating, comment, new InteractionManager.BooleanCallback() {
                            @Override
                            public void onComplete(boolean success) {
                                if (success) {
                                    Toast.makeText(RecipeDetailActivity.this, "별점이 등록되었습니다", Toast.LENGTH_SHORT).show();
                                    loadRecipeDetails(); // 별점 업데이트
                                } else {
                                    Toast.makeText(RecipeDetailActivity.this, "이미 별점을 등록하셨습니다", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void showReportDialog() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_report, null);

        RadioGroup reasonRadioGroup = dialogView.findViewById(R.id.reasonRadioGroup);
        EditText descriptionEditText = dialogView.findViewById(R.id.descriptionEditText);

        builder.setView(dialogView)
                .setTitle("레시피 신고")
                .setPositiveButton("신고", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String reason = "";
                        int selectedId = reasonRadioGroup.getCheckedRadioButtonId();
                        if (selectedId == R.id.radioInappropriate) {
                            reason = "부적절한 콘텐츠";
                        } else if (selectedId == R.id.radioSpam) {
                            reason = "스팸/광고";
                        } else if (selectedId == R.id.radioCopyright) {
                            reason = "저작권 침해";
                        } else if (selectedId == R.id.radioOther) {
                            reason = "기타";
                        }

                        String description = descriptionEditText.getText().toString();

                        interactionManager.reportRecipe(recipeId, reason, description, new InteractionManager.BooleanCallback() {
                            @Override
                            public void onComplete(boolean success) {
                                if (success) {
                                    Toast.makeText(RecipeDetailActivity.this, "신고가 접수되었습니다", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(RecipeDetailActivity.this, "신고 접수에 실패했습니다", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void submitComment() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        String content = commentEditText.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "댓글을 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Comment comment = new Comment(recipeId, userId, content);

        FirebaseFirestore.getInstance()
                .collection("comments")
                .add(comment)
                .addOnSuccessListener(documentReference -> {
                    commentEditText.setText("");
                    Toast.makeText(this, "댓글이 등록되었습니다", Toast.LENGTH_SHORT).show();
                    loadComments();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "댓글 등록에 실패했습니다", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadComments() {
        FirebaseFirestore.getInstance()
                .collection("comments")
                .whereEqualTo("recipeId", recipeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Comment> comments = queryDocumentSnapshots.toObjects(Comment.class);

                    // 각 댓글의 사용자 정보 로드
                    for (Comment comment : comments) {
                        userManager.getUserById(comment.getUserId(), new UserManager.UserCallback() {
                            @Override
                            public void onSuccess(com.example.todayrecipe.model.User user) {
                                comment.setUserNickname(user.getNickname());
                                commentAdapter.updateComments(comments);
                            }

                            @Override
                            public void onFailure(String error) {
                                comment.setUserNickname("익명");
                                commentAdapter.updateComments(comments);
                            }
                        });
                    }
                });
    }
}