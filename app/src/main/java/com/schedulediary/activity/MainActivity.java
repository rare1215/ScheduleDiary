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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.schedulediary.R;
import com.schedulediary.adapter.DiaryAdapter;
import com.schedulediary.adapter.DragSortCallback;
import com.schedulediary.controller.DataController;
import com.schedulediary.model.AdDiary;

import java.util.ArrayList;
import java.util.List;

/**
 * 다이어리 목록 메인 화면 (스크린샷 5번, Analysis §Use case #4 Create new diary)
 * - 9번: 그리드(커버 미리보기) / 리스트(드래그 정렬) 보기모드 전환
 * - 8번: 리스트 모드에서 드래그로 다이어리 순서 변경
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvUserGreeting;
    private EditText etSearchDiary;
    private RecyclerView rvDiaries;
    private ExtendedFloatingActionButton fabNewDiary;
    private ImageButton btnLogout;
    private ImageButton btnViewMode;

    private DiaryAdapter diaryAdapter;
    private DataController dataController;
    private ItemTouchHelper itemTouchHelper;
    private int currentUserId;

    /** 검색 중에는 드래그 정렬을 막는다 (검색 결과 순서와 전체 정렬이 섞이지 않도록) */
    private boolean isSearching = false;

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
        btnViewMode = findViewById(R.id.btnViewMode);

        diaryAdapter = new DiaryAdapter(new ArrayList<>(),
                this::onDiaryClick,
                this::onDiaryLongClick);
        diaryAdapter.setViewType(DiaryAdapter.VIEW_TYPE_GRID);

        rvDiaries.setLayoutManager(new LinearLayoutManager(this));
        rvDiaries.setAdapter(diaryAdapter);

        // 드래그 정렬 (8번 수정사항: 리스트 모드에서만 동작)
        DragSortCallback dragCallback = new DragSortCallback(new DragSortCallback.OnItemMoveListener() {
            @Override
            public void onItemMove(int fromPosition, int toPosition) {
                diaryAdapter.moveItem(fromPosition, toPosition);
            }

            @Override
            public void onDragFinished() {
                persistDiaryOrder();
            }
        });
        itemTouchHelper = new ItemTouchHelper(dragCallback);
        itemTouchHelper.attachToRecyclerView(rvDiaries);

        diaryAdapter.setOnDragHandleTouchListener(viewHolder -> {
            if (diaryAdapter.getViewType() == DiaryAdapter.VIEW_TYPE_LIST && !isSearching) {
                itemTouchHelper.startDrag(viewHolder);
            }
        });

        updateViewModeIcon();
    }

    private void setListeners() {
        fabNewDiary.setOnClickListener(v -> handleCreateDiary());

        btnLogout.setOnClickListener(v -> handleLogoutRequest());

        btnViewMode.setOnClickListener(v -> toggleViewMode());

        etSearchDiary.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                String query = s.toString().trim();
                isSearching = !query.isEmpty();
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
    // 보기모드 전환 (9번 수정사항)
    // ────────────────────────────────────────────────

    private void toggleViewMode() {
        int newType = (diaryAdapter.getViewType() == DiaryAdapter.VIEW_TYPE_GRID)
                ? DiaryAdapter.VIEW_TYPE_LIST : DiaryAdapter.VIEW_TYPE_GRID;
        diaryAdapter.setViewType(newType);
        updateViewModeIcon();
    }

    private void updateViewModeIcon() {
        if (diaryAdapter.getViewType() == DiaryAdapter.VIEW_TYPE_GRID) {
            btnViewMode.setImageResource(R.drawable.ic_view_list);
        } else {
            btnViewMode.setImageResource(R.drawable.ic_grid_view);
        }
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

    /** 드래그 정렬 결과를 DB에 저장 (8번 수정사항) */
    private void persistDiaryOrder() {
        if (isSearching) return; // 검색 중에는 정렬 저장하지 않음
        dataController.updateDiaryOrder(diaryAdapter.getCurrentData(), new DataController.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {}
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
