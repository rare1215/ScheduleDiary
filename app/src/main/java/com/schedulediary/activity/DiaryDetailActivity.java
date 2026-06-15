package com.schedulediary.activity;

import android.graphics.Color;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.schedulediary.R;
import com.schedulediary.adapter.DragSortCallback;
import com.schedulediary.adapter.PageAdapter;
import com.schedulediary.controller.DataController;
import com.schedulediary.model.AdDiary;
import com.schedulediary.model.AdPage;

import java.util.ArrayList;
import java.util.List;

/**
 * 다이어리 내 페이지 목록 화면 (스크린샷 3번, Analysis §Use case #5 Create new page)
 * - 3번: 새 페이지 생성 다이얼로그 UI 개선 (제목+위치 한 화면)
 * - 9번: 리스트(드래그 정렬) / 그리드(미리보기) 보기모드 전환
 * - 8번: 리스트 모드에서 드래그로 페이지 순서 변경
 * - 11번: 다이어리 커버 색상을 헤더 배경 및 페이지 강조색에 적용
 */
public class DiaryDetailActivity extends AppCompatActivity {

    private LinearLayout headerLayout;
    private ImageButton btnBack;
    private TextView tvBackLabel;
    private TextView tvDiaryName;
    private TextView tvDescription;
    private EditText etSearchPage;
    private MaterialButton btnNewPage;
    private ImageButton btnViewMode;
    private RecyclerView rvPages;

    private PageAdapter pageAdapter;
    private DataController dataController;
    private ItemTouchHelper itemTouchHelper;
    private int diaryId;
    private String diaryName;
    private AdDiary currentDiary;

