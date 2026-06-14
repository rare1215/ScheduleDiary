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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.schedulediary.R;
import com.schedulediary.adapter.PageAdapter;
import com.schedulediary.controller.DataController;
import com.schedulediary.model.AdPage;

import java.util.ArrayList;
import java.util.List;

/**
 * 다이어리 내 페이지 목록 화면 (스크린샷 3번, Analysis §Use case #5 Create new page)
 */
public class DiaryDetailActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvDiaryName;
    private TextView tvDescription;
    private EditText etSearchPage;
    private MaterialButton btnNewPage;
    private RecyclerView rvPages;

    private PageAdapter pageAdapter;
    private DataController dataController;
    private int diaryId;
    private String diaryName;

    public static final String EXTRA_PAGE_ID = "pageId";
    public static final String EXTRA_DIARY_ID = "diaryId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_detail);

        diaryId = getIntent().getIntExtra(MainActivity.EXTRA_DIARY_ID, -1);
        diaryName = getIntent().getStringExtra(MainActivity.EXTRA_DIARY_NAME);

        if (diaryId == -1) {
            finish();
            return;
        }

        dataController = DataController.getInstance(this);
        initViews();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPages();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvDiaryName = findViewById(R.id.tvDiaryName);
        tvDescription = findViewById(R.id.tvDescription);
        etSearchPage = findViewById(R.id.etSearchPage);
        btnNewPage = findViewById(R.id.btnNewPage);
        rvPages = findViewById(R.id.rvPages);

        tvDiaryName.setText(diaryName != null ? diaryName : "다이어리");

        // 다이어리 설명 로드
        dataController.getDiaryById(diaryId, new DataController.Callback<com.schedulediary.model.AdDiary>() {
            @Override
            public void onSuccess(com.schedulediary.model.AdDiary diary) {
                if (diary != null && diary.getDescription() != null) {
                    tvDescription.setText(diary.getDescription());
                }
            }
            @Override
            public void onError(String message) {}
        });

        pageAdapter = new PageAdapter(new ArrayList<>(),
                this::onPageClick,
                this::onPageLongClick);
        rvPages.setLayoutManager(new LinearLayoutManager(this));
        rvPages.setAdapter(pageAdapter);
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnNewPage.setOnClickListener(v -> handleCreatePage());

        etSearchPage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                String q = s.toString().trim();
                if (q.isEmpty()) loadPages();
                else searchPages(q);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadPages() {
        dataController.loadPageList(diaryId, new DataController.Callback<List<AdPage>>() {
            @Override
            public void onSuccess(List<AdPage> pages) {
                pageAdapter.updateData(pages);
            }
            @Override
            public void onError(String message) {
                Toast.makeText(DiaryDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPages(String query) {
        dataController.searchPages(diaryId, query, new DataController.Callback<List<AdPage>>() {
            @Override
            public void onSuccess(List<AdPage> pages) {
                pageAdapter.updateData(pages);
            }
            @Override
            public void onError(String message) {}
        });
    }

    // ────────────────────────────────────────────────
    // 새 페이지 생성 (Design.md §4 handleCreatePage, Analysis §Use case #5)
    // ────────────────────────────────────────────────

    private void handleCreatePage() {
        // 페이지 제목 입력
        final EditText etTitle = new EditText(this);
        etTitle.setHint("페이지 제목");

        new AlertDialog.Builder(this)
                .setTitle("새 페이지")
                .setView(etTitle)
                .setPositiveButton("생성", (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    if (title.isEmpty()) title = "새 페이지";
                    showPositionDialog(title);
                })
                .setNegativeButton("취소", null)
                .show();
    }

    /** 페이지 삽입 위치 선택 (Analysis §Use case #5, Design.md §8 addPage) */
    private void showPositionDialog(String title) {
        String[] options = {"맨 앞에 추가", "맨 뒤에 추가"};
        new AlertDialog.Builder(this)
                .setTitle("추가 위치 선택")
                .setItems(options, (d, which) -> {
                    int position = (which == 0) ? 0 : -1;
                    createPage(title, position);
                })
                .show();
    }

    private void createPage(String title, int position) {
        dataController.createPage(diaryId, title, position, new DataController.Callback<Long>() {
            @Override
            public void onSuccess(Long pageId) {
                loadPages();
                // 생성 후 바로 편집 화면으로 이동
                Intent intent = new Intent(DiaryDetailActivity.this, PageEditActivity.class);
                intent.putExtra(EXTRA_PAGE_ID, pageId.intValue());
                intent.putExtra(EXTRA_DIARY_ID, diaryId);
                startActivity(intent);
            }
            @Override
            public void onError(String message) {
                new AlertDialog.Builder(DiaryDetailActivity.this)
                        .setTitle("페이지 생성 실패")
                        .setMessage(message)
                        .setPositiveButton("확인", null)
                        .show();
            }
        });
    }

    // ────────────────────────────────────────────────
    // 페이지 클릭 → 편집 화면
    // ────────────────────────────────────────────────

    private void onPageClick(AdPage page) {
        Intent intent = new Intent(this, PageEditActivity.class);
        intent.putExtra(EXTRA_PAGE_ID, page.getPageId());
        intent.putExtra(EXTRA_DIARY_ID, diaryId);
        startActivity(intent);
    }

    private void onPageLongClick(AdPage page) {
        new AlertDialog.Builder(this)
                .setTitle(page.getTitle())
                .setItems(new String[]{"삭제"}, (dialog, which) -> {
                    if (which == 0) confirmDeletePage(page);
                })
                .show();
    }

    private void confirmDeletePage(AdPage page) {
        new AlertDialog.Builder(this)
                .setTitle("페이지 삭제")
                .setMessage("'" + page.getTitle() + "' 페이지를 삭제하시겠습니까?")
                .setPositiveButton("삭제", (d, w) -> {
                    dataController.deletePage(page, new DataController.Callback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            loadPages();
                        }
                        @Override
                        public void onError(String message) {}
                    });
                })
                .setNegativeButton("취소", null)
                .show();
    }
}
