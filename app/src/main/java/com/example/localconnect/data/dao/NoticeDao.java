package com.example.localconnect.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.localconnect.model.Notice;

import java.util.List;

@Dao
public interface NoticeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Notice notice);

    @Query("SELECT * FROM notices WHERE area = :area ORDER BY timestamp DESC")
    LiveData<List<Notice>> getNoticesByArea(String area);
}
