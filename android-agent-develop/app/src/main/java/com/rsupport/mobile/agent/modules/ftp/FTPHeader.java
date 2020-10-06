package com.rsupport.mobile.agent.modules.ftp;

import control.Converter;

public class FTPHeader {

    public int totalcount;
    public long totalsize;
    public String savepath;
    public byte byteBuffer[] = new byte[520];

    public void clearData() {
        totalcount = 0;
        totalsize = 0;
        savepath = "";
    }

    private long getFilesize(int high, int low) {
        long ret = 0;
        ret = ((long) high) << 32 | (low & 0xffffffffL);
        return ret;
    }

    public void save(byte[] szBuffer, int start, int len) {
        try {
            int index = start;
            totalcount = Converter.readIntLittleEndian(szBuffer, index);
            index += 4;
            int sizeLow = Converter.readIntLittleEndian(szBuffer, index);
            index += 4;
            int sizeHigh = Converter.readIntLittleEndian(szBuffer, index);
            index += 4;
            totalsize = getFilesize(sizeHigh, sizeLow);
            System.arraycopy(szBuffer, index, byteBuffer, 0, len - index);
            savepath = new String(byteBuffer, 0, len - index, "UTF-16LE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
