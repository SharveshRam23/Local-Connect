package com.example.localconnect.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.localconnect.data.dao.IssueDao;
import com.example.localconnect.model.Issue;

import java.util.List;

public class IssueRepository {

    private IssueDao issueDao;

    public IssueRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        issueDao = db.issueDao();
    }

    public void insert(Issue issue) {
        AppDatabase.databaseWriteExecutor.execute(() -> issueDao.insert(issue));
    }

    public void update(Issue issue) {
        AppDatabase.databaseWriteExecutor.execute(() -> issueDao.update(issue));
    }

    public void delete(Issue issue) {
        AppDatabase.databaseWriteExecutor.execute(() -> issueDao.delete(issue));
    }

    public LiveData<Issue> getIssueById(int issueId) {
        return issueDao.getIssueById(issueId);
    }

    public LiveData<List<Issue>> getIssuesByArea(String area) {
        return issueDao.getIssuesByArea(area);
    }

    public LiveData<List<Issue>> getIssuesByAreaAndStatus(String area, String status) {
        return issueDao.getIssuesByAreaAndStatus(area, status);
    }
}
