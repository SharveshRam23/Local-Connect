package com.example.localconnect.data;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.localconnect.data.dao.NoticeDao;
import com.example.localconnect.model.Notice;

import java.util.List;

public class NoticeRepository {

    private NoticeDao noticeDao;
    private LiveData<List<Notice>> allNotices;

    public NoticeRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        noticeDao = db.noticeDao();
    }

    public void insert(Notice notice) {
        new insertAsyncTask(noticeDao).execute(notice);
    }

    private static class insertAsyncTask extends AsyncTask<Notice, Void, Void> {

        private NoticeDao mAsyncTaskDao;

        insertAsyncTask(NoticeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Notice... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
