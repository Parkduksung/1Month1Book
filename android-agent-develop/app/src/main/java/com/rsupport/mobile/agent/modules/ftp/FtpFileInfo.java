package com.rsupport.mobile.agent.modules.ftp;

import control.Converter;

public class FtpFileInfo {

    public long totalsize;
    public int sizeLow;
    public int sizeHigh;
    public long createdate;
    public long modifydateLong;
    public int modifydateLow;
    public int modifydateHigh;
    public long accessdate;
    public int attribute;
    public String name = "";
    public String fullname = "";
    public String folderpath = "";
    public byte byteBuffer[] = new byte[520];

    private long getFilesize(int high, int low) {
        long ret = 0;
        ret = ((long) high) << 32 | (low & 0xffffffffL);
        return ret;
    }

    public void save(byte[] szBuffer, int start, int len) {
        try {
            int index = start;
            int datalen = Converter.readIntLittleEndian(szBuffer, index);
            index += 4;
            if (datalen > 0) {
                System.arraycopy(szBuffer, index, byteBuffer, 0, datalen);
                folderpath = new String(byteBuffer, 0, datalen, "UTF-16LE");
                index += datalen;
            }

            sizeLow = Converter.readIntLittleEndian(szBuffer, index);
            index += 4;
            sizeHigh = Converter.readIntLittleEndian(szBuffer, index);
            index += 4;
            totalsize = getFilesize(sizeHigh, sizeLow);
            createdate = 0;
            modifydateLow = Converter.readIntLittleEndian(szBuffer, index);
            index += 4;
            modifydateHigh = Converter.readIntLittleEndian(szBuffer, index);
            index += 4;
            accessdate = 0;
            attribute = Converter.readIntLittleEndian(szBuffer, index);
            index += 4;
            System.arraycopy(szBuffer, index, byteBuffer, 0, len - index);
            name = new String(byteBuffer, 0, len - index, "UTF-16LE");
            name = name.trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int sizePacket() {
        int ret = 0, len = 0;
        try {
            ret += 4; // filesize : 8, filedata : 8, fileattr : 4
            len = folderpath.getBytes("UTF-16LE").length;
            ret += len;
            ret += 20; // filesize : 8, filedata : 8, fileattr : 4
            len = name.getBytes("UTF-16LE").length;
            ret += len;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

}
