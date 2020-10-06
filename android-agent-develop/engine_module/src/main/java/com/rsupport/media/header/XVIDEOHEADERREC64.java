package com.rsupport.media.header;

import control.Converter;

public class XVIDEOHEADERREC64 implements IModel {

    public int framepersecond = 10;
    public int videoWidth = 0;
    public int videoHeight = 0;
    public int sps_len = 0;
    public int pps_len = 0;
    public int sps_pps = 0;

    public static int SIZE = 8;

    @Override
    public void save(byte[] szBuffer, int nStart) {
        int nIndex = nStart;

        framepersecond = (int) (szBuffer[0] & 0xff);
        nIndex += 1;
        videoWidth = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += 2;
        videoHeight = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += 2;
        sps_len = (int) (szBuffer[0] & 0xff);
        nIndex += 1;
        pps_len = (int) (szBuffer[0] & 0xff);
        nIndex += 1;
        sps_pps = (int) (szBuffer[0] & 0xff);
        nIndex += 1;
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
        System.arraycopy(Converter.getBytesFromLongLE((short) videoHeight), 0, szBuffer, nIndex, 2);
        nIndex += 2;
        szBuffer[nIndex] = (byte) (sps_len & 0xff);
        nIndex++;
        szBuffer[nIndex] = (byte) (pps_len & 0xff);
        nIndex++;
        szBuffer[nIndex] = (byte) (sps_pps & 0xff);
        nIndex++;
    }

    @Override
    public int size() {
        return 8;
    }
}
