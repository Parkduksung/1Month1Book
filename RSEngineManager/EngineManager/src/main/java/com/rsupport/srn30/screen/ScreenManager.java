package com.rsupport.srn30.screen;

import java.io.FileDescriptor;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.json.JSONObject;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;
import android.view.Surface;

import com.rsupport.rsperm.IEnginePermission;
import com.rsupport.rsperm.ProjectionPermission;
import com.rsupport.rsperm.SonyPermission;
import com.rsupport.srn30.ASHM_SCREEN;
import com.rsupport.srn30.ScreenService;
import com.rsupport.srn30.rcp;
import com.rsupport.srn30.screen.audio.AudioCaptureForAndroidQ;
import com.rsupport.srn30.screen.capture.IScreenCaptureable;
import com.rsupport.srn30.screen.capture.IVirtualDisplay;
import com.rsupport.srn30.screen.capture.OnScreenShotCallback;
import com.rsupport.srn30.screen.channel.ScreenChannel;
import com.rsupport.srn30.screen.channel.Srn30Packet;
import com.rsupport.srn30.screen.encoder.EncoderManager;
import com.rsupport.srn30.screen.encoder.IEncoder.ICommand;
import com.rsupport.srn30.screen.encoder.ScapOption;
import com.rsupport.srn30.screen.rotation.OrientationManager;
import com.rsupport.util.FilePath;
import com.rsupport.util.LockObject;
import com.rsupport.util.MemoryFileEx;
import com.rsupport.util.Net10;
import com.rsupport.util.rslog.MLog;

public class ScreenManager implements IScreenContext{
	private IEnginePermission permission = null;
	private OrientationManager orientationManager = null;
	private EncoderManager encoderManager = null;
	private ScreenChannel screenChannel = null;
	private OnScreenShotCallback onScreenshotCallback = null;
	private RecordReader recordReader = null;

	private AudioCaptureForAndroidQ audioCapture = null;

	private int channelID = 0;
	private Context context = null;
	private byte[] emptyByteArray = new byte[0];
	
	@Override
	public int bindEngine(Context context, String packageName, int permissionPriority) {
		this.context = context;
		if(permission != null){
			PermissionLoader.release(permission);
			permission = null;
		}

		permission = PermissionLoader.createEnginePermission(context, packageName, permissionPriority);
		if(permission == null){
			MLog.e("BIND_ERROR_NOT_FOUND");
			return IEnginePermission.BIND_ERROR_NOT_FOUND;
		}
		return permission.getType();
	}

	@Override
	public synchronized void unBindEngine() {
		MLog.i("#enter unbindEngine");
		if(recordReader != null){
			recordReader.close();
			recordReader = null;
		}

		if(orientationManager != null){
			orientationManager.onDestroy();
			orientationManager = null;
		}

		if(encoderManager != null){
			encoderManager.onDestroy();
			encoderManager = null;
		}

		if(screenChannel != null){
			screenChannel.onDestroy();
			screenChannel = null;
		}

		PermissionLoader.release(permission);

		permission = null;
		context = null;
		onScreenshotCallback = null;
		MLog.i("#exit unbindEngine");
	}

    @Override
    public synchronized boolean screenShot(String imgPath){
        if(FilePath.mkdirs(imgPath) == false){
            return false;
        }

        ScreenShotCommand screenShotCommand = new ScreenShotCommand(imgPath);
        if(encoderManager != null){
            encoderManager.command(screenShotCommand);
            screenShotCommand.lockObject.lock();
        }
        else{
            screenShotCommand.execute();
        }
        return screenShotCommand.getImagePath() == null ? false : true;
    }

	@Override
	public synchronized String screenShot(){
		final String imgPath = FilePath.getScreenshotPath(false); // isPng
		if(onScreenshotCallback != null){
			onScreenshotCallback.onStart(imgPath);
		}
		ScreenShotCommand screenShotCommand = new ScreenShotCommand(imgPath);

		if(encoderManager != null){
			encoderManager.command(screenShotCommand);
			screenShotCommand.lockObject.lock();
		}
		else{
			screenShotCommand.execute();
		}
		return screenShotCommand.getImagePath();
	}

	public synchronized boolean command(String cmd) {
		if(encoderManager != null){
			SettingCommand settindCommand = new SettingCommand(cmd);
			encoderManager.command(settindCommand);
			settindCommand.lockObject.lock();
			return settindCommand.isResult;
		}
		return false;
	}

	public synchronized void encoderSuspend(int timeOut) {
		if(encoderManager != null){
			encoderManager.encoderSuspend(timeOut);
		}
	}

	public synchronized void encoderResume() {
		if(encoderManager != null){
			encoderManager.encoderResume();
		}
	}

