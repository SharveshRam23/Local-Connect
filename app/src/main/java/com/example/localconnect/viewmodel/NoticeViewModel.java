package com.example.localconnect.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.localconnect.data.NoticeRepository;
import com.example.localconnect.model.Notice;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NoticeViewModel extends ViewModel {

    private final NoticeRepository mRepository;

    @Inject
    public NoticeViewModel(NoticeRepository repository) {
        this.mRepository = repository;
    }

    public LiveData<List<Notice>> getNoticesByArea(String area) {
        return mRepository.getNoticesByArea(area);
    }

    public void insert(Notice notice) {
        mRepository.insert(notice);
    }
}
