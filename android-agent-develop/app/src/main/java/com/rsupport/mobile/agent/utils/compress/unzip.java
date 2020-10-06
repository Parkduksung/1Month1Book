package com.rsupport.mobile.agent.utils.compress;

import control.InflaterEx;

public class unzip {
    InflaterEx z_zs;
    byte[] outbuf = null;
    int outbufLen = 0;
    int outbufIdx = 0;

    public unzip() {
        z_zs = new InflaterEx(true);
    }

    public unzip(boolean finish) {
        z_zs = new InflaterEx(true, true);
    }

    /**
     * @author kim kun seok (kskim@rsupport.com)
     */
    public void reset() {
        if (z_zs != null)
            z_zs.reset();
    }

    public void set_inbuf(byte[] buf, int offset, int len) {
        z_zs.setInput(buf, offset, len);
    }

    public void set_outbuf(byte[] buf, int offset, int len) {
        outbuf = buf;
        outbufIdx = offset;
        outbufLen = len;
    }

    public int getRemaining() {
        return z_zs.getRemaining();
    }

    public boolean finished() {
        return z_zs.finished();
    }

    public int get_inbuf_size() {
        return z_zs.getRemaining();
    }

    public int decompress() {
        int res = -1;
        try {
            res = z_zs.inflate(outbuf, outbufIdx, outbufLen);
        } catch (java.util.zip.DataFormatException e) {
            res = -1;
            System.err.println("Caught a DataFormatException, reason: " + e.getMessage());
        }
        return res;
    }

    public int getZipStateSize() {
        int size = z_zs.stateSize();
        return size;
    }

    private byte[] m_zipstate;

    public byte[] getZipState() {
        m_zipstate = new byte[getZipStateSize()];
        int len = z_zs.streamSave(m_zipstate);
        return m_zipstate;
    }

//	public void setZipState() {
//		if (m_zipstate == null) return;
//		z_zs.streamLoad(m_zipstate, m_zipstate.length);
//	}

    public int setZipState(byte[] bytes) {
        if (bytes == null) return 0;
        return z_zs.streamLoad(bytes, bytes.length);
    }

    public void end() {
        z_zs.end();
    }
}
