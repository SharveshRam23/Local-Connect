package com.example.localconnect.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "notices")
public class Notice {
    @PrimaryKey
    @NonNull
    public String id;
    public String title;
    public String content;
    public String type; // "GLOBAL" or "AREA"
    public String targetPincode; // Null for global
    public long scheduledTime;

    // Required for Firestore
    public Notice() {}

    @Ignore
    public Notice(String id, String title, String content, String type, String targetPincode, long scheduledTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.type = type;
        this.targetPincode = targetPincode;
        this.scheduledTime = scheduledTime;
    }

    @Ignore
    public Notice(String title, String content, String type, String targetPincode, long scheduledTime) {
        this(java.util.UUID.randomUUID().toString(), title, content, type, targetPincode, scheduledTime);
    }
}
