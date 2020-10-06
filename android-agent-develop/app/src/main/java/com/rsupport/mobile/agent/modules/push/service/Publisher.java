package com.rsupport.mobile.agent.modules.push.service;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.rsupport.mobile.agent.modules.push.IPushMessaging;
import com.rsupport.mobile.agent.receiver.AgentPushReceiver;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import config.EngineConfigSetting;

import com.rsupport.mobile.agent.constant.GlobalStatic;
import com.rsupport.util.log.RLog;

public class Publisher implements Runnable, MqttCallback, IPushMessaging {
    // second
    private final int KEEP_ALIVE = 60 / 3;
    private String identityKey = null;
    private String topicFilter = null;
    private MqttClient client = null;
    private Context context = null;
    private OnConnectLostListener connectLostListener = null;

    private String willTopic = null;
    private String willMsg = null;

    private boolean isAgentPush = true;

    public Publisher(Context context, String serverURI, String topicFilter, String clientId) {
        this.context = context;
        this.topicFilter = topicFilter;
        this.identityKey = new Identity().create(context, topicFilter);
        try {
            client = new MqttClient(serverURI, clientId, new MemoryPersistence());
        } catch (MqttException e) {
            RLog.e(e);
        }

        isAgentPush = agentTopicCheck(topicFilter);
    }

    public void close() {
        RLog.v("close");
        synchronized (this) {
            try {
                client.close();
                client = null;
            } catch (Exception e) {
                RLog.e(e);
            }
            context = null;
            connectLostListener = null;
            topicFilter = null;
            identityKey = null;
        }
    }

    public void disconnect() {
        try {
            if (client.isConnected()) {
                client.unsubscribe(topicFilter);
                client.disconnect();
            }
        } catch (Exception e) {
            RLog.w(e);
        }
        sendBroadCastRSPushReceiver(isAgentPush ? TYPE_DISCONNECTED : TYPE_VERSION_DISCONNECTED);
    }

    public String getIdentityKey() {
        return identityKey;
    }

    @Override
    public void connectionLost(Throwable cause) {
        if (connectLostListener != null) {
            connectLostListener.connectLost(getIdentityKey());
        }
        sendBroadCastRSPushReceiver(isAgentPush ? TYPE_CONNECTION_LOST : TYPE_VERSION_CONNECTION_LOST, getIdentityKey().getBytes(EngineConfigSetting.UTF_8));
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        isAgentPush = agentTopicCheck(topic);
        sendBroadCastRSPushReceiver(isAgentPush ? TYPE_MSG_ARRIVED : TYPE_VERSION_MSG_ARRIVED, message.getPayload());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        RLog.i("deliveryComplete do not thing");
    }

    public boolean agentTopicCheck(String topic) {
        if (topic == null) return false;
        return topic.contains("agent");
    }

    @Override
    public void run() {
        try {
            synchronized (this) {
                if (TextUtils.isEmpty(topicFilter)) return;

                RLog.v("Publisher try to connect(" + topicFilter + ")");
                if (client != null) {
                    RLog.v("client(" + topicFilter + "): " + client.isConnected());
                    if (!client.isConnected()) {
                        MqttConnectOptions options = new MqttConnectOptions();
                        options.setKeepAliveInterval(KEEP_ALIVE);
                        options.setUserName("90MpdA516uPP79ldh6SNow==-R9SqEAHmNh7Sy5VQ685G7Q==");
                        client.setCallback(this);
                        client.connect(options);
                        client.subscribe(topicFilter);
                        isAgentPush = agentTopicCheck(topicFilter);
                        sendBroadCastRSPushReceiver(isAgentPush ? TYPE_CONNECTED : TYPE_VERSION_CONNECTED);
                    }
                }
                RLog.v("Publisher end to connect");
            }
        } catch (Exception e) {
            RLog.e(e);
            if (connectLostListener != null) {
                connectLostListener.connectLost(getIdentityKey());
            }
            isAgentPush = agentTopicCheck(topicFilter);
            sendBroadCastRSPushReceiver(isAgentPush ? TYPE_CONNECT_ERROR : TYPE_VERSION_CONNECT_ERROR);
            GlobalStatic.g_err = "PushServer Connection Error Try Again..";
        }
    }

    private void sendBroadCastRSPushReceiver(int type) {
        sendBroadCastRSPushReceiver(type, null);
    }

    private void sendBroadCastRSPushReceiver(int type, byte[] message) {
        if (context == null) {
            RLog.w("sendBroadCastRSPushReceiver. context is null!");
            return;
        }
        RLog.v("sendBroadCastRSPushReceiver type." + type);
        Intent intent = new Intent(context, AgentPushReceiver.class);
        intent.setAction(ACTION_PUSH_MESSAGING);
        intent.putExtra(EXTRA_KEY_TYPE, type);
        intent.putExtra(EXTRA_KEY_VALUE, message);
        context.sendBroadcast(intent);
    }

    public void setOnConnectLostListener(OnConnectLostListener connectLostListener) {
        this.connectLostListener = connectLostListener;
    }
}
