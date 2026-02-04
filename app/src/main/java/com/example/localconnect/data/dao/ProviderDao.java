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

    @androidx.room.Delete
    void delete(ServiceProvider provider);

    @Query("SELECT * FROM service_providers WHERE isApproved = 1 AND isAvailable = 1")
    List<ServiceProvider> getAllApprovedProviders();

    @Query("SELECT * FROM service_providers WHERE isApproved = 1 AND isAvailable = 1 AND category = :category")
    List<ServiceProvider> getProvidersByCategory(String category);

    @Query("SELECT * FROM service_providers WHERE isApproved = 1 AND isAvailable = 1 AND pincode = :pincode")
    List<ServiceProvider> getProvidersByPincode(String pincode);

    @Query("SELECT * FROM service_providers WHERE isApproved = 1 AND isAvailable = 1 AND category = :category AND pincode = :pincode")
    List<ServiceProvider> getProvidersByCategoryAndPincode(String category, String pincode);

    @Query("SELECT * FROM service_providers WHERE isApproved = 0")
    List<ServiceProvider> getPendingProviders(); // For Admin

    @Query("UPDATE service_providers SET isApproved = :isApproved, approvalTime = :approvalTime WHERE id = :id")
    void updateApprovalStatus(int id, boolean isApproved, long approvalTime);

    @Query("UPDATE service_providers SET isAvailable = :isAvailable WHERE id = :id")
    void updateAvailability(int id, boolean isAvailable);

    @Query("SELECT * FROM service_providers WHERE phone = :phone AND password = :password")
    ServiceProvider checkLogin(String phone, String password);

    @Query("SELECT * FROM service_providers WHERE id = :id")
    ServiceProvider getProviderById(int id);
}
