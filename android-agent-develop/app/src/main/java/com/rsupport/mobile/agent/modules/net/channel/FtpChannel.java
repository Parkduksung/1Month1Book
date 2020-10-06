package com.rsupport.mobile.agent.modules.net.channel;

import android.content.Context;

import com.rsupport.commons.net.socket.SocketCompat;
import com.rsupport.jni.IPdu;
import com.rsupport.mobile.agent.modules.net.model.MsgPacket;
import com.rsupport.mobile.agent.modules.ftp.ReceiveDataFtpThread;
import com.rsupport.mobile.agent.modules.ftp.RsFTPTrans;
import com.rsupport.mobile.agent.modules.function.ScreenDraw;
import com.rsupport.mobile.agent.modules.net.model.HeaderPacket;
import com.rsupport.mobile.agent.modules.net.protocol.MessageID;
import com.rsupport.mobile.agent.modules.sysinfo.SystemInfo;
import com.rsupport.mobile.agent.utils.Converter;
import com.rsupport.util.log.RLog;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

public class FtpChannel {
    private static final String className = "FtpChannel";

    public SystemInfo systemInfo;
    private ArrayList<Long> channelList;
    private byte[] sendBuffer;
    public ScreenDraw screenDraw;
    private Context mainContext;
    private HeaderPacket headerPacket;
    public final static int MAX_FTPBUF = 1024 * 10 * 2;

    private SocketCompat mStream;

    public FtpChannel(Context context, SocketCompat stream) {
        mainContext = context;
        headerPacket = new HeaderPacket();
        sendBuffer = new byte[MAX_FTPBUF];
        channelList = new ArrayList<Long>();
        RsFTPTrans.reset();
        mStream = stream;
    }

    public void setContext(Context context) {
        mainContext = context;
    }

