package com.rsupport.mobile.agent.service.command;

import com.rsupport.mobile.agent.utils.Converter;

import config.EngineConfigSetting;

public class AgentCMDDefaultStruct {
    String LoginID;
    String remoteIP;
    String remoteMAC;

    public void readRemoteControlData(byte[] data, int startIndex) {
        int index = startIndex;
        int size = 0;

        // loginIDLength
        size = Converter.readIntLittleEndian(data, index);
        index += 4;
        LoginID = new String(data, index, size, EngineConfigSetting.UTF_8);
        index += size;

        // remoteIP
        size = Converter.readIntLittleEndian(data, index);
        index += 4;
        remoteIP = new String(data, index, size, EngineConfigSetting.UTF_8);
        index += size;

        // remoteMAC
        size = Converter.readIntLittleEndian(data, index);
        index += 4;
        remoteMAC = new String(data, index, size, EngineConfigSetting.UTF_8);
        index += size;
    }
}