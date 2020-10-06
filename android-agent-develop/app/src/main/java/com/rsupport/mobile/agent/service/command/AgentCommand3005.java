package com.rsupport.mobile.agent.service.command;

import android.content.Context;
import android.content.Intent;

import com.rsupport.mobile.agent.modules.push.IPushMessaging;
import com.rsupport.mobile.agent.modules.push.RSPushMessaging;

import config.EngineConfigSetting;

import com.rsupport.mobile.agent.constant.AgentBasicInfo;
import com.rsupport.mobile.agent.constant.Global;
import com.rsupport.mobile.agent.receiver.AgentPushReceiver;

public class AgentCommand3005 extends AgentCommandBasic {

    @Override
    public int agentCommandexe(byte[] data, int index) {

        Context context = Global.getInstance().getAppContext();
        RSPushMessaging messaging = RSPushMessaging.getInstance();
        messaging.unregister(AgentBasicInfo.getAgentGuid(context) + "/agent");

        Intent intent = new Intent(context, AgentPushReceiver.class);
        intent.setAction(IPushMessaging.ACTION_PUSH_MESSAGING);
        intent.putExtra(IPushMessaging.EXTRA_KEY_TYPE, IPushMessaging.TYPE_WEB_LOGIN_ERROR);
        intent.putExtra(IPushMessaging.EXTRA_KEY_VALUE, "212".getBytes(EngineConfigSetting.UTF_8));

        Global.getInstance().getAppContext().sendBroadcast(intent);

        return 0;
    }
}