    private void startFTPConfirmDialog() {
//		mainContext.startActivity(new Intent(Utility.mainContext, ApplicationLockActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

//	private void callConfirmActivity() {
//		Intent agentIntent = new Intent(mainContext, AgentService.class);
//		agentIntent.putExtra("type", "userconfirm");
//		agentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		mainContext.startService(agentIntent);
//	}

    public boolean procFileHeader(MsgPacket msg) {
        boolean ret = false;
        try {
            if (msg == null) return ret;
            ret = RsFTPTrans.sendProcFTPHeader(new String(msg.getData(), 0, msg.getDataSize(), "UTF-16LE"));
            if (!ret) RsFTPTrans.procClose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public boolean procFTPStart(MsgPacket msg) {
        boolean ret = false;
        try {
            if (msg == null) return ret;
            ret = RsFTPTrans.recvFTPHeader(msg.getData(), msg.getDataSize());
            if (!ret) RsFTPTrans.procClose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private boolean procRecvFileHeader(MsgPacket msg) {
        boolean ret = false;
        try {
            ret = RsFTPTrans.recvFileHeader(msg.getData(), msg.getDataSize());
            if (!ret) RsFTPTrans.procClose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private boolean procRecvFileTransOpt(MsgPacket msg) {
        boolean ret = false;
        try {
            ret = RsFTPTrans.recvFileTransOpt(msg.getData(), msg.getDataSize());
            if (!ret) {
                RsFTPTrans.sendProtocolFTPEnd();
                RsFTPTrans.procClose();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private boolean procRecvFilePos(MsgPacket msg) {
        boolean ret = false;
        try {
            ret = RsFTPTrans.recvFilePos(msg.getData(), msg.getDataSize());
            if (!ret) {
                RsFTPTrans.sendProtocolFTPEnd();
                RsFTPTrans.procClose();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private boolean procRecvDiskFreeSpace(MsgPacket msg) {
        boolean ret = false;
        try {
            ret = RsFTPTrans.recvDiskFreeSpace(msg.getData(), msg.getDataSize());
            if (!ret) RsFTPTrans.procClose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private boolean procRecvFileData(MsgPacket msg) {
        boolean ret = false;
        try {
            ret = RsFTPTrans.recvFileData(msg);
            if (!ret) RsFTPTrans.procClose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public boolean procCancelFile(MsgPacket msg) {
        RLog.i("FTPChanncel procCancelFile");
        RsFTPTrans.isCanceledFile = true;
        if (RsFTPTrans.isRunningDownload()) {
            RLog.i("FTPChanncel procCancelFile isRunningDownload is true");
            sendPacket(MessageID.rcpSFTP, MessageID.rcpExpFTPCancel);
        } else {
            try {
                String strBody = new String(msg.getData(), "UTF-16LE");
                RLog.i("strBody :::::::  " + strBody);
                int lastindex = strBody.lastIndexOf("/") + 1;
                String filename = strBody.substring(lastindex, strBody.length());
                RLog.i("filename :::::::  " + filename);
//				if (ApplicationLockActivity.restFileCount == 1 || strBody.endsWith("CANCEL_ALL")) {
                if (strBody.equals("CANCEL_ALL")) {
//					RLog.i( "rcpExpFTPCancel");
                    sendPacket(MessageID.rcpSFTP, MessageID.rcpExpFTPCancel);
//					GlobalFunction.cancleFTP();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private boolean procRecvFileDataEnd(MsgPacket msg) {
        boolean ret = false;
        try {
            ret = RsFTPTrans.recvFileDataClose();
            if (!ret) RsFTPTrans.procClose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private boolean procFTPSendEnd(MsgPacket msg) {
        boolean ret = false;
        try {
            ret = RsFTPTrans.FTPSendEnd();
            if (!ret) RsFTPTrans.procClose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private boolean procFTPGetEnd(MsgPacket msg) {
        boolean ret = false;
        try {
            ret = RsFTPTrans.FTPGetEnd();
            if (!ret) RsFTPTrans.procClose();


        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public void closeSession() {

        RsFTPTrans.procClose();
    }

    private synchronized void procRcpSFTPThread(final MsgPacket msg) {
        procRcpSFTP(msg);
    }

    private synchronized void procRcpSFTPThread(final IPdu msg) {
//		procRcpSFTP(msg);
    }

    private void procRcpSFTP(MsgPacket msgPacket) {
        RLog.i("FtpChannel procRcpSFTP : " + msgPacket.getMsgID());
        switch (msgPacket.getMsgID()) {
            case MessageID.rcpExpFTPStart:
                procFTPStart(msgPacket);
                break;
            case MessageID.rcpExpFileRequest:
                RLog.i("RsFTP procRcpSFTP rcpExpFileRequest");
                procFileHeader(msgPacket);
                break;
            case MessageID.rcpExpFTPReceiveFileHeader:
                break;
            case MessageID.rcpExpFTPSendFileHeader:
                procRecvFileHeader(msgPacket);
                break;
            case MessageID.rcpExpFTPDiskFreeSpace:
                RLog.i("RsFTP procRcpSFTP rcpExpFTPDiskFreeSpace");
                procRecvDiskFreeSpace(msgPacket);
                break;
            case MessageID.rcpExpFTPOpt:
                procRecvFileTransOpt(msgPacket);
                break;
            case MessageID.rcpExpFTPFilePos:
                RLog.i("RsFTP procRcpSFTP rcpExpFTPFilePos");
                procRecvFilePos(msgPacket);
                break;
            case MessageID.rcpExpFTPData:
                procRecvFileData(msgPacket);
                break;
            case MessageID.rcpExpFTPCancel:
                procCancelFile(msgPacket);
                break;
            case MessageID.rcpExpFTPFileDataEnd:
                procRecvFileDataEnd(msgPacket);
                break;
            case MessageID.rcpExpFTPGetEnd:
                RLog.i("RsFTP procRcpSFTP rcpExpFTPGetEnd");
                procFTPGetEnd(msgPacket);
                break;
            case MessageID.rcpExpFTPSendEnd:
                RLog.i("RsFTP procRcpSFTP rcpExpFTPSendEnd");
                procFTPSendEnd(msgPacket);
                break;
            default:
        }
    }

    public static long stackPacketSeq = 0;
    public static long completedPacketSeq = -1;
    public static HashMap ftpThreadMap = new HashMap<String, ReceiveDataFtpThread>();

    public void deleteFtpThreadMap(long seq) {
        ftpThreadMap.remove(String.valueOf(seq));
    }

    public void startFtpThreadNextToMap() {
        ReceiveDataFtpThread receiveDataThread = null;
        receiveDataThread = (ReceiveDataFtpThread) ftpThreadMap.get(String.valueOf(completedPacketSeq + 1));

        if (receiveDataThread != null) {
            Thread th = new Thread(receiveDataThread);
            th.start();
        }
    }

    public void startFtpThreadToMap() {
        ReceiveDataFtpThread receiveDataThread = null;

        if ((stackPacketSeq - completedPacketSeq) == 1) {
            receiveDataThread = (ReceiveDataFtpThread) ftpThreadMap.get(String.valueOf(completedPacketSeq + 1));
            if (receiveDataThread != null) {
                Thread th = new Thread(receiveDataThread);
                th.start();
            }
        }
    }

    public void deleteThreadMap() {
        ftpThreadMap.clear();
    }


    private static int m_seq;

    public void receiveData(int payloadtype, MsgPacket m_msg) {

        switch (payloadtype) {
            case MessageID.rcpSFTP:
                procRcpSFTPThread(m_msg);
                break;
            default:
                break;
        }
    }

    private synchronized void waitForOrder(long currentTime) {
        channelList.add(currentTime);

        if (channelList.size() > 1) {
            while (true) {
                boolean isAnotherOperating = false;
                for (int i = 0, n = channelList.size(); i < n; i++) {
                    if ((Long) (channelList.get(i)) < currentTime) {
                        isAnotherOperating = true;
                    }
                }
                if (!isAnotherOperating) {
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean sendPacket(int payloadtype, int msgid) {
        return sendPacket(payloadtype, msgid, null, 0);
    }

    public boolean sendPacket(int payloadtype, int msgid, byte[] data, int dataSize) {
        headerPacket.clear();
        int totalPacketSize = 0;
        if (data != null && dataSize > 0) {
            totalPacketSize = MessageID.sz_rcpPacket + MessageID.sz_rcpDataMessage + dataSize;
        } else {
            totalPacketSize = MessageID.sz_rcpPacket + MessageID.sz_rcpMessage;
        }

//		byte sendPacket[] = new byte[totalPacketSize];
        byte sendPacket[] = new byte[totalPacketSize + 1];

//		int packetPos = ChannelProto.sz_rcpPacket;
        int packetPos = MessageID.sz_rcpPacket;

//		sendPacket[0] = (byte)0;

        headerPacket.setPayloadtype(payloadtype);
        headerPacket.setMsgsize(totalPacketSize - MessageID.sz_rcpPacket);
        headerPacket.push(sendPacket, 0);
//		rcpPacket.push(sendPacket, 1);

        if (dataSize > 0 && (data != null)) {
            sendPacket[packetPos] = (byte) msgid;
            packetPos++;
            System.arraycopy(Converter.getBytesFromIntLE(dataSize), 0, sendPacket, packetPos, 4);
            packetPos += 4;
            System.arraycopy(data, 0, sendPacket, packetPos, dataSize);
        } else {
            sendPacket[packetPos] = (byte) msgid;
        }
        boolean ok = mStream.getDataStream().write(sendPacket, 0, totalPacketSize);
        ;
        if (ok) {
            return true;
        } else {
            RLog.e("WriteToFTPChannel_Fail");
            return false;
        }
    }

    private boolean readPacket(HeaderPacket headerPacket, byte[] receivedData, int offset) {
        if (headerPacket.size() > receivedData.length) return false;
        headerPacket.save(receivedData, offset);
        return true;
    }

    private boolean readMsg(MsgPacket msg, byte[] receivedData, int msgSize) {
        if (msgSize <= 0 && receivedData.length <= 0) return false;
        msg.save(receivedData, 9, 0, msgSize);
        return true;
    }

    private boolean readMsgEx(MsgPacket msg, byte[] receivedData, int msgSize, int offset) {
        if (msgSize <= 0 && receivedData.length <= 0) return false;
        msg.save(receivedData, offset + 5, 0, msgSize);
        return true;
    }

}


