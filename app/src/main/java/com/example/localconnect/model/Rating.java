package com.example.localconnect.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "ratings")
public class Rating {
    @PrimaryKey
    @NonNull
    public String id;
    public String userId;
    public String providerId;
    public String bookingId; // Optional: link to specific booking
    public float rating; // 1-5 stars
    public String review; // Optional text review
    public long timestamp;
    public String userName; // For display purposes

    // Required for Firestore/Room
    public Rating() {}

    @Ignore
    public Rating(String id, String userId, String providerId, String bookingId, float rating, String review, long timestamp, String userName) {
        this.id = id;
        this.userId = userId;
        this.providerId = providerId;
        this.bookingId = bookingId;
        this.rating = rating;
        this.review = review;
        this.timestamp = timestamp;
        this.userName = userName;
    }

    @Ignore
    public Rating(String userId, String providerId, String bookingId, float rating, String review, String userName) {
        this(java.util.UUID.randomUUID().toString(), userId, providerId, bookingId, rating, review, System.currentTimeMillis(), userName);
    }
}
