package com.surampaksakosoy.ydig4.services;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EPCEEMService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseService";

    @Override
    public void onCreate() {

    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "onMessageReceived: Data Payload: " + remoteMessage.getData().toString());
        if (remoteMessage.getData().size() > 0){
            try {
                JSONObject jsonObject = new JSONObject(remoteMessage.getData().toString());
                sendPushNotification(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendPushNotification(JSONObject jsonObject) {
        try {
            JSONObject data = jsonObject.getJSONObject("data");

            String typeNotif = data.getString("typeNotif");
            Log.e(TAG, "sendPushNotification: " + typeNotif);
            if (typeNotif.equals("streamingTanya")){
                ArrayList<String> list = new ArrayList<>();
                list.add(data.getString("id"));
                list.add(data.getString("pesan"));
                list.add(data.getString("tanggal"));
                list.add(data.getString("waktu"));
                list.add(data.getString("id_login"));
                list.add(data.getString("photo"));
                Intent intent = new Intent("PESANBARU");
                intent.putStringArrayListExtra("DATANOTIF", list);
                sendBroadcast(intent);
            }
//            else {
//                String title = data.getString("title");
//                String message = data.getString("message");
//                MyNotificationManager myNotificationManager = new MyNotificationManager(getApplicationContext());
//                myNotificationManager.showStreamingNotification(title,message);
//            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "sendPushNotification: " + e);
        }
    }
}
