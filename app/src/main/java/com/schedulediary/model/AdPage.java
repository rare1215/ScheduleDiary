package com.schedulediary.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "pages",
        foreignKeys = @ForeignKey(
                entity = AdDiary.class,
                parentColumns = "diaryId",
                childColumns = "diaryId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("diaryId")})
public class AdPage {

    @PrimaryKey(autoGenerate = true)
    private int pageId;

    private int diaryId;

    @NonNull
    private String title;

    private String textContent;   // rich text content

    private int pageNumber;

    private String backgroundStyle;  // JSON: {"type":"color","value":"#FFFFFF"} or {"type":"image","path":"..."}

    private String elementsJson;     // JSON array of PageElement objects

    private long createdAt;

    private long updatedAt;

    public AdPage(int diaryId, @NonNull String title, int pageNumber) {
        this.diaryId = diaryId;
        this.title = title;
        this.pageNumber = pageNumber;
        this.backgroundStyle = "{\"type\":\"color\",\"value\":\"#FFFFFF\"}";
        this.elementsJson = "[]";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters & Setters
    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }

    public int getDiaryId() { return diaryId; }
    public void setDiaryId(int diaryId) { this.diaryId = diaryId; }

    @NonNull
    public String getTitle() { return title; }
    public void setTitle(@NonNull String title) { this.title = title; }

    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

    public String getBackgroundStyle() { return backgroundStyle; }
    public void setBackgroundStyle(String backgroundStyle) { this.backgroundStyle = backgroundStyle; }

    public String getElementsJson() { return elementsJson; }
    public void setElementsJson(String elementsJson) { this.elementsJson = elementsJson; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
