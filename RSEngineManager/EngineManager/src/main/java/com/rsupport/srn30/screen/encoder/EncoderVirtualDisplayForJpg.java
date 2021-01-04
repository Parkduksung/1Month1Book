package com.rsupport.srn30.screen.encoder;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;

import com.rsupport.srn30.ASHM_SCREEN;
import com.rsupport.srn30.Srn30Native;
import com.rsupport.srn30.screen.capture.IVirtualDisplay;
import com.rsupport.srn30.screen.encoder.surface.OnSurfaceDrawable;
import com.rsupport.srn30.screen.encoder.surface.SurfaceReader;
import com.rsupport.util.LockObject;
import com.rsupport.util.rslog.MLog;

@TargetApi(19)
public class EncoderVirtualDisplayForJpg extends AbstractEncoder{
	private final int NOT_USED_ADDRESS = 999;
	private final int FRAME_RATE = 30;
	private boolean initEncoder = false;
	private SurfaceReader surfaceReader = null;
	private LockObject lockObject = null;
	private ReentrantLock sendLock = null;
	private ByteBuffer byteBuffer = null;
	
	public EncoderVirtualDisplayForJpg(Context context) {
		super(context);
		MLog.i("EncoderVirtualDisplayForJpg");
		sendLock = new ReentrantLock();
		lockObject = new LockObject();
	}

	@Override
	public void onDestroy() {
		MLog.i("#enter onDestroy");
		try {
			stop();
		} catch (InterruptedException e) {
			MLog.e(Log.getStackTraceString(e));
		}

		if(lockObject != null){
			lockObject.notifyLock();
		}
		if(surfaceReader != null){
			surfaceReader.onDestroy();
			surfaceReader = null;
		}
		super.onDestroy();
		MLog.i("#exit onDestroy");
	}

	@Override
	public void setOption(ByteBuffer msg) {
		scapOption.setBitType(ASHM_SCREEN.RGB);
		scapOption.setFrameRate(FRAME_RATE);
		// jpg quality 100보다 큰경우 default 로 설정한다.
		// 'O', 'V' 로 요청하였지만 codec 을 지원 하지 않으면 'T' 로 변환한다.
		// encoder type 이 'O', 'V' 일경우 msg 는 bitrate이므로 100보다 클수 있다.
		int jpgQuality = msg.getInt();
		if(jpgQuality > 100){
			scapOption.setJpegQuality(ScapOption.DEFAULT_QUALITY);
			scapOption.setTileCache(0);
		}
		else{
			scapOption.setJpegQuality(jpgQuality);
			scapOption.setTileCache(msg.getInt());
		}
		scapOption.setRunFlags(ScapOption.STATE_ENCODER_NONE);
	}

	@Override
	public boolean initialized(Object initResult) {
		IVirtualDisplay virtualDisplay = ((IVirtualDisplay)initResult);
		try {
			lockObject.clear();
			initEncoder = false;
			surfaceReader = new SurfaceReader(context);
			surfaceReader.setSurfaceDrawable(surfaceDrawable);
			Surface sourceSurface = surfaceReader.createInputSurface(
					scapOption.getStretch().x, 
					scapOption.getStretch().y, 
					PixelFormat.RGBA_8888);
			createVirtualDisplay(virtualDisplay, scapOption, sourceSurface);
			lockObject.lock(3000);
		} catch (Exception e) {
			MLog.e(Log.getStackTraceString(e));
			return false;
		}
		return true;
	}
	

	private boolean initEncoder(ScapOption scapOption){
		StringBuilder sb = new StringBuilder("opts=enc");
		// vd jpg encoder type
		sb.append("&type=").append(ScapOption.ENCODER_TYPE_JPG);
		sb.append("&stretch=").append((float)scapOption.getStretch().x/(float)scapOption.getStretch().y);
		sb.append("&tilecache=").append(scapOption.getTileCache());
		sb.append("&jpgQuality=").append(scapOption.getJpegQuality());
		sb.append("&remotebpp=").append(scapOption.getRemotebpp());
		sb.append("&useDelaySend=").append(0);
		sb.append("&vdWidth=").append(scapOption.getStretch().x);
		sb.append("&vdHeight=").append(scapOption.getStretch().y);
		sb.append("&vdScanline=").append(scapOption.getStride());
		sb.append("&vdPixelFormat=").append(PixelFormat.RGBA_8888);
		initEncoder = true;
		return Srn30Native.initEncoder(NOT_USED_ADDRESS, sb.toString());
	}

	private OnSurfaceDrawable surfaceDrawable = new OnSurfaceDrawable() {
		
		@Override
		public void onDrawable(ByteBuffer imageBuffer, int width, int height,
				int pixelStride, int rowStride, int rowPadding) {
			sendLock.lock();
			if(initEncoder == false){
				if(lockObject != null){
					scapOption.setStride(rowStride);
					if(initEncoder(scapOption) == false){
						MLog.e("initEncoder fail");
					}
					lockObject.notifyLock();
				}
			}
			byteBuffer = imageBuffer;
			sendLock.unlock();
		}

		@Override
		public void initialized() {
		}

		@Override
		public void release() {
		}

	};

	private void createVirtualDisplay(IVirtualDisplay virtualDisplay, ScapOption scapOption, Surface surface) throws Exception{
		DisplayManager dispMgr = (DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE);
		Display disp = dispMgr.getDisplay(0);
		DisplayMetrics dm = new DisplayMetrics();
		disp.getMetrics(dm);
		if (virtualDisplay.createVirtualDisplay(VirtualDisplayHelper.VIRTUAL_DISPLAY_NAME, 
				scapOption.getStretch().x, 
				scapOption.getStretch().y, 
				dm.densityDpi, 
				surface, 
				DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC|DisplayManager.VIRTUAL_DISPLAY_FLAG_SECURE) == false){
			throw new Exception("can't create virtual display!");
		}
	}

	@Override
	public boolean sendFrame() throws Exception {
		sendLock.lock();
		if(byteBuffer != null && channelWriter != null){
			channelWriter.write(byteBuffer);
			byteBuffer = null;
			
		}
		sendLock.unlock();
		return true;
	}
	
	@Override
	public boolean onSuspended() {
		if(surfaceReader != null){
			surfaceReader.onDestroy();
			surfaceReader = null;
		}
		if(lockObject != null){
			lockObject.clear();
		}
		return true;
	}
}
