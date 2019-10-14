package com.surampaksakosoy.ydig4.fragments;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.surampaksakosoy.ydig4.R;
import com.surampaksakosoy.ydig4.adapters.AdapterStreaming;
import com.surampaksakosoy.ydig4.models.ModelStreaming;
import com.surampaksakosoy.ydig4.services.StreamingService;
import com.surampaksakosoy.ydig4.util.HandlerServer;
import com.surampaksakosoy.ydig4.util.PublicAddress;
import com.surampaksakosoy.ydig4.util.VolleyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class StreamingFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "StreamingFragment";
    private ListenerStreaming listener;
    private Button buttonPlay, buttonStop;
    private ProgressBar progress_play;
    private TextView judul_kajian, pemateri;
    private CircleImageView ustad_photo;
    private List<ModelStreaming> modelStreaming;
    private LinearLayoutManager linearLayoutManager;
    private AdapterStreaming adapterStreaming;
    private RecyclerView streaming_recyclerview;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_streaming, container, false);
        buttonPlay = view.findViewById(R.id.buttonPlay); buttonPlay.setOnClickListener(this);
        buttonStop = view.findViewById(R.id.buttonStop); buttonStop.setOnClickListener(this);
        progress_play = view.findViewById(R.id.progress_play);
        judul_kajian = view.findViewById(R.id.judul_kajian);
        pemateri = view.findViewById(R.id.pemateri);
        ustad_photo = view.findViewById(R.id.ustad_photo);
        streaming_recyclerview = view.findViewById(R.id.streaming_recyclerview);
        jalankanStreaming();
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
        daftarkanBroadcast();
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
                        judul_kajian.setText(data_kajian);
                        pemateri.setText(data_pemateri);
                        judul_kajian.setVisibility(View.VISIBLE);
                        pemateri.setVisibility(View.VISIBLE);
                        ustad_photo.setVisibility(View.VISIBLE);
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
        Objects.requireNonNull(getActivity()).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonPlay:
                if (isMyServiceRunning()){
                    new ServiceStreaming().execute();
                } else {
                    Log.e(TAG, "onClick: service already Running");
                }
                break;
            case R.id.buttonStop:
                progress_play.setVisibility(View.VISIBLE);
                buttonStop.setVisibility(View.GONE);
                Intent intent = new Intent("exit");
                Objects.requireNonNull(getActivity()).sendBroadcast(intent);
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
        } else {
            buttonPlay.setVisibility(View.VISIBLE);
            buttonStop.setVisibility(View.GONE);
            progress_play.setVisibility(View.GONE);
            listener.inputStreaming("nostreaming");
        }
    }

    private void jalankanServiceStreamig(){

        Bundle bundle = new Bundle();
        bundle.putString("url", "http://122.248.39.157:8020");
        bundle.putString("name", "Radio Streaming On Air");
        Intent intent = new Intent(Objects.requireNonNull(getActivity()).getApplicationContext(), StreamingService.class);
        intent.putExtras(bundle);
        getActivity().startService(intent);
    }

    private void daftarkanBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("mediaplayed");
        filter.addAction("mediastoped");
        filter.addAction("lemot");
        filter.addAction("streamingError");
        filter.addAction("getDataKajian");
        Objects.requireNonNull(getActivity()).registerReceiver(broadcastReceiver, filter);
    }
    private void loadingDataChatting() {
        List<String> list =new ArrayList<>();
        list.add("0");
        HandlerServer handlerServer = new HandlerServer(Objects.requireNonNull(getActivity()).getApplicationContext(), PublicAddress.LOAD_COMMENT_DATA);
        synchronized (this){
            handlerServer.sendDataToServer(new VolleyCallback() {
                @Override
                public void onFailed(String result) {
                    listener.inputStreaming("chatgagal");
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
            linearLayoutManager = new LinearLayoutManager(Objects.requireNonNull(getActivity()).getApplicationContext());
//            adapterStreaming =new AdapterStreaming(modelStreaming, this, ID_LOGIN);
            streaming_recyclerview.setAdapter(adapterStreaming);
            streaming_recyclerview.setLayoutManager(linearLayoutManager);
            streaming_recyclerview.setItemAnimator(new DefaultItemAnimator());
        }
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
                    loadingDataChatting();
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
                case "getDataKajian":
                    getTitleStreaming();
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        judul_kajian.setText("");
        judul_kajian.setVisibility(View.GONE);
    }
}
