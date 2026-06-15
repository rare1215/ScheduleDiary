package com.schedulediary.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.schedulediary.R;
import com.schedulediary.controller.DataController;
import com.schedulediary.model.AdDiary;

import java.util.ArrayList;
import java.util.List;

/**
 * 다이어리 생성/편집 다이얼로그 (스크린샷 4번 - 커버 사진, 이름, 설명, 커버 색상)
 * - 1번: 커버 이미지 선택 시 미리보기 적용 (틴트 제거, 영구 권한 부여)
 * - 2번: 커버 색상 HEX 코드 직접 입력 + 사용자 프리셋 저장/불러오기
 */
public class DiaryEditDialogFragment extends DialogFragment {

    public interface OnDiarySaveListener {
        void onSave(AdDiary diary, boolean isNew);
    }

    private static final String[] DEFAULT_PRESET_COLORS = {
            "#E9D5FF", "#FFC1C1", "#FFD8A8", "#A8D8EA", "#B5EAD7",
            "#FFFACD", "#C1C1FF", "#FFB3BA", "#FFFFFF", "#F0F0F0"
    };

    private ImageView ivCoverPreview;
    private EditText etDiaryName;
    private EditText etDescription;
    private TextInputEditText etColorHex;
    private View vColorPreview;
    private LinearLayout llColorPicker;
    private MaterialButton btnAddColorPreset;
    private Button btnCancel;
    private Button btnSave;
    private ImageButton btnClose;

