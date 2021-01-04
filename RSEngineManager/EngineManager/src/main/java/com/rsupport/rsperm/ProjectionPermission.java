package com.rsupport.rsperm;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Process;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.rsupport.rsperm.projection.ProjectionActivity;
import com.rsupport.srn30.screen.capture.AbstractCaptureable;
import com.rsupport.srn30.screen.capture.IScreenCaptureable;
import com.rsupport.srn30.screen.capture.IVirtualDisplay;
import com.rsupport.srn30.screen.encoder.ScapOption;
import com.rsupport.util.LauncherUtils;
import com.rsupport.util.LockObject;
import com.rsupport.util.Screen;
import com.rsupport.util.Utils;
import com.rsupport.util.rslog.MLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**<pre>*******************************************************************************
 *       ______   _____    __    __ _____   _____   _____    ______  _______
 *      / ___  | / ____|  / /   / // __  | / ___ | / __  |  / ___  ||___  __|
 *     / /__/ / | |____  / /   / // /  | |/ /  | |/ /  | | / /__/ /    / /
 *    / ___  |  |____  |/ /   / // /__/ // /__/ / | |  | |/ ___  |    / /
 *   / /   | |   ____| || |__/ //  ____//  ____/  | |_/ // /   | |   / /
 *  /_/    |_|  |_____/ |_____//__/    /__/       |____//_/    |_|  /_/
 *
 ********************************************************************************</pre>
 *
 * <b>Copyright (c) 2012 RSUPPORT Co., Ltd. All Rights Reserved.</b><p>
 *
 * <b>NOTICE</b> :  All information contained herein is, and remains the property
 * of RSUPPORT Company Limited and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to RSUPPORT
 * Company Limited and its suppliers and are protected by trade secret
 * or copyright law. Dissemination of this information or reproduction
 * of this material is strictly forbidden unless prior written permission
 * is obtained from RSUPPORT Company Limited.<p>
 *
 * FileName: ProjectionPermission.java<br>
 * Author  : kwcho<br>
 * Date    : Nov 20, 20147:13:02 PM<br>
 * Purpose : <p>
 *
 * [History]<p>
 */
@TargetApi(21)
public class ProjectionPermission extends AbstractPermission{
	public static final String ACTION_BIND_RESULT = "com.rsupport.mobizen.action.BIND_RESULT";
	public static final String EXTRA_KEY_BIND_RESULT = "extra_key_bind_result";
	
	public static final int BIND_RESULT_SUCCESS = 0x00000001;
	public static final int BIND_RESULT_FAIL = 0x00000002;
	
	private final String SCREEN_SHOT_VD = "ScreenShot";
	
	private LockObject lockObject = null;
	
	private int bindResult = 0;
	private boolean isRegisterBindReceiver = false;
	
	private SocketConnection commandConnection = null;
	private SocketConnection inputConnection = null;

	private static Intent bindIntent = null;
	private boolean useMaintainPermission = false;

	public ProjectionPermission() {
		lockObject = new LockObject();
	}
	public MediaProjection mediaProjection = null;
	
	@Override
	public void onDestroy() {
		lockObject.notifyLock();
		if(mediaProjection != null){
			mediaProjection.stop();
			mediaProjection = null;
		}
		
		if(commandConnection != null){
			commandConnection.close();
			commandConnection = null;
		}
		
		if(inputConnection != null){
			inputConnection.close();
			inputConnection = null;
		}
		super.onDestroy();
	}
	
	@Override
	public int getType() {
		return IEnginePermission.BIND_TYPE_PROJECTION;
	}

