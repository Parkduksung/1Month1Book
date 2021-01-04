package com.rsupport.srn30.screen.encoder;

import java.nio.ByteBuffer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;

import com.rsupport.srn30.adjust.AdjustBitRate;
import com.rsupport.srn30.adjust.BitRateMonitor;
import com.rsupport.srn30.screen.capture.IVirtualDisplay;
import com.rsupport.srn30.screen.encoder.codec.MediaCodecEncoder;
import com.rsupport.srn30.screen.encoder.codec.MediaCodecEncoder.OnDequeueBufferListener;
import com.rsupport.util.rslog.MLog;

@TargetApi(19)
public class EncoderVirtualDisplayForOmx extends AbstractEncoder{
	private final int FRAME_RATE = 30;
	 
	private MediaCodecEncoder mediaCodecEncoder = null;
	private BitRateMonitor bitRateMonitor = null;
	private AdjustBitRate adjustBitRate = null;
	private VirtualDisplayHelper virtualDisplayHelper = null;

	public EncoderVirtualDisplayForOmx(Context context) {
		super(context);
		MLog.i("EncoderVirtualDisplayForOmx");
		virtualDisplayHelper = new VirtualDisplayHelper(context);
		mediaCodecEncoder = new MediaCodecEncoder();
		bitRateMonitor = new BitRateMonitor();
		adjustBitRate = new AdjustBitRate();
		bitRateMonitor.setOnFrameRateChangeListener(adjustBitRate);
	}

	@Override
	public void onDestroy() {
		MLog.i("#enter onDestroy");
		scapOption.saveBitRateSetting(adjustBitRate);

		if(virtualDisplayHelper != null){
			virtualDisplayHelper.onDestroy();
			virtualDisplayHelper = null;
		}
		
		try {
			stop();
		} catch (InterruptedException e) {
			MLog.e(Log.getStackTraceString(e));
		}

		if(mediaCodecEncoder != null){
			mediaCodecEncoder.onDestroy();
			mediaCodecEncoder = null;
		}

		if(bitRateMonitor != null){
			bitRateMonitor.onDestroy();
			bitRateMonitor = null;
		}

		if(adjustBitRate != null){
			adjustBitRate.onDestroy();
			adjustBitRate = null;
		}

		super.onDestroy();
		MLog.i("#exit onDestroy");
	}

	@Override
	public void setOption(ByteBuffer msg) {
		// viewer 로부터 전달 받은 bitrate 는 사용하지 않는다.
		int requestBitrate = msg.getInt();
		MLog.v("requestBitrate " + requestBitrate);
		adjustBitRate.setConfigure(scapOption.getStretch().x, scapOption.getStretch().y, FRAME_RATE);
		scapOption.setBitrate(adjustBitRate.getCurrentBitrate());
		
		scapOption.setFrameRate(FRAME_RATE);
		scapOption.setTileCache(0);
		scapOption.setRunFlags(ScapOption.STATE_ENCODER_NONE);
		scapOption.setBitType(mediaCodecEncoder.getBitType());
		scapOption.setColorFormat(mediaCodecEncoder.getColorFormat());
	}

	@Override
	public void setOption(ScapOption scapOption) {
		super.setOption(scapOption);
		scapOption.restoreBitRateSetting(adjustBitRate);
	}
	
	@Override
	public boolean initialized(Object initResult) {
		IVirtualDisplay virtualDisplay = ((IVirtualDisplay)initResult);
		mediaCodecEncoder.stop();
		int ySize = scapOption.getStretch().x * scapOption.getStretch().y;
		int yuvSize = ySize * 3 / 2;
		mediaCodecEncoder.initEncoder(
				scapOption.getStretch().x,
				scapOption.getStretch().y, 
				yuvSize,
				scapOption.getBitrate(),
				scapOption.getFrameRate(),
				scapOption.getIFrameInterval());
		try {
			Surface encoderInputSurface = mediaCodecEncoder.preEncodingSurface();
			virtualDisplayHelper.initialized(encoderInputSurface, 
					scapOption.getStretch().x, 
					scapOption.getStretch().y, 
					PixelFormat.RGBA_8888);
			Surface sourceSurface = virtualDisplayHelper.getImageReaderSurface();
			createVirtualDisplay(virtualDisplay, scapOption, sourceSurface);
			adjustBitRate.setMediaCodec(mediaCodecEncoder.getMediaCodec());
		} catch (Exception e) {
			MLog.e(Log.getStackTraceString(e));
			return false;
		}
		return true;
	}
	
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
		if(mediaCodecEncoder.dequeueOutputBuffer(dequeueBufferListener) == false){
			MLog.e("dequeueOutputBuffer error");
			return false;
		}
		return true;
	}

	private OnDequeueBufferListener dequeueBufferListener = new OnDequeueBufferListener() {
		@Override
		public boolean onDequeueEvent(byte[] buffer, int offset, int count) throws Exception{
			bitRateMonitor.startTime();
			boolean writeResult =  channelWriter.write(buffer, offset, count);
			bitRateMonitor.endTime();
			bitRateMonitor.checkChangeFrameRate();
			return writeResult;
		}
	};

	@Override
	public boolean onSuspended() {
		if(mediaCodecEncoder != null){
			mediaCodecEncoder.stop();
		}
		
		if(virtualDisplayHelper != null){
			virtualDisplayHelper.release();
		}
		return true;
	}
}
