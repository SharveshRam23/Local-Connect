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

        private static final String GEOFENCE_CHANNEL_ID = "geofence_channel";
        private static final String GEOFENCE_CHANNEL_NAME = "Smart Alerts";
        private static final String GEOFENCE_CHANNEL_DESC = "Location-aware community alerts";

        public static void showApprovalNotification(Context context, String title, String message) {
                showNotification(context, CHANNEL_ID, CHANNEL_NAME, CHANNEL_DESC, 1001, title, message);
        }

        public static void showGeofenceNotification(Context context, String title, String message) {
                showNotification(context, GEOFENCE_CHANNEL_ID, GEOFENCE_CHANNEL_NAME, GEOFENCE_CHANNEL_DESC, 2001, title, message);
        }

        private static void showNotification(Context context, String chId, String chName, String chDesc, int notifyId, String title, String message) {
                NotificationManager notificationManager = (NotificationManager) context
                                .getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(chId, chName,
                                        NotificationManager.IMPORTANCE_DEFAULT);
                        channel.setDescription(chDesc);
                        notificationManager.createNotificationChannel(channel);
                }

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, chId)
                                .setSmallIcon(android.R.drawable.ic_dialog_info) 
                                .setContentTitle(title)
                                .setContentText(message)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setAutoCancel(true);

                notificationManager.notify(notifyId, builder.build());
        }
}
