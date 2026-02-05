package com.example.localconnect.data;

import android.content.Context;

import com.example.localconnect.data.dao.BookingDao;
import com.example.localconnect.data.dao.IssueDao;
import com.example.localconnect.data.dao.NoticeDao;
import com.example.localconnect.data.dao.ProviderDao;
import com.example.localconnect.data.dao.RatingDao;
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
    public ProviderDao provideProviderDao(AppDatabase database) {
        return database.providerDao();
    }

    @Provides
    public NoticeDao provideNoticeDao(AppDatabase database) {
        return database.noticeDao();
    }

    @Provides
    public IssueDao provideIssueDao(AppDatabase database) {
        return database.issueDao();
    }

    @Provides
    public BookingDao provideBookingDao(AppDatabase database) {
        return database.bookingDao();
    }

    @Provides
    public com.example.localconnect.data.dao.CommentDao provideCommentDao(AppDatabase database) {
        return database.commentDao();
    }

    @Provides
    public RatingDao provideRatingDao(AppDatabase database) {
        return database.ratingDao();
    }
}
