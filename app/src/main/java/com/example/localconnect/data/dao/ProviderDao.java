package com.example.localconnect.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.localconnect.model.ServiceProvider;

import java.util.List;

@Dao
public interface ProviderDao {
    @Insert
    void insert(ServiceProvider provider);

    @Update
    void update(ServiceProvider provider);

    @Query("DELETE FROM service_providers")
    void deleteAll();

    @Query("SELECT * FROM service_providers WHERE isApproved = 1 AND isAvailable = 1")
    List<ServiceProvider> getAllApprovedProviders();

    @Query("SELECT * FROM service_providers WHERE isApproved = 1 AND isAvailable = 1 AND category = :category")
    List<ServiceProvider> getProvidersByCategory(String category);

    @Query("SELECT * FROM service_providers WHERE isApproved = 1 AND isAvailable = 1 AND address LIKE '%' || :pincode || '%'")
    List<ServiceProvider> getProvidersByPincode(String pincode);

    @Query("SELECT * FROM service_providers WHERE isApproved = 1 AND isAvailable = 1 AND category = :category AND address LIKE '%' || :pincode || '%'")
    List<ServiceProvider> getProvidersByCategoryAndPincode(String category, String pincode);

    @Query("SELECT * FROM service_providers WHERE isApproved = 0")
    List<ServiceProvider> getPendingProviders(); // For Admin
}
