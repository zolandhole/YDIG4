package com.surampaksakosoy.ydig4.util;

import org.json.JSONArray;

public interface VolleyCallback {
    void onFailed(String result);
    void onSuccess(JSONArray jsonArray);
}
