package com.rsupport.rsperm;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Build;
import android.os.SystemClock;

import com.rsupport.srn30.Srn30Native;
import com.rsupport.srn30.screen.capture.AbstractCaptureable;
import com.rsupport.srn30.screen.capture.IScreenCaptureable;
import com.rsupport.srn30.screen.encoder.ScapOption;
import com.rsupport.util.LauncherUtils;
import com.rsupport.util.MemoryFileEx;
import com.rsupport.util.Net10;
import com.rsupport.util.Screen;
import com.rsupport.util.Utils;
import com.rsupport.util.rslog.MLog;


public class UDSPermission extends AbstractPermission{
	private final String UDS_SCREEN_ADDRESS = "udsbinder";
	private final String UDS_VD_SCREEN_ADDRESS = "vd.udsbinder";

	private UDSConnection screenConnection = null;
	private UDSConnection vdScreenConnection = null;
	private SocketConnection inputConnection = null;
	private boolean isVirtualDisplaySupport = false;

	@Override
	public int getType() {
		return BIND_TYPE_UDS;
	}

	@Override
	public synchronized void onDestroy() {
		MLog.i("#enter onDestroy");
		
		unbind();
		super.onDestroy();
		MLog.i("#exit onDestroy");
	}

	@Override
	public boolean bind(String address) {
		String packageName = getContext().getPackageName();
		screenConnection = new UDSConnection("uds");
		MLog.v(packageName + "." + UDS_SCREEN_ADDRESS +
				"." + LauncherUtils.getLauncherPID(getContext()));
		if(screenConnection.connect(packageName + "." + UDS_SCREEN_ADDRESS +
                "." + LauncherUtils.getLauncherPID(getContext())) == false){
			MLog.e("screenConnection fail");
			screenConnection.close();
			screenConnection = null;
			return false;
		}
		
		inputConnection = new SocketConnection("input");

		// liblauncherxx.so 가 process 에 살아 있을 경우에만 시도한다.
		int launcherPID = LauncherUtils.getLauncherPID(getContext(), true);
		if(launcherPID == LauncherUtils.INVALID_LAUNCHER){
			MLog.e("not found launcher!");
		}

		if(LauncherUtils.hasInjector() == true &&
				inputConnection.connect(
						Utils.getPid(getContext().getPackageName(), new String[]{"liblauncher"}),
						3000) == false){
			MLog.w("inputConnection fail");
			inputConnection.close();
			inputConnection = null;
		}
		
		if(connectVDChannel(packageName, 0) == true){
			isVirtualDisplaySupport = true;
			closeUDSConnection(vdScreenConnection);
		}
		
		waitForBind(BIND_TIME_OUT);
		return isBound();
	}
	
	private boolean connectVDChannel(String packageName, int timeOut){
		closeUDSConnection(vdScreenConnection);
		vdScreenConnection = new UDSConnection("vuds");
		if(vdScreenConnection.connect(packageName + "." + UDS_VD_SCREEN_ADDRESS +
                "." + LauncherUtils.getLauncherPID(getContext()), timeOut) == false){
			MLog.i("vd screenConnection fail");
			vdScreenConnection.close();
			vdScreenConnection = null;
			return false;
		}
		MLog.d("vd uds channel connected");
		return true;
	}
	
	private void closeUDSConnection(UDSConnection connection){
		if(connection != null && connection.isConnected()){
			connection.close();
			connection = null;
		}
	}
	
	@Override
	public synchronized void unbind() {
		MLog.i("unbind");
		
		closeUDSConnection(screenConnection);
		closeUDSConnection(vdScreenConnection);
		if(inputConnection != null){
			inputConnection.close();
			inputConnection = null;
		}
	}

	@Override
	public synchronized boolean isBound() {
		return (screenConnection != null && screenConnection.isConnected());
	}

	@Override
	public synchronized int hwRotation() throws Exception{
		ByteBuffer bb = RSPermHelper.buildRequest(true, 32, JNI_GetHWRotation, Screen.resolution(getContext()));
		screenConnection.write(bb.array(), 0, bb.position());
		screenConnection.read(bb.array(), 0, 8);
		return bb.getInt(4);
	}

	@Override
	public int[] getSupportEncoder() {
		ArrayList<Integer> supportEncoder = new ArrayList<Integer>();
		supportEncoder.add(ScapOption.ENCODER_TYPE_JPG);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			supportEncoder.add(ScapOption.ENCODER_TYPE_OMX);
		}

