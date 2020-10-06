package com.rsupport.phone2phone;


import control.Converter;

import com.rsupport.media.header.IModel;

public class RsVRVDHeader implements IModel {

    public int mViewerBitsPerPixel = 0;
    public int screenWidth = 0;
    public int screenHeight = 0;
    public int rotation = 0;
    public int deviceWidth = 0;
    public int deviceHeight = 0;

//	public static int SIZE = 23;

    public void save(byte[] szBuffer, int nStart) {
        int nIndex = nStart;

        mViewerBitsPerPixel = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += 2;
        screenWidth = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += 2;
        screenHeight = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += 2;
        rotation = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += 2;
        deviceWidth = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += 2;
        deviceHeight = Converter.readShortLittleEndian(szBuffer, nIndex);
        nIndex += 2;
    }

    @Override
    public void save2(byte[] szBuffer, int nStart, int dstOffset, int dstLen) {
    }

    @Override
    public void push(byte[] szBuffer, int nStart) {
        int nIndex = nStart;

        System.arraycopy(Converter.getBytesFromShortLE((short) mViewerBitsPerPixel), 0, szBuffer, nIndex, 2);
        nIndex += 2;
        System.arraycopy(Converter.getBytesFromShortLE((short) screenWidth), 0, szBuffer, nIndex, 2);
        nIndex += 2;
        System.arraycopy(Converter.getBytesFromShortLE((short) screenHeight), 0, szBuffer, nIndex, 2);
        nIndex += 2;
        System.arraycopy(Converter.getBytesFromShortLE((short) rotation), 0, szBuffer, nIndex, 2);
        nIndex += 2;
        System.arraycopy(Converter.getBytesFromShortLE((short) deviceWidth), 0, szBuffer, nIndex, 2);
        nIndex += 2;
        System.arraycopy(Converter.getBytesFromShortLE((short) deviceHeight), 0, szBuffer, nIndex, 2);
        nIndex += 2;
    }

    @Override
    public int size() {
        return 12;
    }
}
