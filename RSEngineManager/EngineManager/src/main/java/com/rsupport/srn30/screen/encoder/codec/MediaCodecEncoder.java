package com.rsupport.srn30.screen.encoder.codec;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.rsupport.srn30.ASHM_SCREEN;
import com.rsupport.srn30.scap;
import com.rsupport.util.CodecUtils;
import com.rsupport.util.Net10;
import com.rsupport.util.rslog.MLog;

@TargetApi(16)
public class MediaCodecEncoder {
	private final String MIME_TYPE = "video/avc";
	private long startTimeMs = 0;
	private long presentationTimeUs = 0;
	private int yuvSize = 0;
	private int colorFormat = -1;
	private int encoderBitType = -1;

	private MediaCodecInfo mediaCodecInfo = null;
	private MediaCodec mediaCodec;
	private MediaFormat mediaFormat;

	private MediaCodec.BufferInfo bufferInfo = null;

	private ByteBuffer[] mInputBuffers  = null; 
	private ByteBuffer[] mOutputBuffers = null;
	private byte[] nalBuf = null;
	
	private Surface surface = null;
	
	public MediaCodecEncoder(){
		mediaCodecInfo = CodecUtils.selectAVCCodec();
		colorFormat = selectColorFormat(mediaCodecInfo);
		encoderBitType = getYUVColorFormat(colorFormat); 
		bufferInfo = new MediaCodec.BufferInfo();
		nalBuf = new byte[1];
	}

	public void initEncoder(int width, int height, int yuvSize, int bitRate, int frameRate, int iFrameInterval) {
		MLog.d("initEncoder width.%d, height.%d, yuvSize.%d, bitRate.%d, frameRate.%d, iFrameInterval.%d",
				width, height, yuvSize, bitRate, frameRate, iFrameInterval);
		mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
		mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
		mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
		mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
		mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval);

