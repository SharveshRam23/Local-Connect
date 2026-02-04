package com.example.localconnect.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.localconnect.model.Booking;

import java.util.List;

@Dao
public interface BookingDao {
    @Insert
    void insert(Booking booking);

    @Update
    void update(Booking booking);

    @Query("SELECT * FROM bookings WHERE userId = :userId")
    List<Booking> getBookingsForUser(String userId);

    @Query("SELECT * FROM bookings WHERE providerId = :providerId")
    List<Booking> getBookingsForProvider(String providerId);

    @Query("UPDATE bookings SET status = :status WHERE id = :id")
    void updateStatus(String id, String status);
}
