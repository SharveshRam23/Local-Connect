package com.example.localconnect.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "bookings")
public class Booking {
    @PrimaryKey
    @NonNull
    public String id;
    public String userId;
    public String providerId;
    public String workType;
    public String date;
    public String details;
    public String status; // PENDING, ACCEPTED, REJECTED, COMPLETED
    public double latitude;
    public double longitude;
    public String address;

    // Required for Firestore
    public Booking() {}

    @Ignore
    public Booking(String id, String userId, String providerId, String workType, String date, String details) {
        this.id = id;
        this.userId = userId;
        this.providerId = providerId;
        this.workType = workType;
        this.date = date;
        this.details = details;
        this.status = "PENDING";
    }

    @Ignore
    public Booking(String userId, String providerId, String workType, String date, String details) {
        this(java.util.UUID.randomUUID().toString(), userId, providerId, workType, date, details);
    }
}
