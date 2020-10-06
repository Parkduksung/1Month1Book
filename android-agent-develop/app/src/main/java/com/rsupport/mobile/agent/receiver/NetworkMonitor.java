package com.rsupport.mobile.agent.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class NetworkMonitor extends BroadcastReceiver {
    public final static int WIFI_STATE_DISABLED = 0x00;
    public final static int WIFI_STATE_DISABLING = WIFI_STATE_DISABLED + 1;
    public final static int WIFI_STATE_ENABLED = WIFI_STATE_DISABLING + 1;
    public final static int WIFI_STATE_ENABLING = WIFI_STATE_ENABLED + 1;
    public final static int WIFI_STATE_UNKNOWN = WIFI_STATE_ENABLING + 1;
    public final static int NETWORK_STATE_CONNECTED = WIFI_STATE_UNKNOWN + 1;
    public final static int NETWORK_STATE_CONNECTING = NETWORK_STATE_CONNECTED + 1;
    public final static int NETWORK_STATE_DISCONNECTED = NETWORK_STATE_CONNECTING + 1;
    public final static int NETWORK_STATE_DISCONNECTING = NETWORK_STATE_DISCONNECTED + 1;
    public final static int NETWORK_STATE_SUSPENDED = NETWORK_STATE_DISCONNECTING + 1;
    public final static int NETWORK_STATE_UNKNOWN = NETWORK_STATE_SUSPENDED + 1;

    public interface OnChangeNetworkStatusListener {
        public void OnChanged(int status);
    }

    private WifiManager m_WifiManager = null;
    private ConnectivityManager m_ConnManager = null;
    private OnChangeNetworkStatusListener m_OnChangeNetworkStatusListener = null;

    public NetworkMonitor(Context context) {
        m_WifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        m_ConnManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public void setOnChangeNetworkStatusListener(
            OnChangeNetworkStatusListener listener) {
        m_OnChangeNetworkStatusListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (m_OnChangeNetworkStatusListener == null) {
            return;
        }

        String strAction = intent.getAction();

        if (strAction.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            switch (m_WifiManager.getWifiState()) {
                case WifiManager.WIFI_STATE_DISABLED:
                    m_OnChangeNetworkStatusListener.OnChanged(WIFI_STATE_DISABLED);
                    break;

                case WifiManager.WIFI_STATE_DISABLING:
                    m_OnChangeNetworkStatusListener.OnChanged(WIFI_STATE_DISABLING);
                    break;

                case WifiManager.WIFI_STATE_ENABLED:
                    m_OnChangeNetworkStatusListener.OnChanged(WIFI_STATE_ENABLED);
                    break;

                case WifiManager.WIFI_STATE_ENABLING:
                    m_OnChangeNetworkStatusListener.OnChanged(WIFI_STATE_ENABLING);
                    break;

                case WifiManager.WIFI_STATE_UNKNOWN:
                    m_OnChangeNetworkStatusListener.OnChanged(WIFI_STATE_UNKNOWN);
                    break;
            }
        } else if (strAction.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo networkInfo = m_ConnManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if ((networkInfo != null) && (networkInfo.isAvailable() == true)) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    m_OnChangeNetworkStatusListener
                            .OnChanged(NETWORK_STATE_CONNECTED);
                } else if (networkInfo.getState() == NetworkInfo.State.CONNECTING) {
                    m_OnChangeNetworkStatusListener
                            .OnChanged(NETWORK_STATE_CONNECTING);
                } else if (networkInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                    m_OnChangeNetworkStatusListener
                            .OnChanged(NETWORK_STATE_DISCONNECTED);
                } else if (networkInfo.getState() == NetworkInfo.State.DISCONNECTING) {
                    m_OnChangeNetworkStatusListener
                            .OnChanged(NETWORK_STATE_DISCONNECTING);
                } else if (networkInfo.getState() == NetworkInfo.State.SUSPENDED) {
                    m_OnChangeNetworkStatusListener
                            .OnChanged(NETWORK_STATE_SUSPENDED);
                } else if (networkInfo.getState() == NetworkInfo.State.UNKNOWN) {
                    m_OnChangeNetworkStatusListener
                            .OnChanged(NETWORK_STATE_UNKNOWN);
                }
            }
        }
    }
}
