package com.example.localconnect.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notices")
public class Notice {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String description;
    private String type; // GLOBAL, AREA
    private String pincode; // For AREA type notices
    private long scheduledTime;

    public Notice(String title, String description, String type, String pincode, long scheduledTime) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.pincode = pincode;
        this.scheduledTime = scheduledTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getPincode() {
        return pincode;
    }

    public long getScheduledTime() {
        return scheduledTime;
    }
}