    private boolean isSearching = false;

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
        loadDiaryInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPages();
    }

    private void initViews() {
        headerLayout = findViewById(R.id.headerLayout);
        btnBack = findViewById(R.id.btnBack);
        tvBackLabel = findViewById(R.id.tvBackLabel);
        tvDiaryName = findViewById(R.id.tvDiaryName);
        tvDescription = findViewById(R.id.tvDescription);
        etSearchPage = findViewById(R.id.etSearchPage);
        btnNewPage = findViewById(R.id.btnNewPage);
        btnViewMode = findViewById(R.id.btnViewMode);
        rvPages = findViewById(R.id.rvPages);

        tvDiaryName.setText(diaryName != null ? diaryName : "다이어리");

        pageAdapter = new PageAdapter(new ArrayList<>(),
                this::onPageClick,
                this::onPageLongClick);
        pageAdapter.setViewType(PageAdapter.VIEW_TYPE_LIST);

        rvPages.setLayoutManager(new LinearLayoutManager(this));
        rvPages.setAdapter(pageAdapter);

        // 드래그 정렬 (8번 수정사항: 리스트 모드에서만 동작)
        DragSortCallback dragCallback = new DragSortCallback(new DragSortCallback.OnItemMoveListener() {
            @Override
            public void onItemMove(int fromPosition, int toPosition) {
                pageAdapter.moveItem(fromPosition, toPosition);
            }

            @Override
            public void onDragFinished() {
                persistPageOrder();
            }
        });
        itemTouchHelper = new ItemTouchHelper(dragCallback);
        itemTouchHelper.attachToRecyclerView(rvPages);

        pageAdapter.setOnDragHandleTouchListener(viewHolder -> {
            if (pageAdapter.getViewType() == PageAdapter.VIEW_TYPE_LIST && !isSearching) {
                itemTouchHelper.startDrag(viewHolder);
            }
        });

        updateViewModeIcon();
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnNewPage.setOnClickListener(v -> handleCreatePage());
        btnViewMode.setOnClickListener(v -> toggleViewMode());

        etSearchPage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                String q = s.toString().trim();
                isSearching = !q.isEmpty();
                if (q.isEmpty()) loadPages();
                else searchPages(q);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // ────────────────────────────────────────────────
    // 다이어리 정보 로드 (11번: 헤더/강조색 동기화)
    // ────────────────────────────────────────────────

    private void loadDiaryInfo() {
        dataController.getDiaryById(diaryId, new DataController.Callback<AdDiary>() {
            @Override
            public void onSuccess(AdDiary diary) {
                currentDiary = diary;
                if (diary == null) return;

                if (diary.getDescription() != null) {
                    tvDescription.setText(diary.getDescription());
                }

                applyAccentColor(diary.getCoverColor());
            }
            @Override
            public void onError(String message) {}
        });
    }

    /** 다이어리 커버 색상을 헤더 배경 + 페이지 강조색에 적용 (11번 수정사항) */
    private void applyAccentColor(String hex) {
        int color;
        try {
            color = Color.parseColor(hex != null ? hex : "#C9A8F5");
        } catch (Exception e) {
            color = Color.parseColor("#C9A8F5");
        }
        headerLayout.setBackgroundColor(color);
        pageAdapter.setAccentColor(color);

        // 커버 색상이 밝은 색(흰색/연한 회색 계열)이면 헤더 텍스트를 어둡게,
        // 어두운 색이면 밝게 — 가독성 확보
        boolean isLight = isLightColor(color);
        int textColor = isLight ? Color.parseColor("#6B21A8") : Color.WHITE;
        tvDiaryName.setTextColor(textColor);
        tvDescription.setTextColor(isLight ? Color.parseColor("#9F7AEA") : Color.parseColor("#F3E8FF"));
        btnBack.setColorFilter(textColor);
        tvBackLabel.setTextColor(textColor);
    }

    private boolean isLightColor(int color) {
        double luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return luminance > 0.7;
    }

    // ────────────────────────────────────────────────
    // 보기모드 전환 (9번 수정사항)
    // ────────────────────────────────────────────────

    private void toggleViewMode() {
        int newType = (pageAdapter.getViewType() == PageAdapter.VIEW_TYPE_LIST)
                ? PageAdapter.VIEW_TYPE_GRID : PageAdapter.VIEW_TYPE_LIST;
        pageAdapter.setViewType(newType);
        updateRecyclerLayoutManager(newType);
        updateViewModeIcon();
    }

    private void updateRecyclerLayoutManager(int viewType) {
        if (viewType == PageAdapter.VIEW_TYPE_GRID) {
            rvPages.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            rvPages.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    private void updateViewModeIcon() {
        if (pageAdapter.getViewType() == PageAdapter.VIEW_TYPE_LIST) {
            btnViewMode.setImageResource(R.drawable.ic_grid_view);
        } else {
            btnViewMode.setImageResource(R.drawable.ic_view_list);
        }
    }

    // ────────────────────────────────────────────────
    // 페이지 목록 로드
    // ────────────────────────────────────────────────

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

    /** 드래그 정렬 결과를 DB에 저장 (8번 수정사항) */
    private void persistPageOrder() {
        if (isSearching) return;
        dataController.updatePageOrder(pageAdapter.getCurrentData(), new DataController.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                // 화면에 보이는 페이지 번호도 갱신
                pageAdapter.updateData(pageAdapter.getCurrentData());
            }
            @Override
            public void onError(String message) {}
        });
    }

    // ────────────────────────────────────────────────
    // 새 페이지 생성 (3번 수정사항: 다이얼로그 UI 개선)
    // ────────────────────────────────────────────────

    private void handleCreatePage() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_page, null);

        TextInputEditText etTitle = dialogView.findViewById(R.id.etPageTitle);
        RadioGroup rgPosition = dialogView.findViewById(R.id.rgPosition);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnCreate = dialogView.findViewById(R.id.btnCreate);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnCreate.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) title = "새 페이지";

            int position = (rgPosition.getCheckedRadioButtonId() == R.id.rbPositionFront) ? 0 : -1;

            dialog.dismiss();
            createPage(title, position);
        });

        dialog.show();
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
