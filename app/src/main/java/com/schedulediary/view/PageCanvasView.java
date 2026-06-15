package com.schedulediary.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.schedulediary.model.PageElement;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 페이지 편집 캔버스 뷰
 * - 요소 렌더링 (텍스트, 이미지, 도형)
 * - 터치로 요소 선택 / 이동 / 코너 핸들 리사이즈 / 회전 핸들 회전 (4, 5번 수정사항)
 * - 비율 고정(aspectRatioLocked) 지원
 * - 이미지 비트맵 캐싱으로 부드러운 드래그 지원
 * - 텍스트 요소 더블탭 시 텍스트 입력 다이얼로그를 띄우기 위한 콜백 지원 (6번 수정사항)
 */
public class PageCanvasView extends View {

    // ── 콜백 인터페이스 ──
    public interface OnElementSelectedListener {
        void onElementSelected(PageElement element);
    }

    /** 요소의 변형(이동/리사이즈/회전)이 끝났을 때 호출 (최종 상태를 전달) */
    public interface OnElementTransformListener {
        void onElementTransformed(PageElement element);
    }

    /** 텍스트 요소를 더블탭했을 때 호출 (텍스트 편집 다이얼로그를 띄우기 위함, 6번 수정사항) */
    public interface OnElementDoubleTapListener {
        void onElementDoubleTap(PageElement element);
    }

    // ── 변형 모드 ──
    private enum TransformMode {
        NONE, MOVE, RESIZE_TL, RESIZE_TR, RESIZE_BL, RESIZE_BR, ROTATE
    }

    // ── 상태 ──
    private List<PageElement> elements = new ArrayList<>();
    private PageElement selectedElement = null;
    private TransformMode transformMode = TransformMode.NONE;

    private float lastTouchX, lastTouchY;
    private float startWidth, startHeight, startX, startY, startRotation;
    private float pivotX, pivotY;          // 회전/리사이즈 기준점 (요소 중심)
    private float startTouchAngle;         // 터치 시작 시 중심 기준 각도

    private OnElementSelectedListener selectionListener;
    private OnElementTransformListener transformListener;
    private OnElementDoubleTapListener doubleTapListener;
    private GestureDetector gestureDetector;

    // ── 페인트 ──
    private final Paint shapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handleStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rotateLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ── 이미지 캐시 (경로 → 비트맵), 부드러운 드래그를 위해 디코딩 1회만 수행 ──
    private final Map<String, Bitmap> bitmapCache = new LinkedHashMap<>();
    private static final int MAX_CACHE_SIZE = 20;

    /** 캔버스에 표시할 비트맵의 최대 변 크기 (이보다 큰 원본은 다운샘플하여 캐싱) */
    private static final int MAX_CACHED_BITMAP_DIMENSION = 800;

    private static final float HANDLE_RADIUS = 14f;
    private static final float ROTATE_HANDLE_OFFSET = 36f; // 위쪽으로 회전 핸들이 떨어진 거리
    private static final float MIN_DRAG_THRESHOLD = 4f;
    private static final float MIN_ELEMENT_SIZE = 24f;

