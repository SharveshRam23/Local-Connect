package com.example.localconnect.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.localconnect.model.MandatoryService;

import java.util.List;

@Dao
public interface MandatoryServiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MandatoryService service);

    @Update
    void update(MandatoryService service);

    @Delete
    void delete(MandatoryService service);

    @Query("SELECT * FROM mandatory_services WHERE pincode = :pincode ORDER BY name ASC")
    List<MandatoryService> getServicesByPincode(String pincode);

    @Query("SELECT * FROM mandatory_services WHERE pincode = :pincode AND isEmergency = 1 ORDER BY name ASC")
    List<MandatoryService> getEmergencyServicesByPincode(String pincode);

    @Query("SELECT * FROM mandatory_services WHERE category = :category AND pincode = :pincode ORDER BY name ASC")
    List<MandatoryService> getServicesByCategoryAndPincode(String category, String pincode);

    @Query("SELECT * FROM mandatory_services ORDER BY lastUpdated DESC")
    List<MandatoryService> getAllServices();
}
