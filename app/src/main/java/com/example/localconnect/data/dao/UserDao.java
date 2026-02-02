package com.example.localconnect.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.localconnect.model.User;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    LiveData<User> getUser(String email, String password);

    @Query("DELETE FROM users")
    void deleteAll();
}
