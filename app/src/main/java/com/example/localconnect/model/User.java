package com.example.localconnect.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    public String id;
    public String name;
    public String phone;
    public String pincode;
    public String password;
    public String profileImageUrl;
    public String bio;

    // Required for Firestore
    public User() {}

    @Ignore
    public User(String id, String name, String phone, String pincode, String password) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.pincode = pincode;
        this.password = password;
    }

    @Ignore
    public User(String name, String phone, String pincode, String password) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.phone = phone;
        this.pincode = pincode;
        this.password = password;
    }
}
