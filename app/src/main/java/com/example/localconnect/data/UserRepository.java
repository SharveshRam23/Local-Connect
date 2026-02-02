package com.example.localconnect.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.localconnect.data.dao.UserDao;
import com.example.localconnect.model.User;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class UserRepository {

    private UserDao userDao;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
    }

    public void insert(User user) {
        AppDatabase.databaseWriteExecutor.execute(() -> userDao.insert(user));
    }

    public LiveData<User> getUser(String email, String password) {
        return userDao.getUser(email, password);
    }

    public User findByEmail(String email) throws ExecutionException, InterruptedException {
        Callable<User> callable = () -> userDao.findByEmail(email);
        Future<User> future = AppDatabase.databaseWriteExecutor.submit(callable);
        return future.get();
    }

    public List<User> getUsersInArea(String area) throws ExecutionException, InterruptedException {
        Callable<List<User>> callable = () -> userDao.getUsersInArea(area);
        Future<List<User>> future = AppDatabase.databaseWriteExecutor.submit(callable);
        return future.get();
    }
}
