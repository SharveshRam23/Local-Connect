package com.example.localconnect.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.localconnect.data.dao.ServiceProviderDao;
import com.example.localconnect.model.ServiceProvider;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ServiceProviderRepository {

    private ServiceProviderDao serviceProviderDao;

    public ServiceProviderRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        serviceProviderDao = db.serviceProviderDao();
    }

    public void insert(ServiceProvider serviceProvider) {
        AppDatabase.databaseWriteExecutor.execute(() -> serviceProviderDao.insert(serviceProvider));
    }

    public LiveData<List<ServiceProvider>> getServiceProvidersByArea(String area) {
        return serviceProviderDao.getServiceProvidersByArea(area);
    }

    public ServiceProvider getServiceProviderByUserId(int userId) throws ExecutionException, InterruptedException {
        Callable<ServiceProvider> callable = () -> serviceProviderDao.getServiceProviderByUserId(userId);
        Future<ServiceProvider> future = AppDatabase.databaseWriteExecutor.submit(callable);
        return future.get();
    }
}
