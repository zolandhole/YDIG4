package com.surampaksakosoy.ydig4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import java.text.SimpleDateFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.internal.ImageRequest;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.surampaksakosoy.ydig4.adapters.AdapterStreaming;
import com.surampaksakosoy.ydig4.models.ModelStreaming;
import com.surampaksakosoy.ydig4.services.StreamingService;
import com.surampaksakosoy.ydig4.util.DBHandler;
import com.surampaksakosoy.ydig4.util.HandlerServer;
import com.surampaksakosoy.ydig4.util.PublicAddress;
import com.surampaksakosoy.ydig4.util.VolleyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private String SUMBER_LOGIN, ID_LOGIN, NAMA, EMAIL, ADSID, TOKENFCM, VERSI;
    private DBHandler dbHandler;
    private Button buttonPlay, buttonStop, btn_send, btn_pertanyaan, btn_komentar, btn_saran;
    private ProgressBar progress_play, progressbar_send;
    private TextView main_status_streaming, judul_kajian;
    private FirebaseAuth mAuth;
    private int countError = 0;
    private GoogleSignInClient mGoogleSignInClient;
    boolean doubleBackToExitPressedOnce = false;
    private RecyclerView streaming_recyclerview;
    private LinearLayoutManager linearLayoutManager;
    private AdapterStreaming adapterStreaming;
    private List<ModelStreaming> modelStreaming;
    private EditText editTextPesan;
    private CardView cv_pesanBaru;
    private LinearLayout ll_serverdown, ll_serverup, ll_popup_sendbutton;
    private RelativeLayout main_layout;

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
                    assert bufferCode != null;
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
                        if (!isMyServiceRunning()){
                            new MyTask().execute();
                        }
                    } else {
                        infokanKeUser("Streaming Terputus, silahkan ulangi kembali");
                        buttonPlay.setVisibility(View.VISIBLE);
                        buttonStop.setVisibility(View.GONE);
                        progress_play.setVisibility(View.GONE);
                    }
                    Log.e(TAG, "onReceive: " + countError);
                    break;
                case "PESANBARU":
                    ArrayList<String> dataPesan = intent.getStringArrayListExtra("DATANOTIF");
                    assert dataPesan != null;
                    Log.e(TAG, "onReceive: "+ dataPesan);
                    pesanBaruDatang(dataPesan);
                    break;
                case "errorsenddata":
                    infokanKeUser("Gagal mengirim pesan silakan coba lagi");
                    editTextPesan.setEnabled(true);
                    progressbar_send.setVisibility(View.GONE);
                    btn_send.setVisibility(View.VISIBLE);
                    break;
                case "datakajian":
                    judul_kajian.setText(intent.getStringExtra("title"));
                    judul_kajian.setVisibility(View.VISIBLE);
                    break;
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initListener();
        daftarkanBroadcast();

        //pengecekan User Login
        ID_LOGIN = cekApakahUserPernahLogin();
        if (ID_LOGIN == null){
            keLogin();
        } else {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();


            getAppVersion();
            collectPhoneData();

            //JalanKan Service Streaming
            if (!isMyServiceRunning()){
                new MyTask().execute();
            }

            loadingDataChatting();
        }
    }

    private void getAppVersion() {
        List<String> list = new ArrayList<>();
        list.add("0");
        HandlerServer handlerServer = new HandlerServer(this, PublicAddress.GET_VERSION);
        synchronized (this){
            handlerServer.sendDataToServer(new VolleyCallback() {
                @Override
                public void onFailed(String result) {
                    Log.e(TAG, "onFailed: " + result);
                }

                @Override
                public void onSuccess(JSONArray jsonArray) {
                    JSONObject dataServer;
                    for (int i=0; i< jsonArray.length(); i++){
                        try {
                            dataServer = jsonArray.getJSONObject(i);
                            if (Integer.parseInt(VERSI) < Integer.parseInt(dataServer.getString("version"))){
                                update_versi();
                            } else {
                                Log.e(TAG, "onSuccess: Versi Sama");
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "exception: " + e);
                            e.printStackTrace();
                        }
                    }
                }
            }, list);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListener() {
        dbHandler = new DBHandler(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        judul_kajian = findViewById(R.id.judul_kajian);
        buttonPlay = findViewById(R.id.buttonPlay); buttonPlay.setOnClickListener(this);
        buttonStop = findViewById(R.id.buttonStop); buttonStop.setOnClickListener(this);
        btn_pertanyaan = findViewById(R.id.btn_pertanyaan); btn_pertanyaan.setOnClickListener(this);
        btn_komentar = findViewById(R.id.btn_komentar); btn_komentar.setOnClickListener(this);
        btn_saran = findViewById(R.id.btn_saran); btn_saran.setOnClickListener(this);
        progress_play = findViewById(R.id.progress_play);
        main_status_streaming = findViewById(R.id.main_status_streaming);
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        streaming_recyclerview = findViewById(R.id.streaming_recyclerview);
        btn_send = findViewById(R.id.streaming_sendpesan); btn_send.setOnClickListener(this);
        progressbar_send = findViewById(R.id.progressbar_send);
        editTextPesan = findViewById(R.id.streaming_edittext);
        cv_pesanBaru = findViewById(R.id.cv_pesan_baru); cv_pesanBaru.setOnClickListener(this);

        streaming_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {


            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (linearLayoutManager.findFirstVisibleItemPosition() == 0){
                    cv_pesanBaru.setVisibility(View.GONE);
                }
            }
        });

        ll_serverdown = findViewById(R.id.serverdown);
        ll_serverup = findViewById(R.id.ll_serverup);
        ll_popup_sendbutton = findViewById(R.id.popup_sendbutton);
        main_layout = findViewById(R.id.main_layout);
        main_layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hidePopupandKeyboard(v);
                return false;
            }
        });
        streaming_recyclerview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hidePopupandKeyboard(v);
                return false;
            }
        });
    }



    private void daftarkanBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("mediaplayed");
        filter.addAction("mediastoped");
        filter.addAction("lemot");
        filter.addAction("streamingError");
        filter.addAction("PESANBARU");
        filter.addAction("errorsenddata");
        filter.addAction("datakajian");
        registerReceiver(broadcastReceiver, filter);
    }

    private String cekApakahUserPernahLogin() {
        ArrayList<HashMap<String, String>> userDB = dbHandler.getUser(1);
        for (Map<String,String> map : userDB){
            SUMBER_LOGIN = map.get("sumber_login");
            ID_LOGIN = map.get("id_login");
            NAMA = map.get("nama");
            EMAIL = map.get("email");
            VERSI = map.get("version");
        }
        return ID_LOGIN;
    }

    private void collectPhoneData() {
        // pengambilan Phone Uniq ID
        ADSID = "35" +
                Build.BOARD.length()%10+ Build.BRAND.length()%10 +
                Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 +
                Build.DISPLAY.length()%10 + Build.HOST.length()%10 +
                Build.ID.length()%10 + Build.MANUFACTURER.length()%10 +
                Build.MODEL.length()%10 + Build.PRODUCT.length()%10 +
                Build.TAGS.length()%10 + Build.TYPE.length()%10 +
                Build.USER.length()%10 ;

        //Subscribe topic fcm
        FirebaseMessaging.getInstance().subscribeToTopic("STREAMING_RADIO");
        FirebaseMessaging.getInstance().subscribeToTopic("streamingTanya");


        // Get Photo Profile
        final CircleImageView imageView = findViewById(R.id.nav_image_view);
        final Uri photo;
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

        //get TOKEN
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                TOKENFCM = instanceIdResult.getToken();
                sendCollectPhoneData(photo);
            }
        });
    }
        private void sendCollectPhoneData(Uri photo) {
        List<String> list = new ArrayList<>();
        list.add(ID_LOGIN); list.add(ADSID); list.add(TOKENFCM); list.add(String.valueOf(photo));
        HandlerServer handlerServer = new HandlerServer(this, PublicAddress.SAVE_PHONE_DATA);
        Log.e(TAG, "sendCollectPhoneData: " + list);
        synchronized (this){
            handlerServer.sendDataToServer(new VolleyCallback() {
                @Override
                public void onFailed(String result) {
                    Log.e(TAG, "onFailed: sendToken" + result);
                }

                @Override
                public void onSuccess(JSONArray jsonArray) {
                    Log.e(TAG, "onSuccess: sendToken" + jsonArray);
                }
            }, list);
        }
    }

    private void loadingDataChatting() {
        List<String> list =new ArrayList<>();
        list.add("0");
        HandlerServer handlerServer = new HandlerServer(this, PublicAddress.LOAD_COMMENT_DATA);
        synchronized (this){
            handlerServer.sendDataToServer(new VolleyCallback() {
                @Override
                public void onFailed(String result) {

                    infokanKeUser("Gagal mengambil data Chat");
                }

                @Override
                public void onSuccess(JSONArray jsonArray) {
                    parsingDataChatting(jsonArray);
                }
            }, list);
        }
    }
        private void parsingDataChatting(JSONArray jsonArray) {
            List<ModelStreaming> list = new ArrayList<>();
            JSONObject dataServer;
            for (int i=0; i< jsonArray.length(); i++){
                try {
                    dataServer = jsonArray.getJSONObject(i);
                    JSONObject isiData = dataServer.getJSONObject("data");
                    list.add(new ModelStreaming(
                            Integer.parseInt(dataServer.getString("id")),
                            isiData.getString("pesan"),
                            isiData.getString("tanggal"),
                            isiData.getString("waktu"),
                            isiData.getString("id_login"),
                            isiData.getString("photo"),
                            isiData.getString("uniq_id")
                    ));
                    tampilkanDataChatting(list);
                } catch (JSONException e) {
                    Log.e(TAG, "parsingDataChatting: exception: " + e);
                    e.printStackTrace();
                }
            }
        }
        private void tampilkanDataChatting(List<ModelStreaming> list) {
            this.modelStreaming = list;
            if (list.isEmpty()){
                Log.e(TAG, "tampilkanDataChatting: " + list.size());
            } else {
                linearLayoutManager = new LinearLayoutManager(this);
                adapterStreaming =new AdapterStreaming(modelStreaming, this, ID_LOGIN);
                streaming_recyclerview.setAdapter(adapterStreaming);
                streaming_recyclerview.setLayoutManager(linearLayoutManager);
                streaming_recyclerview.setItemAnimator(new DefaultItemAnimator());
            }
    }

    private void pesanBaruDatang(ArrayList<String> dataPesan) {
        ModelStreaming item = (new ModelStreaming(
                Integer.parseInt(dataPesan.get(0)),dataPesan.get(1),dataPesan.get(2),dataPesan.get(3),dataPesan.get(4),dataPesan.get(5),dataPesan.get(6)
        ));
        int insertIndex = 0;
        if (modelStreaming!= null){
            modelStreaming.add(insertIndex, item);
            adapterStreaming.notifyItemInserted(insertIndex);
            int scrollPosition = linearLayoutManager.findFirstVisibleItemPosition();
            if (scrollPosition == 0 || dataPesan.get(6).equals(ID_LOGIN)){
                linearLayoutManager.scrollToPosition(0);
                cv_pesanBaru.setVisibility(View.GONE);
            } else {
                cv_pesanBaru.setVisibility(View.VISIBLE);
            }
            progressbar_send.setVisibility(View.GONE);
            btn_send.setVisibility(View.VISIBLE);
            editTextPesan.setEnabled(true);
        } else {
            Log.e(TAG, "pesanBaruDatang: " + item);
        }
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
            case R.id.streaming_sendpesan:
                ll_popup_sendbutton.setVisibility(View.VISIBLE);
//                kirimPesan();
                break;
            case R.id.cv_pesan_baru:
                linearLayoutManager.scrollToPosition(0);
                cv_pesanBaru.setVisibility(View.GONE);
                break;
            case R.id.btn_pertanyaan:
                kirimPesan("1#");
                break;
            case R.id.btn_komentar:
                kirimPesan("2#");
                break;
            case R.id.btn_saran:
                kirimPesan("3#");
                break;
        }
    }

    private void hidePopupandKeyboard(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null){
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        ll_popup_sendbutton.setVisibility(View.GONE);
    }

        private void kirimPesan(String s) {
        ll_popup_sendbutton.setVisibility(View.GONE);
        String pesan = editTextPesan.getText().toString();
        if (!pesan.equals("")){
            progressbar_send.setVisibility(View.VISIBLE);
            btn_send.setVisibility(View.GONE);
            editTextPesan.setEnabled(false);
            Date c = Calendar.getInstance().getTime();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
            String tanggal = df.format(c);
            String waktu = tf.format(c);

            List<String> list = new ArrayList<>();
            if (s!=null){
                list.add(s);
            }
            list.add(tanggal);
            list.add(waktu);
            list.add(ID_LOGIN);
            list.add(pesan);
            HandlerServer handlerServer = new HandlerServer(this, PublicAddress.SEND_COMMENT_DATA);
            synchronized (this){
                handlerServer.sendDataToServer(new VolleyCallback() {
                    @Override
                    public void onFailed(String result) {
                        if (!result.contains("berhasil")){
                            progressbar_send.setVisibility(View.GONE);
                            btn_send.setVisibility(View.VISIBLE);
                            editTextPesan.setEnabled(true);
                            Toast.makeText(MainActivity.this, "Gagal Mengirim Pesan, Silahkan Coba lagi", Toast.LENGTH_SHORT).show();
                        } else {
                            progressbar_send.setVisibility(View.GONE);
                            btn_send.setVisibility(View.VISIBLE);
                            editTextPesan.setText("");
                            editTextPesan.setEnabled(true);
                        }
                    }

                    @Override
                    public void onSuccess(JSONArray jsonArray) {
                        Toast.makeText(MainActivity.this, "Pesan Terkirim", Toast.LENGTH_SHORT).show();
                    }
                }, list);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        countError = 0;
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
                mAuth.signOut();
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
                        Log.e(TAG, "onFailed ubah status di server: " + result);
                    }
                }

                @Override
                public void onSuccess(JSONArray jsonArray) {
                    Log.e(TAG, "onSuccess: " + jsonArray);
                }
            }, list);
        }
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
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
                ll_serverdown.setVisibility(View.GONE);
                ll_serverup.setVisibility(View.VISIBLE);
            } else {
                ll_serverdown.setVisibility(View.VISIBLE);
                ll_serverup.setVisibility(View.GONE);
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

    private void update_versi(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setMessage("Anda akan di arahkan ke PlayStore untuk pembaharuan aplikasi ")
                .setTitle("Perbaharui Versi")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try
                        {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getApplicationContext().getPackageName())));
                        }
                        catch (ActivityNotFoundException exception)
                        {
                            Toast.makeText(MainActivity.this, "Aplikasi tidak ditemukan di Play Store", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        builder.setCancelable(false);
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();
    }

    private void tampilkanPromo(){
        final Dialog promo = new Dialog(this);
        promo.setContentView(R.layout.layout_promo);
        CardView cardViewPromo = promo.findViewById(R.id.promo_cv);
        cardViewPromo.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            cardViewPromo.getBackground().setAlpha(0);
        } else {
            cardViewPromo.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        }
        Button buttonTutup = promo.findViewById(R.id.promo_tutup);
        buttonTutup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promo.dismiss();
            }
        });
        promo.show();
    }

    private void jalankanServiceStreamig(){
        Bundle bundle = new Bundle();
        bundle.putString("url", "http://122.248.39.157:8000");
        bundle.putString("name", "Radio Streaming On Air");
        Intent intent = new Intent(this, StreamingService.class);
        intent.putExtras(bundle);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isMyServiceRunning()){
            buttonPlay.setVisibility(View.GONE);
            buttonStop.setVisibility(View.VISIBLE);
        } else {
            buttonStop.setVisibility(View.GONE);
            buttonPlay.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        infokanKeUser("Tekan sekali lagi untuk keluar dari Aplikasi");

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent("exit"));
        unregisterReceiver(broadcastReceiver);
    }
}
