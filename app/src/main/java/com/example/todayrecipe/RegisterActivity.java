package com.example.todayrecipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.todayrecipe.manager.UserManager;

public class RegisterActivity extends AppCompatActivity {
    private EditText nameEditText, emailEditText, passwordEditText, passwordConfirmEditText, nicknameEditText;
    private Button registerButton;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userManager = UserManager.getInstance();
        initViews();
        setupListeners();
    }

    private void initViews() {
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordConfirmEditText = findViewById(R.id.passwordConfirmEditText);
        nicknameEditText = findViewById(R.id.nicknameEditText);
        registerButton = findViewById(R.id.registerButton);
    }

    private void setupListeners() {
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String passwordConfirm = passwordConfirmEditText.getText().toString().trim();
                String nickname = nicknameEditText.getText().toString().trim();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "모든 필드를 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(passwordConfirm)) {
                    Toast.makeText(RegisterActivity.this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "비밀번호는 6자 이상이어야 합니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                registerButton.setEnabled(false);
                userManager.register(email, password, name, nickname, new UserManager.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(RegisterActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(String error) {
                        registerButton.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}