package com.surampaksakosoy.ydig4;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.surampaksakosoy.ydig4.adapters.pagerAdapter;
import com.surampaksakosoy.ydig4.fragments.ProfileFragment;
import com.surampaksakosoy.ydig4.fragments.StreamingFragment;
import com.surampaksakosoy.ydig4.util.DBHandler;
import com.surampaksakosoy.ydig4.util.HandlerServer;
import com.surampaksakosoy.ydig4.util.PublicAddress;
import com.surampaksakosoy.ydig4.util.VolleyCallback;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity
implements ProfileFragment.ListenerProfile, StreamingFragment.ListenerStreaming {

    private static final String TAG = "HomeActivity";
    private TextView titleBar, customTab0, customTab1, customTab2;
    private String IDLOGIN, SUMBERLOGIN;
    private DBHandler dbHandler;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initLisener();
        setupToolbar();
        setupTabLayout();
        IDLOGIN = checkLocalUserDB();
        if (IDLOGIN == null){
            backToLoginActivity();
        }
    }

    private void backToLoginActivity() {
        dbHandler.deleteDB();
        if (SUMBERLOGIN!= null){
            if (SUMBERLOGIN.equals("FACEBOOK")){
                LoginManager.getInstance().logOut();
            }
            if (SUMBERLOGIN.equals("GOOGLE")){
                firebaseAuth.signOut();
                googleSignInClient.signOut();
            }
            sendDataToServerToChangeStatusUser();
        }
        Intent intentStopStreaming = new Intent("exit");
        sendBroadcast(intentStopStreaming);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendDataToServerToChangeStatusUser() {
        List<String> list =new ArrayList<>();
        list.add(IDLOGIN);
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

    private void initLisener() {
        dbHandler = new DBHandler(this);
        firebaseAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupToolbar() {
        Toolbar toolbar_home = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar_home);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        titleBar = findViewById(R.id.titleBar);
    }

    private void setupViewPager(TabLayout tabLayout) {
        final ViewPager viewPager = findViewById(R.id.pagerAdapter);
        final PagerAdapter adapter = new pagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 0){
                    titleBar.setText(R.string.radio_streaming);
                    customTab0.setBackgroundResource(R.drawable.button_biru);
                    customTab1.setBackgroundResource(R.drawable.button_abu);
                    customTab2.setBackgroundResource(R.drawable.button_abu);
                } else if(position == 1){
                    titleBar.setText(R.string.tentang_kami);
                    customTab0.setBackgroundResource(R.drawable.button_abu);
                    customTab1.setBackgroundResource(R.drawable.button_biru);
                    customTab2.setBackgroundResource(R.drawable.button_abu);
                } else if (position == 2){
                    titleBar.setText(R.string.profile);
                    customTab0.setBackgroundResource(R.drawable.button_abu);
                    customTab1.setBackgroundResource(R.drawable.button_abu);
                    customTab2.setBackgroundResource(R.drawable.button_biru);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void infokanKeUser(String keterangan) {
        CoordinatorLayout mRoot =this.findViewById(R.id.layout_main);
        Snackbar snackbar = Snackbar.make(mRoot, keterangan, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.merahmarun));
        snackbar.show();
    }

    private String checkLocalUserDB() {
        ArrayList<HashMap<String, String>> userDB = dbHandler.getUser(1);
        for (Map<String,String> map : userDB){
            SUMBERLOGIN = map.get("sumber_login");
            IDLOGIN = map.get("id_login");
//            NAMA = map.get("nama");
//            EMAIL = map.get("email");
//            VERSI = map.get("version");
        }
        return IDLOGIN;
    }

    @SuppressLint("InflateParams")
    private void setupTabLayout() {
        TabLayout tabLayout = findViewById(R.id.tablayoutFragment);

        customTab0 = (TextView) LayoutInflater.from(HomeActivity.this).inflate(R.layout.layout_tab, null);
        customTab0.setText(R.string.radio_streaming);
        tabLayout.addTab(tabLayout.newTab());
        Objects.requireNonNull(tabLayout.getTabAt(0)).setCustomView(customTab0);

        customTab1 = (TextView) LayoutInflater.from(HomeActivity.this).inflate(R.layout.layout_tab, null);
        customTab1.setText(R.string.tentang_kami);
        tabLayout.addTab(tabLayout.newTab());
        Objects.requireNonNull(tabLayout.getTabAt(1)).setCustomView(customTab1);

        customTab2 = (TextView) LayoutInflater.from(HomeActivity.this).inflate(R.layout.layout_tab, null);
        customTab2.setText(R.string.profile);
        tabLayout.addTab(tabLayout.newTab());
        Objects.requireNonNull(tabLayout.getTabAt(2)).setCustomView(customTab2);

        setupViewPager(tabLayout);

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
    public void inputStreaming(CharSequence input) {
        if (input.equals("nostreaming")){
            infokanKeUser("Sumber streaming tidak ditemukan");
        } else if (input.equals("streamingError")){
            infokanKeUser("Streaming Terputus, silahkan ulangi kembali");
        }
    }
    @Override
    public void inputProfile(CharSequence input) {
        if (input.equals("logout")){
            backToLoginActivity();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent("exit"));
    }
}
