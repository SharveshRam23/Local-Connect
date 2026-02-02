package com.example.localconnect.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.localconnect.data.dao.IssueDao;
import com.example.localconnect.model.Issue;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

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

    public Issue getIssueById(int issueId) throws Exception {
        Callable<Issue> callable = () -> issueDao.getIssueById(issueId);
        Future<Issue> future = AppDatabase.databaseWriteExecutor.submit(callable);
        return future.get();
    }

    public LiveData<List<Issue>> getIssuesByArea(String area) {
        return issueDao.getIssuesByArea(area);
    }

    public LiveData<List<Issue>> getIssuesByAreaAndStatus(String area, String status) {
        return issueDao.getIssuesByAreaAndStatus(area, status);
    }
}
