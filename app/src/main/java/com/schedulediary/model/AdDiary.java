package com.schedulediary.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "diaries",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "userId",
                childColumns = "ownerId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("ownerId")})
public class AdDiary {

    @PrimaryKey(autoGenerate = true)
    private int diaryId;

    @NonNull
    private String diaryName;

    private String description;

    private int ownerId;

    private String coverColor;   // hex color string e.g. "#E9D5FF"

    private String coverImagePath; // local path to cover image

    private long createdAt;

    private long updatedAt;

    // 사용자가 드래그로 정렬한 순서 (8번 수정사항, 낮을수록 위)
    private int sortOrder;

    public AdDiary(@NonNull String diaryName, String description, int ownerId, String coverColor) {
        this.diaryName = diaryName;
        this.description = description;
        this.ownerId = ownerId;
        this.coverColor = coverColor != null ? coverColor : "#E9D5FF";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.sortOrder = 0;
    }

    // Getters & Setters
    public int getDiaryId() { return diaryId; }
    public void setDiaryId(int diaryId) { this.diaryId = diaryId; }

    @NonNull
    public String getDiaryName() { return diaryName; }
    public void setDiaryName(@NonNull String diaryName) { this.diaryName = diaryName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getCoverColor() { return coverColor; }
    public void setCoverColor(String coverColor) { this.coverColor = coverColor; }

    public String getCoverImagePath() { return coverImagePath; }
    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
