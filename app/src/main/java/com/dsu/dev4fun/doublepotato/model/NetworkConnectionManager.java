package com.dsu.dev4fun.doublepotato.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class NetworkConnectionManager {
    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    public static boolean isConnected(Context context) {
        NetworkInfo info = NetworkConnectionManager.getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    public static boolean isConnectedWifi(Context context) {
        NetworkInfo info = NetworkConnectionManager.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public static boolean isConnectedMobile(Context context) {
        NetworkInfo info = NetworkConnectionManager.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }
}
