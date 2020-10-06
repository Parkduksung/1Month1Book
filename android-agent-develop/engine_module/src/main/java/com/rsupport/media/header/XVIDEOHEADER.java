package com.rsupport.media.header;

import config.EngineConfigSetting;
import control.Converter;

public class XVIDEOHEADER implements IModel {

    /**
     * ASHM을 사용할 경우 : H.264 처리
     */
    public static final int ENCODER_TYPE_OMX = 'O';

    /**
     * 5.0이상 MediaProjection(VirtualDisplay)에서 사용할 경우 : H.264 처리
     */
    public static final int ENCODER_TYPE_OMX_FOR_VD = 'V';

    public static final int ENCODER_TYPE_OMX_FOR_KNOX = 'N';

    /**
     * 화면 회전시 디코더 재시작할 경우 엔코더도 다시시작 요청하기 위한 프로토콜
     */
    public static final int ENCODER_TYPE_OMX_FOR_RELOAD_VD = 'R';

    public int framepersecond = 10;
    public int videoWidth = 0;
    public int videoHeight = 0;
    public int frameWidth = 0;
    public int frameHeight = 0;
    public int rotation = 0;
    public int videoRatio = 100;
    public int videoQuality = 10;
    public int monitorIndex = 1; // 0 : 화면전체, 1 : 1번모니터
    public int valOption1 = 0;
    public int valOption2 = 0;
    public int valOption3 = 0;
    public int isLandscape = 0;  //기본 가로 단말 일경우 1로 전송.

    public int modelnameLen = "rvagent_android".getBytes(EngineConfigSetting.UTF_8).length;
    public byte[] modelname = "rvagent_android".getBytes(EngineConfigSetting.UTF_8);


    /**
     * Encoder의 Source Type을 정의한다. Default는 {@link #ENCODER_TYPE_OMX} 으로 처리한다.
     */
    public int sourceType = ENCODER_TYPE_OMX;

//	public static int SIZE = 23;

    public void save(byte[] szBuffer, int nStart) {
        int nIndex = nStart;

        framepersecond = (int) (szBuffer[0] & 0xff);
        nIndex += 1;
        videoWidth = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += 2;
        videoHeight = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += 2;
        frameWidth = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += 2;
        frameHeight = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += 2;
        rotation = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += 2;
        videoRatio = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += 2;
        videoQuality = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += 2;
        monitorIndex = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += 2;
        valOption1 = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += 2;
        valOption2 = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += 2;
        valOption3 = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += 2;
        modelnameLen = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += modelnameLen;
        modelname = Converter.readByte(szBuffer, 0, nIndex);
        nIndex += 2;
        sourceType = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += 2;
        isLandscape = Converter.readShortLittleEndian(szBuffer, nIndex);
    }

    @Override
    public void save2(byte[] szBuffer, int nStart, int dstOffset, int dstLen) {
    }

    @Override
    public void push(byte[] szBuffer, int nStart) {
        int nIndex = nStart;

        szBuffer[0] = (byte) (framepersecond & 0xff);
        nIndex++;
        System.arraycopy(Converter.getBytesFromShortLE((short) videoWidth), 0, szBuffer, nIndex, 2);
        nIndex += 2;
        System.arraycopy(Converter.getBytesFromShortLE((short) videoHeight), 0, szBuffer, nIndex, 2);
        nIndex += 2;
        System.arraycopy(Converter.getBytesFromShortLE((short) frameWidth), 0, szBuffer, nIndex, 2);
        nIndex += 2;
        System.arraycopy(Converter.getBytesFromShortLE((short) frameHeight), 0, szBuffer, nIndex, 2);
        nIndex += 2;
        System.arraycopy(Converter.getBytesFromShortLE((short) rotation), 0, szBuffer, nIndex, 2);
        nIndex += 2;
        System.arraycopy(Converter.getBytesFromShortLE((short) videoRatio), 0, szBuffer, nIndex, 2);
        nIndex += 2;
        System.arraycopy(Converter.getBytesFromShortLE((short) videoQuality), 0, szBuffer, nIndex, 2);
        nIndex += 2;
        System.arraycopy(Converter.getBytesFromShortLE((short) monitorIndex), 0, szBuffer, nIndex, 2);
        nIndex += 2;
        System.arraycopy(Converter.getBytesFromShortLE((short) valOption1), 0, szBuffer, nIndex, 2);
        nIndex += 2;
        System.arraycopy(Converter.getBytesFromShortLE((short) valOption2), 0, szBuffer, nIndex, 2);
        nIndex += 2;
        System.arraycopy(Converter.getBytesFromShortLE((short) valOption3), 0, szBuffer, nIndex, 2);
        nIndex += 2;
        System.arraycopy(Converter.getBytesFromShortLE((short) modelnameLen), 0, szBuffer, nIndex, 2);
        nIndex += 2;
        System.arraycopy(modelname, 0, szBuffer, nIndex, modelnameLen);
        nIndex += modelnameLen;
        System.arraycopy(Converter.getBytesFromShortLE((short) sourceType), 0, szBuffer, nIndex, 2);
        nIndex += 2;
        System.arraycopy(Converter.getBytesFromShortLE((short) isLandscape), 0, szBuffer, nIndex, 2);
    }

    @Override
    public int size() {
        return 29 + modelnameLen;
    }
}
