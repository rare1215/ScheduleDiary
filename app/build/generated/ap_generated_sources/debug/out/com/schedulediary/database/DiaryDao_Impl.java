package com.schedulediary.database;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.schedulediary.model.AdDiary;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DiaryDao_Impl implements DiaryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AdDiary> __insertionAdapterOfAdDiary;

  private final EntityDeletionOrUpdateAdapter<AdDiary> __deletionAdapterOfAdDiary;

  private final EntityDeletionOrUpdateAdapter<AdDiary> __updateAdapterOfAdDiary;

  public DiaryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAdDiary = new EntityInsertionAdapter<AdDiary>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `diaries` (`diaryId`,`diaryName`,`description`,`ownerId`,`coverColor`,`coverImagePath`,`createdAt`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final AdDiary entity) {
        statement.bindLong(1, entity.getDiaryId());
        if (entity.getDiaryName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getDiaryName());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getDescription());
        }
        statement.bindLong(4, entity.getOwnerId());
        if (entity.getCoverColor() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getCoverColor());
        }
        if (entity.getCoverImagePath() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCoverImagePath());
        }
        statement.bindLong(7, entity.getCreatedAt());
        statement.bindLong(8, entity.getUpdatedAt());
      }
    };
    this.__deletionAdapterOfAdDiary = new EntityDeletionOrUpdateAdapter<AdDiary>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `diaries` WHERE `diaryId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final AdDiary entity) {
        statement.bindLong(1, entity.getDiaryId());
      }
    };
    this.__updateAdapterOfAdDiary = new EntityDeletionOrUpdateAdapter<AdDiary>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `diaries` SET `diaryId` = ?,`diaryName` = ?,`description` = ?,`ownerId` = ?,`coverColor` = ?,`coverImagePath` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `diaryId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final AdDiary entity) {
        statement.bindLong(1, entity.getDiaryId());
        if (entity.getDiaryName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getDiaryName());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getDescription());
        }
        statement.bindLong(4, entity.getOwnerId());
        if (entity.getCoverColor() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getCoverColor());
        }
        if (entity.getCoverImagePath() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCoverImagePath());
        }
        statement.bindLong(7, entity.getCreatedAt());
        statement.bindLong(8, entity.getUpdatedAt());
        statement.bindLong(9, entity.getDiaryId());
      }
    };
  }

  @Override
  public long insertDiary(final AdDiary diary) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfAdDiary.insertAndReturnId(diary);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteDiary(final AdDiary diary) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfAdDiary.handle(diary);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void updateDiary(final AdDiary diary) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfAdDiary.handle(diary);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<AdDiary> getDiariesByOwner(final int ownerId) {
    final String _sql = "SELECT * FROM diaries WHERE ownerId = ? ORDER BY updatedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, ownerId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfDiaryId = CursorUtil.getColumnIndexOrThrow(_cursor, "diaryId");
      final int _cursorIndexOfDiaryName = CursorUtil.getColumnIndexOrThrow(_cursor, "diaryName");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfOwnerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerId");
      final int _cursorIndexOfCoverColor = CursorUtil.getColumnIndexOrThrow(_cursor, "coverColor");
      final int _cursorIndexOfCoverImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "coverImagePath");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
      final List<AdDiary> _result = new ArrayList<AdDiary>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final AdDiary _item;
        final String _tmpDiaryName;
        if (_cursor.isNull(_cursorIndexOfDiaryName)) {
          _tmpDiaryName = null;
        } else {
          _tmpDiaryName = _cursor.getString(_cursorIndexOfDiaryName);
        }
        final String _tmpDescription;
        if (_cursor.isNull(_cursorIndexOfDescription)) {
          _tmpDescription = null;
        } else {
          _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
        }
        final int _tmpOwnerId;
        _tmpOwnerId = _cursor.getInt(_cursorIndexOfOwnerId);
        final String _tmpCoverColor;
        if (_cursor.isNull(_cursorIndexOfCoverColor)) {
          _tmpCoverColor = null;
        } else {
          _tmpCoverColor = _cursor.getString(_cursorIndexOfCoverColor);
        }
        _item = new AdDiary(_tmpDiaryName,_tmpDescription,_tmpOwnerId,_tmpCoverColor);
        final int _tmpDiaryId;
        _tmpDiaryId = _cursor.getInt(_cursorIndexOfDiaryId);
        _item.setDiaryId(_tmpDiaryId);
        final String _tmpCoverImagePath;
        if (_cursor.isNull(_cursorIndexOfCoverImagePath)) {
          _tmpCoverImagePath = null;
        } else {
          _tmpCoverImagePath = _cursor.getString(_cursorIndexOfCoverImagePath);
        }
        _item.setCoverImagePath(_tmpCoverImagePath);
        final long _tmpCreatedAt;
        _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
        _item.setCreatedAt(_tmpCreatedAt);
        final long _tmpUpdatedAt;
        _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
        _item.setUpdatedAt(_tmpUpdatedAt);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public AdDiary getDiaryById(final int diaryId) {
    final String _sql = "SELECT * FROM diaries WHERE diaryId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, diaryId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfDiaryId = CursorUtil.getColumnIndexOrThrow(_cursor, "diaryId");
      final int _cursorIndexOfDiaryName = CursorUtil.getColumnIndexOrThrow(_cursor, "diaryName");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfOwnerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerId");
      final int _cursorIndexOfCoverColor = CursorUtil.getColumnIndexOrThrow(_cursor, "coverColor");
      final int _cursorIndexOfCoverImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "coverImagePath");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
      final AdDiary _result;
      if (_cursor.moveToFirst()) {
        final String _tmpDiaryName;
        if (_cursor.isNull(_cursorIndexOfDiaryName)) {
          _tmpDiaryName = null;
        } else {
          _tmpDiaryName = _cursor.getString(_cursorIndexOfDiaryName);
        }
        final String _tmpDescription;
        if (_cursor.isNull(_cursorIndexOfDescription)) {
          _tmpDescription = null;
        } else {
          _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
        }
        final int _tmpOwnerId;
        _tmpOwnerId = _cursor.getInt(_cursorIndexOfOwnerId);
        final String _tmpCoverColor;
        if (_cursor.isNull(_cursorIndexOfCoverColor)) {
          _tmpCoverColor = null;
        } else {
          _tmpCoverColor = _cursor.getString(_cursorIndexOfCoverColor);
        }
        _result = new AdDiary(_tmpDiaryName,_tmpDescription,_tmpOwnerId,_tmpCoverColor);
        final int _tmpDiaryId;
        _tmpDiaryId = _cursor.getInt(_cursorIndexOfDiaryId);
        _result.setDiaryId(_tmpDiaryId);
        final String _tmpCoverImagePath;
        if (_cursor.isNull(_cursorIndexOfCoverImagePath)) {
          _tmpCoverImagePath = null;
        } else {
          _tmpCoverImagePath = _cursor.getString(_cursorIndexOfCoverImagePath);
        }
        _result.setCoverImagePath(_tmpCoverImagePath);
        final long _tmpCreatedAt;
        _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
        _result.setCreatedAt(_tmpCreatedAt);
        final long _tmpUpdatedAt;
        _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
        _result.setUpdatedAt(_tmpUpdatedAt);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<AdDiary> searchDiaries(final int ownerId, final String query) {
    final String _sql = "SELECT * FROM diaries WHERE ownerId = ? AND diaryName LIKE '%' || ? || '%' ORDER BY updatedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, ownerId);
    _argIndex = 2;
    if (query == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, query);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfDiaryId = CursorUtil.getColumnIndexOrThrow(_cursor, "diaryId");
      final int _cursorIndexOfDiaryName = CursorUtil.getColumnIndexOrThrow(_cursor, "diaryName");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfOwnerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerId");
      final int _cursorIndexOfCoverColor = CursorUtil.getColumnIndexOrThrow(_cursor, "coverColor");
      final int _cursorIndexOfCoverImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "coverImagePath");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
      final List<AdDiary> _result = new ArrayList<AdDiary>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final AdDiary _item;
        final String _tmpDiaryName;
        if (_cursor.isNull(_cursorIndexOfDiaryName)) {
          _tmpDiaryName = null;
        } else {
          _tmpDiaryName = _cursor.getString(_cursorIndexOfDiaryName);
        }
        final String _tmpDescription;
        if (_cursor.isNull(_cursorIndexOfDescription)) {
          _tmpDescription = null;
        } else {
          _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
        }
        final int _tmpOwnerId;
        _tmpOwnerId = _cursor.getInt(_cursorIndexOfOwnerId);
        final String _tmpCoverColor;
        if (_cursor.isNull(_cursorIndexOfCoverColor)) {
          _tmpCoverColor = null;
        } else {
          _tmpCoverColor = _cursor.getString(_cursorIndexOfCoverColor);
        }
        _item = new AdDiary(_tmpDiaryName,_tmpDescription,_tmpOwnerId,_tmpCoverColor);
        final int _tmpDiaryId;
        _tmpDiaryId = _cursor.getInt(_cursorIndexOfDiaryId);
        _item.setDiaryId(_tmpDiaryId);
        final String _tmpCoverImagePath;
        if (_cursor.isNull(_cursorIndexOfCoverImagePath)) {
          _tmpCoverImagePath = null;
        } else {
          _tmpCoverImagePath = _cursor.getString(_cursorIndexOfCoverImagePath);
        }
        _item.setCoverImagePath(_tmpCoverImagePath);
        final long _tmpCreatedAt;
        _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
        _item.setCreatedAt(_tmpCreatedAt);
        final long _tmpUpdatedAt;
        _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
        _item.setUpdatedAt(_tmpUpdatedAt);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
