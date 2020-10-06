package com.rsupport.mobile.agent.service.command;


import android.os.Message;

import com.rsupport.mobile.agent.BuildConfig;
import com.rsupport.mobile.agent.R;
import com.rsupport.mobile.agent.constant.AgentBasicInfo;
import com.rsupport.mobile.agent.constant.Global;
import com.rsupport.mobile.agent.constant.GlobalStatic;
import com.rsupport.mobile.agent.constant.ViewerType;
import com.rsupport.mobile.agent.modules.channel.OnChannelListener;
import com.rsupport.mobile.agent.modules.net.protocol.MessageID;
import com.rsupport.mobile.agent.modules.push.RSPushMessaging;
import com.rsupport.mobile.agent.repo.config.ConfigRepository;
import com.rsupport.mobile.agent.repo.config.ProxyInfo;
import com.rsupport.mobile.agent.service.AgentMainService;
import com.rsupport.mobile.agent.service.HxdecThread;
import com.rsupport.mobile.agent.status.AgentStatus;
import com.rsupport.mobile.agent.utils.AgentLogManager;
import com.rsupport.util.log.RLog;

import org.apache.http.util.ByteArrayBuffer;
import org.jetbrains.annotations.Nullable;
import org.koin.java.KoinJavaComponent;

import config.EngineConfigSetting;
import control.Converter;

public class AgentCommand5007 extends AgentCommandBasic {

    RemoteControlData remoteData;

    int errorCode = 0;

    class RemoteControlData {
        String loginID;
        String connectIP;
        int connectPort;
        String deamonIP;
        int deamonPort;
        int useSSL;
        String connectGUID;
        int forceConnectFlag;
        String remoteMAC;
        String logKey;
        int autoSystemLock;
        int legacyType;
        int sslType; // RemotePC 전용

        int executeRestriction;
        int systemLockMode;
        int useUrlFilter;
        int useVideoMode;
        String viewerMode;
        int autoScreenLock;
        int useScreenLockType;
        int screenLockType;

        @Override
        public String toString() {
            return "RemoteControlData{" +
                    "loginID='" + loginID + '\'' +
                    ", connectIP='" + connectIP + '\'' +
                    ", connectPort=" + connectPort +
                    ", deamonIP='" + deamonIP + '\'' +
                    ", deamonPort=" + deamonPort +
                    ", useSSL=" + useSSL +
                    ", connectGUID='" + connectGUID + '\'' +
                    ", forceConnectFlag=" + forceConnectFlag +
                    ", remoteMAC='" + remoteMAC + '\'' +
                    ", logKey='" + logKey + '\'' +
                    ", autoSystemLock=" + autoSystemLock +
                    ", legacyType=" + legacyType +
                    ", sslType=" + sslType +
                    ", executeRestriction=" + executeRestriction +
                    ", systemLockMode=" + systemLockMode +
                    ", useUrlFilter=" + useUrlFilter +
                    ", useVideoMode=" + useVideoMode +
                    ", viewerMode='" + viewerMode + '\'' +
                    ", autoScreenLock=" + autoScreenLock +
                    ", useScreenLockType=" + useScreenLockType +
                    ", screenLockType=" + screenLockType +
                    '}';
        }
    }

    @Override
    public int agentCommandexe(byte[] data, int index) {

        readRemoteControlData(data, index);
        final AgentLogManager agentLogManager = KoinJavaComponent.get(AgentLogManager.class);
        agentLogManager.addAgentLog(Global.getInstance().getAppContext(), String.format(Global.getInstance().getAppContext().getString(R.string.agent_log_remote_connection_requst), remoteData.connectIP, remoteData.remoteMAC));
        startRemoteControl();
        return 0;
    }

    private ViewerType findViewType(RemoteControlData remoteControlData) {
        return "xenc".equalsIgnoreCase(remoteControlData.viewerMode) ? ViewerType.XENC : ViewerType.SCAP;
    }

