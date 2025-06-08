package com.example.todayrecipe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.todayrecipe.manager.UserManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 2초 후 다음 화면으로 이동
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                UserManager userManager = UserManager.getInstance();
                Intent intent;

                if (userManager.isLoggedIn()) {
                    // 로그인 상태면 홈 화면으로
                    intent = new Intent(MainActivity.this, HomeActivity.class);
                } else {
                    // 비로그인 상태면 로그인 화면으로
                    intent = new Intent(MainActivity.this, LoginActivity.class);
                }

                startActivity(intent);
                finish();
            }
        }, 2000);
    }
}