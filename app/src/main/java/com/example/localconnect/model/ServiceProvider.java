package com.example.localconnect.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "service_providers")
public class ServiceProvider {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String category;
    private String contact;
    private String address;
    private boolean isApproved;
    private boolean isAvailable;

    public ServiceProvider(String name, String category, String contact, String address, boolean isApproved) {
        this.name = name;
        this.category = category;
        this.contact = contact;
        this.address = address;
        this.isApproved = isApproved;
        this.isAvailable = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getContact() {
        return contact;
    }

    public String getAddress() {
        return address;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
