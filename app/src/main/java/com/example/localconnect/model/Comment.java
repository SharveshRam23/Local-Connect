package com.example.localconnect.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "comments")
public class Comment {
    @PrimaryKey
    @NonNull
    public String id;
    public String noticeId;
    public String userId;
    public String userName;
    public String content;
    public long timestamp;

    // Required for Firestore
    public Comment() {}

    @Ignore
    public Comment(String id, String noticeId, String userId, String userName, String content, long timestamp) {
        this.id = id;
        this.noticeId = noticeId;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.timestamp = timestamp;
    }

    @Ignore
    public Comment(String noticeId, String userId, String userName, String content, long timestamp) {
        this(java.util.UUID.randomUUID().toString(), noticeId, userId, userName, content, timestamp);
    }
}
