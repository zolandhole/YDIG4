package com.surampaksakosoy.ydig4.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.surampaksakosoy.ydig4.R;
import com.surampaksakosoy.ydig4.util.NotificationReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.surampaksakosoy.ydig4.util.App.CHANNEL_1;

public class EPCEEMService extends FirebaseMessagingService {

    private static final String TAG = "EPCEEMSERVICE";
    private NotificationManagerCompat notificationManagerCompat;

    @Override
    public void onCreate() {
        notificationManagerCompat = NotificationManagerCompat.from(this);
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

//            Log.e(TAG, "sendPushNotification: " + data);
            if (typeNotif.equals("streamingTanya")){
                ArrayList<String> list = new ArrayList<>();
                list.add(data.getString("id"));
                list.add(data.getString("pesan"));
                list.add(data.getString("tanggal"));
                list.add(data.getString("waktu"));
                list.add(data.getString("id_login"));
                list.add(data.getString("photo"));
                list.add(data.getString("uniq_id"));
                Intent intent = new Intent("PESANBARU");
                intent.putStringArrayListExtra("DATANOTIF", list);
//                Log.e(TAG, "sendPushNotification: Streaming: " + list);
                sendBroadcast(intent);
            }
            else {
                String title = data.getString("title");
                String message = data.getString("message");
                Intent intentdatakajian = new Intent("datakajian");
                intentdatakajian.putExtra("title", title);
                intentdatakajian.putExtra("message", message);
                sendBroadcast(intentdatakajian);
                showNotificationInfo(title, message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "sendPushNotification: " + e);
        }
    }

    private void showNotificationInfo(String title, String message) {
        RemoteViews collapsedView = new RemoteViews(getPackageName(),
                R.layout.notification_collapsed);
        RemoteViews expandedView = new RemoteViews(getPackageName(),
                R.layout.notification_expanded);

        Intent clickIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent clickPendingIntent = PendingIntent.getBroadcast(this,
                0, clickIntent, 0);

        collapsedView.setTextViewText(R.id.text_view_collapsed_1, title);
        collapsedView.setTextViewText(R.id.text_view_collapsed_2, message);
        collapsedView.setOnClickPendingIntent(R.id.ll_colapsed_layout, clickPendingIntent);

        expandedView.setTextViewText(R.id.expanded_nama_kajian, title);
        expandedView.setTextViewText(R.id.expanded_title, message);

        expandedView.setImageViewResource(R.id.image_view_expanded, R.drawable.bgnotif);
        expandedView.setOnClickPendingIntent(R.id.image_view_expanded, clickPendingIntent);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1)
                .setSmallIcon(R.drawable.ic_ydig_notif)
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(expandedView)
                .build();

        notificationManagerCompat.notify(1, notification);
    }
}
