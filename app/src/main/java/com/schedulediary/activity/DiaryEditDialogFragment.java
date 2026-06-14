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

import com.schedulediary.R;
import com.schedulediary.model.AdDiary;

/**
 * 다이어리 생성/편집 다이얼로그 (스크린샷 4번 - 커버 사진, 이름, 설명, 커버 색상)
 */
public class DiaryEditDialogFragment extends DialogFragment {

    public interface OnDiarySaveListener {
        void onSave(AdDiary diary, boolean isNew);
    }

    private static final String ARG_DIARY_ID = "diaryId";
    private static final String ARG_OWNER_ID = "ownerId";

    private static final String[] PRESET_COLORS = {
            "#E9D5FF", "#FFC1C1", "#FFD8A8", "#A8D8EA", "#B5EAD7",
            "#FFFACD", "#C1C1FF", "#FFB3BA", "#FFFFFF", "#F0F0F0"
    };

    private ImageView ivCoverPreview;
    private EditText etDiaryName;
    private EditText etDescription;
    private LinearLayout llColorPicker;
    private Button btnCancel;
    private Button btnSave;
    private ImageButton btnClose;

    private OnDiarySaveListener listener;
    private AdDiary existingDiary;
    private int ownerId;
    private String selectedColor = "#E9D5FF";
    private String selectedCoverImagePath = null;

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

        initViews(view);
        buildColorPicker();
        populateIfEditing();
        setListeners();
    }

    private void initViews(View v) {
        ivCoverPreview = v.findViewById(R.id.ivCoverPreview);
        etDiaryName = v.findViewById(R.id.etDiaryName);
        etDescription = v.findViewById(R.id.etDescription);
        llColorPicker = v.findViewById(R.id.llColorPicker);
        btnCancel = v.findViewById(R.id.btnCancel);
        btnSave = v.findViewById(R.id.btnSave);
        btnClose = v.findViewById(R.id.btnClose);
    }

    /** 스크린샷 4번처럼 컬러 원 목록 생성 */
    private void buildColorPicker() {
        llColorPicker.removeAllViews();
        int sizePx = (int) (48 * getResources().getDisplayMetrics().density);
        int marginPx = (int) (6 * getResources().getDisplayMetrics().density);

        for (String hex : PRESET_COLORS) {
            ImageButton btn = new ImageButton(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sizePx, sizePx);
            lp.setMargins(marginPx, 0, marginPx, 0);
            btn.setLayoutParams(lp);

            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(Color.parseColor(hex));
            if (hex.equals(selectedColor)) {
                circle.setStroke(4, Color.parseColor("#9C27B0"));
            }
            btn.setBackground(circle);
            btn.setOnClickListener(v -> {
                selectedColor = hex;
                buildColorPicker(); // 선택 표시 갱신
                updateCoverPreviewColor();
            });
            llColorPicker.addView(btn);
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
            buildColorPicker();
        }
        updateCoverPreviewColor();
    }

    private void setListeners() {
        ivCoverPreview.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*"));

        btnSave.setOnClickListener(v -> clickSave());
        btnCancel.setOnClickListener(v -> dismiss());
        btnClose.setOnClickListener(v -> dismiss());
    }

    private void handleCoverImageSelected(Uri uri) {
        selectedCoverImagePath = uri.toString();
        ivCoverPreview.setImageURI(uri);
    }

    private void clickSave() {
        String name = etDiaryName.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();

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
