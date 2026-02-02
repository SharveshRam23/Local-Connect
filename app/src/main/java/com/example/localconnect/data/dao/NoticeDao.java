package com.example.localconnect.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.localconnect.model.Notice;

import java.util.List;

@Dao
public interface NoticeDao {
    @Insert
    void insert(Notice notice);

    @Query("SELECT * FROM notices WHERE type = 'GLOBAL' OR (type = 'AREA' AND pincode = :pincode) ORDER BY scheduledTime DESC")
    List<Notice> getNoticesForUser(String pincode);

    @Query("SELECT * FROM notices ORDER BY scheduledTime DESC")
    List<Notice> getAllNotices(); // For Admin

    @Query("DELETE FROM notices")
    void deleteAll();
}
