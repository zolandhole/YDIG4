package com.surampaksakosoy.ydig4.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.Objects;

public class StreamingService extends Service implements
        MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener
{

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private boolean isPausedCall = false;
    private BroadcastReceiver broadcastReceiver;
    private static final String TAG = "StreamingService";
    private NotificationManagerCompat notificationManagerCompat;

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate: StreamingService");

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.e(TAG, "onReceive: ONSERVICE " + action);
                assert action != null;
                switch (action){
                    case "stop":
                        pauseMedia();
                        break;
                    case "start":
                        playMedia();
                        break;
                    case "exit":
                        notificationManagerCompat.cancel(1);
                        stopMedia();
                        break;

                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("stop");
        filter.addAction("start");
        filter.addAction("exit");
        registerReceiver(broadcastReceiver, filter);

        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.reset();

        AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener =
                new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case (AudioManager.AUDIOFOCUS_LOSS):
                        break;
                    case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT):
                        pauseMedia();
                        break;
                    case (AudioManager.AUDIOFOCUS_GAIN):
                        playMedia();
                        break;
                    case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK):
                        mediaPlayer.setVolume(0.1f, 0.1f);
                        break;
                }
            }
        };

        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        int mediaresult = audioManager.requestAudioFocus(onAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (mediaresult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            Log.e(TAG, "onCreate: GRANTED");
        }

        notificationManagerCompat = NotificationManagerCompat.from(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: StreamingService");
        initIfPhoneCall();
//        String nama;
        if (intent!=null){
//            nama = Objects.requireNonNull(intent.getExtras()).getString("name");
//            showNotification(nama);
            mediaPlayer.reset();
            if (!mediaPlayer.isPlaying()){
                try {
                    mediaPlayer.setDataSource(Objects.requireNonNull(intent.getExtras()).getString("url"));
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return START_STICKY;
    }

//    private void showNotification(String nama) {
//        Intent intentNotification = new Intent(this, MainActivity.class);
//        intentNotification.putExtra("streamingRadio", "streamingRadio");
//        intentNotification.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent pendingIntentOpenApp = PendingIntent.getActivity(this, 0, intentNotification, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        RemoteViews collapsedView = new RemoteViews(getPackageName(), R.layout.notification_collapsed);
//        RemoteViews expandedView = new RemoteViews(getPackageName(), R.layout.notification_expanded);
//
//        collapsedView.setTextViewText(R.id.notification_collapsed_1, nama);
//        expandedView.setTextViewText(R.id.notification_expanded_1, nama);
//        Notification notification = new NotificationCompat.Builder(this, CHANNEL_PLAY_STREAMING)
//                .setSmallIcon(R.drawable.ic_ydig_notif)
//                .setCustomContentView(collapsedView)
//                .setCustomBigContentView(expandedView)
//                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
//                .setContentIntent(pendingIntentOpenApp)
//                .setPriority(2)
//                .build();
//
//        notificationManagerCompat.notify(1,notification);
//    }

    private void initIfPhoneCall(){
        Log.e(TAG, "initIfPhoneCall: ");
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        Log.e(TAG, "onCallStateChanged: OFFHOOK + STATE RINGING");
                        if (mediaPlayer != null) {
                            pauseMedia();
                            isPausedCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (mediaPlayer != null) {
                            if (isPausedCall){
                                isPausedCall = false;
                                playMedia();
                            }
                        }
                        break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void playMedia(){
        Log.e(TAG, "playMedia: ");
        if (!mediaPlayer.isPlaying()){
            mediaPlayer.start();
            Intent intent = new Intent("mediaplayed");
            sendBroadcast(intent);
        }
    }

    public void stopMedia(){
        Log.e(TAG, "stopMedia: ");
        if (mediaPlayer != null){
            Log.e(TAG, "stopMedia: NOT NULL");
            if (mediaPlayer.isPlaying()){
                mediaPlayer.stop();
                Log.e(TAG, "stopMedia: STOP");
                broadcastStopMedia();
            }
            mediaPlayer.release();
        } else {
            Log.e(TAG, "stopMedia: ELSE");
        }
        stopForeground(true);
        stopSelf();
    }

    private void pauseMedia(){
        Log.e(TAG, "pauseMedia: ");
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            broadcastStopMedia();
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: StreamingService");
        super.onDestroy();
        broadcastStopMedia();
        unregisterReceiver(broadcastReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind: StreamingService " + intent);
        sendBroadcast(new Intent("streamingError"));
        return null;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Log.e(TAG, "onBufferingUpdate: StreamingService" + mp + " percent: " + percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e(TAG, "onBind: StreamingService" + mp);
        stopMedia();
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "onError: WHAT: " + what + "EXTRA: " + extra);
        Intent intent = new Intent("streamingError");
        sendBroadcast(intent);
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "onInfo: WHAT: " + what + "EXTRA: " + extra);
        Intent intent = new Intent("lemot");
        intent.putExtra("lemot", String.valueOf(what));
        sendBroadcast(intent);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.e(TAG, "onPrepared: " + mp);
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        Log.e(TAG, "onSeekComplete: StreamingService" + mp);
    }

    private void broadcastStopMedia(){
        Intent intent = new Intent("mediastoped");
        sendBroadcast(intent);
    }
}
