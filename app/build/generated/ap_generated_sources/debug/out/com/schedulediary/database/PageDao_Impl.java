package com.schedulediary.database;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.schedulediary.model.AdPage;
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
public final class PageDao_Impl implements PageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AdPage> __insertionAdapterOfAdPage;

  private final EntityDeletionOrUpdateAdapter<AdPage> __deletionAdapterOfAdPage;

  private final EntityDeletionOrUpdateAdapter<AdPage> __updateAdapterOfAdPage;

  private final SharedSQLiteStatement __preparedStmtOfShiftPagesDown;

  private final SharedSQLiteStatement __preparedStmtOfUpdatePageNumber;

  public PageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAdPage = new EntityInsertionAdapter<AdPage>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `pages` (`pageId`,`diaryId`,`title`,`textContent`,`pageNumber`,`backgroundStyle`,`elementsJson`,`createdAt`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final AdPage entity) {
        statement.bindLong(1, entity.getPageId());
        statement.bindLong(2, entity.getDiaryId());
        if (entity.getTitle() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getTitle());
        }
        if (entity.getTextContent() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getTextContent());
        }
        statement.bindLong(5, entity.getPageNumber());
        if (entity.getBackgroundStyle() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getBackgroundStyle());
        }
        if (entity.getElementsJson() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getElementsJson());
        }
        statement.bindLong(8, entity.getCreatedAt());
        statement.bindLong(9, entity.getUpdatedAt());
      }
    };
    this.__deletionAdapterOfAdPage = new EntityDeletionOrUpdateAdapter<AdPage>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `pages` WHERE `pageId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final AdPage entity) {
        statement.bindLong(1, entity.getPageId());
      }
    };
    this.__updateAdapterOfAdPage = new EntityDeletionOrUpdateAdapter<AdPage>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `pages` SET `pageId` = ?,`diaryId` = ?,`title` = ?,`textContent` = ?,`pageNumber` = ?,`backgroundStyle` = ?,`elementsJson` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `pageId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final AdPage entity) {
        statement.bindLong(1, entity.getPageId());
        statement.bindLong(2, entity.getDiaryId());
        if (entity.getTitle() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getTitle());
        }
        if (entity.getTextContent() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getTextContent());
        }
        statement.bindLong(5, entity.getPageNumber());
        if (entity.getBackgroundStyle() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getBackgroundStyle());
        }
        if (entity.getElementsJson() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getElementsJson());
        }
        statement.bindLong(8, entity.getCreatedAt());
        statement.bindLong(9, entity.getUpdatedAt());
        statement.bindLong(10, entity.getPageId());
      }
    };
    this.__preparedStmtOfShiftPagesDown = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE pages SET pageNumber = pageNumber + 1 WHERE diaryId = ? AND pageNumber >= ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdatePageNumber = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE pages SET pageNumber = ? WHERE pageId = ?";
        return _query;
      }
    };
  }

  @Override
  public long insertPage(final AdPage page) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfAdPage.insertAndReturnId(page);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deletePage(final AdPage page) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfAdPage.handle(page);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void updatePage(final AdPage page) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfAdPage.handle(page);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void shiftPagesDown(final int diaryId, final int fromPosition) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfShiftPagesDown.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, diaryId);
    _argIndex = 2;
    _stmt.bindLong(_argIndex, fromPosition);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfShiftPagesDown.release(_stmt);
    }
  }

  @Override
  public void updatePageNumber(final int pageId, final int pageNumber) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdatePageNumber.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, pageNumber);
    _argIndex = 2;
    _stmt.bindLong(_argIndex, pageId);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfUpdatePageNumber.release(_stmt);
    }
  }

  @Override
  public List<AdPage> getPagesByDiary(final int diaryId) {
    final String _sql = "SELECT * FROM pages WHERE diaryId = ? ORDER BY pageNumber ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, diaryId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfPageId = CursorUtil.getColumnIndexOrThrow(_cursor, "pageId");
      final int _cursorIndexOfDiaryId = CursorUtil.getColumnIndexOrThrow(_cursor, "diaryId");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfTextContent = CursorUtil.getColumnIndexOrThrow(_cursor, "textContent");
      final int _cursorIndexOfPageNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "pageNumber");
      final int _cursorIndexOfBackgroundStyle = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundStyle");
      final int _cursorIndexOfElementsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "elementsJson");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
      final List<AdPage> _result = new ArrayList<AdPage>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final AdPage _item;
        final int _tmpDiaryId;
        _tmpDiaryId = _cursor.getInt(_cursorIndexOfDiaryId);
        final String _tmpTitle;
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _tmpTitle = null;
        } else {
          _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
        }
        final int _tmpPageNumber;
        _tmpPageNumber = _cursor.getInt(_cursorIndexOfPageNumber);
        _item = new AdPage(_tmpDiaryId,_tmpTitle,_tmpPageNumber);
        final int _tmpPageId;
        _tmpPageId = _cursor.getInt(_cursorIndexOfPageId);
        _item.setPageId(_tmpPageId);
        final String _tmpTextContent;
        if (_cursor.isNull(_cursorIndexOfTextContent)) {
          _tmpTextContent = null;
        } else {
          _tmpTextContent = _cursor.getString(_cursorIndexOfTextContent);
        }
        _item.setTextContent(_tmpTextContent);
        final String _tmpBackgroundStyle;
        if (_cursor.isNull(_cursorIndexOfBackgroundStyle)) {
          _tmpBackgroundStyle = null;
        } else {
          _tmpBackgroundStyle = _cursor.getString(_cursorIndexOfBackgroundStyle);
        }
        _item.setBackgroundStyle(_tmpBackgroundStyle);
        final String _tmpElementsJson;
        if (_cursor.isNull(_cursorIndexOfElementsJson)) {
          _tmpElementsJson = null;
        } else {
          _tmpElementsJson = _cursor.getString(_cursorIndexOfElementsJson);
        }
        _item.setElementsJson(_tmpElementsJson);
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
  public AdPage getPageById(final int pageId) {
    final String _sql = "SELECT * FROM pages WHERE pageId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, pageId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfPageId = CursorUtil.getColumnIndexOrThrow(_cursor, "pageId");
      final int _cursorIndexOfDiaryId = CursorUtil.getColumnIndexOrThrow(_cursor, "diaryId");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfTextContent = CursorUtil.getColumnIndexOrThrow(_cursor, "textContent");
      final int _cursorIndexOfPageNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "pageNumber");
      final int _cursorIndexOfBackgroundStyle = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundStyle");
      final int _cursorIndexOfElementsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "elementsJson");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
      final AdPage _result;
      if (_cursor.moveToFirst()) {
        final int _tmpDiaryId;
        _tmpDiaryId = _cursor.getInt(_cursorIndexOfDiaryId);
        final String _tmpTitle;
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _tmpTitle = null;
        } else {
          _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
        }
        final int _tmpPageNumber;
        _tmpPageNumber = _cursor.getInt(_cursorIndexOfPageNumber);
        _result = new AdPage(_tmpDiaryId,_tmpTitle,_tmpPageNumber);
        final int _tmpPageId;
        _tmpPageId = _cursor.getInt(_cursorIndexOfPageId);
        _result.setPageId(_tmpPageId);
        final String _tmpTextContent;
        if (_cursor.isNull(_cursorIndexOfTextContent)) {
          _tmpTextContent = null;
        } else {
          _tmpTextContent = _cursor.getString(_cursorIndexOfTextContent);
        }
        _result.setTextContent(_tmpTextContent);
        final String _tmpBackgroundStyle;
        if (_cursor.isNull(_cursorIndexOfBackgroundStyle)) {
          _tmpBackgroundStyle = null;
        } else {
          _tmpBackgroundStyle = _cursor.getString(_cursorIndexOfBackgroundStyle);
        }
        _result.setBackgroundStyle(_tmpBackgroundStyle);
        final String _tmpElementsJson;
        if (_cursor.isNull(_cursorIndexOfElementsJson)) {
          _tmpElementsJson = null;
        } else {
          _tmpElementsJson = _cursor.getString(_cursorIndexOfElementsJson);
        }
        _result.setElementsJson(_tmpElementsJson);
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
  public int countPagesByDiary(final int diaryId) {
    final String _sql = "SELECT COUNT(*) FROM pages WHERE diaryId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, diaryId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<AdPage> searchPages(final int diaryId, final String query) {
    final String _sql = "SELECT * FROM pages WHERE diaryId = ? AND title LIKE '%' || ? || '%' ORDER BY pageNumber ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, diaryId);
    _argIndex = 2;
    if (query == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, query);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfPageId = CursorUtil.getColumnIndexOrThrow(_cursor, "pageId");
      final int _cursorIndexOfDiaryId = CursorUtil.getColumnIndexOrThrow(_cursor, "diaryId");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfTextContent = CursorUtil.getColumnIndexOrThrow(_cursor, "textContent");
      final int _cursorIndexOfPageNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "pageNumber");
      final int _cursorIndexOfBackgroundStyle = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundStyle");
      final int _cursorIndexOfElementsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "elementsJson");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
      final List<AdPage> _result = new ArrayList<AdPage>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final AdPage _item;
        final int _tmpDiaryId;
        _tmpDiaryId = _cursor.getInt(_cursorIndexOfDiaryId);
        final String _tmpTitle;
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _tmpTitle = null;
        } else {
          _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
        }
        final int _tmpPageNumber;
        _tmpPageNumber = _cursor.getInt(_cursorIndexOfPageNumber);
        _item = new AdPage(_tmpDiaryId,_tmpTitle,_tmpPageNumber);
        final int _tmpPageId;
        _tmpPageId = _cursor.getInt(_cursorIndexOfPageId);
        _item.setPageId(_tmpPageId);
        final String _tmpTextContent;
        if (_cursor.isNull(_cursorIndexOfTextContent)) {
          _tmpTextContent = null;
        } else {
          _tmpTextContent = _cursor.getString(_cursorIndexOfTextContent);
        }
        _item.setTextContent(_tmpTextContent);
        final String _tmpBackgroundStyle;
        if (_cursor.isNull(_cursorIndexOfBackgroundStyle)) {
          _tmpBackgroundStyle = null;
        } else {
          _tmpBackgroundStyle = _cursor.getString(_cursorIndexOfBackgroundStyle);
        }
        _item.setBackgroundStyle(_tmpBackgroundStyle);
        final String _tmpElementsJson;
        if (_cursor.isNull(_cursorIndexOfElementsJson)) {
          _tmpElementsJson = null;
        } else {
          _tmpElementsJson = _cursor.getString(_cursorIndexOfElementsJson);
        }
        _item.setElementsJson(_tmpElementsJson);
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
  public int getMaxPageNumber(final int diaryId) {
    final String _sql = "SELECT MAX(pageNumber) FROM pages WHERE diaryId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, diaryId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
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
