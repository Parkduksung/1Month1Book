package com.rsupport.mobile.agent.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class CNetStatus {
    public static final int NET_TYPE_NONE = 0x00;
    public static final int NET_TYPE_WIFI = 0x01;
    public static final int NET_TYPE_WIFI_P2P = 0x02;
    public static final int NET_TYPE_3G = 0x03;
    public static final int NET_TYPE_WIBRO = 0x04;
    private static CNetStatus current = null;

    public static CNetStatus getInstance() {
        if (current == null)
            current = new CNetStatus();
        return current;
    }

    public boolean getWifiStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (ni == null) return false;
        boolean isConn = ni.isConnected();
        return isConn;
    }

    public boolean get3GStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (ni == null) return false;
        boolean isConn = ni.isConnected();
        return isConn;
    }

    public boolean getWIBROStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
        if (ni == null) return false;
        boolean isConn = ni.isConnected();
        return isConn;
    }

    public int getNetType(Context context) {
        int netType = CNetStatus.NET_TYPE_NONE;

        if (getWifiStatus(context)) {
            netType = CNetStatus.NET_TYPE_WIFI;
        } else if (get3GStatus(context)) {
            netType = CNetStatus.NET_TYPE_3G;
        } else if (getWIBROStatus(context)) {
            netType = CNetStatus.NET_TYPE_WIBRO;
        }

        return netType;
    }
}
