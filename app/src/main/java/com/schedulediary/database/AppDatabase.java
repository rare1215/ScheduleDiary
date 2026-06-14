package com.schedulediary.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.schedulediary.model.AdDiary;
import com.schedulediary.model.AdPage;
import com.schedulediary.model.User;

@Database(entities = {User.class, AdDiary.class, AdPage.class},
        version = 1,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final String DB_NAME = "schedule_diary.db";

    public abstract UserDao userDao();
    public abstract DiaryDao diaryDao();
    public abstract PageDao pageDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DB_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
