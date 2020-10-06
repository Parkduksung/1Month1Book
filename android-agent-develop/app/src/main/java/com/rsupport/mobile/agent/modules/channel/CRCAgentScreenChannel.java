package com.rsupport.mobile.agent.modules.channel;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Display;
import android.view.WindowManager;

import com.rsupport.commons.net.socket.SocketCompat;
import com.rsupport.commons.net.socket.rsnetcl.callback.OnFPSChangedCallback;
import com.rsupport.media.ICodecAdapter;
import com.rsupport.media.stream.ScreenStream;
import com.rsupport.mobile.agent.modules.channel.screen.ChannelConstants;
import com.rsupport.mobile.agent.modules.channel.screen.ScreenStreamFactory;
import com.rsupport.mobile.agent.modules.channel.screen.StreamController;
import com.rsupport.mobile.agent.modules.net.model.HeaderPacket;
import com.rsupport.mobile.agent.modules.net.model.MsgPacket;
import com.rsupport.mobile.agent.modules.net.protocol.MessageID;
import com.rsupport.mobile.agent.utils.Converter;
import com.rsupport.mobile.agent.utils.timer.DebounceTimer;
import com.rsupport.util.log.RLog;

import org.jetbrains.annotations.NotNull;

public class CRCAgentScreenChannel extends CRCChannel implements StreamController, OnFPSChangedCallback {
    public static final int PACKET_HEADERSIZE = MessageID.sz_rcpPacket + MessageID.sz_rcpDataMessage;

    private ICodecAdapter codecAdapter = new ICodecAdapter();
    private HeaderPacket headerPacket = new HeaderPacket();

    private ScreenStreamFactory screenStreamFactory;
    private ScreenStream screenStream;
    private OnSendPacketListener sendPacketListener;
    private int savedRotation;
    private Display display;

    public CRCAgentScreenChannel(@NotNull Context context, @NotNull ScreenStreamFactory screenStreamFactory, @NotNull DebounceTimer nopTimer) {
        super(context, nopTimer);
        display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        this.screenStreamFactory = screenStreamFactory;
        savedRotation = display.getRotation();
    }

    public void setSendPacketListener(OnSendPacketListener sendPacketListener) {
        this.sendPacketListener = sendPacketListener;
    }

    @Override
    protected void onPrepare() {
    }

    @Override
    protected void onConnected(@NotNull SocketCompat socketCompat) {
    }

    @Override
    protected void onDisconnected() {

    }

    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setCodecDataHandler(ICodecAdapter codecAdapter) {
        this.codecAdapter = codecAdapter;
    }

    /**
     * 화면 회전 시 callback.
     */
    private void startStreamReload() {
        if (!isConnected()) {
            return;
        }
        if (screenStream == null) return;
        screenStream.startStreamReload();
    }

    @Override
    public void restart() {
        startStreamReload();
    }

    @Override
    public void start() {
        startStream();
    }

    @Override
    public void stop() {
        stopStream();
    }

    @Override
    public void pause() {
        pauseStream();
    }

    @Override
    public void resume() {
        resumeStream();
    }

    private void startStream() {
        if (!isConnected()) return;

        if (screenStream == null) {
            screenStream = screenStreamFactory.create();
            if (screenStream != null) {
                screenStream.setEncoderListener(codecAdapter);
            }
        }
        if (screenStream != null) {
            screenStream.start();
        }
    }

    private void stopStream() {
        if (screenStream == null) return;
        screenStream.stop();
    }

    private void resumeStream() {
        RLog.i("VideoStreamTrace CRCAgentScreenChannel resumeStream");
        if (screenStream == null) return;
        screenStream.resume();
    }

    private void pauseStream() {
        RLog.i("VideoStreamTrace CRCAgentScreenChannel pauseStream");
        if (screenStream == null) return;
        screenStream.pause();
        sleep();
    }

    private void closeStream() {
        RLog.i("VideoStreamTrace CRCAgentScreenChannel closeStream");
        if (screenStream == null) return;
        screenStream.close();
        screenStream = null;
    }

    @Override
    public void onConfigChanged(@NotNull Configuration newConfig) {
        if (screenStream == null) return;

        int currentRotation = display.getRotation();
        screenStream.changeRotation(getContext(), savedRotation, currentRotation);
        savedRotation = currentRotation;
    }

    @Override
    protected void onConnectFail() {

    }

    @Override
    protected void onReleased() {
        stopStream();
        closeStream();
    }

    private H264OutPutData createTestOutData(int size) {
        return new H264OutPutData(size);
    }

