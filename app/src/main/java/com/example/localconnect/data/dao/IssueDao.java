package com.example.localconnect.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.localconnect.model.Issue;

import java.util.List;

@Dao
public interface IssueDao {
    @Insert
    void insert(Issue issue);

    @Query("SELECT * FROM issues ORDER BY timestamp DESC")
    List<Issue> getAllIssues();
}
