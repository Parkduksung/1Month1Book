package com.rsupport.rsperm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.hardware.display.DisplayManager;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import com.rsupport.srn30.Srn30Native;
import com.rsupport.srn30.screen.capture.AbstractCaptureable;
import com.rsupport.srn30.screen.capture.IScreenCaptureable;
import com.rsupport.srn30.screen.capture.IVirtualDisplay;
import com.rsupport.srn30.screen.encoder.ScapOption;
import com.rsupport.util.Converter;
import com.rsupport.util.LauncherUtils;
import com.rsupport.util.MemoryFileEx;
import com.rsupport.util.Net10;
import com.rsupport.util.PermissionUtils;
import com.rsupport.util.Screen;
import com.rsupport.util.Utils;
import com.rsupport.util.rslog.MLog;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class RSPermission extends AbstractPermission{

	private static final int INFO_SCREENSHOT_HEADER = 0x01;
	private static final int INFO_SCREENSHOT_DATA = 0x02;
	private static final String PERM_64BIT_PREFIX = "_perm";

	private IDummy mBinder = null;
	private boolean bindResult = false;
	private String packageName = null;
	private boolean isVer2 = false;
	private RSScreenShot rsScreenShot = null;

	@Override
	public int getType() {
		return BIND_TYPE_RSPERM;
	}

	@Override
	public synchronized void onDestroy() {
		MLog.i("#enter onDestroy");
		unbind();
		super.onDestroy();
		MLog.i("#exit onDestroy");
	}

	@Override
	public synchronized boolean bind(String packageName) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			MLog.e("called from UI thread.");
			return false;
		}

		try {
			unbind();
			this.packageName = packageName;
			if (mBinder == null && bindResult == false) {
				String mClassname = "com.rsupport.rsperm.i";
				Intent intent1 = new Intent();
				intent1.setClassName(packageName, mClassname);
				List<ResolveInfo> list = getContext().getPackageManager().queryIntentServices(intent1, PackageManager.MATCH_DEFAULT_ONLY);
				if (list.size() <= 0) {
					Toast.makeText(getContext(), "There is no rsperm-v2 package", Toast.LENGTH_LONG).show();
					return false;
				}

				Intent intent = new Intent();
				intent.setClassName(packageName, mClassname);
				MLog.i("BindSrnCaller bind : " + packageName + " : " + mClassname);
				bindResult = getContext().bindService(intent, srnConn, Context.BIND_AUTO_CREATE);
				if(bindResult == true){
					if(waitForBind(BIND_TIME_OUT) == true){
						int sdkver = Build.VERSION.SDK_INT;
						if (sdkver == 15) sdkver = 14;

						//7.0 이상 버전 처리...
						if (sdkver > Build.VERSION_CODES.M){
							sdkver = 24;
						}
						//5.0 이상 버전 처리...
						else if (sdkver >= Build.VERSION_CODES.LOLLIPOP){
							sdkver = Build.VERSION_CODES.LOLLIPOP;
						}

						String nativeLibDir = getContext().getApplicationInfo().nativeLibraryDir;
						String perm_prefix = "";
						if (nativeLibDir.contains("/arm64") ) {
							MLog.d("This is 64bit process!!!");
							perm_prefix = PERM_64BIT_PREFIX;
						}

						String soPath = String.format(Locale.ENGLISH, nativeLibDir + "/librspdk%d%s.so", sdkver, perm_prefix);
						loadJni(soPath);
						if (sdkver >= Build.VERSION_CODES.LOLLIPOP){
							LauncherUtils.killLauncher(getContext());
						}
					}
				}
			}
		} catch (Exception e) {
			MLog.e("bind error : " + Log.getStackTraceString(e));
		}
		MLog.i("BindSrnCaller bind : " + bindResult);
		return bindResult;
	}

	private boolean loadJni(String jniPath) {
		if (!new File(jniPath).exists()) {
			MLog.e("Null file: " + jniPath);
			return false;
		}
		if (mBinder == null) {
			MLog.e("not binded");
			return false;
		}

		try {
			int ret = mBinder.testDummy1(jniPath);
			MLog.i(String.format("[%s], ret:%d", jniPath, ret));
			if (ret == COMMAND_OK) {
				String query = "features";
				byte[] answer = mBinder.query(query.getBytes(Charset.defaultCharset()), query.length());
				int features = Integer.parseInt( new String(answer, Charset.defaultCharset()) );
				isVer2 = (features & 0x0100) != 0;
				MLog.i(String.format("rsperm features: %08x", features));
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}


	@Override
	public synchronized void unbind() {
		if (mBinder != null && bindResult == true){
			getContext().unbindService(srnConn);
		}
		bindResult = false;
		mBinder = null;
		packageName = null;
	}

	@Override
	public synchronized boolean isBound() {
		return (bindResult && mBinder != null);
	}

	private ServiceConnection srnConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			MLog.w("rsperm is disconnected");
			mBinder = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			MLog.i("rsperm is connected");
			mBinder = IDummy.Stub.asInterface(binder);
			try {
				if(name == null || getContext() == null){
					MLog.e("onServiceConnected null error... name : " + name + " , getContext : " + getContext());
				}else{
					if(name.getPackageName().equals(getContext().getPackageName())){
						mBinder.setFlags(
								// stopSelf
								0x0002
						);
					}
				}

				mBinder.registerCallback(mSrnEventStub);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};

	private IDummyCallback mSrnEventStub = new IDummyCallback.Stub() {

		public int getInt(byte[] data) {
			return -1234;
		}

		@Override
		public void onEvent(byte[] data) {
			ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
			int infoType = Converter.byte4Toint(data, 0);
			MLog.v("Infotype: " + infoType);

			try {
				switch (infoType) {
					case INFO_SCREENSHOT_HEADER:
						rsScreenShot = new RSScreenShot(data);
						break;
					case INFO_SCREENSHOT_DATA:
						rsScreenShot.write(data);
						break;
				}
			}
			catch(Exception e) {
				MLog.v(e);
			}
		}
	};

	@Override
	public int hwRotation() throws Exception {
		ByteBuffer bb = RSPermHelper.buildRequest(false, 30, JNI_GetHWRotation, Screen.resolution(getContext()));
		return mBinder.testDummy2(bb.array(), bb.position());
	}

	@Override
	public int[] getSupportEncoder() {

		ArrayList<Integer> supportEncoder = new ArrayList<Integer>();
		// 6.0 이하 일때만 readFrameBuffer 을 지원 할 수 있다.
		if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.M &&
				PermissionUtils.isSupportReadFrameBuffer(getContext(), packageName) == true){
			supportEncoder.add(ScapOption.ENCODER_TYPE_JPG);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				supportEncoder.add(ScapOption.ENCODER_TYPE_OMX);
			}
		}

		if (PermissionUtils.isSupportVirtualDisplay(getContext(), packageName) == true) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				supportEncoder.add(ScapOption.ENCODER_TYPE_OMX_FOR_VD);
			}
			supportEncoder.add(ScapOption.ENCODER_TYPE_JPG_FOR_VD);
		}
		return toIntArray(supportEncoder);
	}

	@Override
	public int[] getSupportCaptureType() {
		ArrayList<Integer> supportEncoder = new ArrayList<Integer>();
		if(PermissionUtils.isSupportReadFrameBuffer(getContext(), packageName) == true){
			supportEncoder.add(ScapOption.ENCODER_TYPE_JPG);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				supportEncoder.add(ScapOption.ENCODER_TYPE_OMX);
			}
		}

		if (PermissionUtils.isSupportVirtualDisplay(getContext(), packageName)) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				supportEncoder.add(ScapOption.ENCODER_TYPE_OMX_FOR_VD);
			}
			supportEncoder.add(ScapOption.ENCODER_TYPE_JPG_FOR_VD);
		}
		return toIntArray(supportEncoder);
	}

	@Override
	public String exec(String cmd) throws Exception {
		// not used
		return null;
	}

	@Override
	public boolean screenshot(String imgPath) throws IOException {
		try {

			if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
				MLog.w("not support this function.");
				return false;
			}

			ByteBuffer bb = RSPermHelper.buildRequest(false, 256, JNI_ASHMScreenshot, imgPath);
			boolean screenResult = mBinder.testDummy2(bb.array(), bb.position()) == COMMAND_OK;
			if(screenResult == true){
				if (screenShotCallback != null) {
					screenShotCallback.onReady();
				}

				screenResult = rsScreenShot.capture(imgPath);

				return screenResult;
			}
		} catch (Exception e) {
			MLog.e(Log.getStackTraceString(e));
		} finally {
			if (rsScreenShot != null) {
				rsScreenShot.release();
			}
		}
		return false;
	}

	@Override
	public boolean setMaxLayer(int type) throws IOException {
		try {
			ByteBuffer bb = RSPermHelper.buildRequest(false, 15, JNI_SetScreenshotMaxLayer, type);
			return mBinder.testDummy2(bb.array(), bb.position()) == COMMAND_OK;
		} catch (Exception e) {
			MLog.e(Log.getStackTraceString(e));
		}
		return false;
	}

	@Override
	public void inject(byte[] data, int offset, int length) throws Exception {
		try {
			--offset;
			++length;
			data[offset] = (byte)JNI_ASHMInject;
			mBinder.testDummy2(Arrays.copyOfRange(data, offset, offset+length), length);
		} catch (Exception e) {
			MLog.e(Log.getStackTraceString(e));
		}
	}

	@Override
	public IScreenCaptureable createScreenCaptureable(ScapOption scapOption) {
		switch(scapOption.getEncoderType()){
			case ScapOption.ENCODER_TYPE_JPG:
			case ScapOption.ENCODER_TYPE_OMX:
				captureable = new AshmemCaptureable();
				break;
			case ScapOption.ENCODER_TYPE_JPG_FOR_VD:
			case ScapOption.ENCODER_TYPE_OMX_FOR_VD:
				captureable = new VirtualDisplayCaptureable();
				break;
		}
		captureable.setScapOption(scapOption);
		return captureable;
	}

	@Override
	public boolean putSFloat(String name, float value) throws Exception {
		if(mBinder != null){
			return mBinder.putSFloat(name, value);
		}
		return false;
	}

	@Override
	public boolean putSInt(String name, int value) throws Exception {
		if(mBinder != null){
			return mBinder.putSInt(name, value);
		}
		return false;
	}

	@Override
	public boolean putSLong(String name, long value) throws Exception {
		if(mBinder != null){
			return mBinder.putSLong(name, value);
		}
		return false;
	}

	@Override
	public boolean putSString(String name, String value) throws Exception {
		if(mBinder != null){
			return mBinder.putSString(name, value);
		}
		return false;
	}

	@Override
	public boolean putGFloat(String name, float value) throws Exception {
		if(mBinder != null){
			return mBinder.putGFloat(name, value);
		}
		return false;
	}

	@Override
	public boolean putGInt(String name, int value) throws Exception {
		if(mBinder != null){
			return mBinder.putGInt(name, value);
		}
		return false;
	}

	@Override
	public boolean putGLong(String name, long value) throws Exception {
		if(mBinder != null){
			return mBinder.putGLong(name, value);
		}
		return false;
	}

	@Override
	public boolean putGString(String name, String value) throws Exception {
		if(mBinder != null){
			return mBinder.putGString(name, value);
		}
		return false;
	}

	private FileDescriptor getScreenFD(String args) throws Exception {
		if (mBinder == null) return null;
		if (isVer2) {
			return mBinder.getFile(args, 0, 0, 0).getFileDescriptor(); // rsperm v2+
		}

		String udsaddr = getContext().getPackageName()+".udsashm";
		LocalServerSocket svrSock = new LocalServerSocket(udsaddr);
		args = "udsaddr=" + udsaddr + "&" + args;
		ByteBuffer bb = RSPermHelper.buildRequest(false, 256, JNI_ASHMCreate, args);
		if (mBinder.testDummy2(bb.array(), bb.position()) != 0)
			return null;

		LocalSocket sock = svrSock.accept();
		FileDescriptor theFd = Net10.recvFd(sock.getFileDescriptor());
		sock.close();
		svrSock.close();
		return theFd;
	}

	private boolean initScreen(int w, int h) throws Exception {
		ByteBuffer bb = RSPermHelper.buildRequest(false, 13, JNI_ASHMInitScreen, w,h);
		return mBinder.testDummy2(bb.array(), bb.position()) == COMMAND_OK;
	}

	class VirtualDisplay implements IVirtualDisplay{
		@Override
		public boolean createVirtualDisplay(String name, int w, int h, int dpi,
											Surface surface, int flag) {
			try {
				// LGE Permission 이면 secure flag 를 제거 한다.
				if (isLGEPermission() == true) {
					flag &= ~DisplayManager.VIRTUAL_DISPLAY_FLAG_SECURE;
				}
				return mBinder.createVirtualDisplay(name, w, h, dpi, surface, flag);
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
			return false;
		}

		@Override
		public boolean release() {
			try {
				if(mBinder!= null){
					mBinder.createVirtualDisplay(null, 0, 0, 0, null, 0);
				}
				return true;
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
			return false;
		}

		/**
		 * LGE signature 이며 rsperm 을 바인딩 했는지를 확인한다.
		 * @return
		 */
		private boolean isLGEPermission() {
			try {
				PackageManager pm = getContext().getPackageManager();
				PackageInfo info  = pm.getPackageInfo("com.android.settings", PackageManager.GET_SIGNATURES);
				int size = info.signatures.length;
				if(size > 0){
					for(Signature signature : info.signatures){
						if(signature.hashCode() == -1160602166){
							MLog.v("LGE signature");
							return true;
						}
					}
				}
			} catch (Exception e) {
				MLog.e(e);
			}
			return false;
		}
	}

	class VirtualDisplayCaptureable extends AbstractCaptureable{
		private IVirtualDisplay virtualDisplay = null;
		@Override
		public Object initialized() throws Exception {
			MLog.i("vd initialized");
			if(virtualDisplay == null){
				virtualDisplay = new VirtualDisplay();
			}
			return virtualDisplay;
		}

		@Override
		public boolean capture() throws Exception {
			return true;
		}

		@Override
		public void close() {
			MLog.i("vd captureable close");
			if(virtualDisplay != null){
				virtualDisplay.release();
				virtualDisplay = null;
			}
		}

		@Override
		public boolean isAlive() {
			return virtualDisplay != null;
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


	class AshmemCaptureable extends AbstractCaptureable{
		private MemoryFileEx ashmem = null;
		private ByteBuffer mCaptureReqCache = null;

		private int width = 0;
		private int height = 0;

		@Override
		public synchronized Object initialized() throws Exception {
			MLog.i("initialized");
			width = scapOption.getStretch().x;
			height = scapOption.getStretch().y;

			StringBuilder sb = new StringBuilder("ashm=screen");
			sb.append("&width=").append(width);
			sb.append("&height=").append(height);
			sb.append("&bitType=").append(scapOption.getBitType());

			MLog.i("initAshmem width(%d), height(%d)", width, height);

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
		public synchronized boolean capture() throws Exception {
			if (isVer2)
				return mBinder.capture(scapOption.getStretch().x, scapOption.getStretch().y, 0) == COMMAND_OK; // rsperm v2+

			if (mCaptureReqCache == null) {
				mCaptureReqCache = RSPermHelper.buildRequest(false, 13, JNI_ASHMCapture, scapOption.getStretch().x, scapOption.getStretch().y);
				assert mCaptureReqCache.position() == 13;
			}
			return mBinder.testDummy2(mCaptureReqCache.array(), mCaptureReqCache.position()) == COMMAND_OK;
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
			return ashmem != null;
		}

		@Override
		public boolean isRotationResetEncoder() {
			return false;
		}

		@Override
		public boolean isResizeResetEncoder() {
			return true;
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


	@Override
	public int getCurrentCaptureType() {
		if(captureable instanceof VirtualDisplayCaptureable){
			return CAPTURE_TYPE_VIRTUAL_DISPLAY;
		}
		return CAPTURE_TYPE_SURFACE_FLINGER;
	}

	private class RSScreenShot {
		private int imageSize = 0;
		private int width = 0, height = 0, bytesPerLine = 0, colorFormat = 0, dstPos = 0;
		private byte[] imageData = null;

		/** set Header Info **/
		public RSScreenShot(byte[] header) {
			width 		 = Converter.byte4Toint(header, 4);
			height 		 = Converter.byte4Toint(header, 8);
			bytesPerLine = Converter.byte4Toint(header, 12);
			colorFormat	 = Converter.byte4Toint(header, 16);

			MLog.v(String.format("INFO_SCREENSHOT_HEADER width: %d, height: %d, bytesPerLine: %d, colorFormat: %d", width, height, bytesPerLine, colorFormat));
			imageSize = height * bytesPerLine;
			imageData = new byte[imageSize];
		}

		/** set Data Info **/
		public synchronized void write(byte[] data) {
			int length = Converter.byte4Toint(data, 4);

			MLog.v(String.format("INFO_SCREENSHOT_DATA dstPos: %d, imageSize: %d", dstPos, imageSize));
			System.arraycopy(data, 8, imageData, dstPos, length);

			dstPos += length;
		}

		public synchronized void release() {
			imageData = null;
		}

		public synchronized boolean capture(String imgPath) {
			return Utils.makeJPEGFile(imageData, 0, imageSize, width, height, bytesPerLine / 4, imgPath);
		}
	}
}
