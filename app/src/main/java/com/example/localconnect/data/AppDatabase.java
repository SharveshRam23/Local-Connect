package com.example.localconnect.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.localconnect.data.dao.BookingDao;
import com.example.localconnect.data.dao.IssueDao;
import com.example.localconnect.data.dao.NoticeDao;
import com.example.localconnect.data.dao.ProviderDao;
import com.example.localconnect.data.dao.RatingDao;
import com.example.localconnect.data.dao.UserDao;
import com.example.localconnect.model.Booking;
import com.example.localconnect.model.Issue;
import com.example.localconnect.model.Notice;
import com.example.localconnect.model.Rating;
import com.example.localconnect.model.ServiceProvider;
import com.example.localconnect.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = { User.class, ServiceProvider.class, Notice.class,
        Issue.class, Booking.class, com.example.localconnect.model.Comment.class, Rating.class }, version = 8, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();

    public abstract ProviderDao providerDao();

    public abstract NoticeDao noticeDao();

    public abstract IssueDao issueDao();

    public abstract BookingDao bookingDao();

    public abstract com.example.localconnect.data.dao.CommentDao commentDao();

    public abstract RatingDao ratingDao();

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
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@androidx.annotation.NonNull androidx.sqlite.db.SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                try {
                    AppDatabase database = INSTANCE;
                    if (database != null) {
                        UserDao dao = database.userDao();
                        // Pre-populate admin in Room with a STABLE ID
                        User admin = new User("admin_fixed_id", "Admin", "admin", "000000", "admin123");
                        dao.insert(admin);

                        // Also sync admin to Firestore to ensure persistent login
                        com.google.firebase.firestore.FirebaseFirestore firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance();
                        firestore.collection("users")
                                .document(admin.id)
                                .set(admin)
                                .addOnSuccessListener(aVoid -> android.util.Log.d("AppDatabase", "Admin synced to Firestore"))
                                .addOnFailureListener(e -> android.util.Log.e("AppDatabase", "Failed to sync admin: " + e.getMessage()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    };
}
