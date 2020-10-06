package com.rsupport.mobile.agent.modules.net.model;

import com.rsupport.mobile.agent.utils.Converter;


public class RcpClipboardBeginMsg implements Packet {
    //	public char format;
    public byte format;
    public int datasize = 0;

    public RcpClipboardBeginMsg() {
//		format = MessageID.Clipboard_Text;
//		format = MessageID.Clipboard_UnicodeText;
        format = (byte) 2;
    }

    @Override
    public void push(byte[] szBuffer, int start) {
        int nIndex = start;
        System.arraycopy(new byte[]{format}, 0, szBuffer, nIndex, 1);
//		System.arraycopy(format, 0, szBuffer, nIndex, 1);
        nIndex += 1;
        System.arraycopy(Converter.getBytesFromIntLE(datasize), 0, szBuffer, nIndex, 4);
    }

    @Override
    public void save(byte[] szBuffer, int start) {
        int nIndex = start;

//		format = (char)szBuffer[0];
        format = szBuffer[0];
        nIndex += 1;
        byte[] datasizeBytes = new byte[4];
        System.arraycopy(szBuffer, nIndex, datasizeBytes, 0, 4);
        datasize = Converter.readIntLittleEndian(datasizeBytes);
//		datasize = Converter.getIntFromBytes(datasizeBytes);
    }

    @Override
    public void save(byte[] szBuffer, int start, int dstOffset, int dstLen) {

    }

    @Override
    public int size() {
        return 5;
    }

    @Override
    public void clear() {

    }

}