		return toIntArray(supportEncoder);
	}

    @Override
    public int[] getSupportCaptureType() {
        ArrayList<Integer> supportEncoder = new ArrayList<Integer>();
        supportEncoder.add(ScapOption.ENCODER_TYPE_JPG);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            supportEncoder.add(ScapOption.ENCODER_TYPE_OMX);
        }

        return toIntArray(supportEncoder);
    }

	private synchronized FileDescriptor getScreenFD(String args) throws Exception {
		ByteBuffer bb = RSPermHelper.buildRequest(true, 256, JNI_ASHMCreate, args);
		screenConnection.write(bb.array(), 0, bb.position());
		return Net10.recvFd(screenConnection.getSocketFD());
	}

	private synchronized boolean initScreen(int w, int h) throws Exception {
		ByteBuffer bb = RSPermHelper.buildRequest(true, 13, JNI_ASHMInitScreen, w, h);
		screenConnection.write(bb.array(), 0, 13);
		screenConnection.read(bb.array(), 0, 8);
		return bb.getInt(4) == COMMAND_OK;
	}

	@Override
	public IScreenCaptureable createScreenCaptureable(ScapOption scapOption) {
		switch(scapOption.getEncoderType()){
		case ScapOption.ENCODER_TYPE_JPG:
		case ScapOption.ENCODER_TYPE_OMX:
			if(isVirtualDisplaySupport == true){
				if(scapOption.getEncoderType() == ScapOption.ENCODER_TYPE_OMX){
					captureable = new AshmemVirtualDisplayOMXCaptureable();
				}
				else{
					captureable = new AshmemVirtualDisplayCaptureable();
				}
			}
			else{
				captureable = new AshmemCaptureable();
			}
			break;
		}
		captureable.setScapOption(scapOption);
		return captureable;
	}

	@Override
	public synchronized boolean screenshot(String imgPath) throws IOException{
		ByteBuffer bb = RSPermHelper.buildRequest(true, 256, JNI_ASHMScreenshot, imgPath);
		screenConnection.write(bb.array(), 0, bb.position());
		screenConnection.read(bb.array(), 0, 8);
		boolean screenResult = bb.getInt(4) == COMMAND_OK;
		if(screenResult == true){
			if (screenShotCallback != null) {
				screenShotCallback.onReady();
			}
			screenResult = Utils.rgbaToJpg(imgPath);
		}
		return screenResult;
	}

	@Override
	public synchronized boolean setMaxLayer(int layer) throws IOException{
		ByteBuffer bb = RSPermHelper.buildRequest(true, 15, JNI_SetScreenshotMaxLayer, layer);
		screenConnection.write(bb.array(), 0, bb.position());
		screenConnection.read(bb.array(), 0, 8);
		return bb.getInt(4) == COMMAND_OK;
	}

	@Override
	public synchronized boolean putSFloat(String name, float value) throws Exception {
		return new UDSSettings(getContext().getPackageName()).putSFloat(this, name, value);
	}

	@Override
	public synchronized boolean putSInt(String name, int value) throws Exception {
		return new UDSSettings(getContext().getPackageName()).putSInt(this, name, value);
	}

	@Override
	public synchronized boolean putSLong(String name, long value) throws Exception {
		return new UDSSettings(getContext().getPackageName()).putSLong(this, name, value);
	}

	@Override
	public synchronized boolean putSString(String name, String value) throws Exception {
		return new UDSSettings(getContext().getPackageName()).putSString(this, name, value);
	}

	@Override
	public synchronized boolean putGFloat(String name, float value) throws Exception {
		return new UDSSettings(getContext().getPackageName()).putGFloat(this, name, value);
	}

	@Override
	public synchronized boolean putGInt(String name, int value) throws Exception {
		return new UDSSettings(getContext().getPackageName()).putGInt(this, name, value);
	}

	@Override
	public synchronized boolean putGLong(String name, long value) throws Exception {
		return new UDSSettings(getContext().getPackageName()).putGLong(this, name, value);
	}

	@Override
	public synchronized boolean putGString(String name, String value) throws Exception {
		return new UDSSettings(getContext().getPackageName()).putGString(this, name, value);
	}
	
	@Override
	public synchronized String exec(String cmd) throws Exception {
		ByteBuffer bb = RSPermHelper.buildRequest(true, 256, JNI_Execute, cmd);
		screenConnection.write(bb.array(), 0, bb.position());
		screenConnection.read(bb.array(), 0, 4);
		int size = bb.getInt(0);
		byte[] reply = new byte[size];
		screenConnection.read(reply, 0, size);
		return new String(reply, "UTF-8");
	}

	class UDSConnection{
		private LocalSocket socket = null;
		private InputStream inputStream = null;
		private OutputStream outputStream = null;
		private volatile boolean isClosed = false;

		private String name = null;
		
		public UDSConnection(String name){
			this.name = name;
		}
		
		public boolean connect(String udsbinder, int timeOut){
			if(timeOut <= 0){
				return connect(udsbinder);
			}
			
			boolean result = false;
			long startTime = System.currentTimeMillis();
			while((System.currentTimeMillis() - startTime < timeOut) && isClosed == false){
				result = connect(udsbinder);
				if(result == true){
					break;
				}
				MLog.w("retry connection.%s", udsbinder);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					return result;
				}
			}
			return result;
		}
		
		public boolean connect(String udsbinder){
			try {
				socket = new LocalSocket();
				socket.connect(new LocalSocketAddress(udsbinder));
				inputStream = socket.getInputStream();
				outputStream = socket.getOutputStream();
			} catch (Exception e) {
				return false;
			}
			return true;
		}

		public synchronized FileDescriptor getSocketFD(){
			return socket.getFileDescriptor();
		}

		public synchronized boolean write(byte[] buffer, int offset, int count) throws IOException{
			if(outputStream == null){
				return false;
			}
			outputStream.write(buffer, offset, count);
			return true;
		}
		
		public synchronized boolean write(int data) throws IOException{
			if(outputStream == null){
				return false;
			}
			outputStream.write(data);
			return true;
		}

		public synchronized int read(byte[] buffer, int offset, int length) throws IOException{
			if(inputStream == null){
				return -1;
			}
			return inputStream.read(buffer, offset, length);
		}
		
		public synchronized int read() throws IOException{
			if(inputStream == null){
				return -1;
			}
			return inputStream.read();
		}
		
		public synchronized int available() throws IOException{
			if(inputStream == null){
				return -1;
			}
			int available = inputStream.available();
			return available==0?-1:available;
		}

		public synchronized boolean isConnected(){
			return (socket != null && socket.isConnected());
		}

		public void close(){
			MLog.d("close.%s", name);
			isClosed = true;
			safetyClose(inputStream);
			safetyClose(outputStream);
			safetyCloseLocalSocket(socket);
			socket = null;
			inputStream = null;
			outputStream = null;
		}
	}
	
	@Override
	public void inject(byte[] data, int offset, int length) throws Exception {
		data[offset-1] = (byte)length;
		--offset;
		++length;
		inputConnection.write(data, offset, length);
	}
	
	class AshmemVirtualDisplayOMXCaptureable extends AshmemCaptureable{
		private boolean isReuse = false;
		@Override
		public synchronized Object initialized() throws Exception {
			MLog.i("AshmemVirtualDisplayOMXCaptureable initialized");
			// channel 연결절 ashmem 을 먼저 생성해야한다.
			Object resultObject = super.initialized();
			if(resultObject != null){
				closeUDSConnection(vdScreenConnection);
				if(connectVDChannel(getContext().getPackageName(), 2000) == false){
					MLog.e("vd.udsbinder not connected");
					return null;
				}
			}
			return resultObject;
		}

		@Override
		public synchronized boolean capture() throws Exception {
			return true;
		}
		
		@Override
		public int prepareCapture() throws Exception {
			// readEvent
			if(vdScreenConnection.available() == -1){
				isReuse = true;
				return IScreenCaptureable.STATE_NEXT;
			}
			
			isReuse = false;
			// readsize : 0 - ImageReader 객체 없음
			// -1 : reuse
			// 1 : image read 완료.
			int readSize = vdScreenConnection.read();
			return (readSize == 0) ? IScreenCaptureable.STATE_CONTINUE:IScreenCaptureable.STATE_NEXT;
		}
		
		@Override
		public boolean postCapture() throws Exception {
			if(isReuse == true){
				return true;
			}
			// replyEvent
			return vdScreenConnection.write(1);
		}
		
		@Override
		public synchronized void close() {
			closeUDSConnection(vdScreenConnection);
			vdScreenConnection = null;
			super.close();
		}
	}
	
	class AshmemVirtualDisplayCaptureable extends AshmemCaptureable{
		@Override
		public synchronized Object initialized() throws Exception {
			MLog.i("AshmemVirtualDisplayCaptureable initialized");
			// vd channel 연결전에 ashmem 을 먼저 만들어야 한다.
			Object resultObject = super.initialized();
			if(resultObject != null){
				closeUDSConnection(vdScreenConnection);
				if(connectVDChannel(getContext().getPackageName(), 2000) == false){
					MLog.e("vd.udsbinder not connected");
					return null;
				}
			}
			return resultObject;
		}

		@Override
		public synchronized boolean capture() throws Exception {
			return true;
		}
		
		@Override
		public int prepareCapture() throws Exception {
			// readEvent
			int readSize = vdScreenConnection.read();
			if(readSize == 0){
				return IScreenCaptureable.STATE_CONTINUE;
			}
			return IScreenCaptureable.STATE_NEXT;
		}
		
		@Override
		public boolean postCapture() throws Exception {
			// replyEvent
			return vdScreenConnection.write(1);
		}
		
		@Override
		public synchronized void close() {
			closeUDSConnection(vdScreenConnection);
			vdScreenConnection = null;
			super.close();
		}
	}

	class AshmemCaptureable extends AbstractCaptureable{
		private MemoryFileEx ashmem = null;
		private ByteBuffer mCaptureReqCache = null;

		private int width = 0;
		private int height = 0;

		@Override
		public synchronized Object initialized() throws Exception{
			MLog.i("AshmemCaptureable initialized");
			width = scapOption.getStretch().x;
			height = scapOption.getStretch().y;

			StringBuilder sb = new StringBuilder("ashm=screen");
			sb.append("&width=").append(width);
			sb.append("&height=").append(height);
			sb.append("&bitType=").append(scapOption.getBitType());

			MLog.i("initAshmem width(%d), height(%d), bitType(%d)", width, height, scapOption.getBitType());

			ashmem = new MemoryFileEx(getScreenFD(sb.toString()), -1);
			for (int i=0; i<10000; i+= 1000) {
				// fill screen info.
				if (initScreen(width, height)){
					boolean result = Srn30Native.initEncoder(ashmem.address(), getEncOptions(scapOption));
					if(result == true){
						return ashmem;
					}
				}
				SystemClock.sleep(1000); // in DRM state.
				MLog.w("in DRM state...");
			}
			return null;
		}
		
		@Override
		public int prepareCapture() throws Exception {
			return IScreenCaptureable.STATE_NEXT;
		}


		@Override
		public synchronized boolean capture() throws Exception {
			if (mCaptureReqCache == null) {
				mCaptureReqCache = RSPermHelper.buildRequest(true, 30, JNI_ASHMCapture, width, height);
			}
			synchronized (UDSPermission.this) {
				if(screenConnection != null){
					screenConnection.write(mCaptureReqCache.array(), 0, 13);
					screenConnection.read(mCaptureReqCache.array(), 20, 8);
					return mCaptureReqCache.getInt(24) == COMMAND_OK;
				}
			}
			return false;
		}
		
		@Override
		public boolean postCapture() throws Exception {
			return true;
		}

		@Override
		public synchronized void close() {
			MLog.i("ashmem close");
			if(ashmem != null){
				ashmem.close();
				ashmem = null;
			}
			if(mCaptureReqCache != null){
				mCaptureReqCache.clear();
				mCaptureReqCache = null;
			}
		}

		@Override
		public boolean isAlive() {
			return (ashmem != null);
		}
		
		@Override
		public boolean isRotationResetEncoder() {
			return false;
		}
		
		@Override
		public boolean isResizeResetEncoder() {
			return true;
		}
	}

	@Override
	public int getCurrentCaptureType() {
		return CAPTURE_TYPE_SURFACE_FLINGER;
	}

	class SocketConnection{
		private Socket socket = null;
		private InputStream inputStream = null;
		private OutputStream outputStream = null;
		private volatile boolean isClosed = false;

		private String name = null;
		
		public SocketConnection(String name){
			this.name = name;
		}
		
		public boolean connect(int port, int timeOut){
			if(timeOut <= 0){
				return connect(port);
			}
			
			boolean result = false;
			long startTime = System.currentTimeMillis();
			while((System.currentTimeMillis() - startTime < timeOut) && isClosed == false){
				result = connect(port);
				if(result == true){
					break;
				}
				MLog.w("retry connection.%d", port);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					return result;
				}
			}
			return result;
		}
		
		public boolean connect(int port){
			try {
				MLog.v("connect.%d", port);
				socket = new Socket("localhost", port);
				inputStream = socket.getInputStream();
				outputStream = socket.getOutputStream();
			} catch (Exception e) {
				return false;
			}
			return true;
		}

		public synchronized boolean write(byte[] buffer, int offset, int count) throws IOException{
			if(outputStream == null){
				return false;
			}
			outputStream.write(buffer, offset, count);
			return true;
		}
		
		public synchronized boolean write(int data) throws IOException{
			if(outputStream == null){
				return false;
			}
			outputStream.write(data);
			return true;
		}

		public synchronized int read(byte[] buffer, int offset, int length) throws IOException{
			if(inputStream == null){
				return -1;
			}
			return inputStream.read(buffer, offset, length);
		}
		
		public synchronized int read() throws IOException{
			if(inputStream == null){
				return -1;
			}
			return inputStream.read();
		}
		
		public synchronized int available() throws IOException{
			if(inputStream == null){
				return -1;
			}
			int available = inputStream.available();
			return available==0?-1:available;
		}

		public synchronized boolean isConnected(){
			return (socket != null && socket.isConnected());
		}

		public void close(){
			MLog.d("close.%s", name);
			isClosed = true;
			safetyClose(inputStream);
			safetyClose(outputStream);
			safetyCloseSocket(socket);
			socket = null;
			inputStream = null;
			outputStream = null;
		}
	}
}
