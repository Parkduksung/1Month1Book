package com.rsupport.mobile.agent.cn.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rsupport.mobile.agent.service.AgentMainService;
import com.rsupport.mobile.agent.status.AgentStatus;
import com.rsupport.mobile.agent.utils.SdkVersion;

import org.koin.java.KoinJavaComponent;

public class AgentBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AgentStatus agentStatus = KoinJavaComponent.get(AgentStatus.class);

        if (agentStatus.get() == AgentStatus.AGENT_STATUS_LOGOUT || agentStatus.get() == AgentStatus.AGENT_STATUS_NOLOGIN) {
            return;
        }
        startMainService(context);
    }

    private void startMainService(Context context) {
        Intent serviceIntent = new Intent(context, AgentMainService.class);
        serviceIntent.putExtra(AgentMainService.AGENT_SERVICE_MQTT_START, 0);

        SdkVersion sdkVersion = KoinJavaComponent.get(SdkVersion.class);
        if (sdkVersion.greaterThan26()) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}