package com.example.localconnect.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.localconnect.model.Appointment;

import java.util.List;

@Dao
public interface AppointmentDao {
    @Insert
    void insert(Appointment appointment);

    @Update
    void update(Appointment appointment);

    @Query("SELECT * FROM appointments WHERE providerId = :providerId ORDER BY timestamp DESC")
    List<Appointment> getAppointmentsForProvider(int providerId);

    @Query("SELECT * FROM appointments WHERE userId = :userId ORDER BY timestamp DESC")
    List<Appointment> getAppointmentsForUser(int userId);

    @Query("UPDATE appointments SET status = :status WHERE id = :id")
    void updateStatus(int id, String status);
}