		startTimeMs = System.currentTimeMillis();
		presentationTimeUs = 0;
		this.yuvSize = yuvSize;
	}
	
	public void stop() {
		MLog.i("stop");
		if(mediaCodec != null){
			mediaCodec.stop();
			mediaCodec.release();
			mediaCodec = null;
		}
		
		if(surface != null){
			surface.release();
			surface = null;
		}
		mediaFormat = null;
		mInputBuffers = null;
		mOutputBuffers = null;
	}
	
	public void onDestroy(){
		stop();

		mediaCodecInfo = null;
		mediaFormat = null;
		bufferInfo = null;
		nalBuf = null;
		surface = null;
	}

	@SuppressWarnings("deprecation")
	@TargetApi(19)
	public Surface preEncodingSurface(){
		if(mediaCodecInfo == null){
			MLog.e("mediaCodecInfo is null");
			return null;
		}

		try {
			mediaCodec = MediaCodec.createByCodecName(mediaCodecInfo.getName());
		} catch (IOException e) {
			MLog.e("fail MediaCodec.createByCodecName: " + Log.getStackTraceString(e));
			return null;
		}
		if (mediaCodec == null) {
			MLog.e("fail MediaCodec.createByCodecName: " + mediaCodecInfo.getName());
			return null;
		}

		// change surface color format
		colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
		mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
		
		mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		surface = mediaCodec.createInputSurface();
		mediaCodec.start();
		mOutputBuffers = mediaCodec.getOutputBuffers();
		return surface;
	}
	
	@SuppressWarnings("deprecation")
	public boolean preEncoding(){
		if(mediaCodecInfo == null){
			MLog.e("mediaCodecInfo is null");
			return false;
		}

		try {
			mediaCodec = MediaCodec.createByCodecName(mediaCodecInfo.getName());
		} catch (IOException e) {
			MLog.e("fail MediaCodec.createByCodecName: " + Log.getStackTraceString(e));
			return false;
		}
		if (mediaCodec == null) {
			MLog.e("fail MediaCodec.createByCodecName: " + mediaCodecInfo.getName());
			return false;
		}

		mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		
		mediaCodec.start();
		mInputBuffers  = mediaCodec.getInputBuffers(); 
		mOutputBuffers = mediaCodec.getOutputBuffers();
		return true;
	}

	@SuppressWarnings("deprecation")
	public boolean dequeueOutputBuffer(OnDequeueBufferListener listener) throws Exception{
		int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 50000); // 50ms timeout
		if (outputBufferIndex >= 0) {
			ByteBuffer outputBuffer = mOutputBuffers[outputBufferIndex];

			byte[] outData = getNalBuffer(bufferInfo.size);
			outputBuffer.position(bufferInfo.offset);
			outputBuffer.get(outData, Net10.WS_PREPAD+4, bufferInfo.size);

			if(listener.onDequeueEvent(outData, Net10.WS_PREPAD, 4+bufferInfo.size) == false){
				return false;
			}

			mediaCodec.releaseOutputBuffer(outputBufferIndex, false);				
		}
		else switch (outputBufferIndex) {
		case MediaCodec.INFO_TRY_AGAIN_LATER: 
			break;
		case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED: 
			// not expected for an encoder
			mOutputBuffers = mediaCodec.getOutputBuffers();
			MLog.d("encoder output buffers changed");
			break;
		case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED: 
			// not expected for an encoder
			mediaFormat = mediaCodec.getOutputFormat();
			MLog.d("encoder output format changed: " + mediaFormat);
			break;
		default:
			MLog.e("unexpected result from encoder.dequeueOutputBuffer: " + outputBufferIndex);
			break;
		}
		return true;
	}

	public boolean putEncodData(ByteBuffer frameData){
		if(mInputBuffers == null){
			throw new RuntimeException("Called putEncodData before preEncoding.");
		}
		// input yuv data to codec.
		int inputBufferIndex = mediaCodec.dequeueInputBuffer(50000); // 50ms
		if (inputBufferIndex >= 0){
			ByteBuffer inputBuffer = mInputBuffers[inputBufferIndex];
			if( yuvSize > inputBuffer.capacity() ){
				MLog.e("invalid input buffer[%d] size: yuv.%d > inbuf.%d",
						inputBufferIndex, yuvSize, inputBuffer.capacity());
				return false;
			}

			inputBuffer.clear();
			inputBuffer.put(frameData);

			presentationTimeUs = (System.currentTimeMillis() - startTimeMs) * 1000;
			mediaCodec.queueInputBuffer(inputBufferIndex, 0, yuvSize, presentationTimeUs, 0);
		}
		return true;
	}
	
	public boolean putEncodData(byte[] frameData){
		if(mInputBuffers == null){
			throw new RuntimeException("Called putEncodData before preEncoding.");
		}
		// input yuv data to codec.
		int inputBufferIndex = mediaCodec.dequeueInputBuffer(50000); // 50ms
		if (inputBufferIndex >= 0){
			ByteBuffer inputBuffer = mInputBuffers[inputBufferIndex];
			if( yuvSize > inputBuffer.capacity() ){
				MLog.e("invalid input buffer[%d] size: yuv.%d > inbuf.%d",
						inputBufferIndex, yuvSize, inputBuffer.capacity());
				return false;
			}

			inputBuffer.clear();
			inputBuffer.put(frameData);
			

			presentationTimeUs = (System.currentTimeMillis() - startTimeMs) * 1000;
			mediaCodec.queueInputBuffer(inputBufferIndex, 0, yuvSize, presentationTimeUs, 0);
		}
		return true;
	}

	protected byte[] getNalBuffer(int size) {
		if (size+Net10.WS_PREPAD+4 > nalBuf.length) {
			nalBuf = new byte[size+Net10.WS_PREPAD+4 + 4096];
			nalBuf[Net10.WS_PREPAD+0] = (byte)scap.scapEnc;
			nalBuf[Net10.WS_PREPAD+1] = 0;
			nalBuf[Net10.WS_PREPAD+2] = 0;
			nalBuf[Net10.WS_PREPAD+3] = 0;
		}
		return nalBuf;
	}

	protected int align(int num, int unit) { 
		return (num+unit-1) & ~(unit-1); 
	}

	private int getYUVColorFormat(int colorFormat) {
		switch (colorFormat) {
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
			return ASHM_SCREEN.I420;
			
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
		case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
			return ASHM_SCREEN.NV12;
		}
		
		return ASHM_SCREEN.NV12;
	}

	private int selectColorFormat(MediaCodecInfo codecInfo) {
		if (codecInfo == null) {
			MLog.e("There is no encoder support " + MIME_TYPE);
			return 0;
		}

		int ret_colorFormat = selectColorFormat(codecInfo, MIME_TYPE);

		if (ret_colorFormat == 0){
			MLog.e("couldn't find a good color format for %s/%s", codecInfo.getName(), MIME_TYPE);
		}

		return ret_colorFormat;   // not reached
	}

	/**
	 * Retruns a color format that is supported by the codec and by this test
	 * code. If no match is found, this throws a test failure -- the set of
	 * formats known to the test should be expanded for new platforms.
	 */
	private int selectColorFormat(MediaCodecInfo codecInfo, String mimeType) {
		try {
			MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
			for (int i = 0; i < capabilities.colorFormats.length; i++) {
				int colorFormat = capabilities.colorFormats[i];
				if (isRecognizedFormat(colorFormat)) {
					MLog.d("Find a good color format for " + codecInfo.getName() + " / " + mimeType + " / " + colorFormat);
					return colorFormat;
				}
			}
		} catch (IllegalArgumentException e) {
			MLog.e("IllegalArgumentException : " + e);
		}

		return 0;
	}

	/**
	 * Returns true if this is a color format that this test code understands
	 * (i.e. we know how to read and generate frames in this format).
	 */
	private boolean isRecognizedFormat(int colorFormat) {
		switch (colorFormat) {
		// these are the formats we know how to handle for this test
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
			MLog.d("COLOR_FormatYUV420Planar");
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
			MLog.d("COLOR_FormatYUV420PackedPlanar");
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
			MLog.d("COLOR_FormatYUV420SemiPlanar");
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
			MLog.d("COLOR_FormatYUV420PackedSemiPlanar");
		case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
			MLog.d("COLOR_TI_FormatYUV420PackedSemiPlanar");
			return true;
		default:
			return false;
		}
	}
	
	public int getBitType() {
		return encoderBitType;
	}

	public int getColorFormat() {
		return colorFormat;
	}

	public MediaCodec getMediaCodec() {
		return mediaCodec;
	}
	
	public static interface OnDequeueBufferListener{
		public boolean onDequeueEvent(byte[] buffer, int offset, int count) throws Exception;
	}

}
