package com.example.localconnect.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "service_providers")
public class ServiceProvider {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String category;
    public String pincode;
    public String phone;
    public boolean isApproved;
    public boolean isAvailable;

    public ServiceProvider(String name, String category, String pincode, String phone) {
        this.name = name;
        this.category = category;
        this.pincode = pincode;
        this.phone = phone;
        this.isApproved = false; // Default false
        this.isAvailable = true; // Default true
    }
}
