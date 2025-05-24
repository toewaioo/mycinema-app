package com.two.channelmyanmar.services;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;


import com.two.channelmyanmar.preference.PreferenceHelper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChekIp implements  Runnable {
    Context context;
    PreferenceHelper helper;

    public ChekIp(Context context){
        this.context=context;
        this.helper=new PreferenceHelper(context);


    }
    @Override
    public void run() {
        try {
            URL uri = new URL("https://api.myip.com");
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
            conn.setRequestMethod("GET");
            int responerCode = conn.getResponseCode();
            if (responerCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                StringBuilder respone = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    respone.append(line);

                }
                String jsonData = respone.toString();
                JSONObject obj = new JSONObject(jsonData);
                System.out.println("Check ip:" + obj.getString("country"));
                if (obj.getString("country").equals("Myanmar")) {
                   // return false;
                    helper.needApi(true);
                } else {
                   // return true;
                    helper.needApi(false);
                }
            } else {
               // return false;
                helper.needApi(true);
            }
        }catch (Exception e){
            e.printStackTrace();
            helper.needApi(true);

        }
    }


}
