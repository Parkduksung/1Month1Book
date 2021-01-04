package com.rsupport.rsperm;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.LocalSocket;
import android.os.Build;
import android.util.Log;

import com.rsupport.srn30.screen.capture.AbstractCaptureable;
import com.rsupport.srn30.screen.capture.OnScreenShotCallback;
import com.rsupport.util.rslog.MLog;

public abstract class AbstractPermission implements IEnginePermission{
	protected final int BIND_TIME_OUT = 1000 * 5;
	private Context context = null;
	
	protected final int JNI_ASHMInject = 19;
	protected final int JNI_ASHMCreate = 20;
	protected final int JNI_ASHMCapture = 21;
	protected final int JNI_ASHMScreenshot = 22; 
	protected final int JNI_Execute = 23;
	protected final int JNI_GetHWRotation = 24;
	protected final int JNI_ASHMInitScreen = 25;
	protected final int JNI_SetScreenshotMaxLayer = 27;
	protected final int JNI_SetClose = 28;
	
	protected final int TYPE_LAYER_MULTIPLIER = 10000;
	protected final int TYPE_LAYER_OFFSET = 1000;
	protected final int JNI_MAX_CAPTURE_SYSTEM_ERROR = 20 * TYPE_LAYER_MULTIPLIER + TYPE_LAYER_OFFSET;
	protected final int JNI_MAX_CAPTURE_DEFAULT = 50 * TYPE_LAYER_MULTIPLIER + TYPE_LAYER_OFFSET;
	
	protected AbstractCaptureable captureable = null;
	protected OnScreenShotCallback screenShotCallback = null;
	
	abstract public boolean bind(String address); 
	abstract public void unbind();
	abstract public boolean isBound();
	
	public void onDestroy(){
		context = null;
		captureable = null;
		screenShotCallback = null;
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	protected Context getContext(){
		return context;
	}
	
	protected void safetyCloseSocket(Socket target){
		if(target != null){
			try {
				target.close();
			} catch (IOException e) {
				MLog.e(Log.getStackTraceString(e));
			}
		}
	}
	
	protected void safetyCloseLocalSocket(LocalSocket target){
		if(target != null){
			try {
				target.close();
			} catch (IOException e) {
				MLog.e(Log.getStackTraceString(e));
			}
		}
	}
	protected void safetyClose(Closeable target){
		if(target != null){
			try {
				target.close();
			} catch (IOException e) {
				MLog.e(Log.getStackTraceString(e));
			}
		}
	}
	
	protected int[] toIntArray(ArrayList<Integer> list){
		int[] result = new int[list.size()];
		for(int i = 0; i < result.length; i++){
			result[i] = list.get(i);
		}
		return result;
	}
	
	protected boolean waitForBind(int timeout){
		long startTime = System.currentTimeMillis();
		while(isBound() == false){
			if(System.currentTimeMillis() - startTime > timeout){
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					MLog.e(Log.getStackTraceString(e));
				}
				return false;
			}
		}
		return true;
	}
	



	@Override
	public boolean putSFloat(String name, float value) throws Exception { return false; }
	@Override
	public boolean putSInt(String name, int value) throws Exception { return false; }
	@Override
	public boolean putSLong(String name, long value) throws Exception { return false; }
	@Override
	public boolean putSString(String name, String value) throws Exception { return false; }
	@Override
	public boolean putGFloat(String name, float value) throws Exception { return false; }
	@Override
	public boolean putGInt(String name, int value) throws Exception { return false; }
	@Override
	public boolean putGLong(String name, long value) throws Exception { return false; }
	@Override
	public boolean putGString(String name, String value) throws Exception { return false; }

	@Override
	public void setOnScreenShotCallback(OnScreenShotCallback screenShotCallback) {
		this.screenShotCallback = screenShotCallback;
	}
}
