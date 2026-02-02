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

    public String password;

    public ServiceProvider(String name, String category, String pincode, String phone, String password) {
        this.name = name;
        this.category = category;
        this.pincode = pincode;
        this.phone = phone;
        this.password = password;
        this.isApproved = false; // Default false
        this.isAvailable = true; // Default true
    }
}
