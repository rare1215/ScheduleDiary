package com.schedulediary.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.schedulediary.model.PageElement;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

/**
 * 텍스트, 오브젝트, 이미지 등 페이지 내 개체의 속성 변경 및 Undo/Redo 담당 (Design.md §6)
 */
public class ObjectController {

    private final Stack<List<PageElement>> undoStack = new Stack<>();
    private final Stack<List<PageElement>> redoStack = new Stack<>();
    private List<PageElement> currentElements;
    private final Gson gson = new Gson();

    public interface OnChangeListener {
        void onElementsChanged(List<PageElement> elements);
    }

    private OnChangeListener listener;

    public ObjectController() {
        this.currentElements = new ArrayList<>();
    }

    public void setOnChangeListener(OnChangeListener listener) {
        this.listener = listener;
    }

    public void setElements(List<PageElement> elements) {
        this.currentElements = new ArrayList<>(elements);
        undoStack.clear();
        redoStack.clear();
    }

    public List<PageElement> getElements() {
        return new ArrayList<>(currentElements);
    }

    // ────────────────────────────────────────────────
    // 요소 생성 (Design.md §6 createElement)
    // ────────────────────────────────────────────────

    public PageElement createElement(PageElement.ElementType type) {
        String id = UUID.randomUUID().toString();
        PageElement element = new PageElement(id, type);

        // 타입별 기본값 설정
        switch (type) {
            case TEXT:
                element.setText("텍스트를 입력하세요");
                element.setWidth(200f);
                element.setHeight(60f);
                element.setTextSize(14f);
                element.setTextColor(0xFF212121);
                break;
            case IMAGE:
                element.setWidth(200f);
                element.setHeight(200f);
                element.setAspectRatioLocked(true); // 4번: 이미지는 기본 비율 고정
                break;
            case SHAPE:
                element.setShapeType(PageElement.ShapeType.RECTANGLE);
                element.setWidth(150f);
                element.setHeight(100f);
                element.setFillColor(0xFFE9D5FF);
                element.setStrokeColor(0xFF9C27B0);
                element.setStrokeWidth(3f);
                break;
        }

        // z-order: 현재 최상위 + 1
        element.setZIndex(currentElements.size());
        return element;
    }

    // ────────────────────────────────────────────────
    // 요소 추가
    // ────────────────────────────────────────────────

    public void addElement(PageElement element) {
        saveStateForUndo();
        currentElements.add(element);
        redoStack.clear();
        notifyChanged();
    }

    // ────────────────────────────────────────────────
    // 요소 수정 (Design.md §6 modifyElement)
    // ────────────────────────────────────────────────

    public void modifyElement(String elementId, PageElement updated) {
        saveStateForUndo();
        for (int i = 0; i < currentElements.size(); i++) {
            if (currentElements.get(i).getElementId().equals(elementId)) {
                currentElements.set(i, updated);
                break;
            }
        }
        redoStack.clear();
        notifyChanged();
    }

    // ────────────────────────────────────────────────
    // 요소 삭제
    // ────────────────────────────────────────────────

    public void removeElement(String elementId) {
        saveStateForUndo();
        currentElements.removeIf(e -> e.getElementId().equals(elementId));
        redoStack.clear();
        notifyChanged();
    }

    // ────────────────────────────────────────────────
    // 정렬 (Design.md §6 alignElements)
    // ────────────────────────────────────────────────

    /**
     * @param alignType 0=LEFT, 1=CENTER, 2=RIGHT (캔버스 기준 가로 정렬)
     */
    public void alignElements(List<String> elementIds, int alignType, float canvasWidth) {
        saveStateForUndo();
        for (PageElement el : currentElements) {
            if (elementIds.contains(el.getElementId())) {
                switch (alignType) {
                    case 0: // LEFT
                        el.setX(0);
                        break;
                    case 1: // CENTER
                        el.setX((canvasWidth - el.getWidth()) / 2f);
                        break;
                    case 2: // RIGHT
                        el.setX(canvasWidth - el.getWidth());
                        break;
                }
            }
        }
        redoStack.clear();
        notifyChanged();
    }

    // ────────────────────────────────────────────────
    // Undo / Redo (Design.md §6)
    // ────────────────────────────────────────────────

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void undo() {
        if (!canUndo()) return;
        redoStack.push(deepCopy(currentElements));
        currentElements = undoStack.pop();
        notifyChanged();
    }

    public void redo() {
        if (!canRedo()) return;
        undoStack.push(deepCopy(currentElements));
        currentElements = redoStack.pop();
        notifyChanged();
    }

    // ────────────────────────────────────────────────
    // 요소 찾기
    // ────────────────────────────────────────────────

    public PageElement findElement(String elementId) {
        for (PageElement el : currentElements) {
            if (el.getElementId().equals(elementId)) {
                return el;
            }
        }
        return null;
    }

    /** 터치 좌표로 요소 찾기 (z-order 역순 = 위쪽 레이어 우선) */
    public PageElement findElementAtPosition(float x, float y) {
        for (int i = currentElements.size() - 1; i >= 0; i--) {
            PageElement el = currentElements.get(i);
            if (x >= el.getX() && x <= el.getX() + el.getWidth()
                    && y >= el.getY() && y <= el.getY() + el.getHeight()) {
                return el;
            }
        }
        return null;
    }

    // ────────────────────────────────────────────────
    // 내부 유틸
    // ────────────────────────────────────────────────

    private void saveStateForUndo() {
        undoStack.push(deepCopy(currentElements));
        // 스택 너무 커지면 오래된 것 제거 (최대 50단계)
        if (undoStack.size() > 50) {
            undoStack.remove(0);
        }
    }

    /**
     * 리스트 딥카피 - Undo/Redo용 스냅샷.
     * PageCanvasView가 선택된 요소를 직접 mutate하므로(이동/리사이즈/회전 중),
     * 참조를 공유하는 얕은 복사로는 undo 시 변경 전 상태를 보존할 수 없다.
     * Gson을 이용해 완전한 딥카피를 수행한다.
     */
    private List<PageElement> deepCopy(List<PageElement> source) {
        Type type = new TypeToken<List<PageElement>>() {}.getType();
        String json = gson.toJson(source);
        List<PageElement> copy = gson.fromJson(json, type);
        return copy != null ? copy : new ArrayList<>();
    }

    private void notifyChanged() {
        if (listener != null) {
            listener.onElementsChanged(getElements());
        }
    }
}