	@Override
	public boolean start(FileDescriptor sock, int channelId) {
		// It means this service is running as remote process.
		if (sock != null){
			Net10.jniSetFileDescriptor(sock, channelId);
		}
		this.channelID = channelId;
		new Thread(screenServiceRunnable, "screenThread").start();
		return true;
	}

	public void setOnScreenShotCallback(OnScreenShotCallback screenShotCallBack) {
		this.onScreenshotCallback = screenShotCallBack;

		if(permission != null) {
			permission.setOnScreenShotCallback(screenShotCallBack);
		}
	}

	private ScreenChannel.OnPacketHandler handler = new ScreenChannel.OnPacketHandler() {
		@Override
		public boolean handlePacket(ByteBuffer msg) {
			int payloadtype = msg.get() & 0xff;
			int msgsize = msg.getInt();
			switch (payloadtype) {
			case rcp.rcpChannelNop:
				MLog.i("rcpChannelNop");
				break;
			case rcp.rcpKeyMouseCtrl: 
				requestInject(msg);
				break;
			case rcp.rcpScreenCtrl:
			case rcp.rcpOption:
				if(encoderManager != null){
					encoderManager.onCommand(payloadtype, msg);
				}
				break;
			case rcp.rcpScreenshot:
				screenShot();
				break;
			case rcp.rcpChannel:
				requestChannel(msg);
				break;
			default:
				MLog.e("ScreenChannel: invalid payload(%d), size(%d)\n", payloadtype, msgsize);
				return false;
			}
			return true;
		}

		private void requestInject(ByteBuffer msg) {
			try {
				permission.inject(msg.array(), msg.position(), msg.capacity()-msg.position());
			} catch (Exception e) {
				MLog.w(Log.getStackTraceString(e));
			}
		}

		private void requestChannel(ByteBuffer msg) {
			int msgid = msg.get() & 0xff;
			msg.getInt(); // skip datasize.
			if (msgid == rcp.rcpChannelClose) {
				MLog.i("rcpChannelClose received.");
				unBindEngine();
			}
		}

		@Override
		public void onClose() {
			MLog.w("ScreenChannel.OnPacketHandler onDestroy");
			unBindEngine();
		}
	};

