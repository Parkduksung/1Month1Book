package com.rsupport.util;

import java.io.FileDescriptor;
import java.io.IOException;

import android.os.Build;
import android.util.Log;


public class Net10 {
	public final static int WS_PREPAD_SIZE = (2+8+4);

	public final static int INFO_CONNECTED = 1;			// CoNNE-CODE(0x0)-
	public final static int INFO_CLOSED = 2;					// DISCO-CODE(0x0)-message
	
	// 내부 이벤트.
	public final static int INFO_RSPERM_V1 = 201;	
	public final static int INFO_RSPERM_V2 = 202;	
	public final static int INFO_RSPERM_FAIL = 400;
	
	
	public final static int WS_PREPAD = WS_PREPAD_SIZE;
	
	final public static int MAX_CHANNEL;
	// p2p
	static native public boolean jniP2PServerOpen(int port);
	static native public void    jniP2PServerClose();
	static native public int     jniP2PAccept();
	static native public boolean jniP2PConnect(String addr, int port, int channelId);
	static native public void    jniP2PClose(int channelId);
	static native public byte[]  jniP2PRead(int channelId);
	static native public boolean jniP2PWrite(byte[] data, int offset, int size, int channelId);
	static native public String  jniP2PGetChannelInfo(int channelId);
	//static native public boolean jniP2PSendFd(String udsAddr, int channelId, int timeout);

	// relay
	static native public boolean jniRelayOpen(String ip, int port, String guid, int channelId);
	static native public boolean jniRelayAccept(int channelId);
	// static native public void    jniRelayClose(int channelId);
	// static native public byte[]  jniRelayRead(int channelId);
	// static native public boolean jniRelayWrite(byte[] data, int offset, int size, int channelId);
	// static native public String  jniRelayGetChannelInfo(int channelId);
	static public void    jniRelayClose(int channelId) { jniP2PClose(channelId+MAX_CHANNEL); }
	static public byte[]  jniRelayRead(int channelId) { return jniP2PRead(channelId+MAX_CHANNEL); }
	static public boolean jniRelayWrite(byte[] data, int offset, int size, int channelId)  {  return jniP2PWrite(data, offset, size, channelId+MAX_CHANNEL);}
	static public String  jniRelayGetChannelInfo(int channelId)  { return jniP2PGetChannelInfo(channelId+MAX_CHANNEL); }

	static native public void jniTest();
	static native private int jniGetMaxChannel();
	// unix domain socket
	static native public boolean 			sendFd(FileDescriptor sock, int theFd);
	static native public FileDescriptor 	recvFd(FileDescriptor sock) throws IOException;

	public static class ChannelInfo {
		public int 		channelId;
		public int 		fd;
		public String 	connectedHost;
		public int		connectedPort;
		private ChannelInfo(int channelId, int fd, String host, int port) {
			this.channelId = channelId;
			this.fd = fd;
			connectedHost = host;
			connectedPort = port;
		}
		
		@Override
		public String toString() {
			return String.format("ChannelInfo: id:%d, fd:%d, connected: [host: %s, port: %d]", 
					fd, channelId, connectedHost, connectedPort);
		}
	};
	
	static native public ChannelInfo jniGetChannelInfo(int channelId);
	static native public void        jniSetFileDescriptor(FileDescriptor sock, int i);
	
	static native public void        jniProfile(boolean on);

	
	static {
		boolean loaded = false;
		try {
			System.loadLibrary("net10");
			loaded = true;
		}
		catch(Exception e) {
			Log.e("Net10", "loading failed: " + e.toString());
		}
		MAX_CHANNEL = loaded ? jniGetMaxChannel() : 0;
	}
}
