package com.rsupport.rsperm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import android.graphics.Point;

import com.rsupport.srn30.screen.capture.IScreenCaptureable;
import com.rsupport.srn30.screen.capture.OnScreenShotCallback;
import com.rsupport.srn30.screen.encoder.ScapOption;

public interface IEnginePermission {
	
	/** surfaceflinger 방식 **/
	public final static int CAPTURE_TYPE_SURFACE_FLINGER = 100;
	/** virtual display 방식 **/
	public final static int CAPTURE_TYPE_VIRTUAL_DISPLAY = 200;
	
	
	public final static int COMMAND_OK = 0;
	
	public static final int BIND_ERROR_NOT_FOUND = -1;
	public static final int BIND_TYPE_UDS = 0;
	public static final int BIND_TYPE_RSPERM = 1;
	public static final int BIND_TYPE_SONY = 2;
	public static final int BIND_TYPE_PROJECTION = 3;
	public static final int BIND_TYPE_KNOX_PROJECTION = 4;

	/**
	 * ashmemory 사용한 capture
	 */
	public final static int FLAG_CAPTURE_TYPE_ASHMEM 			= 0x00000001;
	
	/**
	 * virtual display 이용 (4.4 이상의 vd 지원 rsperm 설치시 지원)
	 */
	public final static int FLAG_CAPTURE_TYPE_VIRTUAL_DISPLAY 	= 0x00000002;
	
	/** screen encoder 사용 안하는 상태 **/
	static public final int FLAG_STATE_ENCODER_NONE	    		= 0x10000000;
	/** 화면 전송 상태 **/
	static public final int FLAG_STATE_ENCODER_RUNNING    		= FLAG_STATE_ENCODER_NONE + ScapOption.STATE_ENCODER_RUNNING;
	/** 화면 전송 중지 상태 **/
	static public final int FLAG_STATE_ENCODER_PAUSED		 	= FLAG_STATE_ENCODER_NONE + ScapOption.STATE_ENCODER_PAUSED;
	/** 화면 전송 종료 상태 **/
	static public final int FLAG_STATE_ENCODER_STOP    			= FLAG_STATE_ENCODER_NONE + ScapOption.STATE_ENCODER_STOP;
	/** 화면 전송 중이지만 DRM 상태 **/
	static public final int FLAG_STATE_ENCODER_DRM	    		= FLAG_STATE_ENCODER_NONE + ScapOption.STATE_ENCODER_DRM;
	
	static final int TYPE_LAYER_MULTIPLIER = 10000;
	static final int TYPE_LAYER_OFFSET = 1000;
	
	public static final int JNI_MAX_CAPTURE_SYSTEM_ERROR = 20 * TYPE_LAYER_MULTIPLIER + TYPE_LAYER_OFFSET;
	public static final int JNI_MAX_CAPTURE_DEFAULT = 50 * TYPE_LAYER_MULTIPLIER + TYPE_LAYER_OFFSET;

	
	public int getType();
	public int hwRotation() throws Exception;
	public int[] getSupportEncoder();
	public int getCurrentCaptureType();
    public int[] getSupportCaptureType();
	
	public String exec(String cmd) throws Exception;
	public boolean putSFloat(String name, float value) throws Exception;
	public boolean putSInt(String name, int value) throws Exception;
	public boolean putSLong(String name, long value) throws Exception;
	public boolean putSString(String name, String value) throws Exception;
	
	public boolean putGFloat(String name, float value) throws Exception;
	public boolean putGInt(String name, int value) throws Exception;
	public boolean putGLong(String name, long value) throws Exception;
	public boolean putGString(String name, String value) throws Exception;
	
	public boolean screenshot(String imgPath) throws IOException;
	public boolean setMaxLayer(int jniMaxCaptureDefault) throws IOException;
	
	public void inject(byte[] data, int offset, int length) throws Exception;
	
	public IScreenCaptureable createScreenCaptureable(ScapOption scapOption);

	public void setOnScreenShotCallback(OnScreenShotCallback screenShotCallback);
}


class RSPermHelper {
	static ByteBuffer buildRequest(boolean uds, int bufSize, int type, Object ...args) {
		ByteBuffer bb = ByteBuffer.allocate(bufSize).order(ByteOrder.LITTLE_ENDIAN);
		if (uds)
			bb.position(4);
		bb.put((byte)type);
		for (Object arg : args) {
			if (arg instanceof Integer)
				bb.putInt((Integer)arg);
			else if (arg instanceof Byte)
				bb.put((Byte)arg);
			else if (arg instanceof String){
				bb.put(((String)arg).getBytes(Charset.defaultCharset()));
			}
			else if (arg instanceof Point)
				bb.putInt(((Point)arg).x).putInt(((Point)arg).y);
		}
		if (uds) {
			int size = bb.position();
			bb.putInt(0, size-4);
		}
		return bb;
	}
}