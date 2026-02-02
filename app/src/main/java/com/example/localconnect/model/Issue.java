package com.example.localconnect.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "issues")
public class Issue {
    @PrimaryKey(autoGenerate = true)
    private int issueId;
    private String title;
    private String description;
    private String area;
    private String status;
    private long timestamp;

    public Issue(String title, String description, String area, String status, long timestamp) {
        this.title = title;
        this.description = description;
        this.area = area;
        this.status = status;
        this.timestamp = timestamp;
    }

    public int getIssueId() {
        return issueId;
    }

    public void setIssueId(int issueId) {
        this.issueId = issueId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getArea() {
        return area;
    }

    public String getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
