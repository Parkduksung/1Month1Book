package com.rsupport.srn30.screen.channel;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.content.Context;
import android.util.Log;

import com.rsupport.srn30.Srn30Native;
import com.rsupport.util.Net10;
import com.rsupport.util.rslog.MLog;

public class ScreenChannel implements Runnable, IChannelWriter{
	private OnPacketHandler packetHandler = null;
	private int channelID = -1;
	private boolean isRunning = false;
	private Thread thread = null;
	
	public ScreenChannel(Context context) {
	}

	public synchronized void onDestroy() {
		MLog.i("#enter onDestroy");
		packetHandler = null;
		stop();
		MLog.i("#exit onDestroy");
	}
	
	public synchronized boolean connect(int channelID) throws Exception{
		this.channelID = channelID;
		return true;
	}
	
	@Override
	public synchronized boolean write(byte[] buffer, int offset, int count) throws Exception{
		return Net10.jniP2PWrite(buffer, offset, count, channelID);
	}
	
	@Override
	public synchronized boolean write(ByteBuffer imageBuffer) throws Exception{
		return Srn30Native.sendVDFrame(imageBuffer, channelID);
	}
	
	@Override
	public synchronized boolean writeAshmem(long ashmemAddress) throws Exception{
		return Srn30Native.sendAFrame(ashmemAddress, channelID);
	}
	
	public boolean sendVersionMsg(int hwRotation) throws Exception{
		ByteBuffer versionMsgBuffer = Srn30Packet.scapVersionMsg(hwRotation);
		return write(versionMsgBuffer.array(), 0, versionMsgBuffer.position());
	}
	
	public void setOnHandler(OnPacketHandler packetHandler) {
		this.packetHandler = packetHandler;
	}

	private boolean readNet10() throws Exception{
		byte[] packet = Net10.jniP2PRead(channelID);
		return (packetHandler != null && packetHandler.handlePacket(ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN)));
	}

	public synchronized void start() {
		if(thread != null){
			throw new RuntimeException("thread is already started.");
		}
		
		isRunning = true;
		thread = new Thread(this, "ScreenChannel");
		thread.start();
	}
	
	public synchronized void stop(){
		isRunning = false;
		Net10.jniP2PClose(channelID);
	}
	
	@Override
	public void run() {
		try {
			while(isRunning && Thread.interrupted() == false){
				if(readNet10() == false){
					break;
				}
			}
		} catch (Exception e) {
			MLog.w(Log.getStackTraceString(e));
		}
		
		try {
			if(packetHandler != null){
				packetHandler.onClose();
			}
		} catch (Exception e) {
		}
		MLog.w("screen channel is stopped");
		thread = null;
	}
	
	public static interface OnPacketHandler{
		public boolean handlePacket(ByteBuffer msg);
		public void onClose();
	}
}
