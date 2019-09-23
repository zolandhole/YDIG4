package com.surampaksakosoy.ydig4.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "MyReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        String param = intent.getAction();
        if (param!=null){
            Log.e(TAG, "onReceive MyReceiver : " + param);
            switch (param){
                case "stop":
                    context.sendBroadcast(new Intent("stop"));
                    break;
                case "start":
                    context.sendBroadcast(new Intent("start"));
                    break;
                case "exit":
                    context.sendBroadcast(new Intent("exit"));
                    break;
            }
        }
    }
}
