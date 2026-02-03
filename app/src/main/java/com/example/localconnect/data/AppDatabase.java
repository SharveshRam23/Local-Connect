package com.example.localconnect.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.localconnect.data.dao.IssueDao;
import com.example.localconnect.data.dao.NoticeDao;
import com.example.localconnect.data.dao.ProviderDao;
import com.example.localconnect.data.dao.UserDao;
import com.example.localconnect.model.Issue;
import com.example.localconnect.model.Notice;
import com.example.localconnect.model.ServiceProvider;
import com.example.localconnect.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = { User.class, ServiceProvider.class, Notice.class,
        Issue.class }, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();

    public abstract ProviderDao providerDao();

    public abstract NoticeDao noticeDao();

    public abstract IssueDao issueDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "local_connect_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
