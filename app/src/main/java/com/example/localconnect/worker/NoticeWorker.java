package com.example.localconnect.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.localconnect.R;

public class NoticeWorker extends Worker {

    public NoticeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            SharedPreferences prefs = getApplicationContext().getSharedPreferences("local_connect_prefs",
                    Context.MODE_PRIVATE);
            long lastCheckTime = prefs.getLong("last_notice_check_time", 0);
            String userPincode = prefs.getString("user_pincode", "000000"); // Get actual pincode (mock for now or from session)

            // Simulate fetching new notices for this area
            // In a real app, we would call an API or check DB for notices > lastCheckTime
            
            // For demo, we just remind them occasionally if they assume "new" content
            sendNotification("Local Connect", "Check for new announcements in " + userPincode + "!");

            // Updating check time
            prefs.edit().putLong("last_notice_check_time", System.currentTimeMillis()).apply();

            return Result.success();
        } catch (Exception e) {
            return Result.failure();
        }
    }

    private void sendNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "local_connect_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Local Connect Notices",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }
}
