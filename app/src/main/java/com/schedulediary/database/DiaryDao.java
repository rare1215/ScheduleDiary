package com.schedulediary.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.schedulediary.model.AdDiary;

import java.util.List;

@Dao
public interface DiaryDao {

    @Insert
    long insertDiary(AdDiary diary);

    @Update
    void updateDiary(AdDiary diary);

    @Delete
    void deleteDiary(AdDiary diary);

    @Query("SELECT * FROM diaries WHERE ownerId = :ownerId ORDER BY sortOrder ASC, diaryId ASC")
    List<AdDiary> getDiariesByOwnerSorted(int ownerId);

    @Query("UPDATE diaries SET sortOrder = :sortOrder WHERE diaryId = :diaryId")
    void updateSortOrder(int diaryId, int sortOrder);

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM diaries WHERE ownerId = :ownerId")
    int getMaxSortOrder(int ownerId);

    @Query("SELECT * FROM diaries WHERE diaryId = :diaryId LIMIT 1")
    AdDiary getDiaryById(int diaryId);

    @Query("SELECT * FROM diaries WHERE ownerId = :ownerId AND diaryName LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    List<AdDiary> searchDiaries(int ownerId, String query);
}
