package com.rsupport.mobile.agent.service.command;


import android.webkit.JavascriptInterface;

import com.rsupport.mobile.agent.R;
import com.rsupport.mobile.agent.constant.AgentBasicInfo;
import com.rsupport.mobile.agent.constant.Global;
import com.rsupport.mobile.agent.modules.push.RSPushMessaging;
import com.rsupport.mobile.agent.status.AgentStatus;
import com.rsupport.mobile.agent.utils.AgentLogManager;

import org.apache.http.util.ByteArrayBuffer;
import org.koin.java.KoinJavaComponent;

import control.Converter;

public class AgentCommand3016 extends AgentCommandBasic {

    @Override
    public int agentCommandexe(byte[] data, int index) {
        returnCommand();
        final AgentLogManager agentLogManager = KoinJavaComponent.get(AgentLogManager.class);
        agentLogManager.addAgentLog(Global.getInstance().getAppContext(), Global.getInstance().getAppContext().getString(R.string.agent_log_remote_connection_check));
        return 0;
    }

    private int returnCommand() {
        int sessionPacket = 30301;
        int status = 0;

        AgentStatus agentStatus = KoinJavaComponent.get(AgentStatus.class);
        switch (agentStatus.get()) {
            case AgentStatus.AGENT_STATUS_REMOTING:
                status = 1;
                break;
            case AgentStatus.AGENT_STATUS_LOGIN:
                status = 0;
                break;
            case AgentStatus.AGENT_STATUS_LOGOUT:
                status = 2;
                break;
            default:
                status = 0;
                break;

        }

        ByteArrayBuffer buf = new ByteArrayBuffer(12);
        buf.append(Converter.getBytesFromIntLE(4), 0, 4);
        buf.append(Converter.getBytesFromIntLE(sessionPacket), 0, 4);
        buf.append(Converter.getBytesFromIntLE(status), 0, 4);

        byte[] sendByte = buf.toByteArray();

        AgentCommand.dec_bitcrosswise(sendByte, 4);

        RSPushMessaging messaging = RSPushMessaging.getInstance();
        messaging.send(AgentBasicInfo.getAgentGuid(Global.getInstance().getAppContext()) + "/console", sendByte);
        return 0;
    }
}
