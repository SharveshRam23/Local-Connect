package com.example.localconnect.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.localconnect.data.ServiceProviderRepository;
import com.example.localconnect.model.ServiceProvider;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ServiceProviderViewModel extends ViewModel {

    private final ServiceProviderRepository mRepository;
    private final LiveData<List<ServiceProvider>> mPendingProviders;
    private final LiveData<List<ServiceProvider>> mApprovedProviders;

    @Inject
    public ServiceProviderViewModel(ServiceProviderRepository repository) {
        this.mRepository = repository;
        this.mPendingProviders = mRepository.getPendingProviders();
        this.mApprovedProviders = mRepository.getApprovedProviders();
    }

    public LiveData<List<ServiceProvider>> getPendingProviders() {
        return mPendingProviders;
    }

    public LiveData<List<ServiceProvider>> getApprovedProviders() {
        return mApprovedProviders;
    }

    public void insert(ServiceProvider serviceProvider) {
        mRepository.insert(serviceProvider);
    }

    public void update(ServiceProvider serviceProvider) {
        mRepository.update(serviceProvider);
    }
}
