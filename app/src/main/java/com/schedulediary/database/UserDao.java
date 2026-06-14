package com.schedulediary.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.schedulediary.model.User;

@Dao
public interface UserDao {

    @Insert
    long insertUser(User user);

    @Update
    void updateUser(User user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User findByEmail(String email);

    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash LIMIT 1")
    User login(String email, String passwordHash);

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    User findById(int userId);

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int countByEmail(String email);
}