	private Runnable screenServiceRunnable = new Runnable() {
		@Override
		public void run() {

			if(orientationManager != null){
				orientationManager.onDestroy();
				orientationManager = null;
			}

			if(screenChannel != null){
				screenChannel.onDestroy();
				screenChannel = null;
			}

			if(encoderManager != null){
				encoderManager.onDestroy();
				encoderManager = null;
			}

			synchronized (ScreenManager.this) {
				orientationManager = new OrientationManager(context);
				orientationManager.setOrientationEventListener(orientationCallBack);
				orientationManager.findHWRotation(permission);
				screenChannel = new ScreenChannel(context);
				screenChannel.setOnHandler(handler);
			}

			try {
				if(screenChannel.connect(channelID) == false){
					MLog.e("screenChannelManager connect fail");
					return;
				}

				Srn30Packet.init(context, orientationManager.getHWRotation());

				if(screenChannel.sendVersionMsg(orientationManager.getHWRotation()) == false){
					MLog.e("sendVersionMsg fail");
					return;
				}

				encoderManager = new EncoderManager(context);
				encoderManager.setIEnginePermission(permission);
				encoderManager.setOnChannelWriter(screenChannel);
				encoderManager.setOrientationManager(orientationManager);
				screenChannel.start();
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
		}
	};

	public boolean setMaxLayer(int maxLayer){
		try {
			if(permission != null){
				return permission.setMaxLayer(maxLayer);
			}
		} catch (Exception e) {
			MLog.e(Log.getStackTraceString(e));
		}
		return false;
	}

	public void releaseRecorder() {
		if(recordReader != null){
			recordReader.close();
			recordReader = null;
		}
		setMaxLayer(IEnginePermission.JNI_MAX_CAPTURE_DEFAULT);
	}

	public RecordReader getRecorder() {
		if(recordReader == null){
			recordReader = new RecordReader();
		}
		return recordReader;
	}

	public class RecordReader{
		private IScreenCaptureable captureable = null;
		private MemoryFileEx ashmem = null;
		private ByteBuffer ashmemHeaderBuffer = null;

		public RecordReader() {
			ashmemHeaderBuffer = ByteBuffer.allocate(ASHM_SCREEN.HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN);
		}

		public int createAshmem(int width, int height, int ashmType)throws RemoteException {
			ScapOption scapOption = new ScapOption();
			scapOption.clear();
			scapOption.setBitType(ashmType);
			scapOption.setStretch(width, height);
			
			captureable = permission.createScreenCaptureable(scapOption);
			try {
				ashmem = (MemoryFileEx)captureable.initialized();
				if(permission instanceof SonyPermission){
					ashmemHeaderBuffer.position(4 * 2);
					ashmemHeaderBuffer.putInt(ASHM_SCREEN.Header.FLAG_SUCCESS);
					ashmemHeaderBuffer.putInt(scapOption.getStretch().x);
					ashmemHeaderBuffer.putInt(scapOption.getStretch().y);
					ashmemHeaderBuffer.putInt(scapOption.getStride() * 4);
					ashmemHeaderBuffer.putInt(ASHM_SCREEN.RGB);
					ashmemHeaderBuffer.putInt(scapOption.getBitType());
					return ashmem.length() + ASHM_SCREEN.HEADER_SIZE;
				}
				return ashmem.length();

			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
			return -1;
		}

		public boolean createVirtualDisplay(String name, int w, int h, int dpi,
				Surface surface, int flag) throws RemoteException {
			try {
				ScapOption scapOption = new ScapOption();
				scapOption.setStretch(w, h);
				scapOption.setEncoderType(ScapOption.ENCODER_TYPE_OMX_FOR_VD);
				captureable = permission.createScreenCaptureable(scapOption);
				IVirtualDisplay virtualDisplay = (IVirtualDisplay)captureable.initialized();
				return virtualDisplay.createVirtualDisplay(name, w, h, dpi, surface, flag);
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
			return false;
		}

		public synchronized int capture(int width, int height) throws Exception{
			if (captureable != null) {
				return captureable.capture() ? ScreenService.RESULT_CAPTURE_SUCCESS : ScreenService.RESULT_CAPTURE_FAIL_DRM;
			}
			return ScreenService.RESULT_CAPTURE_FAIL_DISCONNECTED;
		}

		public synchronized int readBytes(byte[] buffer, int srcOffset, int destOffset,
				int count) throws RemoteException{
			try {
				if(ashmem != null){
					// sonypermission 은 header 를 설정해준다.
					if(permission instanceof SonyPermission){
						// image data
						if(srcOffset >= ASHM_SCREEN.HEADER_SIZE){
							return ashmem.readBytes(buffer, srcOffset - ASHM_SCREEN.HEADER_SIZE, destOffset, count);
						}
						else{
							ashmemHeaderBuffer.rewind();
							ashmemHeaderBuffer.position(srcOffset);
							int dstOffset = ashmemHeaderBuffer.remaining();
							ashmemHeaderBuffer.get(buffer, 0, dstOffset);
							int remainCount = count - dstOffset;
							if(remainCount > 0){
								int readCount = ashmem.readBytes(buffer, 0, dstOffset, remainCount);
								return readCount + dstOffset;
							}
							else{
								return dstOffset;
							}
						}
					}
					else{
						return ashmem.readBytes(buffer, srcOffset, destOffset, count);
					}
				}
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
			return -1;
		}
		
		public synchronized void close() {
			if(captureable != null){
				captureable.close();
				captureable = null;
			}
			ashmem = null;
		}
	}

	class ScreenShotCommand implements ICommand{
		private String imgPath = null;
		private LockObject lockObject = null;

		public ScreenShotCommand(String imgPath){
			this.imgPath= imgPath;
			lockObject = new LockObject();
		}
		
		public String getImagePath() {
			return this.imgPath;
		}

		@Override
		public void execute() {
			try {
				if(permission.screenshot(imgPath) == false){
					if(screenChannel != null){
						ByteBuffer bb = Srn30Packet.scapNotifyMsg(0, "capture failed.");
						screenChannel.write(bb.array(), 0, bb.position());
					}
					throw new RuntimeException("screen shot fail.");
				}
			} catch (Exception e) {
				if(onScreenshotCallback != null){
					onScreenshotCallback.onError(imgPath);
				}
				imgPath = null;
				MLog.e(Log.getStackTraceString(e));
				return;
			}finally{
				lockObject.notifyLock();
			}

			if(onScreenshotCallback != null){
				onScreenshotCallback.onComplete(imgPath);
			}
		}
	}

	class SettingCommand implements ICommand{
		private String cmd = null;
		private boolean isResult = false;
		private LockObject lockObject = null;

		SettingCommand(String cmd){
			this.cmd = cmd;
			lockObject = new LockObject();
		}

		@Override
		public void execute() {
			try {
				JSONObject jobject = new JSONObject(cmd);
				int type = jobject.getInt("type");
				switch(type){
				// setting
				case 0:
					String method = jobject.getString("method");
					String key = jobject.getString("key");
					if("putGString".equals(method) == true){
						String value = jobject.getString("valueString");
						isResult = permission.putGString(key, value);
					}
					else if("putGInt".equals(method) == true){
						int value = jobject.getInt("valueInt");
						isResult = permission.putGInt(key, value);
					}
					else if("putGLong".equals(method) == true){
						long value = jobject.getInt("valueLong");
						isResult = permission.putGLong(key, value);
					}
					else if("putGFloat".equals(method) == true){
						float value = jobject.getInt("valueFloat");
						isResult = permission.putGFloat(key, value);
					}
					else if("putSString".equals(method) == true){
						String value = jobject.getString("valueString");
						isResult = permission.putSString(key, value);
					}
					else if("putSInt".equals(method) == true){
						int value = jobject.getInt("valueInt");
						isResult = permission.putSInt(key, value);
					}
					else if("putSLong".equals(method) == true){
						long value = jobject.getInt("valueLong");
						isResult = permission.putSLong(key, value);
					}
					else if("putSFloat".equals(method) == true){
						float value = jobject.getInt("valueFloat");
						isResult = permission.putSFloat(key, value);
					}
					break;
				}
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			} finally{
				lockObject.notifyLock();
			}
		}
	}

	private OrientationManager.OnOrientationListener orientationCallBack = new OrientationManager.OnOrientationListener() {
		@Override
		public void onChanged(int beforeRotation, int rotation) {
			synchronized (ScreenManager.this) {
				try {
					if(orientationManager != null){
						if(orientationManager.checkRotation(beforeRotation, rotation) == true){
							encoderManager.orientationChanged(rotation);
						}
					}
					
					if(encoderManager.getScapOption().getRunState() == ScapOption.STATE_ENCODER_RUNNING){
						int sendRotation = (rotation + orientationManager.getHWRotation()) % 4;
						sendRotation(sendRotation);
					}
				} catch (Exception e) {
					MLog.e(Log.getStackTraceString(e));
				}
			}
		}
	};

	public void sendRotation(int rotation) {
		if(screenChannel != null){
			try {
				ByteBuffer bb = Srn30Packet.scapRotationMsg(rotation, false, encoderManager.getScapOption());
				if (bb != null){
					screenChannel.write(bb.array(), 0, bb.position());
				}
			} catch (Exception e) {
				MLog.e(Log.getStackTraceString(e));
			}
		}
	}

	public void onConfigurationChanged(Configuration newConfig) {
		if(orientationManager != null){
			orientationManager.onConfigurationChanged(newConfig);
		}
	}

	public int getFlag() {
		int resultFlag = getCaptureTypeFlag();
		resultFlag |= getEncoderStateFlag();
		return resultFlag;
	}
	
	public int getCurrentCaptureType(){
		return permission.getCurrentCaptureType();
	}
	
	private int getEncoderStateFlag(){
		if(encoderManager == null){
			return IEnginePermission.FLAG_STATE_ENCODER_NONE;
		}
		if(encoderManager.getScapOption() == null){
			return IEnginePermission.FLAG_STATE_ENCODER_NONE;
		}
		return IEnginePermission.FLAG_STATE_ENCODER_NONE + encoderManager.getScapOption().getRunState();
	}
	
	private int getCaptureTypeFlag(){
		int resultFlag = 0;
		int[] supportEncoder = permission.getSupportCaptureType();
		
		for(int encoderType : supportEncoder){
			switch(encoderType){
			case ScapOption.ENCODER_TYPE_JPG:
			case ScapOption.ENCODER_TYPE_OMX:
				resultFlag |= IEnginePermission.FLAG_CAPTURE_TYPE_ASHMEM;
				break;
			case ScapOption.ENCODER_TYPE_JPG_FOR_VD:
			case ScapOption.ENCODER_TYPE_OMX_FOR_VD:
				resultFlag |= IEnginePermission.FLAG_CAPTURE_TYPE_VIRTUAL_DISPLAY;
				break;
			}
		}
		return resultFlag;
	}

	/**
	 * Android Q 부터 Audio Capture를 지원함.
	 */
	@TargetApi(Build.VERSION_CODES.Q)
	public void createAudio() {
		if (permission instanceof ProjectionPermission) {
			audioCapture = new AudioCaptureForAndroidQ(((ProjectionPermission) permission).mediaProjection);
		}
	}

	public boolean initAudio(int sampleRate, int channelConfig, int audioFormat) {
		if (audioCapture == null) return false;
		return audioCapture.initialized(sampleRate, channelConfig, audioFormat);
	}

	public boolean startAudio() {
		if (audioCapture == null) return false;
		return audioCapture.start();
	}

	public void resumeAudio() {
		if (audioCapture != null) {
			audioCapture.resume();
		}
	}

	public byte[] readAudio(byte[] buffer, int size) {
		if (audioCapture == null) return emptyByteArray;
		return audioCapture.read(buffer, size);
	}

	public void pauseAudio() {
		if (audioCapture != null) {
			audioCapture.pause();
		}
	}

	public void muteAudio(boolean isMute) {
		if (audioCapture != null) {
			audioCapture.mute(isMute);
		}
	}

	public void releaseAudio() {
		if (audioCapture != null) {
			audioCapture.release();
		}
	}

}
