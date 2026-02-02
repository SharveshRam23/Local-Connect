package com.example.localconnect.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.localconnect.data.dao.NoticeDao;
import com.example.localconnect.model.Notice;

import java.util.List;

public class NoticeRepository {

    private NoticeDao noticeDao;

    public NoticeRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        noticeDao = db.noticeDao();
    }

    public void insert(Notice notice) {
        AppDatabase.databaseWriteExecutor.execute(() -> noticeDao.insert(notice));
    }

    public LiveData<List<Notice>> getNoticesByArea(String area) {
        return noticeDao.getNoticesByArea(area);
    }
}
