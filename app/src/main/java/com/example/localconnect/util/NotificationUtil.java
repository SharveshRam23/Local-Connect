package com.example.localconnect.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.localconnect.R;

public class NotificationUtil {

        private static final String CHANNEL_ID = "provider_approval_channel";
        private static final String CHANNEL_NAME = "Provider Approvals";
        private static final String CHANNEL_DESC = "Notifications for Service Provider Account Approval";

        public static void showApprovalNotification(Context context, String title, String message) {
                NotificationManager notificationManager = (NotificationManager) context
                                .getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                                        NotificationManager.IMPORTANCE_DEFAULT);
                        channel.setDescription(CHANNEL_DESC);
                        notificationManager.createNotificationChannel(channel);
                }

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(android.R.drawable.ic_dialog_info) // using system icon for simplicity
                                .setContentTitle(title)
                                .setContentText(message)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setAutoCancel(true);

                notificationManager.notify(1001, builder.build());
        }
}
