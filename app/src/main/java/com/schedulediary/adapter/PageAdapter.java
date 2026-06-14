package com.schedulediary.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.schedulediary.R;
import com.schedulediary.model.AdPage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PageAdapter extends RecyclerView.Adapter<PageAdapter.PageViewHolder> {

    public interface OnPageClickListener {
        void onClick(AdPage page);
    }

    public interface OnPageLongClickListener {
        void onLongClick(AdPage page);
    }

    private List<AdPage> pages;
    private final OnPageClickListener clickListener;
    private final OnPageLongClickListener longClickListener;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA);

    public PageAdapter(List<AdPage> pages,
                       OnPageClickListener cl,
                       OnPageLongClickListener ll) {
        this.pages = pages;
        this.clickListener = cl;
        this.longClickListener = ll;
    }

    public void updateData(List<AdPage> newData) {
        this.pages = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_page, parent, false);
        return new PageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        holder.bind(pages.get(position));
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    class PageViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView tvPageNumber;
        private final TextView tvTitle;
        private final TextView tvDate;
        private final TextView tvPreview;

        PageViewHolder(@NonNull View v) {
            super(v);
            cardView     = v.findViewById(R.id.cardPage);
            tvPageNumber = v.findViewById(R.id.tvPageNumber);
            tvTitle      = v.findViewById(R.id.tvTitle);
            tvDate       = v.findViewById(R.id.tvDate);
            tvPreview    = v.findViewById(R.id.tvPreview);
        }

        void bind(AdPage page) {
            tvPageNumber.setText(String.valueOf(page.getPageNumber()));
            tvTitle.setText(page.getTitle());
            tvDate.setText(dateFormat.format(new Date(page.getCreatedAt())));
            // 텍스트 미리보기
            String preview = page.getTextContent() != null ? page.getTextContent() : "";
            tvPreview.setText(preview.length() > 50 ? preview.substring(0, 50) + "…" : preview);

            itemView.setOnClickListener(v -> clickListener.onClick(page));
            itemView.setOnLongClickListener(v -> {
                longClickListener.onLongClick(page);
                return true;
            });
        }
    }
}
