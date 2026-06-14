package com.schedulediary.model;

/**
 * 페이지 내에 삽입되는 요소(텍스트박스, 도형, 이미지)를 나타냅니다.
 * Gson으로 직렬화하여 AdPage.elementsJson에 저장됩니다.
 */
public class PageElement {

    public enum ElementType {
        TEXT, IMAGE, SHAPE
    }

    public enum ShapeType {
        RECTANGLE, CIRCLE, TRIANGLE, LINE
    }

    private String elementId;       // UUID
    private ElementType type;

    // 위치 & 크기
    private float x;
    private float y;
    private float width;
    private float height;
    private float rotation;         // degrees

    // 텍스트 전용
    private String text;
    private float textSize;         // sp
    private int textColor;          // ARGB
    private boolean bold;
    private boolean italic;
    private boolean underline;
    private boolean strikethrough;
    private int textAlign;          // 0=LEFT, 1=CENTER, 2=RIGHT

    // 이미지 전용
    private String imagePath;       // local URI string

    // 도형 전용
    private ShapeType shapeType;
    private int fillColor;
    private int strokeColor;
    private float strokeWidth;

    // z-order (레이어 순서)
    private int zIndex;

    public PageElement() {}

    public PageElement(String elementId, ElementType type) {
        this.elementId = elementId;
        this.type = type;
        this.width = 200f;
        this.height = 100f;
        this.rotation = 0f;
        this.zIndex = 0;
        // 기본 텍스트 설정
        this.textSize = 14f;
        this.textColor = 0xFF000000;
        this.textAlign = 0;
        // 기본 도형 설정
        this.fillColor = 0xFFE9D5FF;
        this.strokeColor = 0xFF9C27B0;
        this.strokeWidth = 2f;
    }

    // ---- Getters & Setters ----
    public String getElementId() { return elementId; }
    public void setElementId(String elementId) { this.elementId = elementId; }

    public ElementType getType() { return type; }
    public void setType(ElementType type) { this.type = type; }

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }

    public float getY() { return y; }
    public void setY(float y) { this.y = y; }

    public float getWidth() { return width; }
    public void setWidth(float width) { this.width = width; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    public float getRotation() { return rotation; }
    public void setRotation(float rotation) { this.rotation = rotation; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public float getTextSize() { return textSize; }
    public void setTextSize(float textSize) { this.textSize = textSize; }

    public int getTextColor() { return textColor; }
    public void setTextColor(int textColor) { this.textColor = textColor; }

    public boolean isBold() { return bold; }
    public void setBold(boolean bold) { this.bold = bold; }

    public boolean isItalic() { return italic; }
    public void setItalic(boolean italic) { this.italic = italic; }

    public boolean isUnderline() { return underline; }
    public void setUnderline(boolean underline) { this.underline = underline; }

    public boolean isStrikethrough() { return strikethrough; }
    public void setStrikethrough(boolean strikethrough) { this.strikethrough = strikethrough; }

    public int getTextAlign() { return textAlign; }
    public void setTextAlign(int textAlign) { this.textAlign = textAlign; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public ShapeType getShapeType() { return shapeType; }
    public void setShapeType(ShapeType shapeType) { this.shapeType = shapeType; }

    public int getFillColor() { return fillColor; }
    public void setFillColor(int fillColor) { this.fillColor = fillColor; }

    public int getStrokeColor() { return strokeColor; }
    public void setStrokeColor(int strokeColor) { this.strokeColor = strokeColor; }

    public float getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(float strokeWidth) { this.strokeWidth = strokeWidth; }

    public int getZIndex() { return zIndex; }
    public void setZIndex(int zIndex) { this.zIndex = zIndex; }
}
