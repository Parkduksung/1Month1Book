package com.rsupport.mobile.agent.utils.compress;

import control.Converter;

import com.rsupport.mobile.agent.modules.net.model.Packet;;

public class rcpZipHeader implements Packet {

    public int originalsize;
    public int compresssize;

    @Override
    public void push(byte[] szBuffer, int nStart) {
        int nIndex = nStart;

        System.arraycopy(Converter.getBytesFromIntLE(originalsize), 0, szBuffer, nIndex, 4);
        nIndex += 4;
        System.arraycopy(Converter.getBytesFromIntLE(compresssize), 0, szBuffer, nIndex, 4);
        nIndex += 4;
    }

    @Override
    public void save(byte[] szBuffer, int nStart) {
        int nIndex = nStart;

        originalsize = Converter.readIntLittleEndian(szBuffer, nIndex);
        nIndex += 4;
        compresssize = Converter.readIntLittleEndian(szBuffer, nIndex);
    }

    @Override
    public void save(byte[] szBuffer, int start, int dstOffset, int dstLen) {
    }

    @Override
    public int size() {
        return 8;
    }

    @Override
    public void clear() {

    }

}
