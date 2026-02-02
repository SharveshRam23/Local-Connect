package com.example.localconnect.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.localconnect.model.ServiceProvider;

import java.util.List;

@Dao
public interface ProviderDao {
    @Insert
    void insert(ServiceProvider serviceProvider);

    @Update
    void update(ServiceProvider serviceProvider);

    @Query("SELECT * FROM service_providers WHERE isApproved = 1")
    LiveData<List<ServiceProvider>> getApprovedServiceProviders();

    @Query("SELECT * FROM service_providers WHERE isApproved = 0")
    LiveData<List<ServiceProvider>> getPendingServiceProviders();

    @Query("SELECT * FROM service_providers")
    LiveData<List<ServiceProvider>> getAllServiceProviders();
}
