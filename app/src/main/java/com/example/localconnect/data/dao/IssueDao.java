package com.example.localconnect.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.localconnect.model.Issue;

import java.util.List;

@Dao
public interface IssueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Issue issue);

    @Query("SELECT * FROM issues ORDER BY timestamp DESC")
    List<Issue> getAllIssues();

    @Query("SELECT * FROM issues WHERE pincode = :pincode ORDER BY timestamp DESC")
    List<Issue> getIssuesByPincode(String pincode);

    @Query("UPDATE issues SET status = :status, adminResponse = :response WHERE id = :id")
    void updateIssueStatus(String id, String status, String response);

    @Query("SELECT * FROM issues WHERE id = :id")
    Issue getIssueById(String id);
}
