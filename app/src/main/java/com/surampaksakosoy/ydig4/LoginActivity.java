package com.surampaksakosoy.ydig4;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.surampaksakosoy.ydig4.models.ModelUser;
import com.surampaksakosoy.ydig4.util.DBHandler;
import com.surampaksakosoy.ydig4.util.HandlerServer;
import com.surampaksakosoy.ydig4.util.PublicAddress;
import com.surampaksakosoy.ydig4.util.VolleyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private LoginButton signInFacebook;
    private CallbackManager callbackManager;
    private String ID_LOGIN, NAMA, EMAIL, SUMBER_LOGIN, VERSI;
    private DBHandler dbHandler;
    private ProgressBar login_progressBar;

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initListener();
        getAppVersion();
    }

    private void getAppVersion() {
//        List<String> list = new ArrayList<>();
//        list.add("0");
//        HandlerServer handlerServer = new HandlerServer(this, PublicAddress.GET_VERSION);
//        synchronized (this){
//            handlerServer.sendDataToServer(new VolleyCallback() {
//                @Override
//                public void onFailed(String result) {
//                    Log.e(TAG, "onFailed: " + result);
//                }
//
//                @Override
//                public void onSuccess(JSONArray jsonArray) {
//                    JSONObject dataServer;
//                    for (int i=0; i< jsonArray.length(); i++){
//                        try {
//                            dataServer = jsonArray.getJSONObject(i);
////                            JSONObject isiData = dataServer.getJSONObject("version");
//                            Log.e(TAG, "onSuccess: Version: "+ dataServer.getString("version"));
//                            VERSI = dataServer.getString("version");
//                        } catch (JSONException e) {
//                            Log.e(TAG, "exception: " + e);
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }, list);
//        }

        VERSI = String.valueOf(BuildConfig.VERSION_CODE);
//        String versionName = BuildConfig.VERSION_NAME;
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            ID_LOGIN = user.getUid();
            NAMA = user.getDisplayName();
            EMAIL = user.getEmail();
            SUMBER_LOGIN = "GOOGLE";
            masukanKeDatabaseLokal();
        } else {
            infokanKeUser("Batal masuk menggunakan akun Google");
        }
    }

    private void initListener() {
        login_progressBar = findViewById(R.id.login_progressBar);
        Button button_facebook = findViewById(R.id.button_facebook);
        button_facebook.setOnClickListener(this);
        signInFacebook = findViewById(R.id.signInFacebook);
        signInFacebook.setReadPermissions(Arrays.asList("email","public_profile"));
        callbackManager = CallbackManager.Factory.create();
        Button button_google = findViewById(R.id.button_google);
        button_google.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
    }

    private void hubungkanAkunKeFacebook() {
        signInFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                login_progressBar.setVisibility(View.VISIBLE);
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                if (response != null){
                                    try {
                                        ID_LOGIN = object.getString("id");
                                        NAMA = object.getString("name");
                                        object.getString("email");
                                        EMAIL = object.getString("email");
                                        SUMBER_LOGIN = "FACEBOOK";
                                        masukanKeDatabaseLokal();
                                    } catch (JSONException e) {
                                        Log.e(TAG, "onCompleted: hubungkanAkunKeFacebook: " + e);
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields","id, name, email, gender, birthday, link, location");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "onCancel: hubungkanAkunKeFacebook");
                infokanKeUser("Batal masuk menggunakan akun Facebook");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "onError: " + error);
            }
        });
    }

    private void hubungkanAkunKeGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

//    private void revokeAccess() {
//        // Firebase sign out
//        mAuth.signOut();
//
//        // Google revoke access
//        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
//                new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        updateUI(null);
//                    }
//                });
//    }

    private void masukanKeDatabaseLokal() {
        dbHandler = new DBHandler(this);
        dbHandler.addUser(new ModelUser(1,SUMBER_LOGIN,ID_LOGIN,NAMA,EMAIL, VERSI));
        dbHandler.close();
        cekLokalDB();
    }

    private void cekLokalDB() {
        ArrayList<HashMap<String,String>> userDB = dbHandler.getUser(1);
        for (Map<String,String> map : userDB){
            ID_LOGIN = map.get("id_login");
        }
        if (ID_LOGIN != null){
            simpanDataKeServer();
        } else {
            infokanKeUser("Login Facebook Gagal");
        }
    }

    private void simpanDataKeServer() {
        if (ID_LOGIN!=null){
            if (SUMBER_LOGIN.equals("FACEBOOK")){
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
                Log.e(TAG, "simpanDataKeServer: FACEBOOK" + isLoggedIn);
            }

            List<String> list = new ArrayList<>();
            list.add(SUMBER_LOGIN);
            list.add(ID_LOGIN);
            list.add(NAMA);
            list.add(EMAIL);
            list.add("1");
            list.add(VERSI);
            HandlerServer handlerServer = new HandlerServer(this, PublicAddress.POST_LOGIN);
            synchronized (this){
                handlerServer.sendDataToServer(new VolleyCallback() {
                    @Override
                    public void onFailed(String result) {
                        if (!result.contains("Berhasil")) {
                            infokanKeUser("Gagal Menghubungi Server");
                            Log.e(TAG, "onFailed: " + result);
                            login_progressBar.setVisibility(View.GONE);
                        } else {
                            login_progressBar.setVisibility(View.GONE);
                            keMainActivity();
                        }
                    }

                    @Override
                    public void onSuccess(JSONArray jsonArray) {
                        Log.e(TAG, "onSuccess: " + jsonArray);
                        login_progressBar.setVisibility(View.GONE);
                    }
                },list);
            }
        } else {
            Log.e(TAG, "simpanDataKeServer: NULL ID :");
        }

    }

    private void keMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void infokanKeUser(String keterangan) {
        LinearLayout mRoot =this.findViewById(R.id.layout_login);
        Snackbar snackbar = Snackbar.make(mRoot, keterangan, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.merahmarun));
        snackbar.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            // GOOGLE
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                updateUI(null);
            }
        } else {
            //FACEBOOK
            callbackManager.onActivityResult(requestCode,resultCode,data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        login_progressBar.setVisibility(View.VISIBLE);
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            infokanKeUser("Gagal Login menggunakan Akun Google");
//                            updateUI(null);
                        }
                        login_progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (isNetworkAvailable()){
            switch (v.getId()){
                case R.id.button_facebook:
                    signInFacebook.performClick();
                    hubungkanAkunKeFacebook();
                    break;
                case R.id.button_google:
                    hubungkanAkunKeGoogle();
                    break;
            }
        } else {
            infokanKeUser("Tidak ada Koneksi Iinternet");
        }

    }
}
