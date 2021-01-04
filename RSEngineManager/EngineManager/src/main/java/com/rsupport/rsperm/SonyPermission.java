package com.rsupport.rsperm;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.ParcelFileDescriptor;
import android.os.RemoteControl;
import android.os.RemoteControl.DeviceInfo;
import android.os.RemoteControl.ICallbacks;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.rsupport.srn30.Srn30Native;
import com.rsupport.srn30.screen.capture.AbstractCaptureable;
import com.rsupport.srn30.screen.capture.IScreenCaptureable;
import com.rsupport.srn30.screen.encoder.ScapOption;
import com.rsupport.util.LockObject;
import com.rsupport.util.MemoryFileEx;
import com.rsupport.util.PermissionUtils;
import com.rsupport.util.rslog.MLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;


public class SonyPermission extends AbstractPermission{
	private final static int rcpMonkeyTouch = 24;
	private final static int rcpMonkeyWheel = 25;
	private final static int rcpMonkeyKeypad = 26;
	static InjCommon inj = new InjCommon();
	boolean isConnected = false;
	private RemoteControl mRemote;
	private Rect rcSrn = new Rect();
	private ParcelFileDescriptor mfdScreen;
	private SonyAshmemCaptureable captureable = null;
	private int keyEventMetaState = 0;

	private LockObject lockObject = null;
	private ICallbacks mListener = new ICallbacks() {
		@Override
		public void authorizationChanged(boolean srnOK, boolean inpOK) {
			MLog.i("first : "+ srnOK + " second :" + inpOK );
			isConnected = srnOK;
			if (lockObject != null) {
				lockObject.notifyLock();
			}
		}

		@Override
		public void connectionStatus(int status) {
			MLog.i("the connection to the service has completed: "+status);
			if(status == RemoteControl.RC_SUCCESS){
				isConnected = true;
			}
			if (lockObject != null) {
				lockObject.notifyLock();
			}
		}

		@Override
		public void deviceInfoChanged() {
			MLog.i("the frame buffer orientation, flip mode, etc has changed.");
		}
	};

	public SonyPermission() {
		lockObject = new LockObject();
	}
	
	@Override
	public void onDestroy(){
		MLog.i("#enter onDestroy");
		if(captureable != null && captureable.isAlive()){
			captureable.close();
		}
		captureable = null;
		unbind();
		super.onDestroy();
		MLog.i("#exit onDestroy");
	}

	@Override
	public int getType() {
		return BIND_TYPE_SONY;
	}

