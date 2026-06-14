package com.schedulediary.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.schedulediary.model.AdPage;

import java.util.List;

@Dao
public interface PageDao {

    @Insert
    long insertPage(AdPage page);

    @Update
    void updatePage(AdPage page);

    @Delete
    void deletePage(AdPage page);

    @Query("SELECT * FROM pages WHERE diaryId = :diaryId ORDER BY pageNumber ASC")
    List<AdPage> getPagesByDiary(int diaryId);

    @Query("SELECT * FROM pages WHERE pageId = :pageId LIMIT 1")
    AdPage getPageById(int pageId);

    @Query("SELECT COUNT(*) FROM pages WHERE diaryId = :diaryId")
    int countPagesByDiary(int diaryId);

    @Query("SELECT * FROM pages WHERE diaryId = :diaryId AND title LIKE '%' || :query || '%' ORDER BY pageNumber ASC")
    List<AdPage> searchPages(int diaryId, String query);

    @Query("UPDATE pages SET pageNumber = pageNumber + 1 WHERE diaryId = :diaryId AND pageNumber >= :fromPosition")
    void shiftPagesDown(int diaryId, int fromPosition);

    @Query("SELECT MAX(pageNumber) FROM pages WHERE diaryId = :diaryId")
    int getMaxPageNumber(int diaryId);
}
