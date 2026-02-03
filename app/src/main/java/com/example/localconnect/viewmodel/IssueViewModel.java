package com.example.localconnect.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.localconnect.data.IssueRepository;
import com.example.localconnect.model.Issue;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class IssueViewModel extends ViewModel {

    private final IssueRepository mRepository;

    @Inject
    public IssueViewModel(IssueRepository repository) {
        this.mRepository = repository;
    }

    public LiveData<List<Issue>> getIssuesByArea(String area) {
        return mRepository.getIssuesByArea(area);
    }

    public void insert(Issue issue) {
        mRepository.insert(issue);
    }
}
