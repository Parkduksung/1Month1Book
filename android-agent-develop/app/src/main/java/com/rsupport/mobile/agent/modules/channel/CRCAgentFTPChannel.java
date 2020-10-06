package com.rsupport.mobile.agent.modules.channel;

import android.content.Context;
import android.content.res.Configuration;

import com.rsupport.commons.net.socket.SocketCompat;
import com.rsupport.mobile.agent.modules.net.channel.FtpChannel;
import com.rsupport.mobile.agent.modules.net.model.MsgPacket;
import com.rsupport.mobile.agent.utils.timer.DebounceTimer;
import com.rsupport.util.log.RLog;

import org.jetbrains.annotations.NotNull;

import control.DeflaterEx;

public class CRCAgentFTPChannel extends CRCChannel {

    private DeflaterEx compressor = new DeflaterEx(DeflaterEx.DEFAULT_COMPRESSION, true);

    private FtpChannel rcmpFTPChannel;

    public CRCAgentFTPChannel(Context context, @NotNull DebounceTimer debounceTimer) {
        super(context, debounceTimer);
    }

    public FtpChannel getRcmpFTPChannel() {
        return rcmpFTPChannel;
    }

    @Override
    protected void onPrepare() {

    }

    @Override
    protected void onConnected(SocketCompat socketCompat) {
        rcmpFTPChannel = new FtpChannel(getContext(), socketCompat);
        RLog.e("Close FTPChannel...");
    }

    @Override
    protected void onDisconnected() {

    }

    public void onReceivePacket(int payloadtype, MsgPacket msg) {
        rcmpFTPChannel.receiveData(payloadtype, msg);
    }

    @Override
    public void onConfigChanged(@NotNull Configuration newConfig) {

    }

    @Override
    protected void onConnectFail() {

    }

    @Override
    protected void onReleased() {
        compressor.end();
    }
}


