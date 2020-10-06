package com.rsupport.mobile.agent.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rsupport.mobile.agent.modules.push.IPushMessaging;

import com.rsupport.mobile.agent.service.AgentMainService;
import com.rsupport.util.log.RLog;

public class AgentPushReceiver extends BroadcastReceiver implements IPushMessaging {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_PUSH_MESSAGING)) {
            byte[] message = null;
            int type = intent.getIntExtra(EXTRA_KEY_TYPE, -1);
            Intent serviceIntent;

            serviceIntent = new Intent(context, AgentMainService.class);

            switch (type) {
                case TYPE_MSG_ARRIVED:
                    RLog.v("TYPE_MSG_ARRIVED START Service");
                    message = intent.getByteArrayExtra(EXTRA_KEY_VALUE);
                    serviceIntent.putExtra(AgentMainService.AGENT_SERVICE_PUSH_DATA, message);
                    break;
                case TYPE_CONNECT_ERROR:
                    RLog.v("TYPE_CONNECT_ERROR");
                    message = intent.getByteArrayExtra(EXTRA_KEY_VALUE);
                    if (message != null) {
                        serviceIntent.putExtra(AgentMainService.AGENT_SERVICE_PUSH_OPEN_VALUE, Integer.parseInt(new String(message)));
                    } else {
                        serviceIntent.putExtra(AgentMainService.AGENT_SERVICE_PUSH_OPEN_VALUE, -1);
                    }
                    serviceIntent.putExtra(AgentMainService.AGENT_SERVICE_PUSH_OPEN, message);
                    break;
                case TYPE_WEB_LOGIN_ERROR:
                    RLog.v("TYPE_WEB_LOGIN_ERROR");
                    message = intent.getByteArrayExtra(EXTRA_KEY_VALUE);
                    if (message != null) {
                        serviceIntent.putExtra(AgentMainService.AGENT_SERVICE_PUSH_OPEN_VALUE, Integer.parseInt(new String(message)));
                    } else {
                        serviceIntent.putExtra(AgentMainService.AGENT_SERVICE_PUSH_OPEN_VALUE, -1);
                    }
                    serviceIntent.putExtra(AgentMainService.AGENT_SERVICE_PUSH_OPEN, message);
                    break;
                case TYPE_CONNECTED:
                    RLog.v("TYPE_CONNECTED");
                    serviceIntent.putExtra(AgentMainService.AGENT_SERVICE_PUSH_OPEN_VALUE, 0);
                    serviceIntent.putExtra(AgentMainService.AGENT_SERVICE_PUSH_OPEN, message);
                    break;
                case TYPE_CONNECTION_LOST:
                    message = intent.getByteArrayExtra(EXTRA_KEY_VALUE);
                    RLog.v("TYPE_CONNECTION_LOST : " + new String(message));
                case TYPE_DISCONNECTED:
                    RLog.v("TYPE_DISCONNECTED");
                    serviceIntent.putExtra(AgentMainService.AGENT_SERVICE_PUSH_CLOSE, message);
                    break;
                case TYPE_SELF_DISCONNECT_REMOTE:
                    RLog.v("TYPE_SELF_DISCONNECT_REMOTE");
                    serviceIntent.putExtra(AgentMainService.AGENT_SERVICE_NOTI_CLOSE, intent.getIntExtra(EXTRA_KEY_VALUE, -1));
                    break;
                case TYPE_VERSION_MSG_ARRIVED:
                    RLog.v("TYPE_VERSION_MSG_ARRIVED");
                    message = intent.getByteArrayExtra(EXTRA_KEY_VALUE);
                    serviceIntent.putExtra(AgentMainService.AGENT_SERVICE_PUSH_DATA, message);
                    break;
                case TYPE_VERSION_CONNECT_ERROR:
                    RLog.v("TYPE_VERSION_CONNECT_ERROR");
                    return; // return 임 break 아님
                case TYPE_VERSION_CONNECTION_LOST:
                    RLog.v("TYPE_VERSION_CONNECTION_LOST");
                    return; // return 임 break 아님
                case TYPE_VERSION_CONNECTED:
                    RLog.v("TYPE_VERSION_CONNECTED");
                    return; // return 임 break 아님
                case TYPE_VERSION_DISCONNECTED:
                    RLog.v("TYPE_VERSION_DISCONNECTED");
                    return; // return 임 break 아님
            }
            context.startService(serviceIntent);
        } else {
            RLog.e("not define action");
        }
    }
}
