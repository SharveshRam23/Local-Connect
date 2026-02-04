package com.example.localconnect.model;

public class Notice {
    public String id;
    public String title;
    public String content;
    public String type; // "GLOBAL" or "AREA"
    public String targetPincode; // Null for global
    public long scheduledTime;

    // Required for Firestore
    public Notice() {}

    public Notice(String id, String title, String content, String type, String targetPincode, long scheduledTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.type = type;
        this.targetPincode = targetPincode;
        this.scheduledTime = scheduledTime;
    }

    public Notice(String title, String content, String type, String targetPincode, long scheduledTime) {
        this(null, title, content, type, targetPincode, scheduledTime);
    }
}
