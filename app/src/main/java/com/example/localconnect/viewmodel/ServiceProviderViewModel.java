package com.example.localconnect.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.localconnect.data.ServiceProviderRepository;
import com.example.localconnect.model.ServiceProvider;

import java.util.List;

public class ServiceProviderViewModel extends AndroidViewModel {

    private ServiceProviderRepository mRepository;

    private LiveData<List<ServiceProvider>> mPendingProviders;
    private LiveData<List<ServiceProvider>> mApprovedProviders;

    public ServiceProviderViewModel (Application application) {
        super(application);
        mRepository = new ServiceProviderRepository(application);
        mPendingProviders = mRepository.getPendingProviders();
        mApprovedProviders = mRepository.getApprovedProviders();
    }

    public LiveData<List<ServiceProvider>> getPendingProviders() { return mPendingProviders; }

    public LiveData<List<ServiceProvider>> getApprovedProviders() { return mApprovedProviders; }

    public void insert(ServiceProvider serviceProvider) { mRepository.insert(serviceProvider); }

    public void update(ServiceProvider serviceProvider) { mRepository.update(serviceProvider); }
}
