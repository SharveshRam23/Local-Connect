package com.example.localconnect.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "appointments")
public class Appointment {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public int providerId;
    public String userName;
    public String userPhone;
    public String date;
    public String time;
    public String issue;
    public String status; // PENDING, CONFIRMED, REJECTED
    public long timestamp;

    public Appointment(int userId, int providerId, String userName, String userPhone, String date, String time,
            String issue) {
        this.userId = userId;
        this.providerId = providerId;
        this.userName = userName;
        this.userPhone = userPhone;
        this.date = date;
        this.time = time;
        this.issue = issue;
        this.status = "PENDING";
        this.timestamp = System.currentTimeMillis();
    }
}
