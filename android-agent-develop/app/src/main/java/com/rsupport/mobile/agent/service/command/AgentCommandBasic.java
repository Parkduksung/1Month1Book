package com.rsupport.mobile.agent.service.command;

import android.os.Handler;
import android.os.Message;

import com.rsupport.mobile.agent.utils.SdkVersion;

import org.koin.java.KoinJavaComponent;

import kotlin.Lazy;

abstract class AgentCommandBasic implements IAgentCommand {
    private Handler serviceHandler;

    protected Lazy<SdkVersion> sdkVersionLazy = KoinJavaComponent.inject(SdkVersion.class);

    public void setServiceHandler(Handler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    protected boolean sendMessage(Message message) {
        if (serviceHandler != null) {
            return serviceHandler.sendMessage(message);
        }
        return false;
    }

    @Override
    public int agentCommandexe(byte[] data, int index) {
        return 0;
    }
}