	@Override
	public int hwRotation() throws Exception {
		return 0;
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
	public int[] getSupportEncoder() {
		ArrayList<Integer> supportEncoder = new ArrayList<Integer>();
		supportEncoder.add(ScapOption.ENCODER_TYPE_OMX_FOR_VD);
		supportEncoder.add(ScapOption.ENCODER_TYPE_JPG_FOR_VD);
		return toIntArray(supportEncoder);
	}

    @Override
    public int[] getSupportCaptureType() {
        ArrayList<Integer> supportEncoder = new ArrayList<Integer>();
        supportEncoder.add(ScapOption.ENCODER_TYPE_OMX_FOR_VD);
        supportEncoder.add(ScapOption.ENCODER_TYPE_JPG_FOR_VD);
        return toIntArray(supportEncoder);
    }

	@Override
	public int getCurrentCaptureType() {
		return CAPTURE_TYPE_VIRTUAL_DISPLAY;
	}

	@Override
	public String exec(String cmd) throws Exception {
		ByteBuffer bb = RSPermHelper.buildRequest(true, 256, JNI_Execute, cmd);
		commandConnection.write(bb.array(), 0, bb.position());
		commandConnection.read(bb.array(), 0, 4);
		int size = bb.getInt(0);
		byte[] reply = new byte[size];
		commandConnection.read(reply, 0, size);
		return new String(reply, "UTF-8");
	}
	
	@Override
	public boolean screenshot(String imgPath) throws IOException {
		Point p = Screen.virtualDisplayResolution(getContext());
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		Display d = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		d.getMetrics(metrics);
		
		ImageReader imageReader = ImageReader.newInstance(p.x, p.y, PixelFormat.RGBA_8888, 1);
		Surface surface = imageReader.getSurface();
		VirtualDisplay virtualDisplay = null;
		try {
			virtualDisplay = mediaProjection.createVirtualDisplay(
					SCREEN_SHOT_VD,
					p.x,
					p.y, 
					metrics.densityDpi, 
					DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, 
					surface, 
					null, 
					null);

			Image image = null;
			long time = System.currentTimeMillis();
			MLog.v("enter capture");
			while(System.currentTimeMillis() - time < 1000){
				image = imageReader.acquireLatestImage();
				if(image != null){
					if (screenShotCallback != null) {
						screenShotCallback.onReady();
					}
					Plane plane = image.getPlanes()[0];
					ByteBuffer buffer = plane.getBuffer();
					int width = image.getWidth();
					int height = image.getHeight();
					int pixelStride = plane.getPixelStride();
					int rowStride = plane.getRowStride();
					int stride = rowStride/pixelStride;
					Utils.ARGBtoJPEFile(buffer, 0, buffer.capacity(), width, height, stride, imgPath);
					image.close();
					return true;
				}
			}
			
		}finally{
			MLog.v("exit capture");
			if(virtualDisplay != null){
				virtualDisplay.release();
			}
			if(imageReader != null){
				imageReader.close();
			}
		}
		return false;
	}

	@Override
	public boolean setMaxLayer(int jniMaxCaptureDefault) throws IOException {
		return false;
	}

	@Override
	public void inject(byte[] data, int offset, int length) throws Exception {
		if(inputConnection != null){
			data[offset-1] = (byte)length;
			--offset;
			++length;
			inputConnection.write(data, offset, length);
		}
	}

	@Override
	public IScreenCaptureable createScreenCaptureable(ScapOption scapOption) {
		switch(scapOption.getEncoderType()){
		case ScapOption.ENCODER_TYPE_JPG_FOR_VD:
		case ScapOption.ENCODER_TYPE_OMX_FOR_VD:
			captureable = new VirtualDisplayCaptureable();
			break;
		}
		captureable.setScapOption(scapOption);
		return captureable;
	}
	
	@Override
	public boolean bind(String address) {
		MLog.d("projection Permission bind : "+ useMaintainPermission);
		if(useMaintainPermission == true && bindIntent != null){
			createMediaProjection(getContext(), bindIntent);
		}
		else{
			registerBindReceiver();
			Intent intent = new Intent(getContext(), ProjectionActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			try {
				pendingIntent.send();
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
				unRegisterBindReceiver();
				return false;
			}

			lockObject.lock();
		}

		if(bindResult == BIND_RESULT_SUCCESS){
            onBindSuccess(address);
		}
		unRegisterBindReceiver();
		return (bindResult == BIND_RESULT_SUCCESS);
	}

	protected void onBindSuccess(String arg) {
		// liblauncherxx.so 가 process 에 살아 있을 경우에만 시도한다.
		int launcherPID = LauncherUtils.getLauncherPID(getContext(), true);
		if(launcherPID != LauncherUtils.INVALID_LAUNCHER){
			commandConnection = new SocketConnection("command");
			if(commandConnection.connect(launcherPID) == false){
				MLog.w("command channel connection fail");
			}

			// Injector 권한이 필요한 실행만 input channel 접속 한다..
			if(LauncherUtils.hasInjector() == true){
				inputConnection = new SocketConnection("input");
				// 안드로이드 7.0 부터 ps 를 통해서 자신의 pid 고 가져오지 못한다.
				if(inputConnection.connect(Process.myPid(), 3000) == false){
					MLog.w("input channel connection fail");
				}
			}
		}
	}
	
	private synchronized void unRegisterBindReceiver() {
		if(isRegisterBindReceiver == true){
			isRegisterBindReceiver = false;
			getContext().unregisterReceiver(bindNotifyReceiver);
		}
	}

	private synchronized void registerBindReceiver() {
		isRegisterBindReceiver = true;
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_BIND_RESULT);
		filter.addCategory(getContext().getPackageName());
		getContext().registerReceiver(bindNotifyReceiver, filter);
	}

	@Override
	public void unbind() {
		unRegisterBindReceiver();
	}

	@Override
	public boolean isBound() {
		return (bindResult == BIND_RESULT_SUCCESS);
	}
	
	private BroadcastReceiver bindNotifyReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(ACTION_BIND_RESULT.equals(intent.getAction())){
				createMediaProjection(context, intent);

				if(lockObject != null){
					lockObject.notifyLock();
				}
			}
		}
	};

	private void createMediaProjection(Context context, Intent intent){
		bindResult = intent.getIntExtra(EXTRA_KEY_BIND_RESULT, BIND_RESULT_FAIL);

		if(bindResult == BIND_RESULT_SUCCESS){
			bindIntent = intent;

			MediaProjectionManager projectionManager = (MediaProjectionManager)context
					.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
			mediaProjection = projectionManager.getMediaProjection(Activity.RESULT_OK, intent);

		}
	}

	public void setUseMaintainPermission(boolean useMaintainPermission){
		this.useMaintainPermission = useMaintainPermission;
	}
	
	class VirtualDisplayImpl implements IVirtualDisplay{
		VirtualDisplay virtualDisplay = null;
		
		@Override
		public boolean createVirtualDisplay(String name, int w, int h, int dpi,
				Surface surface, int flag) {
			try {
				virtualDisplay = mediaProjection.createVirtualDisplay(
						name, 
						w, 
						h, 
						dpi, 
						DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, 
						surface, 
						null, 
						null);
				return (virtualDisplay != null);
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
			return false;
		}
		
		@Override
		public boolean release() {
			try {
				if(virtualDisplay != null){
					virtualDisplay.release();
				}
				return true;
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
			return false;
		}
	}
	
	class VirtualDisplayCaptureable extends AbstractCaptureable{
		private IVirtualDisplay virtualDisplayImpl = null;
		@Override
		public Object initialized() throws Exception {
			MLog.i("vd initialized");
			if(virtualDisplayImpl == null){
				virtualDisplayImpl = new VirtualDisplayImpl();
			}
			return virtualDisplayImpl;
		}

		@Override
		public boolean capture() throws Exception {
			return true;
		}

		@Override
		public void close() {
			MLog.i("vd captureable close");
			if(virtualDisplayImpl != null){
				virtualDisplayImpl.release();
				virtualDisplayImpl = null;
			}
		}

		@Override
		public boolean isAlive() {
			return virtualDisplayImpl != null;
		}

		@Override
		public boolean isRotationResetEncoder() {
			return true;
		}

		@Override
		public boolean isResizeResetEncoder() {
			return false;
		}

		@Override
		public int prepareCapture() throws Exception {
			return IScreenCaptureable.STATE_NEXT;
		}

		@Override
		public boolean postCapture() throws Exception {
			return true;
		}
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
				MLog.v("%s connect.%d", name, port);
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
			safetyClose(socket);
			socket = null;
			inputStream = null;
			outputStream = null;
		}
	}
}