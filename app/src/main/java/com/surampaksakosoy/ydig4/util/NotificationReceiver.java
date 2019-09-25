package com.surampaksakosoy.ydig4.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.surampaksakosoy.ydig4.MainActivity;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e(TAG, "onReceive: " + intent);

        Intent i = new Intent(context, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(i);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(1);
    }
}
