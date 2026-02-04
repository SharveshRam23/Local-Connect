package com.example.localconnect.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "issues")
public class Issue {
    @PrimaryKey
    @NonNull
    public String id;
    public String description;
    public String imagePath;
    public String pincode;
    public long timestamp;
    public String status; // PENDING, IN_PROGRESS, RESOLVED
    public String adminResponse;
    public String reporterName;

    // Required for Firestore
    public Issue() {}

    public Issue(String id, String description, String imagePath, String pincode, long timestamp, String reporterName) {
        this.id = id;
        this.description = description;
        this.imagePath = imagePath;
        this.pincode = pincode;
        this.timestamp = timestamp;
        this.reporterName = reporterName;
        this.status = "PENDING";
    }

    @Ignore
    public Issue(String description, String imagePath, String pincode, long timestamp, String reporterName) {
        this(java.util.UUID.randomUUID().toString(), description, imagePath, pincode, timestamp, reporterName);
    }
}
