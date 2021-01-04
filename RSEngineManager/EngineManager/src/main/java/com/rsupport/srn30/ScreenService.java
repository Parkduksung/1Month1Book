package com.rsupport.srn30;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import android.view.Surface;

import com.rsupport.rsperm.IEnginePermission;
import com.rsupport.srn30.screen.ScreenManager;
import com.rsupport.srn30.screen.capture.OnScreenShotCallback;
import com.rsupport.util.rslog.MLog;

public class ScreenService extends Service {
	/** 캡쳐 성공 **/
	public static final int RESULT_CAPTURE_SUCCESS = 100;
	/** DRM 으로 인한 캡쳐 실패 **/
	public static final int RESULT_CAPTURE_FAIL_DRM = 400;
	/** 뷰어와 접속중 녹화상태에서 접속 종료시 **/
	public static final int RESULT_CAPTURE_FAIL_DISCONNECTED = 401;
	/** 스크린 메니저 null **/
	public static final int RESULT_CAPTURE_FAIL_SCREEN_MANAGER = 402;
	/** AIDL remote exception **/
	public static final int RESULT_CAPTURE_FAIL_REMOTE_EXCEPTION = 403;
	/** Remote 객체 null **/
	public static final int RESULT_CAPTURE_FAIL_REMOTE_NULL = 404;
	/** 알수 없는 오류 **/
	public static final int RESULT_CAPTURE_FAIL_UNKNOW = 499;
	
	private ScreenManager screenManager = null;
	private IScreenCallback mCallback;
	private int bindResult = IEnginePermission.BIND_ERROR_NOT_FOUND;
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onLowMemory() {
		for (int i=0; i<3; ++i) MLog.w("low memory state...");
		super.onLowMemory();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if(screenManager != null){
			screenManager.unBindEngine();
			screenManager = null;
		}
        bindResult = IEnginePermission.BIND_ERROR_NOT_FOUND;
		return super.onUnbind(intent);
	}

	private final IBinder mBinder = new IScreen.Stub() {
		@Override
		public boolean start(String rsperm, int bindTimeout, ParcelFileDescriptor sock, int channelId, int permissionPriority)
				throws RemoteException {
			if(bindNewEngine(rsperm, permissionPriority, bindTimeout) == true){
				return screenManager.start(sock == null ? null : sock.getFileDescriptor(), channelId);
			}
			return false;
		}

		@Override
		public void registerCallback(IScreenCallback cb) throws RemoteException {
			mCallback = cb;
		}

		@Override
		public void enableUpdate(boolean enable) throws RemoteException {
			if(screenManager != null){
				if(enable == false){
					screenManager.encoderSuspend(3000);
				}
				else{
					screenManager.encoderResume();
				}
			}
			return;
		}

		@Override
		public boolean test(String args) throws RemoteException {
			return false;
		}

		@Override
		public boolean command(String cmd) throws RemoteException {
			if(screenManager != null){
				return screenManager.command(cmd);
			}
			return false;
		}

		@Override
		public int capture(int width, int height) throws RemoteException {
			try {
				if(screenManager!= null){
					return screenManager.getRecorder().capture(width, height);
				}
				return ScreenService.RESULT_CAPTURE_FAIL_SCREEN_MANAGER;
			} 
			catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
				return ScreenService.RESULT_CAPTURE_FAIL_UNKNOW;
			}
		}

