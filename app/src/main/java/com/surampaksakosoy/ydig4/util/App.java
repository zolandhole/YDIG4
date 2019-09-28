package com.surampaksakosoy.ydig4.util;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String CHANNEL_1 = "Channel Info";
    public static final String CHANNEL_2 = "Channel service";
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationManager manager = getSystemService(NotificationManager.class);

            NotificationChannel channel = new NotificationChannel(
                CHANNEL_1,"channel info", NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2, "channel service", NotificationManager.IMPORTANCE_LOW
            );
            manager.createNotificationChannel(channel2);
        }
    }
}
