package com.example.localconnect.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import com.example.localconnect.data.UserRepository;
import com.example.localconnect.model.User;

public class UserViewModel extends AndroidViewModel {

    private UserRepository mRepository;

    public UserViewModel (Application application) {
        super(application);
        mRepository = new UserRepository(application);
    }

    public void insert(User user) {
        mRepository.insert(user);
    }

    public User getUser(String email, String password) {
        return mRepository.getUser(email, password);
    }
}
