package com.example.localconnect.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String phone;
    public String pincode;

    public User(String name, String phone, String pincode) {
        this.name = name;
        this.phone = phone;
        this.pincode = pincode;
    }
}
