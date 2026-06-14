package com.schedulediary.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.schedulediary.model.PageElement;

import java.util.ArrayList;
import java.util.List;

/**
 * 페이지 편집 캔버스 뷰
 * - 요소 렌더링 (텍스트, 이미지, 도형)
 * - 터치로 요소 선택 / 드래그 이동
 * - 선택된 요소에 핸들 표시
 */
public class PageCanvasView extends View {

    // ── 콜백 인터페이스 ──
    public interface OnElementSelectedListener {
        void onElementSelected(PageElement element);
    }

    public interface OnElementMovedListener {
        void onElementMoved(PageElement element, float dx, float dy);
    }

    // ── 상태 ──
    private List<PageElement> elements = new ArrayList<>();
    private PageElement selectedElement = null;

    private float lastTouchX, lastTouchY;
    private boolean isDragging = false;

    private OnElementSelectedListener selectionListener;
    private OnElementMovedListener moveListener;

    // ── 페인트 ──
    private final Paint shapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final float HANDLE_RADIUS = 12f;
    private static final float MIN_DRAG_THRESHOLD = 5f;

    public PageCanvasView(Context context) {
        super(context);
        init();
    }

    public PageCanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackgroundColor(Color.WHITE);

        selectionPaint.setStyle(Paint.Style.STROKE);
        selectionPaint.setColor(Color.parseColor("#9C27B0"));
        selectionPaint.setStrokeWidth(3f);
        selectionPaint.setPathEffect(new DashPathEffect(new float[]{10f, 5f}, 0));

