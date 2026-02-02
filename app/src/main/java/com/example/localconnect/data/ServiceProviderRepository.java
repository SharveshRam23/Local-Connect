package com.example.localconnect.data;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.localconnect.data.dao.ServiceProviderDao;
import com.example.localconnect.model.ServiceProvider;

import java.util.List;

public class ServiceProviderRepository {

    private ServiceProviderDao serviceProviderDao;
    private LiveData<List<ServiceProvider>> pendingProviders;
    private LiveData<List<ServiceProvider>> approvedProviders;

    public ServiceProviderRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        serviceProviderDao = db.serviceProviderDao();
        pendingProviders = serviceProviderDao.getPendingProviders();
        approvedProviders = serviceProviderDao.getApprovedProviders();
    }

    public LiveData<List<ServiceProvider>> getPendingProviders() {
        return pendingProviders;
    }

    public LiveData<List<ServiceProvider>> getApprovedProviders() {
        return approvedProviders;
    }

    public void insert(ServiceProvider serviceProvider) {
        new insertAsyncTask(serviceProviderDao).execute(serviceProvider);
    }

    public void update(ServiceProvider serviceProvider) {
        new updateAsyncTask(serviceProviderDao).execute(serviceProvider);
    }

    private static class insertAsyncTask extends AsyncTask<ServiceProvider, Void, Void> {

        private ServiceProviderDao mAsyncTaskDao;

        insertAsyncTask(ServiceProviderDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ServiceProvider... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class updateAsyncTask extends AsyncTask<ServiceProvider, Void, Void> {

        private ServiceProviderDao mAsyncTaskDao;

        updateAsyncTask(ServiceProviderDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ServiceProvider... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }
}
