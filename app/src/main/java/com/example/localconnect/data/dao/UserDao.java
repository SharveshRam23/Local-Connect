package com.example.localconnect.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.localconnect.model.User;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Query("SELECT * FROM users WHERE phone = :phone AND password = :password LIMIT 1")
    User login(String phone, String password);

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    User getUserByPhone(String phone);

    @Query("SELECT * FROM users LIMIT 1")
    User getAnyUser(); // To check if user is logged in/registered

    @Query("SELECT phone FROM users")
    java.util.List<String> getAllUserPhones();

    @Query("SELECT * FROM users")
    java.util.List<User> getAllUsers();
}