        handlePaint.setStyle(Paint.Style.FILL);
        handlePaint.setColor(Color.parseColor("#9C27B0"));
    }

    // ────────────────────────────────────────────────
    // 공개 API
    // ────────────────────────────────────────────────

    public void setElements(List<PageElement> elements) {
        this.elements = new ArrayList<>(elements);
        invalidate();
    }

    public void setSelectedElement(PageElement element) {
        this.selectedElement = element;
        invalidate();
        if (selectionListener != null) selectionListener.onElementSelected(element);
    }

    public PageElement getSelectedElement() {
        return selectedElement;
    }

    public void setOnElementSelectedListener(OnElementSelectedListener l) {
        this.selectionListener = l;
    }

    public void setOnElementMovedListener(OnElementMovedListener l) {
        this.moveListener = l;
    }

    // ────────────────────────────────────────────────
    // 그리기
    // ────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // z-order 순서대로 렌더링
        for (PageElement el : elements) {
            canvas.save();
            // 회전 적용
            canvas.rotate(el.getRotation(), el.getX() + el.getWidth() / 2f,
                    el.getY() + el.getHeight() / 2f);

            switch (el.getType()) {
                case TEXT:   drawText(canvas, el);   break;
                case IMAGE:  drawImage(canvas, el);  break;
                case SHAPE:  drawShape(canvas, el);  break;
            }

            // 선택된 요소 하이라이트
            if (selectedElement != null &&
                    el.getElementId().equals(selectedElement.getElementId())) {
                drawSelectionHandle(canvas, el);
            }
            canvas.restore();
        }
    }

    private void drawText(Canvas canvas, PageElement el) {
        textPaint.setTextSize(el.getTextSize() * getResources().getDisplayMetrics().scaledDensity);
        textPaint.setColor(el.getTextColor());

        int style = Typeface.NORMAL;
        if (el.isBold() && el.isItalic()) style = Typeface.BOLD_ITALIC;
        else if (el.isBold()) style = Typeface.BOLD;
        else if (el.isItalic()) style = Typeface.ITALIC;
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, style));
        textPaint.setUnderlineText(el.isUnderline());
        textPaint.setStrikeThruText(el.isStrikethrough());

        switch (el.getTextAlign()) {
            case 1: textPaint.setTextAlign(Paint.Align.CENTER); break;
            case 2: textPaint.setTextAlign(Paint.Align.RIGHT);  break;
            default: textPaint.setTextAlign(Paint.Align.LEFT);
        }

        // 텍스트 박스 배경 (연한 라벤더, 선택 시만)
        if (selectedElement != null && el.getElementId().equals(selectedElement.getElementId())) {
            shapePaint.setStyle(Paint.Style.FILL);
            shapePaint.setColor(Color.parseColor("#F3E8FF"));
            canvas.drawRoundRect(el.getX(), el.getY(),
                    el.getX() + el.getWidth(), el.getY() + el.getHeight(),
                    8f, 8f, shapePaint);
        }

        // 텍스트 렌더링 (줄바꿈 지원)
        String text = el.getText() != null ? el.getText() : "";
        String[] lines = text.split("\n");
        float lineHeight = textPaint.getFontSpacing();
        float startY = el.getY() + lineHeight;
        float x = el.getTextAlign() == 1 ? el.getX() + el.getWidth() / 2f
                : (el.getTextAlign() == 2 ? el.getX() + el.getWidth() : el.getX() + 8f);

        for (String line : lines) {
            canvas.drawText(line, x, startY, textPaint);
            startY += lineHeight;
            if (startY > el.getY() + el.getHeight()) break;
        }
    }

    private void drawImage(Canvas canvas, PageElement el) {
        if (el.getImagePath() == null) {
            // placeholder
            shapePaint.setStyle(Paint.Style.FILL);
            shapePaint.setColor(Color.parseColor("#E9D5FF"));
            canvas.drawRect(el.getX(), el.getY(),
                    el.getX() + el.getWidth(), el.getY() + el.getHeight(), shapePaint);
            return;
        }

        Bitmap bmp = BitmapFactory.decodeFile(el.getImagePath());
        if (bmp != null) {
            RectF dst = new RectF(el.getX(), el.getY(),
                    el.getX() + el.getWidth(), el.getY() + el.getHeight());
            canvas.drawBitmap(bmp, null, dst, null);
        }
    }

    private void drawShape(Canvas canvas, PageElement el) {
        shapePaint.setStyle(Paint.Style.FILL);
        shapePaint.setColor(el.getFillColor());

        Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setColor(el.getStrokeColor());
        stroke.setStrokeWidth(el.getStrokeWidth());

        float l = el.getX(), t = el.getY();
        float r = l + el.getWidth(), b = t + el.getHeight();

        if (el.getShapeType() == null) el.setShapeType(PageElement.ShapeType.RECTANGLE);

        switch (el.getShapeType()) {
            case RECTANGLE:
                canvas.drawRoundRect(l, t, r, b, 8f, 8f, shapePaint);
                canvas.drawRoundRect(l, t, r, b, 8f, 8f, stroke);
                break;
            case CIRCLE:
                float cx = (l + r) / 2f, cy = (t + b) / 2f;
                float rad = Math.min(el.getWidth(), el.getHeight()) / 2f;
                canvas.drawCircle(cx, cy, rad, shapePaint);
                canvas.drawCircle(cx, cy, rad, stroke);
                break;
            case TRIANGLE:
                Path tri = new Path();
                tri.moveTo((l + r) / 2f, t);
                tri.lineTo(r, b);
                tri.lineTo(l, b);
                tri.close();
                canvas.drawPath(tri, shapePaint);
                canvas.drawPath(tri, stroke);
                break;
            case LINE:
                stroke.setStrokeWidth(el.getStrokeWidth() * 2);
                canvas.drawLine(l, (t + b) / 2f, r, (t + b) / 2f, stroke);
                break;
        }
    }

    private void drawSelectionHandle(Canvas canvas, PageElement el) {
        float l = el.getX(), t = el.getY();
        float r = l + el.getWidth(), b = t + el.getHeight();

        // 점선 테두리
        canvas.drawRect(l - 4, t - 4, r + 4, b + 4, selectionPaint);

        // 모서리 핸들
        canvas.drawCircle(l, t, HANDLE_RADIUS, handlePaint);
        canvas.drawCircle(r, t, HANDLE_RADIUS, handlePaint);
        canvas.drawCircle(l, b, HANDLE_RADIUS, handlePaint);
        canvas.drawCircle(r, b, HANDLE_RADIUS, handlePaint);
    }

    // ────────────────────────────────────────────────
    // 터치 핸들링 (선택 + 드래그)
    // ────────────────────────────────────────────────

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = x;
                lastTouchY = y;
                isDragging = false;

                // z-order 역순으로 탐색 (위 레이어 우선)
                PageElement touched = null;
                for (int i = elements.size() - 1; i >= 0; i--) {
                    PageElement el = elements.get(i);
                    if (x >= el.getX() && x <= el.getX() + el.getWidth()
                            && y >= el.getY() && y <= el.getY() + el.getHeight()) {
                        touched = el;
                        break;
                    }
                }
                setSelectedElement(touched);
                return true;

            case MotionEvent.ACTION_MOVE:
                if (selectedElement != null) {
                    float dx = x - lastTouchX;
                    float dy = y - lastTouchY;

                    if (!isDragging && (Math.abs(dx) > MIN_DRAG_THRESHOLD
                            || Math.abs(dy) > MIN_DRAG_THRESHOLD)) {
                        isDragging = true;
                    }

                    if (isDragging) {
                        // 뷰 안에서만 이동
                        float newX = selectedElement.getX() + dx;
                        float newY = selectedElement.getY() + dy;
                        newX = Math.max(0, Math.min(newX, getWidth() - selectedElement.getWidth()));
                        newY = Math.max(0, Math.min(newY, getHeight() - selectedElement.getHeight()));

                        selectedElement.setX(newX);
                        selectedElement.setY(newY);
                        invalidate();

                        lastTouchX = x;
                        lastTouchY = y;
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (isDragging && selectedElement != null && moveListener != null) {
                    moveListener.onElementMoved(selectedElement, 0, 0);
                }
                isDragging = false;
                return true;
        }
        return super.onTouchEvent(event);
    }
}
