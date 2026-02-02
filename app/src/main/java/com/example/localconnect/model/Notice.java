package com.example.localconnect.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notices")
public class Notice {
    @PrimaryKey(autoGenerate = true)
    private int noticeId;
    private String title;
    private String message;
    private String area;
    private long timestamp;

    public Notice(String title, String message, String area, long timestamp) {
        this.title = title;
        this.message = message;
        this.area = area;
        this.timestamp = timestamp;
    }

    public int getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(int noticeId) {
        this.noticeId = noticeId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getArea() {
        return area;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
