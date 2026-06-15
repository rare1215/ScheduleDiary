package com.schedulediary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.schedulediary.R;
import com.schedulediary.controller.DataController;
import com.schedulediary.model.User;

/**
 * 로그인 화면 (Analysis §Use case #2 Login, 스크린샷 1번)
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvGoRegister;

    private DataController dataController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataController = DataController.getInstance(this);

        // 이미 로그인된 경우 메인으로 바로 이동
        if (dataController.isLoggedIn()) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);
        initViews();
        setListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);
    }

    private void setListeners() {
        btnLogin.setOnClickListener(v -> handleLoginSubmit());
        tvGoRegister.setOnClickListener(v -> handleRegisterRoute());
    }

    /**
     * 로그인 이벤트 처리 (Design.md §4 handleLoginSubmit)
     */
    private void handleLoginSubmit() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        // 유효성 검사
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("이메일을 입력해주세요.");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("비밀번호를 입력해주세요.");
            etPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);

        dataController.login(email, password, new DataController.Callback<User>() {
            @Override
            public void onSuccess(User user) {
                btnLogin.setEnabled(true);
                goToMain();
            }

            @Override
            public void onError(String message) {
                btnLogin.setEnabled(true);
                etPassword.setText("");
                showErrorDialog(message);
            }
        });
    }

    /**
     * 회원가입 화면으로 이동 (Design.md §4 handleRegisterRoute)
     */
    private void handleRegisterRoute() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showErrorDialog(String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("로그인 실패")
                .setMessage(message)
                .setPositiveButton("확인", null)
                .show();
    }
}
