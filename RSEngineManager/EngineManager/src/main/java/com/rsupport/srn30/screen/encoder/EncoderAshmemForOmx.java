package com.rsupport.srn30.screen.encoder;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.Log;

import com.rsupport.media.image.ImageProcessing;
import com.rsupport.srn30.ASHM_SCREEN;
import com.rsupport.srn30.adjust.AdjustBitRate;
import com.rsupport.srn30.adjust.BitRateMonitor;
import com.rsupport.srn30.screen.encoder.codec.MediaCodecEncoder;
import com.rsupport.srn30.screen.encoder.codec.MediaCodecEncoder.OnDequeueBufferListener;
import com.rsupport.util.MemoryFileEx;
import com.rsupport.util.rslog.MLog;

import java.nio.ByteBuffer;

public class EncoderAshmemForOmx extends AbstractEncoder{
	private final int FRAME_RATE = 30;
	private MemoryFileEx ashmem = null;
	private byte[] frameData = null;
    private byte[] yuvData = null;
	private MediaCodecEncoder mediaCodecEncoder = null;
	private int yuvSize = 0;

	private BitRateMonitor bitRateMonitor = null;
	private AdjustBitRate adjustBitRate = null;
    private ASHM_SCREEN.Header ashmemHeader = null;

	public EncoderAshmemForOmx(Context context) {
		super(context);
		MLog.i("EncoderAshmemForOmx");
		ImageProcessing.load();
		mediaCodecEncoder = new MediaCodecEncoder();

		if(Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT){
			adjustBitRate = new AdjustBitRate();
			bitRateMonitor = new BitRateMonitor();
			bitRateMonitor.setOnFrameRateChangeListener(adjustBitRate);
		}
	}

	@Override
	public void onDestroy() {
		MLog.i("#enter onDestroy");
		if(adjustBitRate != null){
			scapOption.saveBitRateSetting(adjustBitRate);
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
		frameData = null;
        yuvData = null;
		ashmem = null;
        ashmemHeader = null;
		super.onDestroy();
		MLog.i("#exit onDestroy");
	}

	@Override
	public void setOption(ByteBuffer msg) {
		// viewer 로부터 전달 받은 bitrate 는 사용하지 않는다.
		int requestBitrate = msg.getInt();
		MLog.v("requestBitrate : " + requestBitrate);
		if(adjustBitRate != null){
			adjustBitRate.setConfigure(scapOption.getStretch().x, scapOption.getStretch().y, FRAME_RATE);
			scapOption.setBitrate(adjustBitRate.getCurrentBitrate());
		}

		scapOption.setFrameRate(FRAME_RATE);
		scapOption.setTileCache(0);
		scapOption.setRunFlags(ScapOption.STATE_ENCODER_NONE);
		scapOption.setBitType(ASHM_SCREEN.RGB);
		scapOption.setColorFormat(mediaCodecEncoder.getColorFormat());
	}

	@Override
	public void setOption(ScapOption scapOption) {
		super.setOption(scapOption);
		if(adjustBitRate != null){
			scapOption.restoreBitRateSetting(adjustBitRate);
		}
	}

	@Override
	public boolean initialized(Object initResult) {
		mediaCodecEncoder.stop();

		int ySize = scapOption.getStretch().x * scapOption.getStretch().y;
		yuvSize = ySize * 3 / 2;
        yuvData = new byte[yuvSize];
		mediaCodecEncoder.initEncoder(
				scapOption.getStretch().x,
				scapOption.getStretch().y,
				yuvSize,
				scapOption.getBitrate(),
				scapOption.getFrameRate(),
				scapOption.getIFrameInterval());

		ashmem = (MemoryFileEx)initResult;
        ashmemHeader = ASHM_SCREEN.get(ashmem);


		int AshmSize = ashmem.length() - ASHM_SCREEN.HEADER_SIZE;
        frameData = new byte[AshmSize];

		mediaCodecEncoder.preEncoding();

		if(adjustBitRate != null){
			adjustBitRate.setMediaCodec(mediaCodecEncoder.getMediaCodec());
		}
		return true;
	}

	@Override
	public boolean sendFrame() throws Exception {
        if(mediaCodecEncoder.dequeueOutputBuffer(dequeueBufferListener) == false){
            MLog.e("dequeueOutputBuffer error");
            return false;
        }

        ashmem.readBytes(frameData, ASHM_SCREEN.HEADER_SIZE, 0, frameData.length);

        if(mediaCodecEncoder.getBitType() == ASHM_SCREEN.I420) {
			ImageProcessing.convertARGBToI420(
					frameData,
					scapOption.getStretch().x,
					scapOption.getStretch().y,
					ashmemHeader.bytesPerLine,
					yuvData,
					ashmemHeader.width,
					ashmemHeader.height);
		} else {
			ImageProcessing.convertARGBToNV21(
					frameData,
					scapOption.getStretch().x,
					scapOption.getStretch().y,
					ashmemHeader.bytesPerLine,
					yuvData,
					ashmemHeader.width,
					ashmemHeader.height);
		}

        return mediaCodecEncoder.putEncodData(yuvData);
    }

	private OnDequeueBufferListener dequeueBufferListener = new OnDequeueBufferListener() {
		@Override
		public boolean onDequeueEvent(byte[] buffer, int offset, int count) throws Exception{
			if(bitRateMonitor != null){
				bitRateMonitor.startTime();
				boolean writeResult =  channelWriter.write(buffer, offset, count);
				bitRateMonitor.endTime();
				bitRateMonitor.checkChangeFrameRate();
				return writeResult;
			}
			return channelWriter.write(buffer, offset, count);
		}
	};

	@Override
	public boolean onSuspended() {
		if(mediaCodecEncoder != null){
			mediaCodecEncoder.stop();
		}
		return true;
	}
}