		@Override
		public boolean setMaxLayer(int maxLayer) throws RemoteException {
			try {
				if(screenManager!= null){
					return screenManager.setMaxLayer(maxLayer);
				}
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
			return false;
		};

		@Override
		public int createAshmem(int width, int height, int ashmType)
				throws RemoteException {
			try {
				if(screenManager!= null){
					return screenManager.getRecorder().createAshmem(width, height, ashmType);
				}
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
			return -1;
		}

		@Override
		public int readBytes(byte[] buffer, int srcOffset, int destOffset,
				int count) throws RemoteException{
			try {
				if(screenManager!= null){
					return screenManager.getRecorder().readBytes(buffer, srcOffset, destOffset, count);
				}
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
			return -1;
		}

		@Override
		public void releaseAshmem() throws RemoteException {
			try {
				if(screenManager!= null){
					screenManager.releaseRecorder();
				}
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
			return;
		}

		private boolean bindNewEngine(String rsperm, int permissionPriority, int bindTimeout){
			if(screenManager != null){
				MLog.i("already create screenManager");
                return true;
			}
			screenManager = new ScreenManager();
			bindResult = screenManager.bindEngine(getApplicationContext(), rsperm, permissionPriority);
			screenManager.setOnScreenShotCallback(screenShotCallBack);
			return (bindResult == IEnginePermission.BIND_ERROR_NOT_FOUND) ? false: true;
		}


		@Override
		public boolean bindRsperm(String rsperm, int boostMode, int bindTimeout)
				throws RemoteException {
			return bindNewEngine(rsperm, boostMode, bindTimeout);
		}

		OnScreenShotCallback screenShotCallBack = new OnScreenShotCallback() {
			@Override
			public void onStart(String imgPath) {
				if(mCallback != null){
					try {
						mCallback.onScreenshot(SCREEN_SHOT_STATUS_START);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onReady() {
				if(mCallback != null){
					try {
						mCallback.onScreenshot(SCREEN_SHOT_STATUS_READY);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onComplete(String imgPath) {
				if(mCallback != null){
					try {
						mCallback.onScreenshot(imgPath);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onError(String imgPath) {
				if(mCallback != null){
					try {
						mCallback.onScreenshot(SCREEN_SHOT_STATUS_ERROR);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		};
		@Override
		public String screenShot() throws RemoteException {
			if(screenManager != null){
				return screenManager.screenShot();
			}
			return null;
		}

		@Override
		public boolean screenShotToPath(String filePath) throws RemoteException {
			if(screenManager != null){
				return screenManager.screenShot(filePath);
			}
			return false;
		}

		@Override
		public int getBindedType() throws RemoteException {
			return bindResult;
		}

		@Override
		public boolean createVirtualDisplay(String name, int w, int h, int dpi,
				Surface surf, int flag) throws RemoteException {
			try {
				if(screenManager!= null){
					return screenManager.getRecorder().createVirtualDisplay(name, w, h, dpi, surf, flag);
				}
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
			return false;
		}

		@Override
		public int getFlag() throws RemoteException {
			try {
				if(screenManager!= null){
					return screenManager.getFlag();
				}
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
			return 0;
		}

		@Override
		public boolean releaseVirtualDisplay() throws RemoteException {
			try {
				if(screenManager!= null){
					screenManager.releaseRecorder();
					return true;
				}
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
			return false;
		}

		@Override
		public int getCaptureType() throws RemoteException {
			try {
				if(screenManager!= null){
					return screenManager.getCurrentCaptureType();
				}
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
			return -1;
		}

		@Override
		public void createAudio() throws RemoteException {
			try {
				if(screenManager != null){
					screenManager.createAudio();
				}
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
		}

		@Override
		public boolean initAudio(int sampleRate, int channelConfig, int audioFormat) throws RemoteException {
			try {
				if(screenManager != null){
					return screenManager.initAudio(sampleRate, channelConfig, audioFormat);
				}
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}

			return false;
		}

		@Override
		public boolean startAudio() throws RemoteException {
			try {
				if(screenManager != null){
					return screenManager.startAudio();
				}
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}

			return false;
		}

		@Override
		public void resumeAudio() throws RemoteException {
			try {
				if(screenManager != null){
					screenManager.resumeAudio();
				}
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
		}

		@Override
		public byte[] readAudio(byte[] buffer, int size) throws RemoteException {
			try {
				if(screenManager != null){
					return screenManager.readAudio(buffer, size);
				}
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}

			return new byte[0];
		}

		@Override
		public void pauseAudio() throws RemoteException {
			try {
				if(screenManager != null){
					screenManager.pauseAudio();
				}
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
		}

		@Override
		public void muteAudio(boolean isMute) throws RemoteException {
			try {
				if(screenManager != null){
					screenManager.muteAudio(isMute);
				}
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
		}

		@Override
		public void releaseAudio() throws RemoteException {
			try {
				if(screenManager != null){
					screenManager.releaseAudio();
				}
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
		}
	};

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(screenManager!= null){
			screenManager.onConfigurationChanged(newConfig);
		}
		return;
	};
};