    private void startRemoteControl() {

        if (AgentCommandFunction.ipMacBlockCheck(Global.getInstance().getAppContext(), remoteData.connectIP, remoteData.remoteMAC, remoteData.loginID)) {
            errorCode = 90509;
        }

        if (returnCommand() != 0) {
            return;
        }

        RLog.v("startRemoteControl : " + remoteData.toString());

        if (Global.getInstance().getAgentThread() != null) {
            Global.getInstance().getAgentThread().releaseAll();
            Global.getInstance().setAgentThread(null);
        }
        Global.getInstance().setAgentThread(new HxdecThread(Global.getInstance().getAppContext(), remoteData.deamonIP, remoteData.deamonPort, findViewType(remoteData), getProxyInfo()));
        Global.getInstance().getAgentThread().setOnMouseEventListener(mouseEvent -> {
            Message message = Message.obtain();
            message.what = AgentMainService.AGENT_SERVICE_TOP_MOST_TEXT_EVENT;
            message.obj = mouseEvent;
            sendMessage(message);
        });

        if (BuildConfig.DEBUG) {
            Global.getInstance().getAgentThread().setSendScreenPacketListener(data -> {
                Message message = Message.obtain();
                message.what = AgentMainService.AGENT_SERVICE_UPDATE_DATA_PACKET_SIZE;
                message.obj = data;
                sendMessage(message);
            });
        }

        Global.getInstance().getAgentThread().setOnDataChannelListener(new OnChannelListener() {
            @Override
            public void onConnecting() {
            }

            @Override
            public void onConnectFail() {
                Message message = Message.obtain();
                message.what = AgentMainService.AGENT_SERVICE_DISCONNECTED;
                sendMessage(message);
            }

            @Override
            public void onConnected() {
                Message message = Message.obtain();
                message.what = AgentMainService.AGENT_SERVICE_CONNECTED;
                sendMessage(message);
            }

            @Override
            public void onDisconnected() {
                Message message = Message.obtain();
                message.what = AgentMainService.AGENT_SERVICE_DISCONNECTED;
                sendMessage(message);
            }
        });
        GlobalStatic.g_param = null;
        Global.getInstance().getAgentThread().connectChannel(MessageID.rcpChannelData, remoteData.deamonPort, remoteData.connectGUID);
    }

    @Nullable
    private ProxyInfo getProxyInfo() {
        ProxyInfo proxyInfo = null;
        ConfigRepository configRepository = KoinJavaComponent.get(ConfigRepository.class);
        if(configRepository.isProxyUse()){
            proxyInfo = configRepository.getProxyInfo();
        }
        return proxyInfo;
    }

