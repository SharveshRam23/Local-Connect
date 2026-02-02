package com.example.localconnect.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "service_providers",
        foreignKeys = @ForeignKey(entity = User.class,
                                  parentColumns = "id",
                                  childColumns = "userId",
                                  onDelete = ForeignKey.CASCADE),
        indices = {@Index("userId")})
public class ServiceProvider {
    @PrimaryKey(autoGenerate = true)
    private int providerId;
    private int userId;
    private String serviceType;
    private String phone;
    private String area;
    private String status;

    public ServiceProvider(int userId, String serviceType, String phone, String area, String status) {
        this.userId = userId;
        this.serviceType = serviceType;
        this.phone = phone;
        this.area = area;
        this.status = status;
    }

    public int getProviderId() {
        return providerId;
    }

    public void setProviderId(int providerId) {
        this.providerId = providerId;
    }

    public int getUserId() {
        return userId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getPhone() {
        return phone;
    }

    public String getArea() {
        return area;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
