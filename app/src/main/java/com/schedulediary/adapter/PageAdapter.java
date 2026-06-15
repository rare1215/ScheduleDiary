package com.schedulediary.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.schedulediary.R;
import com.schedulediary.model.AdPage;
import com.schedulediary.model.PageElement;
import com.schedulediary.view.PagePreviewView;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 페이지 목록 어댑터
 * - VIEW_TYPE_LIST: 드래그 정렬 가능한 리스트 (8, 9번 수정사항)
 * - VIEW_TYPE_GRID: 페이지 내용 미리보기 그리드 (9번 수정사항)
 * - 다이어리 커버 색상에 맞춰 페이지 번호 뱃지 색상 동기화 (11번 수정사항)
 */
public class PageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_LIST = 0;
    public static final int VIEW_TYPE_GRID = 1;

    public interface OnPageClickListener {
        void onClick(AdPage page);
    }

    public interface OnPageLongClickListener {
        void onLongClick(AdPage page);
    }

    public interface OnDragHandleTouchListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    private List<AdPage> pages;
    private final OnPageClickListener clickListener;
    private final OnPageLongClickListener longClickListener;
    private OnDragHandleTouchListener dragHandleListener;
    private int viewType = VIEW_TYPE_LIST;

    /** 다이어리 커버 색상 (11번 수정사항: 페이지 번호 뱃지 등에 적용) */
    private int accentColor = Color.parseColor("#C9A8F5");

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA);
    private final Gson gson = new Gson();

    public PageAdapter(List<AdPage> pages,
                       OnPageClickListener cl,
                       OnPageLongClickListener ll) {
        this.pages = pages;
        this.clickListener = cl;
        this.longClickListener = ll;
    }

    public void setOnDragHandleTouchListener(OnDragHandleTouchListener l) {
        this.dragHandleListener = l;
    }

    public void updateData(List<AdPage> newData) {
        this.pages = newData;
        notifyDataSetChanged();
    }

    public List<AdPage> getCurrentData() {
        return pages;
    }

    /** 보기 모드 전환 (리스트 ↔ 그리드, 9번 수정사항) */
    public void setViewType(int viewType) {
        if (this.viewType != viewType) {
            this.viewType = viewType;
            notifyDataSetChanged();
        }
    }

    public int getViewType() {
        return viewType;
    }

    /** 다이어리 커버 색상 적용 (11번 수정사항) */
    public void setAccentColor(int color) {
        this.accentColor = color;
        notifyDataSetChanged();
    }

    /** 드래그 중 실시간 위치 변경 (ItemTouchHelper에서 호출) */
    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition < 0 || toPosition < 0
                || fromPosition >= pages.size() || toPosition >= pages.size()) return;
        Collections.swap(pages, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        if (type == VIEW_TYPE_GRID) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_page_grid, parent, false);
            return new GridViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_page, parent, false);
            return new ListViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AdPage page = pages.get(position);
        if (holder instanceof ListViewHolder) {
            ((ListViewHolder) holder).bind(page);
        } else if (holder instanceof GridViewHolder) {
            ((GridViewHolder) holder).bind(page);
        }
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    // ────────────────────────────────────────────────
    // 리스트뷰 (드래그 정렬, 8번)
    // ────────────────────────────────────────────────

    class ListViewHolder extends RecyclerView.ViewHolder {
        private final FrameLayout flPageNumber;
        private final TextView tvPageNumber;
        private final TextView tvTitle;
        private final TextView tvDate;
        private final TextView tvPreview;
        private final ImageView ivDragHandle;

        ListViewHolder(@NonNull View v) {
            super(v);
            flPageNumber = v.findViewById(R.id.flPageNumber);
            tvPageNumber = v.findViewById(R.id.tvPageNumber);
            tvTitle      = v.findViewById(R.id.tvTitle);
            tvDate       = v.findViewById(R.id.tvDate);
            tvPreview    = v.findViewById(R.id.tvPreview);
            ivDragHandle = v.findViewById(R.id.ivDragHandle);
        }

        void bind(AdPage page) {
            tvPageNumber.setText(String.valueOf(page.getPageNumber()));
            tvTitle.setText(page.getTitle());
            tvDate.setText(dateFormat.format(new Date(page.getCreatedAt())));

            String preview = page.getTextContent() != null ? page.getTextContent() : "";
            tvPreview.setText(preview.length() > 50 ? preview.substring(0, 50) + "…" : preview);

            // 11번: 다이어리 커버 색상으로 페이지 번호 뱃지 배경 동기화
            flPageNumber.getBackground().mutate().setTint(accentColor);

            itemView.setOnClickListener(v -> clickListener.onClick(page));
            itemView.setOnLongClickListener(v -> {
                longClickListener.onLongClick(page);
                return true;
            });

            // 드래그 핸들 터치 시작 -> ItemTouchHelper.startDrag (8번)
            ivDragHandle.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN && dragHandleListener != null) {
                    dragHandleListener.onStartDrag(this);
                }
                return false;
            });
        }
    }

    // ────────────────────────────────────────────────
    // 그리드뷰 (미리보기, 9번)
    // ────────────────────────────────────────────────

    class GridViewHolder extends RecyclerView.ViewHolder {
        private final PagePreviewView previewView;
        private final FrameLayout flPageNumber;
        private final TextView tvPageNumber;
        private final TextView tvTitle;

        GridViewHolder(@NonNull View v) {
            super(v);
            previewView = v.findViewById(R.id.previewView);
            flPageNumber = v.findViewById(R.id.flPageNumber);
            tvPageNumber = v.findViewById(R.id.tvPageNumber);
            tvTitle = v.findViewById(R.id.tvTitle);
        }

        void bind(AdPage page) {
            tvPageNumber.setText(String.valueOf(page.getPageNumber()));
            tvTitle.setText(page.getTitle());
            flPageNumber.getBackground().mutate().setTint(accentColor);

            // 배경 적용
            applyBackground(page.getBackgroundStyle());

            // 요소 미리보기
            List<PageElement> elements = parseElements(page.getElementsJson());
            previewView.setPreviewElements(elements);

            itemView.setOnClickListener(v -> clickListener.onClick(page));
            itemView.setOnLongClickListener(v -> {
                longClickListener.onLongClick(page);
                return true;
            });
        }

        private void applyBackground(String json) {
            if (json == null) {
                previewView.setPreviewBackgroundColor(Color.WHITE);
                return;
            }
            try {
                if (json.contains("\"image\"")) {
                    int startIdx = json.indexOf("\"path\":\"") + 8;
                    int endIdx = json.indexOf("\"", startIdx);
                    String path = json.substring(startIdx, endIdx);
                    previewView.setPreviewBackgroundImage(path);
                } else if (json.contains("\"color\"")) {
                    int startIdx = json.indexOf("\"value\":\"") + 9;
                    int endIdx = json.indexOf("\"", startIdx);
                    String hex = json.substring(startIdx, endIdx);
                    previewView.setPreviewBackgroundColor(Color.parseColor(hex));
                } else {
                    previewView.setPreviewBackgroundColor(Color.WHITE);
                }
            } catch (Exception e) {
                previewView.setPreviewBackgroundColor(Color.WHITE);
            }
        }

        private List<PageElement> parseElements(String json) {
            if (json == null || json.isEmpty() || json.equals("[]")) return new ArrayList<>();
            try {
                Type type = new TypeToken<List<PageElement>>() {}.getType();
                return gson.fromJson(json, type);
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }
    }
}