    private void readRemoteControlData(byte[] data, int startIndex) {
        try {
            int index = startIndex;
            int size = 0;

            remoteData = new RemoteControlData();

            // Web Login ID
            size = Converter.readIntLittleEndian(data, index);
            index += 4;

            remoteData.loginID = new String(data, index, size, EngineConfigSetting.UTF_8);
            index += size;

            // Viewer Listen IP
            size = Converter.readIntLittleEndian(data, index);
            index += 4;
            remoteData.connectIP = new String(data, index, size, EngineConfigSetting.UTF_8);
            index += size;

            // Viewer Listen Port
            remoteData.connectPort = Converter.readIntLittleEndian(data, index);
            index += 4;

            // Daemon IP
            size = Converter.readIntLittleEndian(data, index);
            index += 4;
            remoteData.deamonIP = new String(data, index, size, EngineConfigSetting.UTF_8);
            index += size;

            // Daemon Port
            remoteData.deamonPort = Converter.readIntLittleEndian(data, index);
            index += 4;

            // Use SSL
            remoteData.useSSL = data[index];
            index++;

            // GUID
            size = Converter.readIntLittleEndian(data, index);
            index += 4;
            remoteData.connectGUID = new String(data, index, size, EngineConfigSetting.UTF_8);
            index += size;

            // RemoteControlForceConnect
            remoteData.forceConnectFlag = Converter.readIntLittleEndian(data, index);
            index += 4;

            // MacAddr
            size = Converter.readIntLittleEndian(data, index);
            index += 4;
            remoteData.remoteMAC = new String(data, index, size, EngineConfigSetting.UTF_8);
            index += size;

            // LogKey
            size = Converter.readIntLittleEndian(data, index);
            index += 4;
            remoteData.logKey = new String(data, index, size, EngineConfigSetting.UTF_8);
            AgentBasicInfo.loginKey = remoteData.logKey;
            index += size;

            // AutoSystemLock
            remoteData.autoSystemLock = data[index];
            index++;

            // LegacyType
            remoteData.legacyType = Converter.readIntLittleEndian(data, index);
            index += 4;

            // SSL Type
            remoteData.sslType = Converter.readIntLittleEndian(data, index);
            index += 4;

            // ExecuteRestriction
            remoteData.executeRestriction = Converter.readIntLittleEndian(data, index);
            index += 4;

            // SystemLockMode
            remoteData.systemLockMode = Converter.readIntLittleEndian(data, index);
            index += 4;

            // UseUrlFilter
            remoteData.useUrlFilter = Converter.readIntLittleEndian(data, index);
            index += 4;

            // UseVideoMode
            remoteData.useVideoMode = Converter.readIntLittleEndian(data, index);
            index += 4;

            // ViewerMode length
            int viewerModeLength = Converter.readIntLittleEndian(data, index);
            index += 4;

            remoteData.viewerMode = new String(data, index, viewerModeLength, EngineConfigSetting.UTF_8);
            index += viewerModeLength;

            // AutoScreenLock
            remoteData.autoScreenLock = Converter.readIntLittleEndian(data, index);
            index += 4;

            // UseScreenLockType
            remoteData.useScreenLockType = Converter.readIntLittleEndian(data, index);
            index += 4;

            // ScreenLockType
            remoteData.screenLockType = Converter.readIntLittleEndian(data, index);
            index += 4;
        } catch (Exception e) {
            RLog.e(e);
        }
    }

    private int returnCommand() {
        int sessionPacket = 30301;
        int commandKey = 5007;
        int encordingDataLength = 0;

        byte[] sendByte = null;

        if (errorCode == 0) {
            AgentStatus agentStatus = KoinJavaComponent.get(AgentStatus.class);
            switch (agentStatus.get()) {
                case AgentStatus.AGENT_STATUS_REMOTING:
                    errorCode = 90503;
                    if (remoteData.forceConnectFlag == 1127) {// 임시 강제종료  REMOTECONTROL_FORCE_CONNECT
                        errorCode = 0;
                        if (Global.getInstance().getAgentThread() != null) {
                            Global.getInstance().getAgentThread().releaseAll();
                            Global.getInstance().setAgentThread(null);
                        }

                    }
                    break;
                case AgentStatus.AGENT_STATUS_LOGIN:
                    errorCode = 0;
                    break;
                default:
                    errorCode = 0;
                    break;

            }
        }


        ByteArrayBuffer buf = new ByteArrayBuffer(128);
        buf.append(Converter.getBytesFromIntLE(sessionPacket), 0, 4);
        buf.append(Converter.getBytesFromIntLE(errorCode), 0, 4);

        ByteArrayBuffer encbuf = new ByteArrayBuffer(128);
        encbuf.append(Converter.getBytesFromIntLE(commandKey), 0, 4);
        encbuf.append(Converter.getBytesFromIntLE(encordingDataLength), 0, 4);

        sendByte = AgentCommandFunction.pushBasicInfo(buf, encbuf.toByteArray());

        RLog.d("sendByte !!! " + sendByte.length);

        AgentCommand.dec_bitcrosswise(sendByte, 0);

        RSPushMessaging messaging = RSPushMessaging.getInstance();
        messaging.send(AgentBasicInfo.getAgentGuid(Global.getInstance().getAppContext()) + "/console", sendByte);

        return errorCode;
    }

}
