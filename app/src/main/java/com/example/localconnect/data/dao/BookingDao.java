package com.example.localconnect.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.localconnect.model.Booking;

import java.util.List;

@Dao
public interface BookingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Booking booking);

    @Update
    void update(Booking booking);

    @Query("SELECT * FROM bookings WHERE userId = :userId")
    List<Booking> getBookingsForUser(String userId);

    @Query("SELECT * FROM bookings WHERE providerId = :providerId")
    List<Booking> getBookingsForProvider(String providerId);

    @Query("SELECT * FROM bookings WHERE providerId = :providerId AND status = :status ORDER BY id DESC")
    List<Booking> getBookingsByStatusForProvider(String providerId, String status);

    @Query("SELECT * FROM bookings WHERE providerId = :providerId AND status IN ('COMPLETED', 'DECLINED', 'CANCELLED') ORDER BY id DESC")
    List<Booking> getHistoryBookingsForProvider(String providerId);

    @Query("UPDATE bookings SET status = :status WHERE id = :id")
    void updateStatus(String id, String status);
}
