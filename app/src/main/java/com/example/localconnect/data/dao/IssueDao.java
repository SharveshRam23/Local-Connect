package com.example.localconnect.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.localconnect.model.Issue;

import java.util.List;

@Dao
public interface IssueDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Issue issue);

    @Update
    void update(Issue issue);

    @Delete
    void delete(Issue issue);

    @Query("SELECT * FROM issues WHERE issueId = :issueId")
    LiveData<Issue> getIssueById(int issueId);

    @Query("SELECT * FROM issues WHERE area = :area ORDER BY timestamp DESC")
    LiveData<List<Issue>> getIssuesByArea(String area);

    @Query("SELECT * FROM issues WHERE area = :area AND status = :status ORDER BY timestamp DESC")
    LiveData<List<Issue>> getIssuesByAreaAndStatus(String area, String status);
}
