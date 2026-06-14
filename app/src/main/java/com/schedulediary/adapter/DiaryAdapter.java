package com.schedulediary.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.schedulediary.R;
import com.schedulediary.model.AdDiary;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder> {

    public interface OnDiaryClickListener {
        void onClick(AdDiary diary);
    }

    public interface OnDiaryLongClickListener {
        void onLongClick(AdDiary diary);
    }

    private List<AdDiary> diaries;
    private final OnDiaryClickListener clickListener;
    private final OnDiaryLongClickListener longClickListener;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA);

    public DiaryAdapter(List<AdDiary> diaries,
                        OnDiaryClickListener cl,
                        OnDiaryLongClickListener ll) {
        this.diaries = diaries;
        this.clickListener = cl;
        this.longClickListener = ll;
    }

    public void updateData(List<AdDiary> newData) {
        this.diaries = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_diary, parent, false);
        return new DiaryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryViewHolder holder, int position) {
        AdDiary diary = diaries.get(position);
        holder.bind(diary);
    }

    @Override
    public int getItemCount() {
        return diaries.size();
    }

    class DiaryViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final ImageView ivCover;
        private final TextView tvName;
        private final TextView tvDate;
        private final TextView tvDesc;

        DiaryViewHolder(@NonNull View v) {
            super(v);
            cardView = v.findViewById(R.id.cardDiary);
            ivCover  = v.findViewById(R.id.ivCover);
            tvName   = v.findViewById(R.id.tvDiaryName);
            tvDate   = v.findViewById(R.id.tvDate);
            tvDesc   = v.findViewById(R.id.tvDescription);
        }

        void bind(AdDiary diary) {
            tvName.setText(diary.getDiaryName());
            tvDate.setText(dateFormat.format(new Date(diary.getCreatedAt())));
            tvDesc.setText(diary.getDescription() != null ? diary.getDescription() : "");

            // 커버 이미지 또는 색상
            if (diary.getCoverImagePath() != null && !diary.getCoverImagePath().isEmpty()) {
                Glide.with(ivCover.getContext())
                        .load(diary.getCoverImagePath())
                        .centerCrop()
                        .into(ivCover);
            } else {
                String color = diary.getCoverColor() != null ? diary.getCoverColor() : "#E9D5FF";
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(Color.parseColor(color));
                bg.setCornerRadius(16f);
                ivCover.setBackground(bg);
                ivCover.setImageResource(R.drawable.ic_diary);
                ivCover.setColorFilter(Color.argb(80, 255, 255, 255));
            }

            itemView.setOnClickListener(v -> clickListener.onClick(diary));
            itemView.setOnLongClickListener(v -> {
                longClickListener.onLongClick(diary);
                return true;
            });
        }
    }
}
