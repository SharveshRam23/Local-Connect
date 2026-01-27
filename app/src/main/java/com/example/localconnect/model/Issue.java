package com.example.localconnect.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "issues")
public class Issue {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String description;
    public String imagePath;
    public String pincode;
    public long timestamp;

    public Issue(String description, String imagePath, String pincode, long timestamp) {
        this.description = description;
        this.imagePath = imagePath;
        this.pincode = pincode;
        this.timestamp = timestamp;
    }
}
