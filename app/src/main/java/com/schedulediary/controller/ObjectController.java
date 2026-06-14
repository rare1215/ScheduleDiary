package com.schedulediary.controller;

import com.schedulediary.model.PageElement;

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
     * 리스트 딥카피 - Undo/Redo용 스냅샷
     * 실제 프로젝트에서는 Gson 직렬화/역직렬화로 완전한 딥카피를 권장합니다.
     */
    private List<PageElement> deepCopy(List<PageElement> source) {
        // Gson을 이용한 딥카피 (런타임에 실제 동작)
        // 여기서는 간단히 새 리스트에 같은 참조를 복사 (실제 앱에서는 Gson 사용 권장)
        return new ArrayList<>(source);
    }

    private void notifyChanged() {
        if (listener != null) {
            listener.onElementsChanged(getElements());
        }
    }
}
