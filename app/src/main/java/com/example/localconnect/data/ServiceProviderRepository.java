package com.example.localconnect.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.localconnect.data.dao.ProviderDao;
import com.example.localconnect.model.ServiceProvider;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServiceProviderRepository {
    private ProviderDao mProviderDao;
    private LiveData<List<ServiceProvider>> mAllProviders;
    private LiveData<List<ServiceProvider>> mPendingProviders;
    private LiveData<List<ServiceProvider>> mApprovedProviders;
    private final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);


    public ServiceProviderRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mProviderDao = db.providerDao();
        mAllProviders = mProviderDao.getAllServiceProviders();
        mPendingProviders = mProviderDao.getPendingServiceProviders();
        mApprovedProviders = mProviderDao.getApprovedServiceProviders();
    }

    public LiveData<List<ServiceProvider>> getAllServiceProviders() {
        return mAllProviders;
    }

    public LiveData<List<ServiceProvider>> getPendingProviders() {
        return mPendingProviders;
    }

    public LiveData<List<ServiceProvider>> getApprovedProviders() {
        return mApprovedProviders;
    }

    public void insert(ServiceProvider serviceProvider) {
        databaseWriteExecutor.execute(() -> {
            mProviderDao.insert(serviceProvider);
        });
    }

    public void update(ServiceProvider serviceProvider) {
        databaseWriteExecutor.execute(() -> {
            mProviderDao.update(serviceProvider);
        });
    }
}
