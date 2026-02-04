package com.example.localconnect.model;

public class Issue {
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

    public Issue(String description, String imagePath, String pincode, long timestamp, String reporterName) {
        this(null, description, imagePath, pincode, timestamp, reporterName);
    }
}