    // ── 배경 이미지 (5번 수정사항) ──
    private String backgroundImagePath = null;
    private Bitmap backgroundBitmap = null;
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ── 재사용 객체 (드래그 중 매 프레임 onDraw/toLocal에서 호출되므로
    //    new 생성을 피해 GC 압박 및 버벅임을 방지) ──
    private final Paint shapeStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path reusablePath = new Path();
    private final RectF reusableRectF = new RectF();
    private final Matrix reusableMatrix = new Matrix();
    private final float[] reusablePoint = new float[2];

    // ── 색상/Typeface 상수 (매 프레임 파싱/생성 방지) ──
    private static final int PLACEHOLDER_COLOR = Color.parseColor("#E9D5FF");
    private static final int TEXT_SELECTION_BG_COLOR = Color.parseColor("#F3E8FF");
    private static final Typeface TYPEFACE_NORMAL = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
    private static final Typeface TYPEFACE_BOLD = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
    private static final Typeface TYPEFACE_ITALIC = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC);
    private static final Typeface TYPEFACE_BOLD_ITALIC = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC);

    private final Gson gson = new Gson();

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
        selectionPaint.setColor(Color.parseColor("#B794F4"));
        selectionPaint.setStrokeWidth(3f);
        selectionPaint.setPathEffect(new DashPathEffect(new float[]{10f, 5f}, 0));

        handlePaint.setStyle(Paint.Style.FILL);
        handlePaint.setColor(Color.parseColor("#B794F4"));

        handleStrokePaint.setStyle(Paint.Style.STROKE);
        handleStrokePaint.setColor(Color.WHITE);
        handleStrokePaint.setStrokeWidth(3f);

        rotateLinePaint.setStyle(Paint.Style.STROKE);
        rotateLinePaint.setColor(Color.parseColor("#D8B4FE"));
        rotateLinePaint.setStrokeWidth(2f);

        // 더블탭 감지: 텍스트 요소를 더블탭하면 텍스트 입력 다이얼로그를 띄운다 (6번 수정사항)
        gestureDetector = new GestureDetector(getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        PageElement touched = findElementAt(e.getX(), e.getY());
                        if (touched != null && touched.getType() == PageElement.ElementType.TEXT
                                && doubleTapListener != null) {
                            doubleTapListener.onElementDoubleTap(touched);
                            return true;
                        }
                        return false;
                    }
                });
    }

    /** 화면 좌표(회전 고려)에 있는 최상단 요소를 반환 */
    private PageElement findElementAt(float x, float y) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            PageElement el = elements.get(i);
            PointF local = toLocal(el, x, y);
            if (local.x >= el.getX() && local.x <= el.getX() + el.getWidth()
                    && local.y >= el.getY() && local.y <= el.getY() + el.getHeight()) {
                return el;
            }
        }
        return null;
    }

    // ────────────────────────────────────────────────
    // 공개 API
    // ────────────────────────────────────────────────

    public void setElements(List<PageElement> elements) {
        // ObjectController의 원본 PageElement 인스턴스와 분리되도록 깊은 복사한다.
        // (이 뷰는 드래그/리사이즈/회전 중 선택된 요소를 직접 mutate하므로,
        //  참조를 공유하면 ObjectController의 undo 스냅샷이 오염될 수 있다.)
        Type type = new TypeToken<List<PageElement>>() {}.getType();
        String json = gson.toJson(elements);
        List<PageElement> copy = gson.fromJson(json, type);
        this.elements = copy != null ? copy : new ArrayList<>();

        // 선택된 요소도 최신 데이터로 갱신 (참조가 바뀌었을 수 있음)
        if (selectedElement != null) {
            for (PageElement el : this.elements) {
                if (el.getElementId().equals(selectedElement.getElementId())) {
                    selectedElement = el;
                    break;
                }
            }
        }
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

    public void setOnElementTransformListener(OnElementTransformListener l) {
        this.transformListener = l;
    }

    public void setOnElementDoubleTapListener(OnElementDoubleTapListener l) {
        this.doubleTapListener = l;
    }

    /** 이미지 캐시 비우기 (배경/요소 이미지가 외부에서 변경되었을 때 호출) */
    public void clearBitmapCache() {
        bitmapCache.clear();
        invalidate();
    }

    /**
     * 배경 이미지를 캔버스 전체에 꽉 차도록 설정한다 (5번 수정사항).
     * 배경 이미지가 설정되면 setBackgroundColor로 지정된 단색 배경은 무시되고
     * 이미지가 캔버스 전체 영역을 덮어 그려진다. null을 전달하면 배경 이미지를 제거한다.
     */
    public void setBackgroundImage(String path) {
        this.backgroundImagePath = path;
        if (path == null) {
            backgroundBitmap = null;
        } else {
            backgroundBitmap = BitmapFactory.decodeFile(path);
        }
        invalidate();
    }

    public String getBackgroundImagePath() {
        return backgroundImagePath;
    }

    // ────────────────────────────────────────────────
    // 그리기
    // ────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 배경 이미지가 설정된 경우 캔버스 전체에 꽉 차게 그린다 (5번 수정사항)
        if (backgroundBitmap != null) {
            reusableRectF.set(0, 0, getWidth(), getHeight());
            canvas.drawBitmap(backgroundBitmap, null, reusableRectF, backgroundPaint);
        }

        // z-order 순서대로 요소 렌더링
        for (PageElement el : elements) {
            canvas.save();
            float cx = el.getX() + el.getWidth() / 2f;
            float cy = el.getY() + el.getHeight() / 2f;
            canvas.rotate(el.getRotation(), cx, cy);

            switch (el.getType()) {
                case TEXT:   drawText(canvas, el);   break;
                case IMAGE:  drawImage(canvas, el);  break;
                case SHAPE:  drawShape(canvas, el);  break;
            }
            canvas.restore();
        }

        // 선택된 요소의 핸들은 다른 요소에 가려지지 않도록 항상 최상단에 그린다
        if (selectedElement != null) {
            for (PageElement el : elements) {
                if (el.getElementId().equals(selectedElement.getElementId())) {
                    drawSelectionHandles(canvas, el);
                    break;
                }
            }
        }
    }

    private void drawText(Canvas canvas, PageElement el) {
        textPaint.setTextSize(el.getTextSize() * getResources().getDisplayMetrics().scaledDensity);
        textPaint.setColor(el.getTextColor());

        if (el.isBold() && el.isItalic()) textPaint.setTypeface(TYPEFACE_BOLD_ITALIC);
        else if (el.isBold()) textPaint.setTypeface(TYPEFACE_BOLD);
        else if (el.isItalic()) textPaint.setTypeface(TYPEFACE_ITALIC);
        else textPaint.setTypeface(TYPEFACE_NORMAL);

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
            shapePaint.setColor(TEXT_SELECTION_BG_COLOR);
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
            shapePaint.setColor(PLACEHOLDER_COLOR);
            canvas.drawRect(el.getX(), el.getY(),
                    el.getX() + el.getWidth(), el.getY() + el.getHeight(), shapePaint);
            return;
        }

        Bitmap bmp = getCachedBitmap(el.getImagePath());
        if (bmp != null) {
            reusableRectF.set(el.getX(), el.getY(),
                    el.getX() + el.getWidth(), el.getY() + el.getHeight());
            canvas.drawBitmap(bmp, null, reusableRectF, null);
        }
    }

    /** 비트맵 디코딩 결과를 캐싱하여 드래그 시마다 디코딩하지 않도록 함 (4번: 부드러운 이동) */
    private Bitmap getCachedBitmap(String path) {
        Bitmap cached = bitmapCache.get(path);
        if (cached != null) return cached;

        // 원본 크기를 먼저 확인하여 너무 큰 이미지는 다운샘플링 (드래그 시 매 프레임
        // drawBitmap 스케일링 비용을 줄여 부드러운 이동을 지원)
        BitmapFactory.Options boundsOpts = new BitmapFactory.Options();
        boundsOpts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, boundsOpts);

        int sampleSize = 1;
        while (boundsOpts.outWidth / sampleSize > MAX_CACHED_BITMAP_DIMENSION
                || boundsOpts.outHeight / sampleSize > MAX_CACHED_BITMAP_DIMENSION) {
            sampleSize *= 2;
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = sampleSize;

        Bitmap bmp = BitmapFactory.decodeFile(path, opts);
        if (bmp != null) {
            if (bitmapCache.size() >= MAX_CACHE_SIZE) {
                // 가장 오래된 항목 제거
                String oldestKey = bitmapCache.keySet().iterator().next();
                bitmapCache.remove(oldestKey);
            }
            bitmapCache.put(path, bmp);
        }
        return bmp;
    }

    private void drawShape(Canvas canvas, PageElement el) {
        shapePaint.setStyle(Paint.Style.FILL);
        shapePaint.setColor(el.getFillColor());

        shapeStrokePaint.setStyle(Paint.Style.STROKE);
        shapeStrokePaint.setColor(el.getStrokeColor());
        shapeStrokePaint.setStrokeWidth(el.getStrokeWidth());

        float l = el.getX(), t = el.getY();
        float r = l + el.getWidth(), b = t + el.getHeight();

        if (el.getShapeType() == null) el.setShapeType(PageElement.ShapeType.RECTANGLE);

        switch (el.getShapeType()) {
            case RECTANGLE:
                canvas.drawRoundRect(l, t, r, b, 8f, 8f, shapePaint);
                canvas.drawRoundRect(l, t, r, b, 8f, 8f, shapeStrokePaint);
                break;
            case CIRCLE:
                // 가로/세로 비율이 달라도 타원으로 표현 (리사이즈 자유도)
                canvas.drawOval(l, t, r, b, shapePaint);
                canvas.drawOval(l, t, r, b, shapeStrokePaint);
                break;
            case TRIANGLE:
                reusablePath.reset();
                reusablePath.moveTo((l + r) / 2f, t);
                reusablePath.lineTo(r, b);
                reusablePath.lineTo(l, b);
                reusablePath.close();
                canvas.drawPath(reusablePath, shapePaint);
                canvas.drawPath(reusablePath, shapeStrokePaint);
                break;
            case LINE:
                shapeStrokePaint.setStrokeWidth(Math.max(el.getStrokeWidth() * 2, 2f));
                canvas.drawLine(l, (t + b) / 2f, r, (t + b) / 2f, shapeStrokePaint);
                break;
        }
    }

    /**
     * 선택된 요소의 핸들 그리기: 코너 4개(리사이즈) + 상단 회전 핸들 1개
     * 요소의 회전을 그대로 반영하여 핸들도 함께 회전시킨다.
     */
    private void drawSelectionHandles(Canvas canvas, PageElement el) {
        canvas.save();
        float cx = el.getX() + el.getWidth() / 2f;
        float cy = el.getY() + el.getHeight() / 2f;
        canvas.rotate(el.getRotation(), cx, cy);

        float l = el.getX(), t = el.getY();
        float r = l + el.getWidth(), b = t + el.getHeight();

        // 점선 테두리
        canvas.drawRect(l - 4, t - 4, r + 4, b + 4, selectionPaint);

        // 코너 리사이즈 핸들 4개
        drawHandle(canvas, l, t);
        drawHandle(canvas, r, t);
        drawHandle(canvas, l, b);
        drawHandle(canvas, r, b);

        // 회전 핸들: 상단 중앙에서 위로 떨어진 위치
        float rotCx = (l + r) / 2f;
        float rotCy = t - ROTATE_HANDLE_OFFSET;
        canvas.drawLine(rotCx, t, rotCx, rotCy, rotateLinePaint);
        drawHandle(canvas, rotCx, rotCy);

        canvas.restore();
    }

    private void drawHandle(Canvas canvas, float x, float y) {
        canvas.drawCircle(x, y, HANDLE_RADIUS, handlePaint);
        canvas.drawCircle(x, y, HANDLE_RADIUS, handleStrokePaint);
    }

    // ────────────────────────────────────────────────
    // 터치 핸들링 (선택 + 이동 + 리사이즈 + 회전 + 더블탭)
    // ────────────────────────────────────────────────

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 더블탭 감지 (텍스트 요소 재편집용, 6번 수정사항)
        gestureDetector.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return handleTouchDown(x, y);

            case MotionEvent.ACTION_MOVE:
                return handleTouchMove(x, y);

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (transformMode != TransformMode.NONE && selectedElement != null
                        && transformListener != null) {
                    transformListener.onElementTransformed(selectedElement);
                }
                transformMode = TransformMode.NONE;
                return true;
        }
        return super.onTouchEvent(event);
    }

    private boolean handleTouchDown(float x, float y) {
        lastTouchX = x;
        lastTouchY = y;
        transformMode = TransformMode.NONE;

        // 1. 이미 선택된 요소가 있다면, 핸들(코너/회전) 터치인지 먼저 확인
        if (selectedElement != null) {
            PointF local = toLocal(selectedElement, x, y);
            float l = selectedElement.getX(), t = selectedElement.getY();
            float r = l + selectedElement.getWidth(), b = t + selectedElement.getHeight();

            if (isNear(local, r, b, HANDLE_RADIUS * 1.5f)) {
                startTransform(TransformMode.RESIZE_BR, selectedElement);
                return true;
            }
            if (isNear(local, l, b, HANDLE_RADIUS * 1.5f)) {
                startTransform(TransformMode.RESIZE_BL, selectedElement);
                return true;
            }
            if (isNear(local, r, t, HANDLE_RADIUS * 1.5f)) {
                startTransform(TransformMode.RESIZE_TR, selectedElement);
                return true;
            }
            if (isNear(local, l, t, HANDLE_RADIUS * 1.5f)) {
                startTransform(TransformMode.RESIZE_TL, selectedElement);
                return true;
            }
            // 회전 핸들 (상단 중앙에서 위로 떨어진 위치)
            float rotHandleX = (l + r) / 2f;
            float rotHandleY = t - ROTATE_HANDLE_OFFSET;
            if (isNear(local, rotHandleX, rotHandleY, HANDLE_RADIUS * 1.5f)) {
                startTransform(TransformMode.ROTATE, selectedElement);
                return true;
            }
        }

        // 2. 요소 본체 터치 탐색 (z-order 역순, 회전 고려)
        PageElement touched = findElementAt(x, y);

        if (touched != null) {
            startTransform(TransformMode.MOVE, touched);
        } else {
            setSelectedElement(null);
        }
        return true;
    }

    private boolean handleTouchMove(float x, float y) {
        if (selectedElement == null || transformMode == TransformMode.NONE) return true;

        float dx = x - lastTouchX;
        float dy = y - lastTouchY;

        switch (transformMode) {
            case MOVE:
                moveElement(dx, dy);
                lastTouchX = x;
                lastTouchY = y;
                break;

            case RESIZE_TL:
            case RESIZE_TR:
            case RESIZE_BL:
            case RESIZE_BR:
                resizeElement(x, y);
                break;

            case ROTATE:
                rotateElement(x, y);
                break;

            default:
                break;
        }
        invalidate();
        return true;
    }

    private void startTransform(TransformMode mode, PageElement element) {
        transformMode = mode;
        setSelectedElement(element);

        startX = element.getX();
        startY = element.getY();
        startWidth = element.getWidth();
        startHeight = element.getHeight();
        startRotation = element.getRotation();
        pivotX = element.getX() + element.getWidth() / 2f;
        pivotY = element.getY() + element.getHeight() / 2f;

        if (mode == TransformMode.ROTATE) {
            startTouchAngle = (float) Math.toDegrees(Math.atan2(lastTouchY - pivotY, lastTouchX - pivotX));
        }
    }

    /** 이동: 뷰 경계 내로 제한 */
    private void moveElement(float dx, float dy) {
        float newX = selectedElement.getX() + dx;
        float newY = selectedElement.getY() + dy;
        newX = Math.max(0, Math.min(newX, getWidth() - selectedElement.getWidth()));
        newY = Math.max(0, Math.min(newY, getHeight() - selectedElement.getHeight()));
        selectedElement.setX(newX);
        selectedElement.setY(newY);
    }

    /**
     * 코너 핸들 드래그로 리사이즈.
     * 비율 고정(aspectRatioLocked)인 경우 가로 변화량을 기준으로 세로를 함께 조정한다.
     * 회전된 요소는 터치 좌표를 요소의 로컬 좌표계로 변환하여 처리한다.
     */
    private void resizeElement(float touchX, float touchY) {
        PointF local = toLocal(selectedElement, touchX, touchY);
        float aspect = startHeight != 0 ? startWidth / startHeight : 1f;

        float newLeft = startX, newTop = startY;
        float newWidth = startWidth, newHeight = startHeight;

        switch (transformMode) {
            case RESIZE_BR:
                newWidth = local.x - startX;
                newHeight = local.y - startY;
                break;
            case RESIZE_BL:
                newWidth = (startX + startWidth) - local.x;
                newHeight = local.y - startY;
                newLeft = (startX + startWidth) - newWidth;
                break;
            case RESIZE_TR:
                newWidth = local.x - startX;
                newHeight = (startY + startHeight) - local.y;
                newTop = (startY + startHeight) - newHeight;
                break;
            case RESIZE_TL:
                newWidth = (startX + startWidth) - local.x;
                newHeight = (startY + startHeight) - local.y;
                newLeft = (startX + startWidth) - newWidth;
                newTop = (startY + startHeight) - newHeight;
                break;
            default:
                break;
        }

        // 최소 크기 보장
        newWidth = Math.max(newWidth, MIN_ELEMENT_SIZE);
        newHeight = Math.max(newHeight, MIN_ELEMENT_SIZE);

        // 비율 고정: 가로 기준으로 세로를 재계산 (코너 기준점 유지)
        if (selectedElement.isAspectRatioLocked() && aspect > 0) {
            newHeight = newWidth / aspect;
            if (newHeight < MIN_ELEMENT_SIZE) {
                newHeight = MIN_ELEMENT_SIZE;
                newWidth = newHeight * aspect;
            }
            // 좌상단이 고정되어야 하는 핸들의 경우 위치 재조정
            switch (transformMode) {
                case RESIZE_BL:
                    newLeft = (startX + startWidth) - newWidth;
                    break;
                case RESIZE_TR:
                    newTop = (startY + startHeight) - newHeight;
                    break;
                case RESIZE_TL:
                    newLeft = (startX + startWidth) - newWidth;
                    newTop = (startY + startHeight) - newHeight;
                    break;
                default:
                    break;
            }
        }

        selectedElement.setX(newLeft);
        selectedElement.setY(newTop);
        selectedElement.setWidth(newWidth);
        selectedElement.setHeight(newHeight);
    }

    /** 회전 핸들 드래그: 요소 중심을 기준으로 터치 각도 변화량만큼 회전 */
    private void rotateElement(float touchX, float touchY) {
        float currentAngle = (float) Math.toDegrees(Math.atan2(touchY - pivotY, touchX - pivotX));
        float delta = currentAngle - startTouchAngle;
        float newRotation = startRotation + delta;

        // -180 ~ 180 범위로 정규화
        while (newRotation > 180f) newRotation -= 360f;
        while (newRotation < -180f) newRotation += 360f;

        selectedElement.setRotation(newRotation);
    }

    /**
     * 화면 좌표를 요소의 회전을 역으로 적용한 로컬 좌표로 변환.
     * 회전된 요소에서도 코너 핸들/본체 히트테스트가 정확히 동작하도록 한다.
     */
    private PointF toLocal(PageElement el, float x, float y) {
        if (el.getRotation() == 0f) {
            return new PointF(x, y);
        }
        float cx = el.getX() + el.getWidth() / 2f;
        float cy = el.getY() + el.getHeight() / 2f;

        reusableMatrix.reset();
        reusableMatrix.setRotate(-el.getRotation(), cx, cy);
        reusablePoint[0] = x;
        reusablePoint[1] = y;
        reusableMatrix.mapPoints(reusablePoint);
        return new PointF(reusablePoint[0], reusablePoint[1]);
    }

    private boolean isNear(PointF point, float targetX, float targetY, float radius) {
        float dx = point.x - targetX;
        float dy = point.y - targetY;
        return (dx * dx + dy * dy) <= (radius * radius);
    }
}
