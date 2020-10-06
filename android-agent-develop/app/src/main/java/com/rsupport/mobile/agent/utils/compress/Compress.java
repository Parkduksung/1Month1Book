package com.rsupport.mobile.agent.utils.compress;

import java.util.Arrays;

import control.DeflaterEx;

public class Compress {

    public static final int zipHeaderLength = 8;
    private byte zip_buffer[] = new byte[4096 * 3];
    private byte send_buffer[] = new byte[4096 * 3 + zipHeaderLength];
    private rcpZipHeader zipHeader = new rcpZipHeader();

    public byte[] compressData(byte[] plainData) {
        int datasize = plainData.length;

        if (zip_buffer.length < datasize) {
            zip_buffer = new byte[datasize];
            send_buffer = new byte[datasize + zipHeaderLength];
        }

        DeflaterEx compresser = new DeflaterEx(DeflaterEx.DEFAULT_COMPRESSION, true, true, true);

        compresser.setInput(plainData, 0, datasize);

        Arrays.fill(zip_buffer, (byte) 0);
        Arrays.fill(send_buffer, (byte) 0);

        int compressedSize = compresser.deflate(zip_buffer);
        if (compressedSize <= 0) {
        }

        zipHeader.originalsize = datasize;
        zipHeader.compresssize = compressedSize;

        int index = 0;
        zipHeader.push(send_buffer, index);
        index += zipHeader.size();
        System.arraycopy(zip_buffer, 0, send_buffer, index, compressedSize);
        int packetSize = zipHeader.size() + compressedSize;
        compresser = null;

        byte tempBuf[] = new byte[packetSize];
        System.arraycopy(send_buffer, 0, tempBuf, 0, packetSize);

        return tempBuf;
    }

    public byte[] uncompressData(byte[] compressData) {
        rcpZipHeader zipHeader = new rcpZipHeader();
        byte[] byteHeader = new byte[zipHeader.size()];
        System.arraycopy(compressData, 0, byteHeader, 0, byteHeader.length);
        zipHeader.save(byteHeader, 0);

        if (zipHeader.compresssize == 0 ||
                zipHeader.originalsize == 0 ||
                zipHeader.compresssize == Integer.MAX_VALUE ||
                zipHeader.originalsize == Integer.MAX_VALUE) {
            return null;
        }

        byte[] unzipdata = new byte[zipHeader.originalsize];
        byte[] zipdata = new byte[zipHeader.compresssize];
        System.arraycopy(compressData, zipHeader.size(), zipdata, 0, zipdata.length);
        int decompressSize = 0;

        boolean finish = true;
        unzip uzip = new unzip(finish);

        try {
            uzip.set_outbuf(unzipdata, 0, zipHeader.originalsize);
            uzip.set_inbuf(zipdata, 0, zipHeader.compresssize);
            decompressSize = uzip.decompress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return unzipdata;
    }

}
