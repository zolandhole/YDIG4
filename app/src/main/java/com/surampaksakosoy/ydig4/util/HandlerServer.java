package com.surampaksakosoy.ydig4.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.surampaksakosoy.ydig4.util.PublicAddress.SEND_COMMENT_DATA;

public class HandlerServer {
    private Context context;
    private String alamatServer;
    private static final String TAG = "HandlerServer";

    public HandlerServer(Context context, String alamatServer) {
        this.context = context;
        this.alamatServer = alamatServer;
    }

    public void sendDataToServer(final VolleyCallback callback, final List<String> list) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, alamatServer,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.optString("error").equals("true")) {
                                callback.onFailed(jsonObject.getString("pesan"));
                            } else if (jsonObject.optString("error").equals("false")){
                                JSONArray jsonArray = jsonObject.getJSONArray("pesan");
                                callback.onSuccess(jsonArray);

                            }
                        } catch (JSONException e) {
                            callback.onFailed(String.valueOf(e));
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: " + error);
                        if (String.valueOf(error).equals("com.android.volley.TimeoutError")){
                            Toast.makeText(context, "Tidak dapat menghubungi Server, Hubungi IT YDIG", Toast.LENGTH_SHORT).show();
                        }
                        if (alamatServer.equals(SEND_COMMENT_DATA)){
                            context.sendBroadcast(new Intent("errorsenddata"));
                        }
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("params", String.valueOf(list));
                Log.e(TAG, "getParams: " + list);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }
}
