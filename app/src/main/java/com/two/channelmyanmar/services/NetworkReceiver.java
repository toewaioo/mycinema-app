package com.two.channelmyanmar.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isConnected = connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
        if (isConnected) {
            ExecutorService service = Executors.newFixedThreadPool(2);
            service.execute(new ChekIp(context));

        }
    }
}
