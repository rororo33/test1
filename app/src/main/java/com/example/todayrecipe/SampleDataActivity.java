package com.example.todayrecipe;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.todayrecipe.model.Recipe;
import com.example.todayrecipe.model.Ingredient;
import com.example.todayrecipe.model.CookingStep;
import com.example.todayrecipe.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SampleDataActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private Button addSampleDataButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 간단한 레이아웃 생성
        addSampleDataButton = new Button(this);
        addSampleDataButton.setText("샘플 데이터 추가");
        setContentView(addSampleDataButton);

        db = FirebaseFirestore.getInstance();

        addSampleDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSampleData();
            }
        });
    }

    private void addSampleData() {
        // 샘플 사용자 추가
        addSampleUsers();

        // 샘플 레시피 추가
        addSampleRecipes();

        Toast.makeText(this, "샘플 데이터 추가 시작", Toast.LENGTH_SHORT).show();
    }

    private void addSampleUsers() {
        // 사용자 1
        Map<String, Object> user1Data = new HashMap<>();
        user1Data.put("userId", "sampleUser1");
        user1Data.put("email", "user1@example.com");
        user1Data.put("name", "김철수");
        user1Data.put("nickname", "요리왕철수");
        user1Data.put("registrationDate", new Date());
        user1Data.put("followerCount", 0);
        user1Data.put("followingCount", 0);

        db.collection("users").document("sampleUser1").set(user1Data);

        // 사용자 2
        Map<String, Object> user2Data = new HashMap<>();
        user2Data.put("userId", "sampleUser2");
        user2Data.put("email", "user2@example.com");
        user2Data.put("name", "이영희");
        user2Data.put("nickname", "영희의주방");
        user2Data.put("registrationDate", new Date());
        user2Data.put("followerCount", 0);
        user2Data.put("followingCount", 0);

        db.collection("users").document("sampleUser2").set(user2Data);
    }

    private void addSampleRecipes() {
        // 레시피 1: 김치찌개
        Recipe recipe1 = new Recipe(
                "김치찌개",
                "얼큰하고 시원한 김치찌개입니다. 돼지고기와 잘 익은 김치로 만드는 한국인의 소울푸드!",
                "한식",
                30,
                "쉬움",
                2
        );
        recipe1.setRecipeId("recipe1");
        recipe1.setUserId("sampleUser1");
        recipe1.setAverageRating(4.5f);
        recipe1.setRatingCount(10);
        recipe1.setViewCount(150);

        // 김치찌개 재료
        List<Ingredient> ingredients1 = new ArrayList<>();
        ingredients1.add(new Ingredient("김치", "300", "g", false));
        ingredients1.add(new Ingredient("돼지고기", "200", "g", false));
        ingredients1.add(new Ingredient("두부", "1/2", "모", false));
        ingredients1.add(new Ingredient("대파", "1", "대", false));
        ingredients1.add(new Ingredient("고춧가루", "1", "큰술", true));
        recipe1.setIngredients(ingredients1);

        // 김치찌개 조리 단계
        List<CookingStep> steps1 = new ArrayList<>();
        steps1.add(new CookingStep(1, "돼지고기를 한입 크기로 썰어 팬에 볶아주세요."));
        steps1.add(new CookingStep(2, "고기가 어느 정도 익으면 김치를 넣고 함께 볶아주세요."));
        steps1.add(new CookingStep(3, "물을 붓고 끓여주세요. 끓기 시작하면 두부를 넣어주세요."));
        steps1.add(new CookingStep(4, "마지막에 대파를 썰어 넣고 한소끔 더 끓여주면 완성!"));
        recipe1.setCookingSteps(steps1);

        db.collection("recipes").document("recipe1").set(recipe1);

        // 레시피 2: 불고기
        Recipe recipe2 = new Recipe(
                "불고기",
                "달콤한 양념에 재운 부드러운 불고기. 남녀노소 모두가 좋아하는 한국 대표 요리!",
                "한식",
                40,
                "보통",
                4
        );
        recipe2.setRecipeId("recipe2");
        recipe2.setUserId("sampleUser1");
        recipe2.setAverageRating(4.8f);
        recipe2.setRatingCount(25);
        recipe2.setViewCount(320);

        // 불고기 재료
        List<Ingredient> ingredients2 = new ArrayList<>();
        ingredients2.add(new Ingredient("소고기(불고기용)", "600", "g", false));
        ingredients2.add(new Ingredient("간장", "4", "큰술", false));
        ingredients2.add(new Ingredient("설탕", "2", "큰술", false));
        ingredients2.add(new Ingredient("배", "1/4", "개", false));
        ingredients2.add(new Ingredient("양파", "1", "개", false));
        recipe2.setIngredients(ingredients2);

        // 불고기 조리 단계
        List<CookingStep> steps2 = new ArrayList<>();
        steps2.add(new CookingStep(1, "배를 갈아서 즙을 만들고, 간장, 설탕과 섞어 양념을 만듭니다."));
        steps2.add(new CookingStep(2, "소고기를 양념에 30분 이상 재워둡니다."));
        steps2.add(new CookingStep(3, "팬에 기름을 두르고 양파를 먼저 볶아주세요."));
        steps2.add(new CookingStep(4, "재운 고기를 넣고 중불에서 익혀주면 완성!"));
        recipe2.setCookingSteps(steps2);

        db.collection("recipes").document("recipe2").set(recipe2);

        // 레시피 3: 토마토 파스타
        Recipe recipe3 = new Recipe(
                "토마토 파스타",
                "신선한 토마토로 만드는 정통 이탈리안 파스타",
                "양식",
                25,
                "쉬움",
                2
        );
        recipe3.setRecipeId("recipe3");
        recipe3.setUserId("sampleUser2");
        recipe3.setAverageRating(4.3f);
        recipe3.setRatingCount(15);
        recipe3.setViewCount(200);

        // 파스타 재료
        List<Ingredient> ingredients3 = new ArrayList<>();
        ingredients3.add(new Ingredient("파스타면", "200", "g", false));
        ingredients3.add(new Ingredient("토마토", "3", "개", false));
        ingredients3.add(new Ingredient("마늘", "3", "쪽", false));
        ingredients3.add(new Ingredient("올리브오일", "3", "큰술", false));
        ingredients3.add(new Ingredient("바질", "5", "잎", true));
        recipe3.setIngredients(ingredients3);

        // 파스타 조리 단계
        List<CookingStep> steps3 = new ArrayList<>();
        steps3.add(new CookingStep(1, "끓는 물에 소금을 넣고 파스타를 삶아주세요."));
        steps3.add(new CookingStep(2, "토마토는 껍질을 벗기고 잘게 썰어주세요."));
        steps3.add(new CookingStep(3, "팬에 올리브오일을 두르고 마늘을 볶아 향을 내주세요."));
        steps3.add(new CookingStep(4, "토마토를 넣고 으깨가며 소스를 만들어주세요."));
        steps3.add(new CookingStep(5, "삶은 파스타를 소스에 넣고 잘 섞어주면 완성!"));
        recipe3.setCookingSteps(steps3);

        db.collection("recipes").document("recipe3").set(recipe3);

        // 레시피 4: 된장찌개
        Recipe recipe4 = new Recipe(
                "된장찌개",
                "구수한 된장과 신선한 채소로 만드는 건강한 한식 찌개",
                "한식",
                25,
                "쉬움",
                2
        );
        recipe4.setRecipeId("recipe4");
        recipe4.setUserId("sampleUser1");
        recipe4.setAverageRating(4.6f);
        recipe4.setRatingCount(20);
        recipe4.setViewCount(280);

        List<Ingredient> ingredients4 = new ArrayList<>();
        ingredients4.add(new Ingredient("된장", "2", "큰술", false));
        ingredients4.add(new Ingredient("두부", "1/2", "모", false));
        ingredients4.add(new Ingredient("애호박", "1/2", "개", false));
        ingredients4.add(new Ingredient("감자", "1", "개", false));
        ingredients4.add(new Ingredient("양파", "1/2", "개", false));
        ingredients4.add(new Ingredient("청양고추", "1", "개", true));
        recipe4.setIngredients(ingredients4);

        List<CookingStep> steps4 = new ArrayList<>();
        steps4.add(new CookingStep(1, "감자와 애호박을 먹기 좋은 크기로 썰어주세요."));
        steps4.add(new CookingStep(2, "냄비에 물을 붓고 된장을 풀어주세요."));
        steps4.add(new CookingStep(3, "감자를 먼저 넣고 끓이다가 애호박과 두부를 넣어주세요."));
        steps4.add(new CookingStep(4, "마지막에 양파와 고추를 넣고 한소끔 끓이면 완성!"));
        recipe4.setCookingSteps(steps4);

        db.collection("recipes").document("recipe4").set(recipe4);

        // 레시피 5: 계란말이
        Recipe recipe5 = new Recipe(
                "계란말이",
                "폭신폭신한 계란말이. 도시락 반찬으로도 좋아요!",
                "한식",
                15,
                "쉬움",
                2
        );
        recipe5.setRecipeId("recipe5");
        recipe5.setUserId("sampleUser2");
        recipe5.setAverageRating(4.4f);
        recipe5.setRatingCount(30);
        recipe5.setViewCount(450);

        List<Ingredient> ingredients5 = new ArrayList<>();
        ingredients5.add(new Ingredient("계란", "4", "개", false));
        ingredients5.add(new Ingredient("소금", "약간", "", false));
        ingredients5.add(new Ingredient("설탕", "1/2", "티스푼", true));
        ingredients5.add(new Ingredient("대파", "1", "대", true));
        ingredients5.add(new Ingredient("당근", "1/4", "개", true));
        recipe5.setIngredients(ingredients5);

        List<CookingStep> steps5 = new ArrayList<>();
        steps5.add(new CookingStep(1, "계란을 그릇에 깨고 소금, 설탕을 넣어 잘 풀어주세요."));
        steps5.add(new CookingStep(2, "대파와 당근을 잘게 썰어 계란물에 넣어주세요."));
        steps5.add(new CookingStep(3, "팬에 기름을 두르고 계란물을 1/3씩 부어가며 말아주세요."));
        steps5.add(new CookingStep(4, "한김 식힌 후 먹기 좋게 썰어주면 완성!"));
        recipe5.setCookingSteps(steps5);

        db.collection("recipes").document("recipe5").set(recipe5);

        Toast.makeText(this, "샘플 레시피 5개 추가 완료!", Toast.LENGTH_LONG).show();
    }
}