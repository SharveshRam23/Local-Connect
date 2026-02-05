package com.example.localconnect.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.localconnect.model.Comment;

import java.util.List;

@Dao
public interface CommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Comment comment);

    @Query("SELECT * FROM comments WHERE noticeId = :noticeId ORDER BY timestamp ASC")
    List<Comment> getCommentsForNotice(String noticeId);
}
