package com.example.localconnect.model;

public class Booking {
    public String id;
    public String userId;
    public String providerId;
    public String workType;
    public String date;
    public String details;
    public String status; // PENDING, ACCEPTED, REJECTED, COMPLETED

    // Required for Firestore
    public Booking() {}

    public Booking(String id, String userId, String providerId, String workType, String date, String details) {
        this.id = id;
        this.userId = userId;
        this.providerId = providerId;
        this.workType = workType;
        this.date = date;
        this.details = details;
        this.status = "PENDING";
    }

    public Booking(String userId, String providerId, String workType, String date, String details) {
        this(null, userId, providerId, workType, date, details);
    }
}