    private OnDiarySaveListener listener;
    private AdDiary existingDiary;
    private int ownerId;
    private String selectedColor = "#E9D5FF";
    private String selectedCoverImagePath = null;
    private DataController dataController;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    handleCoverImageSelected(uri);
                }
            });

    public static DiaryEditDialogFragment newInstance(
            @Nullable AdDiary diary, int ownerId, OnDiarySaveListener listener) {
        DiaryEditDialogFragment f = new DiaryEditDialogFragment();
        f.existingDiary = diary;
        f.ownerId = ownerId;
        f.listener = listener;
        return f;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_diary_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dataController = DataController.getInstance(requireContext());

        initViews(view);
        populateIfEditing();
        buildColorPicker();
        updateColorPreview();
        setListeners();
    }

    private void initViews(View v) {
        ivCoverPreview = v.findViewById(R.id.ivCoverPreview);
        etDiaryName = v.findViewById(R.id.etDiaryName);
        etDescription = v.findViewById(R.id.etDescription);
        etColorHex = v.findViewById(R.id.etColorHex);
        vColorPreview = v.findViewById(R.id.vColorPreview);
        llColorPicker = v.findViewById(R.id.llColorPicker);
        btnAddColorPreset = v.findViewById(R.id.btnAddColorPreset);
        btnCancel = v.findViewById(R.id.btnCancel);
        btnSave = v.findViewById(R.id.btnSave);
        btnClose = v.findViewById(R.id.btnClose);
    }

    /**
     * 컬러 원 목록 생성 (기본 프리셋 + 사용자 정의 프리셋, 2번 수정사항)
     */
    private void buildColorPicker() {
        llColorPicker.removeAllViews();
        int sizePx = (int) (48 * getResources().getDisplayMetrics().density);
        int marginPx = (int) (6 * getResources().getDisplayMetrics().density);

        // 기본 프리셋
        for (String hex : DEFAULT_PRESET_COLORS) {
            llColorPicker.addView(createColorCircle(hex, sizePx, marginPx));
        }

        // 사용자 정의 프리셋 (구분선 추가 후)
        List<String> customPresets = dataController.getCustomColorPresets();
        if (!customPresets.isEmpty()) {
            View divider = new View(requireContext());
            LinearLayout.LayoutParams dividerLp = new LinearLayout.LayoutParams(
                    (int) (1 * getResources().getDisplayMetrics().density),
                    sizePx);
            dividerLp.setMargins(marginPx, 0, marginPx, 0);
            divider.setLayoutParams(dividerLp);
            divider.setBackgroundColor(Color.parseColor("#E0E0E0"));
            llColorPicker.addView(divider);

            for (String hex : customPresets) {
                llColorPicker.addView(createColorCircle(hex, sizePx, marginPx));
            }
        }
    }

    private View createColorCircle(String hex, int sizePx, int marginPx) {
        ImageButton btn = new ImageButton(requireContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sizePx, sizePx);
        lp.setMargins(marginPx, 0, marginPx, 0);
        btn.setLayoutParams(lp);

        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        try {
            circle.setColor(Color.parseColor(hex));
        } catch (Exception e) {
            circle.setColor(Color.parseColor("#E9D5FF"));
        }
        if (hex.equalsIgnoreCase(selectedColor)) {
            circle.setStroke((int) (3 * getResources().getDisplayMetrics().density), Color.parseColor("#B794F4"));
        }
        btn.setBackground(circle);
        btn.setOnClickListener(v -> {
            selectedColor = hex;
            etColorHex.setText(hex);
            buildColorPicker(); // 선택 표시 갱신
            updateColorPreview();
            updateCoverPreviewColor();
        });
        return btn;
    }

    /** 색상 미리보기 원과 HEX 입력란 동기화 */
    private void updateColorPreview() {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        try {
            bg.setColor(Color.parseColor(selectedColor));
        } catch (Exception e) {
            bg.setColor(Color.parseColor("#E9D5FF"));
        }
        bg.setStroke((int) (2 * getResources().getDisplayMetrics().density), Color.parseColor("#D8B4FE"));
        vColorPreview.setBackground(bg);

        if (!selectedColor.equalsIgnoreCase(etColorHex.getText().toString())) {
            etColorHex.setText(selectedColor);
        }
    }

    private void updateCoverPreviewColor() {
        if (selectedCoverImagePath == null) {
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Color.parseColor(selectedColor));
            bg.setCornerRadius(16f);
            ivCoverPreview.setBackground(bg);
            ivCoverPreview.setImageDrawable(null);
        }
    }

    private void populateIfEditing() {
        if (existingDiary != null) {
            etDiaryName.setText(existingDiary.getDiaryName());
            etDescription.setText(existingDiary.getDescription());
            if (existingDiary.getCoverColor() != null) {
                selectedColor = existingDiary.getCoverColor();
            }
            selectedCoverImagePath = existingDiary.getCoverImagePath();

            if (selectedCoverImagePath != null && !selectedCoverImagePath.isEmpty()) {
                applyCoverPreview(Uri.parse(selectedCoverImagePath));
            }
        }
        updateCoverPreviewColor();
    }

    private void setListeners() {
        ivCoverPreview.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*"));

        btnSave.setOnClickListener(v -> clickSave());
        btnCancel.setOnClickListener(v -> dismiss());
        btnClose.setOnClickListener(v -> dismiss());

        // HEX 코드 직접 입력 시 미리보기/선택 반영 (2번 수정사항)
        etColorHex.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) applyHexInput();
        });
        etColorHex.setOnEditorActionListener((v, actionId, event) -> {
            applyHexInput();
            return false;
        });

        // 프리셋에 추가 (2번 수정사항)
        btnAddColorPreset.setOnClickListener(v -> {
            applyHexInput();
            if (isValidHexColor(selectedColor)) {
                dataController.addCustomColorPreset(selectedColor);
                buildColorPicker();
                Toast.makeText(requireContext(), "프리셋에 추가되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "올바른 색상 코드(#RRGGBB)를 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** HEX 입력란의 값을 검증하여 selectedColor에 반영 */
    private void applyHexInput() {
        String input = etColorHex.getText().toString().trim();
        if (input.isEmpty()) return;
        if (!input.startsWith("#")) input = "#" + input;

        if (isValidHexColor(input)) {
            selectedColor = input.toUpperCase();
            buildColorPicker();
            updateColorPreview();
            updateCoverPreviewColor();
        } else {
            Toast.makeText(requireContext(), "올바른 색상 코드(#RRGGBB)를 입력해주세요.", Toast.LENGTH_SHORT).show();
            etColorHex.setText(selectedColor);
        }
    }

    private boolean isValidHexColor(String hex) {
        if (hex == null) return false;
        try {
            Color.parseColor(hex);
            return hex.matches("^#([A-Fa-f0-9]{6})$");
        } catch (Exception e) {
            return false;
        }
    }

    /** 커버 이미지 선택 처리 - 영구 읽기 권한 부여 + 미리보기 적용 (1번 수정사항) */
    private void handleCoverImageSelected(Uri uri) {
        try {
            requireContext().getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
            // 일부 Provider는 persistable 권한을 지원하지 않음 - 무시하고 계속 진행
        }
        selectedCoverImagePath = uri.toString();
        applyCoverPreview(uri);
    }

    /** 커버 이미지 미리보기 적용 - 이전 배경/틴트 제거 후 표시 (1번 수정사항) */
    private void applyCoverPreview(Uri uri) {
        ivCoverPreview.setBackground(null);
        ivCoverPreview.clearColorFilter();
        ivCoverPreview.setImageTintList(null);
        ivCoverPreview.setImageURI(uri);
    }

    private void clickSave() {
        String name = etDiaryName.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        applyHexInput();

        if (TextUtils.isEmpty(name)) {
            etDiaryName.setError("다이어리 이름을 입력해주세요.");
            etDiaryName.requestFocus();
            return;
        }

        boolean isNew = (existingDiary == null);
        AdDiary diary = isNew
                ? new AdDiary(name, desc, ownerId, selectedColor)
                : existingDiary;

        if (!isNew) {
            diary.setDiaryName(name);
            diary.setDescription(desc);
            diary.setCoverColor(selectedColor);
        }
        diary.setCoverImagePath(selectedCoverImagePath);

        if (listener != null) {
            listener.onSave(diary, isNew);
        }
        dismiss();
    }
}
