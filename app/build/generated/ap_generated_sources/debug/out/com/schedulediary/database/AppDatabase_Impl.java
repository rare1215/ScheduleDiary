package com.schedulediary.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile UserDao _userDao;

  private volatile DiaryDao _diaryDao;

  private volatile PageDao _pageDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`userId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `email` TEXT NOT NULL, `passwordHash` TEXT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_email` ON `users` (`email`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `diaries` (`diaryId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `diaryName` TEXT NOT NULL, `description` TEXT, `ownerId` INTEGER NOT NULL, `coverColor` TEXT, `coverImagePath` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, FOREIGN KEY(`ownerId`) REFERENCES `users`(`userId`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_diaries_ownerId` ON `diaries` (`ownerId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `pages` (`pageId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `diaryId` INTEGER NOT NULL, `title` TEXT NOT NULL, `textContent` TEXT, `pageNumber` INTEGER NOT NULL, `backgroundStyle` TEXT, `elementsJson` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, FOREIGN KEY(`diaryId`) REFERENCES `diaries`(`diaryId`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pages_diaryId` ON `pages` (`diaryId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'd0bf196f8bc5136ee88e3f7bc7c239da')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `users`");
        db.execSQL("DROP TABLE IF EXISTS `diaries`");
        db.execSQL("DROP TABLE IF EXISTS `pages`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsUsers = new HashMap<String, TableInfo.Column>(5);
        _columnsUsers.put("userId", new TableInfo.Column("userId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("email", new TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("passwordHash", new TableInfo.Column("passwordHash", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsers = new HashSet<TableInfo.Index>(1);
        _indicesUsers.add(new TableInfo.Index("index_users_email", true, Arrays.asList("email"), Arrays.asList("ASC")));
        final TableInfo _infoUsers = new TableInfo("users", _columnsUsers, _foreignKeysUsers, _indicesUsers);
        final TableInfo _existingUsers = TableInfo.read(db, "users");
        if (!_infoUsers.equals(_existingUsers)) {
          return new RoomOpenHelper.ValidationResult(false, "users(com.schedulediary.model.User).\n"
                  + " Expected:\n" + _infoUsers + "\n"
                  + " Found:\n" + _existingUsers);
        }
        final HashMap<String, TableInfo.Column> _columnsDiaries = new HashMap<String, TableInfo.Column>(8);
        _columnsDiaries.put("diaryId", new TableInfo.Column("diaryId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiaries.put("diaryName", new TableInfo.Column("diaryName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiaries.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiaries.put("ownerId", new TableInfo.Column("ownerId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiaries.put("coverColor", new TableInfo.Column("coverColor", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiaries.put("coverImagePath", new TableInfo.Column("coverImagePath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiaries.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiaries.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDiaries = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysDiaries.add(new TableInfo.ForeignKey("users", "CASCADE", "NO ACTION", Arrays.asList("ownerId"), Arrays.asList("userId")));
        final HashSet<TableInfo.Index> _indicesDiaries = new HashSet<TableInfo.Index>(1);
        _indicesDiaries.add(new TableInfo.Index("index_diaries_ownerId", false, Arrays.asList("ownerId"), Arrays.asList("ASC")));
        final TableInfo _infoDiaries = new TableInfo("diaries", _columnsDiaries, _foreignKeysDiaries, _indicesDiaries);
        final TableInfo _existingDiaries = TableInfo.read(db, "diaries");
        if (!_infoDiaries.equals(_existingDiaries)) {
          return new RoomOpenHelper.ValidationResult(false, "diaries(com.schedulediary.model.AdDiary).\n"
                  + " Expected:\n" + _infoDiaries + "\n"
                  + " Found:\n" + _existingDiaries);
        }
        final HashMap<String, TableInfo.Column> _columnsPages = new HashMap<String, TableInfo.Column>(9);
        _columnsPages.put("pageId", new TableInfo.Column("pageId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPages.put("diaryId", new TableInfo.Column("diaryId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPages.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPages.put("textContent", new TableInfo.Column("textContent", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPages.put("pageNumber", new TableInfo.Column("pageNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPages.put("backgroundStyle", new TableInfo.Column("backgroundStyle", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPages.put("elementsJson", new TableInfo.Column("elementsJson", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPages.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPages.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPages = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysPages.add(new TableInfo.ForeignKey("diaries", "CASCADE", "NO ACTION", Arrays.asList("diaryId"), Arrays.asList("diaryId")));
        final HashSet<TableInfo.Index> _indicesPages = new HashSet<TableInfo.Index>(1);
        _indicesPages.add(new TableInfo.Index("index_pages_diaryId", false, Arrays.asList("diaryId"), Arrays.asList("ASC")));
        final TableInfo _infoPages = new TableInfo("pages", _columnsPages, _foreignKeysPages, _indicesPages);
        final TableInfo _existingPages = TableInfo.read(db, "pages");
        if (!_infoPages.equals(_existingPages)) {
          return new RoomOpenHelper.ValidationResult(false, "pages(com.schedulediary.model.AdPage).\n"
                  + " Expected:\n" + _infoPages + "\n"
                  + " Found:\n" + _existingPages);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "d0bf196f8bc5136ee88e3f7bc7c239da", "ef802285580a9ad85a9bdfd9fadad767");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "users","diaries","pages");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `users`");
      _db.execSQL("DELETE FROM `diaries`");
      _db.execSQL("DELETE FROM `pages`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(UserDao.class, UserDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DiaryDao.class, DiaryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PageDao.class, PageDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public UserDao userDao() {
    if (_userDao != null) {
      return _userDao;
    } else {
      synchronized(this) {
        if(_userDao == null) {
          _userDao = new UserDao_Impl(this);
        }
        return _userDao;
      }
    }
  }

  @Override
  public DiaryDao diaryDao() {
    if (_diaryDao != null) {
      return _diaryDao;
    } else {
      synchronized(this) {
        if(_diaryDao == null) {
          _diaryDao = new DiaryDao_Impl(this);
        }
        return _diaryDao;
      }
    }
  }

  @Override
  public PageDao pageDao() {
    if (_pageDao != null) {
      return _pageDao;
    } else {
      synchronized(this) {
        if(_pageDao == null) {
          _pageDao = new PageDao_Impl(this);
        }
        return _pageDao;
      }
    }
  }
}
