package com.example.localconnect.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.localconnect.data.NoticeRepository;
import com.example.localconnect.model.Notice;

import java.util.List;

public class NoticeViewModel extends AndroidViewModel {

    private NoticeRepository mRepository;

    public NoticeViewModel (Application application) {
        super(application);
        mRepository = new NoticeRepository(application);
    }

    public LiveData<List<Notice>> getNoticesByArea(String area) { return mRepository.getNoticesByArea(area); }

    public void insert(Notice notice) { mRepository.insert(notice); }
}
