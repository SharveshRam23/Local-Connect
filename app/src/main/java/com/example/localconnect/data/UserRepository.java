package com.example.localconnect.data;

import com.example.localconnect.data.dao.UserDao;
import com.example.localconnect.model.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;

public class UserRepository {

    private final UserDao userDao;

    @Inject
    public UserRepository(UserDao userDao) {
        this.userDao = userDao;
    }

    public void insert(User user) {
        AppDatabase.databaseWriteExecutor.execute(() -> userDao.insert(user));
    }

    public LiveData<User> getUser(String email, String password) {
        return userDao.getUser(email, password);
    }

    public CompletableFuture<User> findByEmail(String email) {
        return CompletableFuture.supplyAsync(() -> userDao.findByEmail(email), AppDatabase.databaseWriteExecutor);
    }

    public CompletableFuture<List<User>> getUsersInArea(String area) {
        return CompletableFuture.supplyAsync(() -> userDao.getUsersInArea(area), AppDatabase.databaseWriteExecutor);
    }
}
