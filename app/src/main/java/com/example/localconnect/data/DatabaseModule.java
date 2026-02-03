package com.example.localconnect.data;

import android.content.Context;

import com.example.localconnect.data.dao.IssueDao;
import com.example.localconnect.data.dao.NoticeDao;
import com.example.localconnect.data.dao.ServiceProviderDao;
import com.example.localconnect.data.dao.UserDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return AppDatabase.getDatabase(context);
    }

    @Provides
    public UserDao provideUserDao(AppDatabase database) {
        return database.userDao();
    }

    @Provides
    public ServiceProviderDao provideServiceProviderDao(AppDatabase database) {
        return database.serviceProviderDao();
    }

    @Provides
    public NoticeDao provideNoticeDao(AppDatabase database) {
        return database.noticeDao();
    }

    @Provides
    public IssueDao provideIssueDao(AppDatabase database) {
        return database.issueDao();
    }
}