	@Override
	public synchronized boolean bind(String address) {
		if (PermissionUtils.isAvailableSonyRemoteService(getContext()) == false) {
			MLog.e("There is no Sony remote control feature. exclude check false");
			return false;
		}

		if (mRemote == null) {
			try {
				mRemote = RemoteControl.getRemoteControl(getContext(), mListener); 		// 객체 선언시 화면공유 권한 팝업이 올라온다.
				// 소니 퍼미션 사용 권환 팝업 노출됨(미디어 프로젝션 처럼), 사용자 입력 대기
				lockObject.lock();
				return isBound();
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
		}
		return false;

	}

	@Override
	public synchronized void unbind() {
		MLog.i("unbind");
		if(captureable != null){
			captureable.close();
			captureable = null;
		}

		if(mRemote != null && isBound()){
			mRemote.release();
			mRemote = null;
		}
	}

	@Override
	public synchronized boolean screenshot(String imgPath) throws IOException {
		// 연결 안된 상태에서는 null 이다.
		if(captureable == null){
			ScapOption scapOption = new ScapOption();
			scapOption.setEncoderType(ScapOption.ENCODER_TYPE_JPG);
			captureable = (SonyAshmemCaptureable)createScreenCaptureable(scapOption);
			try {
				captureable.initialized();
			} catch (Exception e) {
				return false;
			}
		}

		try {
			captureable.capture();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (screenShotCallback != null) {
			screenShotCallback.onReady();
		}

		return screenShot(captureable.ashmem,
				imgPath,
				captureable.getScapOption().getStretch().x,
				captureable.getScapOption().getStretch().y,
				captureable.getScapOption().getStride());
	}

	@Override
	public synchronized int hwRotation() throws Exception {
		// 하드웨어 오리지날 회전값은 0이 기본값
		// 기존 DeviceInfo의 화면 회전값을 사용하면 하드웨어 회전값과 화면의 회전값이 같아 해상도 스왑을 안함 가로상태에서 접속시 문제 발생
		return 0;
	}

	@Override
	public int[] getSupportEncoder() {
		return new int[]{ScapOption.ENCODER_TYPE_JPG};
	}

    @Override
    public int[] getSupportCaptureType() {
        return new int[]{ScapOption.ENCODER_TYPE_JPG};
    }

	@Override
	public String exec(String cmd) throws Exception {
		return null;
	}

	@Override
	public boolean setMaxLayer(int jniMaxCaptureDefault) throws IOException {
		return false;
	}

	@Override
	public void inject(byte[] data, int offset, int length) throws Exception {
		int msgid = data[offset]&0xFF;
		ByteBuffer parser = ByteBuffer.wrap(data, 0, data.length).order(ByteOrder.LITTLE_ENDIAN); // 5 : skip rcpMsg.datasize
		parser.position(10);

		switch (msgid){
		case rcpMonkeyTouch:
			// touch :         injectWithPrimitive(action,         0,      x       y       x2      y2)
			InjCommon.rcpMonkeyTouch2Msg touch = InjCommon.rcpMonkeyTouch2Msg.obtain(parser);
			injectWithPrimitive(touch.action, 0, touch.x, touch.y, touch.x2, touch.y2);
			break;
		case rcpMonkeyWheel:
			// wheel :         injectWithPrimitive(action,        dy,      x       y       0        0)
			InjCommon.rcpMonkeyWheelMsg wheel = InjCommon.rcpMonkeyWheelMsg.obtain(parser);
			injectWithPrimitive(MotionEvent.ACTION_SCROLL, wheel.dy, wheel.x, wheel.y, 0, 0);
			break;

		case rcpMonkeyKeypad:
			// key   :         injectWithPrimitive(action+100,    code     scan,  repeat  meta      0)
			InjCommon.rcpMonkeyKeypadMsg keybd = InjCommon.rcpMonkeyKeypadMsg.obtain(parser);
			for (int i=0; i<keybd.count; ++i) {
				if(keybd.getKeycode(i) == KeyEvent.KEYCODE_CTRL_LEFT){
					keyEventMetaState = keybd.getAction(i) == KeyEvent.ACTION_DOWN ? KeyEvent.META_CTRL_ON|KeyEvent.META_CTRL_LEFT_ON : 0;
				} else if(keybd.getKeycode(i) == KeyEvent.KEYCODE_SHIFT_LEFT){
					keyEventMetaState = keybd.getAction(i) == KeyEvent.ACTION_DOWN ? KeyEvent.META_SHIFT_ON|KeyEvent.META_SHIFT_LEFT_ON : 0;
				} else if(keybd.getKeycode(i) == KeyEvent.KEYCODE_SHIFT_RIGHT){
					keyEventMetaState = keybd.getAction(i) == KeyEvent.ACTION_DOWN ? KeyEvent.META_SHIFT_ON|KeyEvent.META_SHIFT_RIGHT_ON : 0;
				} else if(keybd.getKeycode(i) == KeyEvent.KEYCODE_ALT_LEFT){
					keyEventMetaState = keybd.getAction(i) == KeyEvent.ACTION_DOWN ? KeyEvent.META_ALT_ON|KeyEvent.META_ALT_LEFT_ON : 0;
				}
				injectWithPrimitive(keybd.getAction(i) + 100, keybd.getKeycode(i), 0, 0, keyEventMetaState, 0);
			}
			break;
		default:
			MLog.e("invalid input msg->id: %d", msgid);
			break;
		}
	}

	@Override
	public IScreenCaptureable createScreenCaptureable(ScapOption scapOption) {
		switch(scapOption.getEncoderType()){
		case ScapOption.ENCODER_TYPE_JPG:
			SonyAshmemCaptureable captureable = new SonyAshmemCaptureable();
			captureable.setScapOption(scapOption);
			this.captureable = captureable;
			break;
		}
		return captureable;
	}

	@Override
	public synchronized boolean isBound() {
		return isConnected;
	}

	public boolean screenShot(MemoryFileEx ashmem, String filePath, int width, int height, int stride){
		try {
			byte[] buffer = new byte[ashmem.length()];
			int readSize = ashmem.readBytes(buffer, 0, 0, ashmem.length());
			Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);

			int[] colors = new int[stride*height];
			int size = readSize;
			for(int i = 0; i < size; i+= 4){
				colors[i != 0?(i/4):0] = (buffer[i+3]<<24 & 0xFF000000 |	
						buffer[i]<<16 & 0x00FF0000 | 
						buffer[i+1]<<8 & 0x0000FF00 | 
						buffer[i+2] & 0x000000FF); 
			}
			bitmap.setPixels(colors, 0, stride, 0, 0, width, height);
			File fileCacheItem = new File(filePath);
			OutputStream out = null;
			try{
				fileCacheItem.createNewFile();
				out = new FileOutputStream(fileCacheItem);
				return bitmap.compress(CompressFormat.JPEG, 100, out);
			}catch (Exception e){
				e.printStackTrace();
			}
			finally{
				if(bitmap != null){
					bitmap.recycle();
				}
				try{
					out.close();
				}catch (IOException e){
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			MLog.e("%s", Log.getStackTraceString(e));
		}
		return false;
	}

	// touch :         injectWithPrimitive(action,         0,      x       y       x2      y2)
	// wheel :         injectWithPrimitive(action,        dy,      x       y       0        0)
	// key   :         injectWithPrimitive(action+100,    code     scan,  repeat  meta      0)
	public void injectWithPrimitive(int action, int i1, int i2, int i3, int i4, int i5) throws Exception { // scroll for ICS+
		if (action < 100) { // 100 = KeycodeActionOffset
			// 12+
			if (action == MotionEvent.ACTION_SCROLL){
				MotionEvent me = inj.getWheelEvent(i2, i3, i1);
				mRemote.injectMotionEvent(me); // x,y,dy
				me.recycle();
			}
			else if (i4 <= 0 || i4 == 32768){
				MotionEvent me = inj.getTouchEvent(action, i2,i3, InputDevice.SOURCE_TOUCHSCREEN);
				mRemote.injectMotionEvent(me); // touch.
				me.recycle();
			}
			else{
				MotionEvent[] me = inj.getMultiTouchEvent(action, i2,i3,i4,i5,InputDevice.SOURCE_TOUCHSCREEN);
				for (MotionEvent event : me) {
					mRemote.injectMotionEvent(event);
					event.recycle();
				}
			}
		}
		else {
			mRemote.injectKeyEvent(inj.getKeyEvent(action-100, i1, i2, i3, i4));
		}
	}

	@Override
	public int getCurrentCaptureType() {
		return CAPTURE_TYPE_SURFACE_FLINGER;
	}

	class SonyAshmemCaptureable extends AbstractCaptureable{

		private MemoryFileEx ashmem = null;

		@Override
		public synchronized Object initialized() throws Exception {
			MLog.i("initialized");
			mfdScreen = mRemote.getFrameBufferFd(PixelFormat.RGBA_8888, true).getParcelFd();

			DeviceInfo deviceInfo = mRemote.getDeviceInfo(); 						// 디바이스 정보 객체 생성
			rcSrn.left = 0;
			rcSrn.top = 0;
			rcSrn.right  = deviceInfo.frameBufferWidth;
			rcSrn.bottom = deviceInfo.frameBufferHeight;

			scapOption.getStretch().x = rcSrn.right;
			scapOption.getStretch().y = rcSrn.bottom;
			scapOption.setStride(deviceInfo.frameBufferStride); // pixelsPerLine.
			scapOption.setBitType(deviceInfo.fbPixelFormat);

			MLog.v(String.format("DeviceInfo: %dx%d,  %dx%d, ppl.%d, pxlFmt.%d, bufsize.%d, orien:%d",
					deviceInfo.fbWidth, deviceInfo.fbHeight,
					deviceInfo.frameBufferWidth, deviceInfo.frameBufferHeight,
					deviceInfo.frameBufferStride, deviceInfo.fbPixelFormat,
					deviceInfo.frameBufferSize,
					deviceInfo.displayOrientation
					));
			ashmem = new MemoryFileEx(mfdScreen.getFileDescriptor(), -1);

			StringBuffer sonyPermOption = new StringBuffer(getEncOptions(scapOption));
			sonyPermOption.append("&sonyWidth=").append(scapOption.getStretch().x);
			sonyPermOption.append("&sonyHeight=").append(scapOption.getStretch().y);
			sonyPermOption.append("&sonyScanline=").append(scapOption.getStride());
			sonyPermOption.append("&sonyPixelFormat=").append(scapOption.getBitType());


			if(Srn30Native.initEncoder(ashmem.address(), sonyPermOption.toString()) == false){
				throw new RuntimeException("initEncoder error");
			}
			return ashmem;
		}

		@Override
		public synchronized boolean capture() throws Exception {
			mRemote.grabScreen(false, rcSrn);
			return true;
		}

		@Override
		public synchronized void close() {
			MLog.i("ashmem close");
			if(ashmem != null){
				ashmem.close();
				ashmem = null;
			}

			if(mfdScreen != null){
				try {
					mfdScreen.close();
				} catch (IOException e) {
					MLog.e("mfdScreen exception : %s", Log.getStackTraceString(e));
				}
				mfdScreen = null;
			}

			if(mRemote != null && isConnected){
				try {
					mRemote.releaseFrameBuffer();
				} catch (Exception e) {
					MLog.e("unbind exception : %s", Log.getStackTraceString(e));
				}
			}
		}

		@Override
		public boolean isAlive() {
			return (ashmem != null);
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
}