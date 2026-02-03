package com.example.localconnect.data;

import com.example.localconnect.data.dao.IssueDao;
import com.example.localconnect.model.Issue;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;

public class IssueRepository {

    private final IssueDao issueDao;

    @Inject
    public IssueRepository(IssueDao issueDao) {
        this.issueDao = issueDao;
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

    public CompletableFuture<Issue> getIssueById(int issueId) {
        return CompletableFuture.supplyAsync(() -> issueDao.getIssueById(issueId), AppDatabase.databaseWriteExecutor);
    }

    public LiveData<List<Issue>> getIssuesByArea(String area) {
        return issueDao.getIssuesByArea(area);
    }

    public LiveData<List<Issue>> getIssuesByAreaAndStatus(String area, String status) {
        return issueDao.getIssuesByAreaAndStatus(area, status);
    }
}
