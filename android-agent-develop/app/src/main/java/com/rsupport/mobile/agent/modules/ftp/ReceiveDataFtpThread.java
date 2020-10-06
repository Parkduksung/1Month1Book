package com.rsupport.mobile.agent.modules.ftp;

public class ReceiveDataFtpThread implements Runnable {

    private byte[] m_data;
    private int m_offset;
    private long m_seq;
    private boolean m_isStarted = false;


    public ReceiveDataFtpThread(byte[] data, long seq, int offset) {
        init(data, seq, offset);
    }

    private void init(byte[] data, long seq, int offset) {
        m_data = data;
        m_seq = seq;
        m_offset = offset;
    }

    @Override
    public void run() {
        if (m_isStarted) return;
        m_isStarted = true;
//		ServiceBind.getInstance(null).ftpChannel.receiveData(m_data, m_offset);
        clearThread();
    }

    private void clearThread() {
        //Todo clearThread
//    	if (ServiceBind.getInstance(null) == null) return;
//    	if (!ServiceBind.isConnected) {
//    		ServiceBind.getInstance(null).ftpChannel.deleteThreadMap();
//    		return;
//    	}
//		ServiceBind.getInstance(null).ftpChannel.deleteFtpThreadMap(m_seq);
//    	FtpChannel.completedPacketSeq = m_seq;
//		ServiceBind.getInstance(null).ftpChannel.startFtpThreadNextToMap();
//    	m_data = null;
    }

}
