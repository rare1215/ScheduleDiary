package com.schedulediary.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.schedulediary.view.CropImageView;
import com.schedulediary.view.PageCanvasView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 페이지 편집 화면 (스크린샷 2번)
 * 텍스트 / 이미지 / 도형 삽입, Undo/Redo, 저장 (Analysis §Use case #8~13)
 *
 * 수정사항 반영:
 * - 4, 5번: 코너 4핸들 + 회전 핸들로 이미지/도형 자유 변형 (PageCanvasView)
 * - 5번: 배경 이미지 크롭 후 적용
 * - 6번: 텍스트 삽입 버튼 (btnAddText)
 * - 7번: 폰트 크기 선택 버튼 (btnFontSize)
 * - 10번: 저장 완료 후에만 화면 종료, 변경사항 없으면 확인 없이 나가기
 */
public class PageEditActivity extends AppCompatActivity {

    // ── 툴바 ──
    private ImageButton btnBack;
    private TextView tvPageTitle;
    private MaterialButton btnSave;

    // ── 서식 툴바 (스크린샷 2번) ──
    private ImageButton btnBold, btnItalic, btnUnderline, btnStrike;
    private ImageButton btnAlignLeft, btnAlignCenter, btnAlignRight;
    private ImageButton btnAddText;
    private ImageButton btnShape, btnBackground, btnImage;
    private MaterialButton btnFontSize;
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

    /** 10번 수정사항: 변경사항이 있는지 추적 */
    private boolean isDirty = false;

    /** 폰트 크기 옵션 (7번 수정사항) */
    private static final String[] FONT_SIZE_LABELS = {"작게", "보통", "크게", "아주 크게"};
    private static final float[] FONT_SIZE_VALUES  = {11f, 14f, 18f, 24f};

    /** 배경 이미지 선택인지 요소(이미지 추가)용 선택인지 구분 */
    private boolean pickingBackgroundImage = false;

    // ── 이미지 선택 런처 ──
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                if (pickingBackgroundImage) {
                    pickingBackgroundImage = false;
                    openCropDialog(uri);
                } else {
                    loadAndInsertImage(uri);
                }
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
        btnAddText = findViewById(R.id.btnAddText);
        btnShape = findViewById(R.id.btnShape);
        btnBackground = findViewById(R.id.btnBackground);
        btnImage = findViewById(R.id.btnImage);
        btnFontSize = findViewById(R.id.btnFontSize);
        btnUndo = findViewById(R.id.btnUndo);
        btnRedo = findViewById(R.id.btnRedo);

        canvasView = findViewById(R.id.canvasView);

        // ObjectController ↔ CanvasView 연결
        objectController.setOnChangeListener(elements -> {
            canvasView.setElements(elements);
            updateUndoRedoState();
            markDirty();
        });

        canvasView.setOnElementSelectedListener(element -> {
            updateToolbarForElement(element);
        });

        // 4, 5번: 이동/리사이즈/회전이 끝났을 때 최종 상태를 ObjectController에 반영
        canvasView.setOnElementTransformListener(element -> {
            PageElement updated = cloneElement(element);
            objectController.modifyElement(element.getElementId(), updated);
            // modifyElement가 canvasView.setElements를 다시 호출하므로
            // 선택 상태를 갱신된 요소로 다시 설정
            canvasView.setSelectedElement(objectController.findElement(element.getElementId()));
        });

        // 6번: 텍스트 요소를 더블탭하면 텍스트 입력 다이얼로그를 다시 표시
        canvasView.setOnElementDoubleTapListener(element ->
                showTextInputDialog(objectController.findElement(element.getElementId())));
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> handleBackPressed());
        btnSave.setOnClickListener(v -> handleSave(false));

        // 6번: 텍스트 삽입
        btnAddText.setOnClickListener(v -> insertTextElement());

        // 서식 버튼 (현재 선택된 텍스트 요소에 적용)
        btnBold.setOnClickListener(v -> applyTextFormat("bold"));
        btnItalic.setOnClickListener(v -> applyTextFormat("italic"));
        btnUnderline.setOnClickListener(v -> applyTextFormat("underline"));
        btnStrike.setOnClickListener(v -> applyTextFormat("strike"));
        btnAlignLeft.setOnClickListener(v -> applyTextAlign(0));
        btnAlignCenter.setOnClickListener(v -> applyTextAlign(1));
        btnAlignRight.setOnClickListener(v -> applyTextAlign(2));

        // 7번: 폰트 크기 선택
        btnFontSize.setOnClickListener(v -> showFontSizeMenu());

        // 이미지 삽입 (Design.md §4 handleInsertElement, §7 ImageLoader)
        btnImage.setOnClickListener(v -> handleInsertImage());

        // 도형 삽입
        btnShape.setOnClickListener(v -> showShapeMenu());

        // 배경 편집 (Analysis §Use case #7)
        btnBackground.setOnClickListener(v -> showBackgroundMenu());

        // Undo / Redo (Design.md §4 handleUndo/handleRedo)
        btnUndo.setOnClickListener(v -> {
            if (objectController.canUndo()) {
                objectController.undo();
                canvasView.setSelectedElement(null);
            } else {
                Toast.makeText(this, "실행 취소할 작업이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        btnRedo.setOnClickListener(v -> {
            if (objectController.canRedo()) {
                objectController.redo();
                canvasView.setSelectedElement(null);
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

                // 로드 직후에는 변경사항 없음 상태로 초기화
                isDirty = false;
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
    // 10번 수정사항: 저장이 실제로 완료된 후에만 화면을 종료한다 (exitAfterSave).
    // ────────────────────────────────────────────────

    private void handleSave(boolean exitAfterSave) {
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
                isDirty = false;
                if (exitAfterSave) {
                    finish();
                } else {
                    Toast.makeText(PageEditActivity.this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
                }
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

    private void markDirty() {
        isDirty = true;
    }

    // ────────────────────────────────────────────────
    // 요소 삽입 (Design.md §4 handleInsertElement)
    // ────────────────────────────────────────────────

    private void insertTextElement() {
        PageElement el = objectController.createElement(PageElement.ElementType.TEXT);
        el.setX(50f);
        el.setY(200f);
        el.setText(""); // 새 텍스트는 빈 값으로 시작, 입력 다이얼로그에서 채움
        objectController.addElement(el);
        canvasView.setSelectedElement(el);

        // 추가 즉시 텍스트 입력 다이얼로그 표시
        showTextInputDialog(el);
    }

    /**
     * 텍스트 요소의 내용을 입력/수정하는 다이얼로그 (6번 수정사항)
     * - 새 텍스트 요소 추가 시 자동으로 표시
     * - 캔버스에서 텍스트 요소를 더블탭하면 다시 표시되어 내용 수정 가능
     */
    private void showTextInputDialog(PageElement element) {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("내용을 입력하세요");
        input.setText(element.getText());
        input.setMinLines(3);
        input.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        input.setSelection(input.getText().length());

        int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

        new AlertDialog.Builder(this)
                .setTitle("텍스트 입력")
                .setView(input)
                .setPositiveButton("확인", (d, w) -> {
                    String text = input.getText().toString();
                    PageElement updated = cloneElement(element);
                    updated.setText(text);
                    objectController.modifyElement(element.getElementId(), updated);
                    PageElement refreshed = objectController.findElement(element.getElementId());
                    canvasView.setSelectedElement(refreshed);
                })
                .setNegativeButton("취소", (d, w) -> {
                    // 새로 추가했는데 빈 텍스트로 취소한 경우 요소 제거
                    if (element.getText() == null || element.getText().isEmpty()) {
                        objectController.removeElement(element.getElementId());
                        canvasView.setSelectedElement(null);
                    }
                })
                .show();

        // 다이얼로그가 뜨면 자동으로 키보드 포커스
        input.requestFocus();
    }

    private void handleInsertImage() {
        pickingBackgroundImage = false;
        requestImagePick();
    }

    private void requestImagePick() {
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

                // 4번: 이미지 실제 비율에 맞춰 초기 높이 조정 (비율 깨짐 방지)
                applyNaturalAspectRatio(el, optimizedPath);

                objectController.addElement(el);
                canvasView.setSelectedElement(el);
                canvasView.clearBitmapCache();
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

    /** 이미지 파일의 실제 가로/세로 비율에 맞춰 요소의 height를 재계산 (4번 수정사항) */
    private void applyNaturalAspectRatio(PageElement el, String path) {
        try {
            android.graphics.BitmapFactory.Options opts = new android.graphics.BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            android.graphics.BitmapFactory.decodeFile(path, opts);
            if (opts.outWidth > 0 && opts.outHeight > 0) {
                float ratio = (float) opts.outWidth / opts.outHeight;
                el.setHeight(el.getWidth() / ratio);
            }
        } catch (Exception ignored) {}
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

    // ────────────────────────────────────────────────
    // 배경 설정 (5번 수정사항: 이미지 크롭 지원)
    // ────────────────────────────────────────────────

    private void showBackgroundMenu() {
        String[] options = {"흰색 배경", "라벤더 배경", "연한 핑크 배경", "배경 이미지 선택"};
        String[] colors = {"#FFFFFF", "#E9D5FF", "#FFC1C1", null};

        new AlertDialog.Builder(this)
                .setTitle("배경 설정")
                .setItems(options, (d, which) -> {
                    if (which < 3) {
                        setBackgroundColor(colors[which]);
                    } else {
                        pickingBackgroundImage = true;
                        requestImagePick();
                    }
                })
                .show();
    }

    private void setBackgroundColor(String hex) {
        canvasView.setBackgroundColor(Color.parseColor(hex));
        canvasView.setBackgroundImage(null);
        if (currentPage != null) {
            currentPage.setBackgroundStyle("{\"type\":\"color\",\"value\":\"" + hex + "\"}");
            markDirty();
        }
    }

    /** 배경 이미지 선택 후 크롭 다이얼로그 표시 (5번 수정사항) */
    private void openCropDialog(Uri uri) {
        imageLoader.loadBitmapForCrop(uri, new ImageLoader.BitmapCallback() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                showCropDialog(bitmap);
            }
            @Override
            public void onError(String message) {
                new AlertDialog.Builder(PageEditActivity.this)
                        .setTitle("이미지 불러오기 실패")
                        .setMessage(message)
                        .setPositiveButton("확인", null)
                        .show();
            }
        });
    }

    private void showCropDialog(Bitmap sourceBitmap) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_crop_image, null);
        CropImageView cropImageView = dialogView.findViewById(R.id.cropImageView);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnApply = dialogView.findViewById(R.id.btnApply);

        // 캔버스와 동일한 가로:세로 비율로 크롭 영역 설정 (5번: 꽉 차게)
        float canvasW = canvasView.getWidth();
        float canvasH = canvasView.getHeight();
        if (canvasW > 0 && canvasH > 0) {
            cropImageView.setTargetAspectRatio(canvasW / canvasH);
        }
        cropImageView.setImageBitmap(sourceBitmap);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnApply.setOnClickListener(v -> {
            Bitmap cropped = cropImageView.getCroppedBitmap();
            if (cropped == null) {
                dialog.dismiss();
                return;
            }
            btnApply.setEnabled(false);
            imageLoader.saveCroppedBitmap(cropped, new ImageLoader.ImageCallback() {
                @Override
                public void onSuccess(String path) {
                    applyBackgroundImage(path);
                    dialog.dismiss();
                }
                @Override
                public void onError(String message) {
                    btnApply.setEnabled(true);
                    Toast.makeText(PageEditActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    /** 크롭된 배경 이미지를 캔버스에 적용 (5번 수정사항) */
    private void applyBackgroundImage(String path) {
        canvasView.setBackgroundImage(path);
        if (currentPage != null) {
            currentPage.setBackgroundStyle("{\"type\":\"image\",\"path\":\"" + path + "\"}");
            markDirty();
        }
    }

    private void applyBackgroundFromJson(String json) {
        if (json == null) return;
        try {
            if (json.contains("\"image\"")) {
                int startIdx = json.indexOf("\"path\":\"") + 8;
                int endIdx = json.indexOf("\"", startIdx);
                String path = json.substring(startIdx, endIdx);
                canvasView.setBackgroundImage(path);
            } else if (json.contains("\"color\"")) {
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
        canvasView.setSelectedElement(objectController.findElement(updated.getElementId()));
    }

    private void applyTextAlign(int align) {
        PageElement selected = canvasView.getSelectedElement();
        if (selected == null || selected.getType() != PageElement.ElementType.TEXT) {
            Toast.makeText(this, "텍스트 요소를 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        PageElement updated = cloneElement(selected);
        updated.setTextAlign(align);
        objectController.modifyElement(selected.getElementId(), updated);
        canvasView.setSelectedElement(objectController.findElement(updated.getElementId()));
    }

    /** 7번 수정사항: 폰트 크기 선택 메뉴 */
    private void showFontSizeMenu() {
        PageElement selected = canvasView.getSelectedElement();
        if (selected == null || selected.getType() != PageElement.ElementType.TEXT) {
            Toast.makeText(this, "텍스트 요소를 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("글자 크기")
                .setItems(FONT_SIZE_LABELS, (d, which) -> {
                    PageElement updated = cloneElement(selected);
                    updated.setTextSize(FONT_SIZE_VALUES[which]);
                    objectController.modifyElement(selected.getElementId(), updated);
                    canvasView.setSelectedElement(objectController.findElement(updated.getElementId()));
                    btnFontSize.setText(FONT_SIZE_LABELS[which]);
                })
                .show();
    }

    private void updateToolbarForElement(PageElement element) {
        boolean isText = element != null && element.getType() == PageElement.ElementType.TEXT;

        btnBold.setAlpha(isText && element.isBold() ? 1.0f : 0.4f);
        btnItalic.setAlpha(isText && element.isItalic() ? 1.0f : 0.4f);
        btnUnderline.setAlpha(isText && element.isUnderline() ? 1.0f : 0.4f);
        btnStrike.setAlpha(isText && element.isStrikethrough() ? 1.0f : 0.4f);

        if (isText) {
            btnFontSize.setText(labelForFontSize(element.getTextSize()));
            btnFontSize.setAlpha(1.0f);
        } else {
            btnFontSize.setText("보통");
            btnFontSize.setAlpha(0.6f);
        }
    }

    private String labelForFontSize(float size) {
        for (int i = 0; i < FONT_SIZE_VALUES.length; i++) {
            if (Math.abs(FONT_SIZE_VALUES[i] - size) < 0.5f) return FONT_SIZE_LABELS[i];
        }
        return "보통";
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

    /** 딥카피용 (Gson 직렬화/역직렬화) */
    private PageElement cloneElement(PageElement el) {
        return gson.fromJson(gson.toJson(el), PageElement.class);
    }

    // ────────────────────────────────────────────────
    // 뒤로가기 (10번 수정사항: 변경사항이 없으면 묻지 않고 바로 종료,
    // 저장 후 나가기는 저장이 완료된 뒤에만 finish 호출)
    // ────────────────────────────────────────────────

    private void handleBackPressed() {
        if (!isDirty) {
            finish();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("페이지 나가기")
                .setMessage("저장하지 않은 변경사항이 있습니다. 저장하고 나가시겠습니까?")
                .setPositiveButton("저장 후 나가기", (d, w) -> handleSave(true))
                .setNegativeButton("저장 안 함", (d, w) -> finish())
                .setNeutralButton("취소", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        handleBackPressed();
    }
}
