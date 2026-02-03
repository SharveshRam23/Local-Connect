package com.example.localconnect.data;

import com.example.localconnect.data.dao.ServiceProviderDao;
import com.example.localconnect.model.ServiceProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;

public class ServiceProviderRepository {

    private final ServiceProviderDao serviceProviderDao;

    @Inject
    public ServiceProviderRepository(ServiceProviderDao serviceProviderDao) {
        this.serviceProviderDao = serviceProviderDao;
    }

    public void insert(ServiceProvider serviceProvider) {
        AppDatabase.databaseWriteExecutor.execute(() -> serviceProviderDao.insert(serviceProvider));
    }

    public void update(ServiceProvider serviceProvider) {
        AppDatabase.databaseWriteExecutor.execute(() -> serviceProviderDao.update(serviceProvider));
    }

    public LiveData<List<ServiceProvider>> getServiceProvidersByArea(String area) {
        return serviceProviderDao.getServiceProvidersByArea(area);
    }

    public CompletableFuture<ServiceProvider> getServiceProviderByUserId(int userId) {
        return CompletableFuture.supplyAsync(() -> serviceProviderDao.getServiceProviderByUserId(userId), AppDatabase.databaseWriteExecutor);
    }

    public LiveData<List<ServiceProvider>> getPendingProviders() {
        return serviceProviderDao.getPendingProviders();
    }

    public LiveData<List<ServiceProvider>> getApprovedProviders() {
        return serviceProviderDao.getApprovedProviders();
    }
}
