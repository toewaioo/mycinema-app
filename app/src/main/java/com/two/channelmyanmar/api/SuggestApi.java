package com.two.channelmyanmar.api;

import android.util.Log;

import com.two.channelmyanmar.model.SuggestModel;
import com.two.channelmyanmar.viewmodel.ApiViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class SuggestApi implements Runnable , ApiViewModel.BaseApi {
    HttpURLConnection conn;
    Callback callback;

    @Override
    public void addListener(Callback callback) {
        this.callback = callback;

    }

    public interface CallBack {
        void onFail(String s);
        void onSuccess(ArrayList<SuggestModel> result);
    }

    private static final CopyOnWriteArrayList<CallBack> listeners = new CopyOnWriteArrayList<>();

    public static void addListener(CallBack listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void removeListener(CallBack listener) {
        listeners.remove(listener);
    }

    public SuggestApi() {
        // Empty constructor as listeners are now added via addListener()
    }

    @Override
    public void run() {
        Log.d("SuggestApi","calling api..");
        BufferedReader reader;
        String line;
        StringBuilder responseContent = new StringBuilder();
        try {
            URL u = new URL("https://php-on-vercel-ashen.vercel.app/api");
            conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int status = conn.getResponseCode();

            if (status / 100 == 3) {
                URL redirectUrl = new URL(conn.getHeaderField("Location"));
                conn = (HttpURLConnection) redirectUrl.openConnection();
            }

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                responseContent.append(line).append("\n");
            }
            reader.close();

            JSONObject o = new JSONObject(responseContent.toString());
            JSONArray arr= o.getJSONArray("items");
            ArrayList<SuggestModel> resultList = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.getJSONObject(i);
                resultList.add(new SuggestModel(item.getString("url"), item.getString("link")));
            }

            // Notify all listeners of success
            for (CallBack listener : listeners) {
                listener.onSuccess(resultList);
            }
            Log.d("Suggestapi","working..");
            callback.onSuccess(resultList);

        } catch (Exception e) {
            // Notify all listeners of failure
            String errorMsg = e.toString();
            callback.onError(e.toString());
            for (CallBack listener : listeners) {
                listener.onFail(errorMsg);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}