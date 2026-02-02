package com.example.localconnect.data;

import android.content.Context;

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

@Database(entities = {User.class, ServiceProvider.class, Notice.class, Issue.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public abstract UserDao userDao();
    public abstract ProviderDao providerDao();
    public abstract NoticeDao noticeDao();
    public abstract IssueDao issueDao();

    public static synchronized AppDatabase getDatabase(final Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "local_connect_db")
                    .fallbackToDestructiveMigration()
                    .addCallback(sRoomDatabaseCallback)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {
                UserDao userDao = instance.userDao();
                userDao.deleteAll();
                User admin = new User("admin", "admin@localconnect.com", "admin123", "admin");
                userDao.insert(admin);
                User user = new User("user", "user@localconnect.com", "user123", "user");
                userDao.insert(user);
                User service = new User("service", "service@localconnect.com", "service123", "service");
                userDao.insert(service);

                ProviderDao providerDao = instance.providerDao();
                providerDao.deleteAll();
                ServiceProvider serviceProvider = new ServiceProvider("Plumber", "Plumbing", "123-456-7890", "123 Main St", true);
                providerDao.insert(serviceProvider);


            });
        }
    };
}
