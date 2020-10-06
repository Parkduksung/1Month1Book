package com.rsupport.mobile.agent.service.command;

import android.os.Handler;

import com.rsupport.mobile.agent.utils.Base64;
import com.rsupport.util.log.RLog;

import java.io.UnsupportedEncodingException;

import control.Converter;

public class AgentCommand {

    public static final int AGENT_COMMAND_3005 = 3005; // 에이전트 삭제
    public static final int AGENT_COMMAND_3016 = 3016; // 새로고침(현재 정보 전달)
    public static final int AGENT_COMMAND_3032 = 3032; // 라이브뷰
    public static final int AGENT_COMMAND_3033 = 3033; // 강제업데이트 알림
    public static final int AGENT_COMMAND_5007 = 5007; // 원격접속
    public static final int AGENT_COMMAND_5010 = 5010; // 화면캡쳐
    public static final int AGENT_COMMAND_5048 = 5048; // 에이전트 동의
    public static final int AGENT_COMMAND_5033 = 5033; // 원격연결 강제종료

    private Handler serviceHandle;

    private int mPacketSize;
    private int mSessionPacket;
    private String mMagicString;
    private int mAgentType;
    private String mAgentGUID;
    private String mWebID;
    private int mGridLength;
    private int mCcidLength;
    private int mCommLength;
    private int mRotation;

    public void setServiceHandler(Handler serviceHandle) {
        this.serviceHandle = serviceHandle;
    }

    private IAgentCommand getAgentCommandClass(int command) {
        AgentCommandBasic commandClass = null;
        switch (command) {
            case AGENT_COMMAND_3005:
                commandClass = new AgentCommand3005();
                break;
            case AGENT_COMMAND_3016:
                commandClass = new AgentCommand3016();
                break;
            case AGENT_COMMAND_3032:
                commandClass = AgentCommand3032.getInstance();
                break;
            case AGENT_COMMAND_5007:
                commandClass = new AgentCommand5007();
                break;
            case AGENT_COMMAND_5010:
                commandClass = new AgentCommand5010(mRotation);
                break;
            case AGENT_COMMAND_5048:
                commandClass = new AgentCommand5048();
                break;
            case AGENT_COMMAND_5033:
                commandClass = new AgentCommand5033();
                break;
            case AGENT_COMMAND_3033:
                commandClass = new AgentCommand3033();
                break;
        }
        if (commandClass != null) {
            commandClass.setServiceHandler(serviceHandle);
        }
        return commandClass;
    }

    public void saveRotation(int rotaion) {
        this.mRotation = rotaion;
    }

    public int readCMDCommand(byte[] data) {
        int commandKey = 0;
        int index = 0;
        int size = 0;
        String key = "";

        //강제 업데이트 푸시는 커맨드 키만 온다.
        if (data.length == 4) {
            try {
                key = new String(data, "UTF-8");
                commandKey = Integer.parseInt(key);
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
            RLog.i(key);
            IAgentCommand comm = getAgentCommandClass(commandKey);

            if (comm != null) {
                comm.agentCommandexe(null, 0);
            } else {
                RLog.d("No have Command : " + commandKey);
            }

            return commandKey;
        }


        try {

            // Network Packet 4byte size
            mPacketSize = Converter.readIntLittleEndian(data, index);
            index += 4;

            // Network FAKE SSL bitCross...
            dec_bitcrosswise(data, index);

            mSessionPacket = Converter.readIntLittleEndian(data, index);
            index += 4;

            size = Converter.readIntLittleEndian(data, index);
            index += 4;

            mMagicString = new String(data, index, size, "UTF-16LE");
            index += size;

            mAgentType = Converter.readIntLittleEndian(data, index);
            index += 4;

            size = Converter.readIntLittleEndian(data, index);
            index += 4;
            mAgentGUID = new String(data, index, size, "UTF-16LE");
            index += size;

            size = Converter.readIntLittleEndian(data, index);
            index += 4;
            mWebID = new String(data, index, size, "UTF-16LE");
            index += size;

            mGridLength = Converter.readIntLittleEndian(data, index);
            index += 4;

            mCcidLength = Converter.readIntLittleEndian(data, index);
            index += 4;

            mCommLength = Converter.readIntLittleEndian(data, index);
            index += 4;

            commandKey = readBasicData(data, index);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return commandKey;
    }

    private int readBasicData(byte[] data, int startIndex) {
        int index = 0;
        byte[] encrypt;
        int commandKey = 0;

        encrypt = android.util.Base64.decode(data, startIndex, mCommLength, Base64.NO_OPTIONS);
        commandKey = Converter.readIntLittleEndian(encrypt, index);
        RLog.i("commandKey  : " + commandKey);
        index += 4;

        RLog.d("come to Command : " + commandKey);
        IAgentCommand comm = getAgentCommandClass(commandKey);

        if (comm != null) {
            comm.agentCommandexe(encrypt, index);
        } else {
            RLog.d("No have Command : " + commandKey);
        }

        return commandKey;

    }

    @Override
    public String toString() {
        return "packetSize : " + mPacketSize + "sessionPacket : " + mSessionPacket + ", magicString " + mMagicString + ", agentType " + mAgentType + ", agentGUID " + mAgentGUID + ", webID " + mWebID + ", gridLength " + mGridLength
                + ", ccidLength " + mCcidLength + ", commLength " + mCommLength;
    }

    public static void dec_bitcrosswise(byte[] p, int offset) {
        // byte c = (byte)0xBE;
        byte c = (byte) 'r';

        for (int i = offset; i < p.length; ++i) {
            p[i] = (byte) (~(p[i]));
            p[i] = (byte) (p[i] ^ c);
        }
    }
}
