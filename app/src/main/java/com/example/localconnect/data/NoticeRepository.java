package com.example.localconnect.data;

import com.example.localconnect.data.dao.NoticeDao;
import com.example.localconnect.model.Notice;

import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;

public class NoticeRepository {

    private final NoticeDao noticeDao;

    @Inject
    public NoticeRepository(NoticeDao noticeDao) {
        this.noticeDao = noticeDao;
    }

    public void insert(Notice notice) {
        AppDatabase.databaseWriteExecutor.execute(() -> noticeDao.insert(notice));
    }

    public LiveData<List<Notice>> getNoticesByArea(String area) {
        return noticeDao.getNoticesByArea(area);
    }
}
