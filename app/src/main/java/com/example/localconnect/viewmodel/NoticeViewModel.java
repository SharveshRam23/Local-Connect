package com.example.localconnect.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import com.example.localconnect.data.NoticeRepository;
import com.example.localconnect.model.Notice;

public class NoticeViewModel extends AndroidViewModel {

    private NoticeRepository mRepository;

    public NoticeViewModel (Application application) {
        super(application);
        mRepository = new NoticeRepository(application);
    }

    public void insert(Notice notice) { mRepository.insert(notice); }
}
