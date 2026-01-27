package com.example.localconnect.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notices")
public class Notice {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String content;
    public String type; // "GLOBAL" or "AREA"
    public String targetPincode; // Null for global
    public long scheduledTime;

    public Notice(String title, String content, String type, String targetPincode, long scheduledTime) {
        this.title = title;
        this.content = content;
        this.type = type;
        this.targetPincode = targetPincode;
        this.scheduledTime = scheduledTime;
    }
}
