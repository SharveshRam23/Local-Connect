package com.example.localconnect.data;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

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

@Database(entities = {User.class, ServiceProvider.class, Notice.class, Issue.class}, version = 1, exportSchema = false)
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
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                UserDao userDao = INSTANCE.userDao();
                userDao.deleteAll();
                User admin = new User("admin", "admin@localconnect.com", "admin123", "admin");
                userDao.insert(admin);
                User user = new User("user", "user@localconnect.com", "user123", "user");
                userDao.insert(user);

                ProviderDao providerDao = INSTANCE.providerDao();
                providerDao.deleteAll();

                ServiceProvider provider = new ServiceProvider("John Doe", "Plumber", "1234567890", "123 Main St", false);
                providerDao.insert(provider);

                IssueDao issueDao = INSTANCE.issueDao();
                issueDao.deleteAll();

                Issue issue = new Issue("Leaky Faucet", "My kitchen faucet is leaking.");
                issueDao.insert(issue);
            });
        }
    };
}
