package com.example.localconnect.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.localconnect.data.UserRepository;
import com.example.localconnect.model.User;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class UserViewModel extends ViewModel {

    private final UserRepository mRepository;

    @Inject
    public UserViewModel(UserRepository repository) {
        this.mRepository = repository;
    }

    public void insert(User user) {
        mRepository.insert(user);
    }

    public LiveData<User> getUser(String email, String password) {
        return mRepository.getUser(email, password);
    }
}
