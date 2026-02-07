package com.example.localconnect.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.localconnect.model.Notice;

import java.util.List;

@Dao
public interface NoticeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Notice notice);

    @Query("SELECT * FROM notices WHERE type = 'GLOBAL' OR (type = 'AREA' AND targetPincode = :pincode) ORDER BY scheduledTime DESC")
    List<Notice> getNoticesForUser(String pincode);

    @Query("SELECT * FROM notices ORDER BY scheduledTime DESC")
    List<Notice> getAllNotices(); // For Admin

    @Update
    void update(Notice notice);

    @Delete
    void delete(Notice notice);
}
