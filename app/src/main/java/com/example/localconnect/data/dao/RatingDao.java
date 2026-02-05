package com.example.localconnect.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.localconnect.model.Rating;

import java.util.List;

@Dao
public interface RatingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Rating rating);

    @Query("SELECT * FROM ratings WHERE providerId = :providerId ORDER BY timestamp DESC")
    List<Rating> getRatingsForProvider(String providerId);

    @Query("SELECT * FROM ratings WHERE userId = :userId ORDER BY timestamp DESC")
    List<Rating> getRatingsByUser(String userId);

    @Query("SELECT AVG(rating) FROM ratings WHERE providerId = :providerId")
    Float getAverageRating(String providerId);

    @Query("SELECT COUNT(*) FROM ratings WHERE providerId = :providerId")
    int getRatingCount(String providerId);

    @Query("SELECT * FROM ratings WHERE bookingId = :bookingId LIMIT 1")
    Rating getRatingForBooking(String bookingId);

    @Query("DELETE FROM ratings WHERE id = :id")
    void deleteRating(String id);
}