    //screenChannel
    public void onReceivePacket(int payloadtype, MsgPacket msg) {

        RLog.i("Screen ChannelReadExact Payload :" + payloadtype + "msgid : " + msg.getMsgID());
        switch (payloadtype) {
            case ChannelConstants.rcpChannelNop:
                if (msg.getMsgID() == ChannelConstants.rcpNopConfirmNoAck) {
                    sendNop(ChannelConstants.rcpNopConfirmNoAck);
                    RLog.i("sendScreenNop!!");
                }
        }
    }

    /**
     * @param payloadtype
     * @param msgid
     * @param data        data 는 headerPacket(5byte) + msgPacket(5byte) 의 크기를 포함한 buffer 크기이다.
     * @param dataSize    header(5) + msg(5) 를 제외한 실 데이터크기
     * @return
     */
    public boolean sendBoostPacket(int payloadtype, int msgid, byte[] data, int dataSize) {
        if (!isConnected()) return false;

        int totalPacketSize = calcTotalPacketSize(data, dataSize);
        byte[] sendBuffer = generateSendBuffer(data, dataSize, totalPacketSize);

        setupHeaderBuffer(payloadtype, sendBuffer, totalPacketSize);
        setupMsgBuffer((byte) msgid, dataSize, sendBuffer);

        return writeExtractNotifyDataSize(dataSize, totalPacketSize, sendBuffer);
    }

    private boolean writeExtractNotifyDataSize(int dataSize, int totalPacketSize, byte[] sendBuffer) {
        if (writeExact(sendBuffer, 0, totalPacketSize)) {
            notifyH264PacketDataSize(dataSize);
            return true;
        }
        return false;
    }

    private void setupMsgBuffer(byte msgid, int dataSize, byte[] sendBuffer) {
        int packetPos = MessageID.sz_rcpPacket;
        sendBuffer[packetPos] = msgid;

        if (sendBuffer.length >= packetPos + MessageID.sz_rcpDataMessage) {
            packetPos++;
            System.arraycopy(Converter.getBytesFromIntLE(dataSize), 0, sendBuffer, packetPos, 4);
        }
    }

    private void setupHeaderBuffer(int payloadtype, byte[] data, int totalPacketSize) {
        headerPacket.clear();
        headerPacket.setPayloadtype(payloadtype);
        headerPacket.setMsgsize(totalPacketSize - MessageID.sz_rcpPacket);
        headerPacket.push(data, 0);
    }

    private byte[] generateSendBuffer(byte[] data, int dataSize, int totalPacketSize) {
        if (!availableData(data, dataSize)) {
            data = new byte[totalPacketSize];
        }
        return data;
    }

    private int calcTotalPacketSize(byte[] data, int dataSize) {
        int totalPacketSize;
        if (availableData(data, dataSize)) {
            totalPacketSize = MessageID.sz_rcpPacket + MessageID.sz_rcpDataMessage + dataSize;
        } else {
            totalPacketSize = MessageID.sz_rcpPacket + MessageID.sz_rcpMessage;
        }
        return totalPacketSize;
    }

    private boolean availableData(byte[] data, int dataSize) {
        return data != null && dataSize > 0;
    }

    private void notifyH264PacketDataSize(int dataSize) {
        if (sendPacketListener != null) {
            sendPacketListener.onSend(createTestOutData(dataSize));
        }
    }

    /**
     * header(5) + msg(5) + data 로 패킷을 구성하여 전송한다.
     *
     * @param payloadtype
     * @param msgid
     * @param data
     * @param dataSize
     * @return
     */
    public boolean sendPacket(int payloadtype, int msgid, byte[] data, int dataSize) {
        if (!isConnected()) return false;

        int totalPacketSize = calcTotalPacketSize(data, dataSize);
        byte[] sendPacket = generateSendBuffer(totalPacketSize);

        setupHeaderBuffer(payloadtype, sendPacket, totalPacketSize);
        setupMsgBuffer((byte) msgid, dataSize, sendPacket);
        setupMsgDataBuffer(data, dataSize, sendPacket);
        return writeExtractOrRelease(totalPacketSize, sendPacket);
    }

    private byte[] generateSendBuffer(int totalPacketSize) {
        return new byte[totalPacketSize];
    }

    private boolean writeExtractOrRelease(int totalPacketSize, byte[] sendPacket) {
        if (!writeExact(sendPacket, 0, totalPacketSize)) {
            release();
            return false;
        }
        return true;
    }

    private void setupMsgDataBuffer(byte[] data, int dataSize, byte[] sendPacket) {
        if (availableData(data, dataSize)) {
            System.arraycopy(data, 0, sendPacket, MessageID.sz_rcpPacket + MessageID.sz_rcpDataMessage, dataSize);
        }
    }

    @Override
    public void onChanged(int fps) {
        if (screenStream == null) return;
        screenStream.setFps(fps);
    }

    public interface OnSendPacketListener {
        void onSend(H264OutPutData data);
    }
}
