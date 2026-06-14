package com.schedulediary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.schedulediary.R;
import com.schedulediary.adapter.DiaryAdapter;
import com.schedulediary.controller.DataController;
import com.schedulediary.model.AdDiary;

import java.util.ArrayList;
import java.util.List;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * 다이어리 목록 메인 화면 (스크린샷 5번, Analysis §Use case #4 Create new diary)
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvUserGreeting;
    private TextInputEditText etSearchDiary;
    private RecyclerView rvDiaries;
    private ExtendedFloatingActionButton fabNewDiary;
    private ImageButton btnLogout;

    private DiaryAdapter diaryAdapter;
    private DataController dataController;
    private int currentUserId;

    public static final String EXTRA_DIARY_ID = "diaryId";
    public static final String EXTRA_DIARY_NAME = "diaryName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataController = DataController.getInstance(this);
        currentUserId = dataController.getLoggedInUserId();

        if (currentUserId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDiaryList();
    }

    private void initViews() {
        tvUserGreeting = findViewById(R.id.tvTitle);
        etSearchDiary = findViewById(R.id.etSearchDiary);
        rvDiaries = findViewById(R.id.rvDiaries);
        fabNewDiary = findViewById(R.id.fabNewDiary);
        btnLogout = findViewById(R.id.btnLogout);

        // RecyclerView 설정 (1열 리스트)
        diaryAdapter = new DiaryAdapter(new ArrayList<>(),
                this::onDiaryClick,
                this::onDiaryLongClick);
        rvDiaries.setLayoutManager(new GridLayoutManager(this, 1));
        rvDiaries.setAdapter(diaryAdapter);
    }

    private void setListeners() {
        fabNewDiary.setOnClickListener(v -> handleCreateDiary());

        btnLogout.setOnClickListener(v -> handleLogoutRequest());

        etSearchDiary.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    loadDiaryList();
                } else {
                    searchDiaries(query);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // ────────────────────────────────────────────────
    // 다이어리 목록 로드
    // ────────────────────────────────────────────────

    private void loadDiaryList() {
        dataController.loadDiaryList(currentUserId, new DataController.Callback<List<AdDiary>>() {
            @Override
            public void onSuccess(List<AdDiary> diaries) {
                diaryAdapter.updateData(diaries);
            }
            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchDiaries(String query) {
        dataController.searchDiaries(currentUserId, query, new DataController.Callback<List<AdDiary>>() {
            @Override
            public void onSuccess(List<AdDiary> diaries) {
                diaryAdapter.updateData(diaries);
            }
            @Override
            public void onError(String message) {}
        });
    }

    // ────────────────────────────────────────────────
    // 새 다이어리 생성 (Design.md §4 handleCreateDiary)
    // ────────────────────────────────────────────────

    private void handleCreateDiary() {
        showDiaryEditDialog(null);
    }

    private void showDiaryEditDialog(AdDiary existingDiary) {
        DiaryEditDialogFragment dialog = DiaryEditDialogFragment.newInstance(
                existingDiary,
                currentUserId,
                (diary, isNew) -> {
                    if (isNew) {
                        dataController.createDiary(diary, new DataController.Callback<Long>() {
                            @Override
                            public void onSuccess(Long id) {
                                loadDiaryList();
                                Toast.makeText(MainActivity.this, "다이어리가 생성되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onError(String message) {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("오류")
                                        .setMessage(message)
                                        .setPositiveButton("확인", null)
                                        .show();
                            }
                        });
                    } else {
                        dataController.updateDiary(diary, new DataController.Callback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                loadDiaryList();
                            }
                            @Override
                            public void onError(String message) {}
                        });
                    }
                }
        );
        dialog.show(getSupportFragmentManager(), "DiaryEditDialog");
    }

    // ────────────────────────────────────────────────
    // 다이어리 클릭 → 페이지 목록으로 이동
    // ────────────────────────────────────────────────

    private void onDiaryClick(AdDiary diary) {
        Intent intent = new Intent(this, DiaryDetailActivity.class);
        intent.putExtra(EXTRA_DIARY_ID, diary.getDiaryId());
        intent.putExtra(EXTRA_DIARY_NAME, diary.getDiaryName());
        startActivity(intent);
    }

    private void onDiaryLongClick(AdDiary diary) {
        new AlertDialog.Builder(this)
                .setTitle(diary.getDiaryName())
                .setItems(new String[]{"편집", "삭제"}, (dialog, which) -> {
                    if (which == 0) {
                        showDiaryEditDialog(diary);
                    } else {
                        confirmDeleteDiary(diary);
                    }
                })
                .show();
    }

    private void confirmDeleteDiary(AdDiary diary) {
        new AlertDialog.Builder(this)
                .setTitle("다이어리 삭제")
                .setMessage("'" + diary.getDiaryName() + "' 다이어리를 삭제하시겠습니까?\n포함된 모든 페이지도 삭제됩니다.")
                .setPositiveButton("삭제", (d, w) -> {
                    dataController.deleteDiary(diary, new DataController.Callback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            loadDiaryList();
                            Toast.makeText(MainActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onError(String message) {}
                    });
                })
                .setNegativeButton("취소", null)
                .show();
    }

    // ────────────────────────────────────────────────
    // 로그아웃 (Analysis §Use case #3 Logout)
    // ────────────────────────────────────────────────

    private void handleLogoutRequest() {
        new AlertDialog.Builder(this)
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("로그아웃", (d, w) -> {
                    dataController.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("취소", null)
                .show();
    }
}
