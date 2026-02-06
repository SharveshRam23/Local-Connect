package com.example.localconnect.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "service_providers")
public class ServiceProvider {
    @PrimaryKey
    @NonNull
    public String id;
    public String name;
    public String category;
    public String pincode;
    public String phone;
    public boolean isApproved;
    public boolean isAvailable;
    public long approvalTime;

    public String password;
    public String experience;

    public String availableFrom;
    public String availableTo;
    public String profileImageUrl;
    public String bio;
    public String address;

    // Rating fields
    public float rating; // Average rating (0-5)
    public int ratingCount; // Number of ratings

    // Required for Firestore
    public ServiceProvider() {}

    @Ignore
    public ServiceProvider(String id, String name, String category, String pincode, String phone, String password,
            String experience) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.pincode = pincode;
        this.phone = phone;
        this.password = password;
        this.experience = experience;
        this.isApproved = false; // Default false
        this.isAvailable = true; // Default true
        this.approvalTime = 0;
        this.availableFrom = "09:00"; // Default
        this.availableTo = "18:00"; // Default
        this.rating = 0.0f; // Default no rating
        this.ratingCount = 0; // Default no ratings
    }

    @Ignore
    public ServiceProvider(String name, String category, String pincode, String phone, String password,
            String experience) {
        this(java.util.UUID.randomUUID().toString(), name, category, pincode, phone, password, experience);
    }
}
