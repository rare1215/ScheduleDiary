package com.schedulediary.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.schedulediary.R;
import com.schedulediary.controller.DataController;

/**
 * 회원가입 화면 (Analysis §Use case #1 Join)
 * Design.md §1 Registration 클래스에 대응
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etPasswordConfirm;
    private Button btnSubmit;
    private Button btnCancel;

    private DataController dataController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dataController = DataController.getInstance(this);
        initViews();
        setListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setListeners() {
        btnSubmit.setOnClickListener(v -> clickSubmit());
        btnCancel.setOnClickListener(v -> clickCancel());
    }

    /**
     * 회원가입 제출 (Design.md §1 clickSubmit)
     */
    private void clickSubmit() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String passwordConfirm = etPasswordConfirm.getText().toString();

        // 유효성 검사
        if (TextUtils.isEmpty(name)) {
            etName.setError("이름을 입력해주세요.");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("올바른 이메일 형식을 입력해주세요.");
            etEmail.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("비밀번호는 6자 이상이어야 합니다.");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(passwordConfirm)) {
            etPasswordConfirm.setError("비밀번호가 일치하지 않습니다.");
            etPasswordConfirm.requestFocus();
            return;
        }

        btnSubmit.setEnabled(false);

        dataController.registerNewUser(email, password, name, new DataController.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                btnSubmit.setEnabled(true);
                showSuccessAndFinish();
            }

            @Override
            public void onError(String message) {
                btnSubmit.setEnabled(true);
                new AlertDialog.Builder(RegisterActivity.this)
                        .setTitle("회원가입 실패")
                        .setMessage(message)
                        .setPositiveButton("확인", null)
                        .show();
            }
        });
    }

    /**
     * 회원가입 취소 → 로그인 화면으로 복귀 (Design.md §1 clickCancel)
     */
    private void clickCancel() {
        finish();
    }

    private void showSuccessAndFinish() {
        new AlertDialog.Builder(this)
                .setTitle("회원가입 완료")
                .setMessage("회원가입이 완료되었습니다!\n로그인 화면으로 이동합니다.")
                .setPositiveButton("확인", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }
}
