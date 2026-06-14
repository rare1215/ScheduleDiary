package com.schedulediary.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.schedulediary.R;
import com.schedulediary.controller.DataController;
import com.schedulediary.controller.ImageLoader;
import com.schedulediary.controller.ObjectController;
import com.schedulediary.model.AdPage;
import com.schedulediary.model.PageElement;
import com.schedulediary.view.PageCanvasView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 페이지 편집 화면 (스크린샷 2번)
 * 텍스트 / 이미지 / 도형 삽입, Undo/Redo, 저장 (Analysis §Use case #8~13)
 */
public class PageEditActivity extends AppCompatActivity {

    // ── 툴바 ──
    private ImageButton btnBack;
    private TextView tvPageTitle;
    private MaterialButton btnSave;

    // ── 서식 툴바 (스크린샷 2번) ──
    private ImageButton btnBold, btnItalic, btnUnderline, btnStrike;
    private ImageButton btnAlignLeft, btnAlignCenter, btnAlignRight;
    private ImageButton btnBullet, btnList;
    private ImageButton btnShape, btnBackground, btnImage;
    private ImageButton btnUndo, btnRedo;

    // ── 캔버스 ──
    private PageCanvasView canvasView;

    // ── 데이터 ──
    private DataController dataController;
    private ObjectController objectController;
    private ImageLoader imageLoader;
    private final Gson gson = new Gson();

    private AdPage currentPage;
    private int pageId;
    private int diaryId;

    // ── 이미지 선택 런처 ──
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) loadAndInsertImage(uri);
            });

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) imagePickerLauncher.launch("image/*");
                else Toast.makeText(this, "이미지 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_edit);

        pageId = getIntent().getIntExtra(DiaryDetailActivity.EXTRA_PAGE_ID, -1);
        diaryId = getIntent().getIntExtra(DiaryDetailActivity.EXTRA_DIARY_ID, -1);

        dataController = DataController.getInstance(this);
        objectController = new ObjectController();
        imageLoader = new ImageLoader(this);

        initViews();
        setListeners();
        loadPage();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvPageTitle = findViewById(R.id.tvPageTitle);
        btnSave = findViewById(R.id.btnSave);

        btnBold = findViewById(R.id.btnBold);
        btnItalic = findViewById(R.id.btnItalic);
        btnUnderline = findViewById(R.id.btnUnderline);
        btnStrike = findViewById(R.id.btnStrike);
        btnAlignLeft = findViewById(R.id.btnAlignLeft);
        btnAlignCenter = findViewById(R.id.btnAlignCenter);
        btnAlignRight = findViewById(R.id.btnAlignRight);
        btnBullet = findViewById(R.id.btnBullet);
        btnList = findViewById(R.id.btnList);
        btnShape = findViewById(R.id.btnShape);
        btnBackground = findViewById(R.id.btnBackground);
        btnImage = findViewById(R.id.btnImage);
        btnUndo = findViewById(R.id.btnUndo);
        btnRedo = findViewById(R.id.btnRedo);

        canvasView = findViewById(R.id.canvasView);

        // ObjectController ↔ CanvasView 연결
        objectController.setOnChangeListener(elements -> {
            canvasView.setElements(elements);
            updateUndoRedoState();
        });

        canvasView.setOnElementSelectedListener(element -> {
            // 요소 선택 시 속성 툴바 업데이트
            updateToolbarForElement(element);
        });

        canvasView.setOnElementMovedListener((element, dx, dy) -> {
            // 드래그로 위치 변경 후 ObjectController에 반영
            PageElement updated = cloneElement(element);
            updated.setX(element.getX() + dx);
            updated.setY(element.getY() + dy);
            objectController.modifyElement(element.getElementId(), updated);
        });
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnSave.setOnClickListener(v -> handleSave());

        // ── 텍스트 삽입 (클릭 시 텍스트 박스 추가)
        btnList.setOnClickListener(v -> insertTextElement());

        // ── 서식 버튼 (현재 선택된 텍스트 요소에 적용)
        btnBold.setOnClickListener(v -> applyTextFormat("bold"));
        btnItalic.setOnClickListener(v -> applyTextFormat("italic"));
        btnUnderline.setOnClickListener(v -> applyTextFormat("underline"));
        btnStrike.setOnClickListener(v -> applyTextFormat("strike"));
        btnAlignLeft.setOnClickListener(v -> applyTextAlign(0));
        btnAlignCenter.setOnClickListener(v -> applyTextAlign(1));
        btnAlignRight.setOnClickListener(v -> applyTextAlign(2));

        // ── 이미지 삽입 (Design.md §4 handleInsertElement, §7 ImageLoader)
        btnImage.setOnClickListener(v -> handleInsertImage());

        // ── 도형 삽입
        btnShape.setOnClickListener(v -> showShapeMenu());

        // ── 배경 편집 (Analysis §Use case #7)
        btnBackground.setOnClickListener(v -> showBackgroundMenu());

        // ── Undo / Redo (Design.md §4 handleUndo/handleRedo)
        btnUndo.setOnClickListener(v -> {
            if (objectController.canUndo()) {
                objectController.undo();
            } else {
                Toast.makeText(this, "실행 취소할 작업이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        btnRedo.setOnClickListener(v -> {
            if (objectController.canRedo()) {
                objectController.redo();
            } else {
                Toast.makeText(this, "재실행할 작업이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ────────────────────────────────────────────────
    // 페이지 데이터 로드
    // ────────────────────────────────────────────────

    private void loadPage() {
        dataController.loadPageData(pageId, new DataController.Callback<AdPage>() {
            @Override
            public void onSuccess(AdPage page) {
                if (page == null) { finish(); return; }
                currentPage = page;
                tvPageTitle.setText(page.getTitle());

                // 저장된 요소 복원
                List<PageElement> elements = parseElements(page.getElementsJson());
                objectController.setElements(elements);
                canvasView.setElements(elements);

                // 배경 적용
                applyBackgroundFromJson(page.getBackgroundStyle());
                updateUndoRedoState();
            }
            @Override
            public void onError(String message) {
                Toast.makeText(PageEditActivity.this, "페이지를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // ────────────────────────────────────────────────
    // 저장 (Design.md §4 handleSave, Analysis §Use case #13)
    // ────────────────────────────────────────────────

    private void handleSave() {
        if (currentPage == null) return;

        btnSave.setEnabled(false);

        // 요소 직렬화
        List<PageElement> elements = objectController.getElements();
        String elementsJson = gson.toJson(elements);
        currentPage.setElementsJson(elementsJson);

        dataController.savePageData(currentPage, new DataController.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                btnSave.setEnabled(true);
                Toast.makeText(PageEditActivity.this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(String message) {
                btnSave.setEnabled(true);
                new AlertDialog.Builder(PageEditActivity.this)
                        .setTitle("저장 실패")
                        .setMessage(message)
                        .setPositiveButton("확인", null)
                        .show();
            }
        });
    }

    // ────────────────────────────────────────────────
    // 요소 삽입 (Design.md §4 handleInsertElement)
    // ────────────────────────────────────────────────

    private void insertTextElement() {
        PageElement el = objectController.createElement(PageElement.ElementType.TEXT);
        el.setX(50f);
        el.setY(200f);
        objectController.addElement(el);
        canvasView.setSelectedElement(el);
    }

    private void handleInsertImage() {
        // 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                return;
            }
        }
        imagePickerLauncher.launch("image/*");
    }

    private void loadAndInsertImage(Uri uri) {
        imageLoader.loadImage(uri, new ImageLoader.ImageCallback() {
            @Override
            public void onSuccess(String optimizedPath) {
                PageElement el = objectController.createElement(PageElement.ElementType.IMAGE);
                el.setImagePath(optimizedPath);
                el.setX(50f);
                el.setY(200f);
                objectController.addElement(el);
                canvasView.setSelectedElement(el);
            }
            @Override
            public void onError(String message) {
                new AlertDialog.Builder(PageEditActivity.this)
                        .setTitle("이미지 삽입 실패")
                        .setMessage(message)
                        .setPositiveButton("확인", null)
                        .show();
            }
        });
    }

    private void showShapeMenu() {
        String[] shapes = {"사각형", "원", "삼각형", "선"};
        PageElement.ShapeType[] types = {
                PageElement.ShapeType.RECTANGLE,
                PageElement.ShapeType.CIRCLE,
                PageElement.ShapeType.TRIANGLE,
                PageElement.ShapeType.LINE
        };
        new AlertDialog.Builder(this)
                .setTitle("도형 선택")
                .setItems(shapes, (d, which) -> {
                    PageElement el = objectController.createElement(PageElement.ElementType.SHAPE);
                    el.setShapeType(types[which]);
                    el.setX(100f);
                    el.setY(200f);
                    objectController.addElement(el);
                    canvasView.setSelectedElement(el);
                })
                .show();
    }

    private void showBackgroundMenu() {
        String[] options = {"흰색 배경", "라벤더 배경", "연한 핑크 배경", "배경 이미지 선택"};
        String[] colors = {"#FFFFFF", "#E9D5FF", "#FFC1C1", null};

        new AlertDialog.Builder(this)
                .setTitle("배경 설정")
                .setItems(options, (d, which) -> {
                    if (which < 3) {
                        setBackgroundColor(colors[which]);
                    } else {
                        imagePickerLauncher.launch("image/*");
                    }
                })
                .show();
    }

    private void setBackgroundColor(String hex) {
        canvasView.setBackgroundColor(Color.parseColor(hex));
        if (currentPage != null) {
            currentPage.setBackgroundStyle("{\"type\":\"color\",\"value\":\"" + hex + "\"}");
        }
    }

    private void applyBackgroundFromJson(String json) {
        if (json == null) return;
        try {
            if (json.contains("\"color\"")) {
                int startIdx = json.indexOf("\"value\":\"") + 9;
                int endIdx = json.indexOf("\"", startIdx);
                String hex = json.substring(startIdx, endIdx);
                canvasView.setBackgroundColor(Color.parseColor(hex));
            }
        } catch (Exception ignored) {}
    }

    // ────────────────────────────────────────────────
    // 텍스트 서식 적용
    // ────────────────────────────────────────────────

    private void applyTextFormat(String format) {
        PageElement selected = canvasView.getSelectedElement();
        if (selected == null || selected.getType() != PageElement.ElementType.TEXT) {
            Toast.makeText(this, "텍스트 요소를 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        PageElement updated = cloneElement(selected);
        switch (format) {
            case "bold":      updated.setBold(!selected.isBold()); break;
            case "italic":    updated.setItalic(!selected.isItalic()); break;
            case "underline": updated.setUnderline(!selected.isUnderline()); break;
            case "strike":    updated.setStrikethrough(!selected.isStrikethrough()); break;
        }
        objectController.modifyElement(selected.getElementId(), updated);
        canvasView.setSelectedElement(updated);
    }

    private void applyTextAlign(int align) {
        PageElement selected = canvasView.getSelectedElement();
        if (selected == null || selected.getType() != PageElement.ElementType.TEXT) return;
        PageElement updated = cloneElement(selected);
        updated.setTextAlign(align);
        objectController.modifyElement(selected.getElementId(), updated);
        canvasView.setSelectedElement(updated);
    }

    private void updateToolbarForElement(PageElement element) {
        if (element == null || element.getType() != PageElement.ElementType.TEXT) return;
        btnBold.setAlpha(element.isBold() ? 1.0f : 0.4f);
        btnItalic.setAlpha(element.isItalic() ? 1.0f : 0.4f);
        btnUnderline.setAlpha(element.isUnderline() ? 1.0f : 0.4f);
        btnStrike.setAlpha(element.isStrikethrough() ? 1.0f : 0.4f);
    }

    private void updateUndoRedoState() {
        btnUndo.setAlpha(objectController.canUndo() ? 1.0f : 0.3f);
        btnRedo.setAlpha(objectController.canRedo() ? 1.0f : 0.3f);
    }

    // ────────────────────────────────────────────────
    // 유틸
    // ────────────────────────────────────────────────

    private List<PageElement> parseElements(String json) {
        if (json == null || json.isEmpty() || json.equals("[]")) return new ArrayList<>();
        try {
            Type type = new TypeToken<List<PageElement>>() {}.getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /** 얕은 복사용 (Gson 직렬화/역직렬화로 딥카피) */
    private PageElement cloneElement(PageElement el) {
        return gson.fromJson(gson.toJson(el), PageElement.class);
    }

    @Override
    public void onBackPressed() {
        // 미저장 변경 사항 있으면 경고
        new AlertDialog.Builder(this)
                .setTitle("페이지 나가기")
                .setMessage("저장하지 않은 변경사항이 있습니다. 저장하고 나가시겠습니까?")
                .setPositiveButton("저장 후 나가기", (d, w) -> {
                    handleSave();
                    super.onBackPressed();
                })
                .setNegativeButton("저장 안 함", (d, w) -> super.onBackPressed())
                .setNeutralButton("취소", null)
                .show();
    }
}
