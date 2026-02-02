package com.example.localconnect.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.localconnect.data.IssueRepository;
import com.example.localconnect.model.Issue;

import java.util.List;

public class IssueViewModel extends AndroidViewModel {

    private IssueRepository mRepository;

    private LiveData<List<Issue>> mAllIssues;

    public IssueViewModel (Application application) {
        super(application);
        mRepository = new IssueRepository(application);
        mAllIssues = mRepository.getIssuesByArea("");
    }

    public LiveData<List<Issue>> getIssuesByArea(String area) { return mRepository.getIssuesByArea(area); }

    public void insert(Issue issue) { mRepository.insert(issue); }
}
