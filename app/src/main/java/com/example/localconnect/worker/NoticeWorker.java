package com.example.localconnect.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.localconnect.util.NotificationUtil;

public class NoticeWorker extends Worker {

    public NoticeWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params
    ) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        NotificationUtil.showNotification(
                getApplicationContext(),
                "Community Update",
                "Garbage collection today 8–10 AM"
        );

        return Result.success();
    }
}
