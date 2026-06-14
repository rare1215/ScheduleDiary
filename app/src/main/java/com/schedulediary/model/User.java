package com.schedulediary.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "users",
        indices = {@Index(value = "email", unique = true)})
public class User {

    @PrimaryKey(autoGenerate = true)
    private int userId;

    @NonNull
    private String email;

    @NonNull
    private String passwordHash;  // SHA-256 hashed

    @NonNull
    private String name;

    private long createdAt;

    public User(@NonNull String email, @NonNull String passwordHash, @NonNull String name) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters & Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    @NonNull
    public String getEmail() { return email; }
    public void setEmail(@NonNull String email) { this.email = email; }

    @NonNull
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(@NonNull String passwordHash) { this.passwordHash = passwordHash; }

    @NonNull
    public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
