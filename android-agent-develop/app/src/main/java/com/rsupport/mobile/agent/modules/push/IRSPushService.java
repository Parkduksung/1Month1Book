package com.rsupport.mobile.agent.modules.push;

import android.content.Context;

import com.rsupport.mobile.agent.modules.push.delegate.RSPushNotificationDelegate;


public interface IRSPushService {
    public static final String EXTRA_KEY_SERVER_URL = "extra_key_server_url";
    public static final String EXTRA_KEY_SERVER_PORT = "extra_key_server_port";

    public void register(Context context, String topicFilter);

    public void unregister(Context context, String topicFilter);

    public void pushNotification(String topic, String message);

    public void pushNotification(String topic, byte[] message);

    public void setServerInfo(String privateAddress, int privatePort);

    public void setPushDelegate(RSPushNotificationDelegate pushDelegate);

    public void setPublisherReconnect(Boolean isReconnect);
}
