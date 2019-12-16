package com.surampaksakosoy.ydig4.fragments;

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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.internal.ImageRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.surampaksakosoy.ydig4.R;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class StreamingFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "StreamingFragment";
    private ListenerStreaming listener;
    private Button buttonPlay;
    private Button buttonStop;
    private Button streaming_sendpesan;
    private ProgressBar progress_play, progressbar_send;
    private TextView judul_kajian, pemateri;
    private CircleImageView ustad_photo;
    private List<ModelStreaming> modelStreaming;
    private LinearLayoutManager linearLayoutManager;
    private RelativeLayout rel_serverdown;
    private LinearLayout ll_serverup, ll_nochat, popup_sendbutton;
    private AdapterStreaming adapterStreaming;
    private RecyclerView streaming_recyclerview;
    private CircleImageView imageView;
    private DBHandler dbHandler;
    private String ID_LOGIN, SUMBER_LOGIN;
    private FirebaseAuth mAuth;
    private EditText editTextPesan;
    private CardView cv_pesanBaru;

    public interface ListenerStreaming{
        void inputStreaming(CharSequence input);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof StreamingFragment.ListenerStreaming) {
            listener = (StreamingFragment.ListenerStreaming) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ListenerLogFragments");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public StreamingFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_streaming, container, false);
        buttonPlay = view.findViewById(R.id.buttonPlay); buttonPlay.setOnClickListener(this);
        buttonStop = view.findViewById(R.id.buttonStop); buttonStop.setOnClickListener(this);
        Button btn_komentar = view.findViewById(R.id.btn_komentar);
        btn_komentar.setOnClickListener(this);
        Button btn_pertanyaan = view.findViewById(R.id.btn_pertanyaan);
        btn_pertanyaan.setOnClickListener(this);
        Button btn_saran = view.findViewById(R.id.btn_saran);
        btn_saran.setOnClickListener(this);
        progress_play = view.findViewById(R.id.progress_play);
        judul_kajian = view.findViewById(R.id.judul_kajian);
        pemateri = view.findViewById(R.id.pemateri);
        ustad_photo = view.findViewById(R.id.ustad_photo);
        streaming_recyclerview = view.findViewById(R.id.streaming_recyclerview);
        rel_serverdown = view.findViewById(R.id.rel_serverdown);
        ll_serverup = view.findViewById(R.id.ll_serverup);
        imageView = view.findViewById(R.id.nav_image_view);
        dbHandler = new DBHandler(Objects.requireNonNull(getActivity()).getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        streaming_sendpesan = view.findViewById(R.id.streaming_sendpesan); streaming_sendpesan.setOnClickListener(this);
        ll_nochat = view.findViewById(R.id.ll_nochat);
        popup_sendbutton = view.findViewById(R.id.popup_sendbutton);
        RelativeLayout main_layout = view.findViewById(R.id.main_layout);
        editTextPesan = view.findViewById(R.id.streaming_edittext);
        progressbar_send = view.findViewById(R.id.progressbar_send);
        cv_pesanBaru = view.findViewById(R.id.cv_pesan_baru); cv_pesanBaru.setOnClickListener(this);
        daftarkanBroadcast();
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
        jalankanStreaming();
        streaming_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (linearLayoutManager.findFirstVisibleItemPosition() == 0){
                    cv_pesanBaru.setVisibility(View.GONE);
                }
            }
        });
        return view;
    }

    private void jalankanStreaming() {
        if (isMyServiceRunning()){
            new ServiceStreaming().execute();
        } else {
            buttonPlay.setVisibility(View.GONE);
            buttonStop.setVisibility(View.VISIBLE);
            progress_play.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (judul_kajian.getText().equals("")){
            getTitleStreaming();
        }
    }

    private void getTitleStreaming() {
        HandlerServer handlerServer = new HandlerServer(Objects.requireNonNull(getActivity()).getApplicationContext(), PublicAddress.GET_TITLE_KAJIAN);
        synchronized (this){
            handlerServer.getStatusServer(new VolleyCallback() {
                @Override
                public void onFailed(String result) {
                    if (result.equals("kosong")){
                        judul_kajian.setVisibility(View.GONE);
                        pemateri.setVisibility(View.GONE);
                        ustad_photo.setVisibility(View.GONE);
                    } else {
                        Log.e(TAG, "onFailed: "+ result);
                    }
                }

                @Override
                public void onSuccess(JSONArray jsonArray) {
                    JSONObject jsonObject;
                    try {
                        jsonObject = jsonArray.getJSONObject(0);
                        String data_kajian = jsonObject.getString("kajian");
                        String data_pemateri = jsonObject.getString("pemateri");
                        String data_photo = jsonObject.getString("photo");
                        judul_kajian.setText(data_kajian);
                        pemateri.setText(data_pemateri);
                        judul_kajian.setVisibility(View.VISIBLE);
                        pemateri.setVisibility(View.VISIBLE);
                        if (!data_photo.equals("")){
                            Glide.with(Objects.requireNonNull(getActivity())
                                    .getApplicationContext()).load(PublicAddress.BASEURLPHOTOASATID+data_photo)
                                    .placeholder(R.drawable.ic_account)
                                    .into(ustad_photo);
                            ustad_photo.setVisibility(View.VISIBLE);
                        } else {
                            ustad_photo.setVisibility(View.GONE);
                        }
                        streaming_recyclerview.setVisibility(View.VISIBLE);
                        rel_serverdown.setVisibility(View.GONE);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonPlay:
                if (buttonPlay.getText().toString().equals("Lanjutkan")){
                    Objects.requireNonNull(getActivity()).getApplicationContext().sendBroadcast(new Intent("start"));
                } else {
                    if (isMyServiceRunning()){
                        new ServiceStreaming().execute();
                    } else {
                        Log.e(TAG, "onClick: service already Running");
                    }
                }

                break;
            case R.id.buttonStop:
                progress_play.setVisibility(View.VISIBLE);
                buttonStop.setVisibility(View.GONE);
                Intent intent = new Intent("exit");
                Objects.requireNonNull(getActivity()).sendBroadcast(intent);
                break;
            case R.id.streaming_sendpesan:
                if (editTextPesan.length() != 0){
                    popup_sendbutton.setVisibility(View.VISIBLE);
                }
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
            case R.id.cv_pesan_baru:
                linearLayoutManager.scrollToPosition(0);
                cv_pesanBaru.setVisibility(View.GONE);
                break;
        }
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) Objects.requireNonNull(getActivity()).getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (StreamingService.class.getName().equals(service.service.getClassName())) {
                return false;
            }
        }
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    private class ServiceStreaming extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "onPreExecute: ");
            progress_play.setVisibility(View.VISIBLE);
            buttonPlay.setVisibility(View.GONE);
            buttonStop.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            Log.e(TAG, "doInBackground: ");
            HandlerServer handlerServer = new HandlerServer(Objects.requireNonNull(getActivity()).getApplicationContext(), PublicAddress.GET_STATUS_SERVER);
                handlerServer.getStatusServer(new VolleyCallback() {
                    @Override
                    public void onFailed(String result) {
                        processResultStatusServer(result);
                    }

                    @Override
                    public void onSuccess(JSONArray jsonArray) {
                        Log.e(TAG, "onSuccess: " + jsonArray);
                    }
                });
            return null;
        }
    }

    private void processResultStatusServer(String result) {
        Log.e(TAG, "processResultStatusServer: " + result);
        if (result.equals("1")){
            jalankanServiceStreamig();
            lanjutkankeChatting();
        } else {
            buttonPlay.setVisibility(View.VISIBLE);
            buttonStop.setVisibility(View.GONE);
            progress_play.setVisibility(View.GONE);
            listener.inputStreaming("nostreaming");
            rel_serverdown.setVisibility(View.VISIBLE);
            ll_serverup.setVisibility(View.GONE);
        }
    }

    private void jalankanServiceStreamig(){

        Bundle bundle = new Bundle();
        bundle.putString("url", "http://122.248.39.157:8000");
        bundle.putString("name", "Radio Streaming On Air");
        Intent intent = new Intent(Objects.requireNonNull(getActivity()).getApplicationContext(), StreamingService.class);
        intent.putExtras(bundle);
        getActivity().startService(intent);
        rel_serverdown.setVisibility(View.GONE);
        ll_serverup.setVisibility(View.VISIBLE);
    }

    private void daftarkanBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("mediaplayed");
        filter.addAction("mediastoped");
        filter.addAction("lemot");
        filter.addAction("streamingError");
        filter.addAction("datakajian");
        filter.addAction("PESANBARU");
        filter.addAction("errorsenddata");
        filter.addAction("pausePlayer");
        Objects.requireNonNull(getActivity()).registerReceiver(broadcastReceiver, filter);
    }


    private int countError = 0;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String param = intent.getAction();
            Log.e(TAG, "onReceive: "+param);
            assert param != null;
            switch (param){
                case "mediaplayed":
                    progress_play.setVisibility(View.GONE);
                    buttonStop.setVisibility(View.VISIBLE);
                    buttonPlay.setVisibility(View.GONE);
                    break;
                case "mediastoped":
                    buttonPlay.setText(R.string.play);
                    buttonStop.setVisibility(View.GONE);
                    progress_play.setVisibility(View.GONE);
                    buttonPlay.setVisibility(View.VISIBLE);
                    break;
                case "lemot":
                    String bufferCode = intent.getStringExtra("lemot");
                    assert bufferCode != null;
                    if (bufferCode.equals("703")){
                        Log.e(TAG, "onReceive: Sedang buffering 703");
                        progress_play.setVisibility(View.VISIBLE);
                        buttonPlay.setVisibility(View.GONE);
                        buttonStop.setVisibility(View.GONE);
                        HandlerServer handlerServer = new HandlerServer(Objects.requireNonNull(getActivity()).getApplicationContext(), PublicAddress.GET_STATUS_SERVER);
                        handlerServer.getStatusServer(new VolleyCallback() {
                            @Override
                            public void onFailed(String result) {
                                if (result.equals("0")){
                                    getActivity().sendBroadcast(new Intent("exit"));
                                    listener.inputStreaming("kajianberakhir");
                                    ll_serverup.setVisibility(View.GONE);
                                    rel_serverdown.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onSuccess(JSONArray jsonArray) {
                                Log.e(TAG, "onSuccess: " + jsonArray);
                            }
                        });
                    }
                    if (bufferCode.equals("702")){
                        Log.e(TAG, "onReceive: Buffer Completed 702");
                        progress_play.setVisibility(View.GONE);
                        buttonPlay.setVisibility(View.GONE);
                        buttonStop.setVisibility(View.VISIBLE);
                    }
                    if (bufferCode.equals("701")){
                        Log.e(TAG, "onReceive: Buffer Completed 701");
                        progress_play.setVisibility(View.GONE);
                        buttonPlay.setVisibility(View.GONE);
                        buttonStop.setVisibility(View.VISIBLE);
                    }
                    break;
                case "streamingError":
                    countError = countError+1;
                    if (countError <= 3){
                        if (isMyServiceRunning()){
                            new ServiceStreaming().execute();
                        }
                    } else {
                        listener.inputStreaming("streamingError");
                        buttonPlay.setVisibility(View.VISIBLE);
                        buttonStop.setVisibility(View.GONE);
                        progress_play.setVisibility(View.GONE);
                    }
                    Log.e(TAG, "onReceive: " + countError);
                    break;
                case "datakajian":
                    jalankanStreaming();
                    getTitleStreaming();
                    break;
                case "PESANBARU":
                    ArrayList<String> dataPesan = intent.getStringArrayListExtra("DATANOTIF");
                    assert dataPesan != null;
                    pesanBaruDatang(dataPesan);
                    break;
                case "errorsenddata":
                    editTextPesan.setText("");
                    editTextPesan.setEnabled(true);
                    progressbar_send.setVisibility(View.GONE);
                    streaming_sendpesan.setVisibility(View.VISIBLE);
                    break;
                case "pausePlayer":
                    Log.e(TAG, "onReceive: Pause Media");
                    buttonPlay.setText(R.string.lanjutkan);
                    buttonStop.setVisibility(View.GONE);
                    buttonPlay.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    private void pesanBaruDatang(ArrayList<String> dataPesan) {
        ModelStreaming item = (new ModelStreaming(
                Integer.parseInt(dataPesan.get(0)),dataPesan.get(1),dataPesan.get(2),dataPesan.get(3),dataPesan.get(4),dataPesan.get(5),dataPesan.get(6),Integer.parseInt(dataPesan.get(7))
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
            streaming_sendpesan.setVisibility(View.VISIBLE);
            editTextPesan.setEnabled(true);
        } else {
            loadingDataChatting();
        }
    }

    private void lanjutkankeChatting() {

        // Get Data From DB
        getDataFromDB();
        // Get Photo Profile
        getPhotoProfile();

        // Load Chat Data From Server
        loadingDataChatting();
    }

    private void getDataFromDB() {
        ArrayList<HashMap<String, String>> userDB = dbHandler.getUser(1);
        for (Map<String,String> map : userDB){
            SUMBER_LOGIN = map.get("sumber_login");
            ID_LOGIN = map.get("id_login");
//            NAMA = map.get("nama");
//            EMAIL = map.get("email");
//            VERSI = map.get("version");
        }
    }

    private void getPhotoProfile() {
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
    }

    private void loadingDataChatting() {
        List<String> list =new ArrayList<>();
        list.add("0");
        HandlerServer handlerServer = new HandlerServer(Objects.requireNonNull(getActivity()).getApplicationContext(), PublicAddress.LOAD_COMMENT_DATA);
        synchronized (this){
            handlerServer.sendDataToServer(new VolleyCallback() {
                @Override
                public void onFailed(String result) {
                    Log.e(TAG, "onFailed: "+ result);
                    if (result.equals("nodata")){
                        ll_nochat.setVisibility(View.VISIBLE);
                        streaming_recyclerview.setVisibility(View.INVISIBLE);
                    } else {
                        listener.inputStreaming("chatgagal");
                    }
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
                        isiData.getString("uniq_id"),
                        Integer.parseInt(dataServer.getString("type_pesan"))
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
            linearLayoutManager = new LinearLayoutManager(Objects.requireNonNull(getActivity()).getApplicationContext());
            adapterStreaming =new AdapterStreaming(modelStreaming, getActivity().getApplicationContext(), ID_LOGIN);
            streaming_recyclerview.setAdapter(adapterStreaming);
            streaming_recyclerview.setLayoutManager(linearLayoutManager);
            streaming_recyclerview.setItemAnimator(new DefaultItemAnimator());
            ll_nochat.setVisibility(View.GONE);
            streaming_recyclerview.setVisibility(View.VISIBLE);
        }
    }

    private void hidePopupandKeyboard(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null){
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        popup_sendbutton.setVisibility(View.GONE);
    }

    private void kirimPesan(String s) {
        popup_sendbutton.setVisibility(View.GONE);
        String pesan = editTextPesan.getText().toString();
        if (!pesan.equals("")){
            progressbar_send.setVisibility(View.VISIBLE);
            streaming_sendpesan.setVisibility(View.GONE);
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
            HandlerServer handlerServer = new HandlerServer(Objects.requireNonNull(getActivity()).getApplicationContext(), PublicAddress.SEND_COMMENT_DATA);
            synchronized (this){
                handlerServer.sendDataToServer(new VolleyCallback() {
                    @Override
                    public void onFailed(String result) {
                        if (!result.contains("berhasil")){
                            progressbar_send.setVisibility(View.GONE);
                            streaming_sendpesan.setVisibility(View.VISIBLE);
                            editTextPesan.setEnabled(true);
                            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Gagal Mengirim Pesan, Silahkan Coba lagi", Toast.LENGTH_SHORT).show();
                        } else {
                            progressbar_send.setVisibility(View.GONE);
                            streaming_sendpesan.setVisibility(View.VISIBLE);
                            editTextPesan.setText("");
                            editTextPesan.setEnabled(true);
                        }
                    }

                    @Override
                    public void onSuccess(JSONArray jsonArray) {
                        Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Pesan Terkirim", Toast.LENGTH_SHORT).show();
                    }
                }, list);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(getActivity()).unregisterReceiver(broadcastReceiver);
        judul_kajian.setText("");
        judul_kajian.setVisibility(View.GONE);
    }
}
