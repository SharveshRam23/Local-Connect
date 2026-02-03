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
    public long approvalTime;

    public String password;
    public String experience;

    public ServiceProvider(String name, String category, String pincode, String phone, String password,
            String experience) {
        this.name = name;
        this.category = category;
        this.pincode = pincode;
        this.phone = phone;
        this.password = password;
        this.experience = experience;
        this.isApproved = false; // Default false
        this.isAvailable = true; // Default true
        this.approvalTime = 0;
    }
}
