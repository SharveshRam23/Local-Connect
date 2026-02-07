package com.example.localconnect.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "mandatory_services")
public class MandatoryService {
    @PrimaryKey
    @NonNull
    public String id;
    public String name;
    public String category; // HOSPITAL, PHARMACY, POLICE, AMBULANCE, FIRE, BLOODBANK, OTHER
    public String address;
    public String pincode;
    public double latitude;
    public double longitude;
    public String phonePrimary;
    public String phoneSecondary;
    public String workingHours;
    public boolean is24x7;
    public boolean isEmergency;
    public boolean isApproved; // Default true for admin-added
    public String imageUrl; // Base64 or URL
    public long lastUpdated;

    public MandatoryService() {
        // Required for Firestore/Room
    }

    @androidx.room.Ignore
    public MandatoryService(@NonNull String id, String name, String category, String address, String pincode, 
                            double latitude, double longitude, String phonePrimary, String phoneSecondary, 
                            String workingHours, boolean is24x7, boolean isEmergency, boolean isApproved, 
                            String imageUrl, long lastUpdated) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.address = address;
        this.pincode = pincode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phonePrimary = phonePrimary;
        this.phoneSecondary = phoneSecondary;
        this.workingHours = workingHours;
        this.is24x7 = is24x7;
        this.isEmergency = isEmergency;
        this.isApproved = isApproved;
        this.imageUrl = imageUrl;
        this.lastUpdated = lastUpdated;
    }
}
