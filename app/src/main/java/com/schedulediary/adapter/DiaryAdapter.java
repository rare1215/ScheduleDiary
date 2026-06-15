package com.schedulediary.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.schedulediary.R;
import com.schedulediary.model.AdDiary;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 다이어리 목록 어댑터
 * - VIEW_TYPE_LIST: 작은 썸네일 + 드래그 정렬 (8, 9번 수정사항)
 * - VIEW_TYPE_GRID: 큰 커버 미리보기 카드 (기존 디자인, 9번 수정사항)
 */
public class DiaryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_LIST = 0;
    public static final int VIEW_TYPE_GRID = 1;

    public interface OnDiaryClickListener {
        void onClick(AdDiary diary);
    }

    public interface OnDiaryLongClickListener {
        void onLongClick(AdDiary diary);
    }

    public interface OnDragHandleTouchListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    private List<AdDiary> diaries;
    private final OnDiaryClickListener clickListener;
    private final OnDiaryLongClickListener longClickListener;
    private OnDragHandleTouchListener dragHandleListener;
    private int viewType = VIEW_TYPE_GRID;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA);

    public DiaryAdapter(List<AdDiary> diaries,
                        OnDiaryClickListener cl,
                        OnDiaryLongClickListener ll) {
        this.diaries = diaries;
        this.clickListener = cl;
        this.longClickListener = ll;
    }

    public void setOnDragHandleTouchListener(OnDragHandleTouchListener l) {
        this.dragHandleListener = l;
    }

    public void updateData(List<AdDiary> newData) {
        this.diaries = newData;
        notifyDataSetChanged();
    }

    public List<AdDiary> getCurrentData() {
        return diaries;
    }

    /** 보기 모드 전환 (9번 수정사항) */
    public void setViewType(int viewType) {
        if (this.viewType != viewType) {
            this.viewType = viewType;
            notifyDataSetChanged();
        }
    }

    public int getViewType() {
        return viewType;
    }

    /** 드래그 중 실시간 위치 변경 */
    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition < 0 || toPosition < 0
                || fromPosition >= diaries.size() || toPosition >= diaries.size()) return;
        Collections.swap(diaries, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        if (type == VIEW_TYPE_LIST) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_diary_list, parent, false);
            return new ListViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_diary, parent, false);
            return new GridViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AdDiary diary = diaries.get(position);
        if (holder instanceof ListViewHolder) {
            ((ListViewHolder) holder).bind(diary);
        } else if (holder instanceof GridViewHolder) {
            ((GridViewHolder) holder).bind(diary);
        }
    }

    @Override
    public int getItemCount() {
        return diaries.size();
    }

    /** 커버 이미지 또는 색상을 ImageView에 적용 (1번 수정사항: 커버 미리보기) */
    private void applyCover(ImageView ivCover, AdDiary diary) {
        // 이전 바인딩에서 남은 배경/필터/이미지 초기화
        ivCover.setBackground(null);
        ivCover.setColorFilter(null);
        ivCover.setImageDrawable(null);

        if (diary.getCoverImagePath() != null && !diary.getCoverImagePath().isEmpty()) {
            Glide.with(ivCover.getContext())
                    .load(diary.getCoverImagePath())
                    .centerCrop()
                    .placeholder(R.drawable.ic_diary)
                    .into(ivCover);
        } else {
            String color = diary.getCoverColor() != null ? diary.getCoverColor() : "#E9D5FF";
            GradientDrawable bg = new GradientDrawable();
            try {
                bg.setColor(Color.parseColor(color));
            } catch (Exception e) {
                bg.setColor(Color.parseColor("#E9D5FF"));
            }
            bg.setCornerRadius(16f);
            ivCover.setBackground(bg);
            ivCover.setImageResource(R.drawable.ic_diary);
            ivCover.setColorFilter(Color.argb(80, 255, 255, 255));
        }
    }

    // ────────────────────────────────────────────────
    // 그리드뷰 (큰 커버 카드, 기본 모드)
    // ────────────────────────────────────────────────

    class GridViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCover;
        private final TextView tvName;
        private final TextView tvDate;
        private final TextView tvDesc;

        GridViewHolder(@NonNull View v) {
            super(v);
            ivCover  = v.findViewById(R.id.ivCover);
            tvName   = v.findViewById(R.id.tvDiaryName);
            tvDate   = v.findViewById(R.id.tvDate);
            tvDesc   = v.findViewById(R.id.tvDescription);
        }

        void bind(AdDiary diary) {
            tvName.setText(diary.getDiaryName());
            tvDate.setText(dateFormat.format(new Date(diary.getCreatedAt())));
            tvDesc.setText(diary.getDescription() != null ? diary.getDescription() : "");

            applyCover(ivCover, diary);

            itemView.setOnClickListener(v -> clickListener.onClick(diary));
            itemView.setOnLongClickListener(v -> {
                longClickListener.onLongClick(diary);
                return true;
            });
        }
    }

    // ────────────────────────────────────────────────
    // 리스트뷰 (작은 썸네일 + 드래그 핸들, 8번 수정사항)
    // ────────────────────────────────────────────────

    class ListViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCover;
        private final TextView tvName;
        private final TextView tvDate;
        private final TextView tvDesc;
        private final ImageView ivDragHandle;

        ListViewHolder(@NonNull View v) {
            super(v);
            ivCover      = v.findViewById(R.id.ivCover);
            tvName       = v.findViewById(R.id.tvDiaryName);
            tvDate       = v.findViewById(R.id.tvDate);
            tvDesc       = v.findViewById(R.id.tvDescription);
            ivDragHandle = v.findViewById(R.id.ivDragHandle);
        }

        void bind(AdDiary diary) {
            tvName.setText(diary.getDiaryName());
            tvDate.setText(dateFormat.format(new Date(diary.getCreatedAt())));
            tvDesc.setText(diary.getDescription() != null ? diary.getDescription() : "");

            applyCover(ivCover, diary);

            itemView.setOnClickListener(v -> clickListener.onClick(diary));
            itemView.setOnLongClickListener(v -> {
                longClickListener.onLongClick(diary);
                return true;
            });

            ivDragHandle.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN && dragHandleListener != null) {
                    dragHandleListener.onStartDrag(this);
                }
                return false;
            });
        }
    }
}
