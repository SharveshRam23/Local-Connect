package com.example.localconnect.data;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.localconnect.data.dao.IssueDao;
import com.example.localconnect.model.Issue;

import java.util.List;

public class IssueRepository {

    private IssueDao issueDao;
    private LiveData<List<Issue>> allIssues;

    public IssueRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        issueDao = db.issueDao();
        allIssues = issueDao.getAllIssues();
    }

    public LiveData<List<Issue>> getAllIssues() {
        return allIssues;
    }

    public void insert(Issue issue) {
        new insertAsyncTask(issueDao).execute(issue);
    }

    private static class insertAsyncTask extends AsyncTask<Issue, Void, Void> {

        private IssueDao mAsyncTaskDao;

        insertAsyncTask(IssueDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Issue... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
