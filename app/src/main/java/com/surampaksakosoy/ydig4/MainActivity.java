package com.surampaksakosoy.ydig4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.internal.ImageRequest;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.surampaksakosoy.ydig4.services.StreamingService;
import com.surampaksakosoy.ydig4.util.DBHandler;
import com.surampaksakosoy.ydig4.util.HandlerServer;
import com.surampaksakosoy.ydig4.util.PublicAddress;
import com.surampaksakosoy.ydig4.util.VolleyCallback;

import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private String SUMBER_LOGIN, ID_LOGIN, NAMA, EMAIL;
    private DBHandler dbHandler;
    private Button buttonPlay, buttonStop;
    private ProgressBar progress_play;
    private TextView main_status_streaming;
    private FirebaseAuth mAuth;
    private int countError = 0;
    private GoogleSignInClient mGoogleSignInClient;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String param = intent.getAction();
            assert param != null;
            switch (param){
                case "mediaplayed":
                    main_status_streaming.setVisibility(View.GONE);
                    progress_play.setVisibility(View.GONE);
                    buttonStop.setVisibility(View.VISIBLE);
                    buttonPlay.setVisibility(View.GONE);
                    break;
                case "mediastoped":
                    buttonStop.setVisibility(View.GONE);
                    progress_play.setVisibility(View.GONE);
                    buttonPlay.setVisibility(View.VISIBLE);
                    break;
                case "lemot":
                    String bufferCode = intent.getStringExtra("lemot");
                    if (bufferCode.equals("703")){
                        Log.e(TAG, "onReceive: Sedang buffering");
                        main_status_streaming.setText(R.string.koneksi_buruk);
                        main_status_streaming.setBackgroundColor(getResources().getColor(R.color.merahmarun));
                        main_status_streaming.setVisibility(View.VISIBLE);
                    }
                    if (bufferCode.equals("702")){
                        Log.e(TAG, "onReceive: Buffer Completed");
                        main_status_streaming.setVisibility(View.GONE);
                    }
                    break;
                case "streamingError":
                    countError = countError+1;
                    if (countError <= 3){
                        new MyTask().execute();
                    } else {
                        infokanKeUser("Streaming Terputus, silahkan ulangi kembali");
                        buttonPlay.setVisibility(View.VISIBLE);
                        buttonStop.setVisibility(View.GONE);
                        progress_play.setVisibility(View.GONE);
                    }
                    Log.e(TAG, "onReceive: " + countError);
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initListener();

        IntentFilter filter = new IntentFilter();
        filter.addAction("mediaplayed");
        filter.addAction("mediastoped");
        filter.addAction("lemot");
        filter.addAction("streamingError");
        registerReceiver(broadcastReceiver, filter);

        ID_LOGIN = cekApakahUserPernahLogin();
        if (ID_LOGIN == null){
            keLogin();
        } else {
            lanjutkanKeLangkahBerikutnya();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        countError = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private void lanjutkanKeLangkahBerikutnya() {
        new MyTask().execute();
        tampilkanFoto();
    }

    private void tampilkanFoto() {
        final CircleImageView imageView = findViewById(R.id.nav_image_view);
        Uri photo;
        int dimensionPixelSize = getResources()
                .getDimensionPixelSize(com.facebook.R.dimen.com_facebook_profilepictureview_preset_size_large);
        if (SUMBER_LOGIN.equals("FACEBOOK")) {
            photo = ImageRequest.getProfilePictureUri(ID_LOGIN, dimensionPixelSize, dimensionPixelSize);
        } else {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            assert currentUser != null;
            photo = currentUser.getPhotoUrl();
        }

        if (photo != null) {
            Glide.with(this).load(photo).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(imageView);
        } else {
            imageView.setImageResource(R.drawable.avatar);
        }
    }

    private void initListener() {
        dbHandler = new DBHandler(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        buttonPlay = findViewById(R.id.buttonPlay); buttonPlay.setOnClickListener(this);
        buttonStop = findViewById(R.id.buttonStop); buttonStop.setOnClickListener(this);
        progress_play = findViewById(R.id.progress_play);
        main_status_streaming = findViewById(R.id.main_status_streaming);
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            keLogin();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    private void keLogin() {
        dbHandler.deleteDB();
        Log.e(TAG, "keLogin: SUMBER LOGIN" + SUMBER_LOGIN);
        if (SUMBER_LOGIN!= null){
            if (SUMBER_LOGIN.equals("FACEBOOK")){
                LoginManager.getInstance().logOut();
            }
            if (SUMBER_LOGIN.equals("GOOGLE")){
                // Firebase sign out
                mAuth.signOut();

                // Google sign out
                mGoogleSignInClient.signOut().addOnCompleteListener(this,
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.e(TAG, "onComplete: Berhasil Logout Google");
                            }
                        });
            }
            ubahStatusDiServer();
        }
        Intent intentStopStreaming = new Intent("exit");
        sendBroadcast(intentStopStreaming);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void ubahStatusDiServer() {
        List<String> list =new ArrayList<>();
        list.add(ID_LOGIN);
        HandlerServer handlerServer = new HandlerServer(this, PublicAddress.POST_LOGOUT);
        synchronized (this){
            handlerServer.sendDataToServer(new VolleyCallback() {
                @Override
                public void onFailed(String result) {
                    if (!result.contains("berhasil")){
                        Log.e(TAG, "onFailed: " + result);
                    }
                }

                @Override
                public void onSuccess(JSONArray jsonArray) {
                    Log.e(TAG, "onSuccess: " + jsonArray);
                }
            }, list);
        }
    }

    private String cekApakahUserPernahLogin() {
        ArrayList<HashMap<String, String>> userDB = dbHandler.getUser(1);
        for (Map<String,String> map : userDB){
            SUMBER_LOGIN = map.get("sumber_login");
            ID_LOGIN = map.get("id_login");
            NAMA = map.get("nama");
            EMAIL = map.get("email");
        }
        return ID_LOGIN;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonPlay:
                progress_play.setVisibility(View.VISIBLE);
                buttonPlay.setVisibility(View.GONE);
                if (!isMyServiceRunning()){
                    new MyTask().execute();
                }
                break;
            case R.id.buttonStop:
                progress_play.setVisibility(View.VISIBLE);
                buttonStop.setVisibility(View.GONE);
                Intent intent = new Intent("exit");
                sendBroadcast(intent);
                break;
        }
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (StreamingService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("StaticFieldLeak")
    private class MyTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            String title = "";
            Document doc;
            try {
                doc = Jsoup.connect("http://122.248.39.157:8000/index.html?sid=1").get();
                Elements elements = doc.select("b");
                if (elements.text().contains("Stream is currently down.")){
                    title = "SERVER STREAMING TIDAK DITEMUKAN";
                } else {
                    title = "HIDUP";
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "doInBackground: "+ e);
            }
            return title;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("HIDUP")){
                Log.e(TAG, "onPostExecute: Server Hidup");
                jalankanServiceStreamig();
                main_status_streaming.setText(R.string.Mohon_tunggu);
                main_status_streaming.setBackgroundColor(getResources().getColor(R.color.darkGrey));
                main_status_streaming.setVisibility(View.VISIBLE);
            } else {
                Log.e(TAG, "onPostExecute: Server Maot");
                buttonStop.setVisibility(View.GONE);
                buttonPlay.setVisibility(View.VISIBLE);
                progress_play.setVisibility(View.GONE);
                infokanKeUser("Radio On Air tidak ditemukan");
            }
        }
    }

    private void infokanKeUser(String keterangan) {
        CoordinatorLayout mRoot =this.findViewById(R.id.layout_main);
        Snackbar snackbar = Snackbar.make(mRoot, keterangan, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.merahmarun));
        snackbar.show();
    }

    private void jalankanServiceStreamig(){
        Bundle bundle = new Bundle();
        bundle.putString("url", "http://122.248.39.157:8000");
        bundle.putString("name", "Radio Streaming On Air");
        Intent intent = new Intent(this, StreamingService.class);
        intent.putExtras(bundle);
        startService(intent);
    }
}
