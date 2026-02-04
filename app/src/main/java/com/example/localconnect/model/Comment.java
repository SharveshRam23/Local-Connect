package com.example.localconnect.model;

public class Comment {
    public String id;
    public String noticeId;
    public String userId;
    public String userName;
    public String content;
    public long timestamp;

    // Required for Firestore
    public Comment() {}

    public Comment(String id, String noticeId, String userId, String userName, String content, long timestamp) {
        this.id = id;
        this.noticeId = noticeId;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Comment(String noticeId, String userId, String userName, String content, long timestamp) {
        this(null, noticeId, userId, userName, content, timestamp);
    }
}
