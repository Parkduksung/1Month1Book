package com.rsupport.srn30.screen.rotation;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

import com.rsupport.hwrotation.MarkerService;
import com.rsupport.rsperm.IEnginePermission;
import com.rsupport.util.SystemProperties;
import com.rsupport.util.rslog.MLog;

public class OrientationManager {
	private Context context = null;
	private int hwRotation = 0;
	private int savedRotation = -1;
	
	private OnOrientationListener orientationCallBack;
	private OrientationEventListener orientation;
	private Display display = null;
	
	public OrientationManager(Context context){
		this.context = context;
		display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		savedRotation = display.getRotation();
		
		orientation = new OrientationEventListener(context) {
			@Override
			public void onOrientationChanged(int orientation) {
				if (display != null) {
					int rotation = display.getRotation(); // 0,1,2,3
					if(savedRotation != rotation){
						int temp = savedRotation;
						savedRotation = rotation;
						sendRotation(temp, savedRotation);
					}
				}
			}
		};
		if(orientation.canDetectOrientation() == true){
			orientation.enable();
		}
	}
	
	public void onDestroy() {
		MLog.i("#enter onDestroy");
		if(orientation != null){
			orientation.disable();
			orientation = null;
		}
		orientationCallBack = null;
		context = null;
		hwRotation = 0;
		savedRotation = -1;
		display = null;
		MLog.i("#exit onDestroy");
	}
	
	public void setOrientationEventListener(OnOrientationListener orientationCallBack){
		this.orientationCallBack = orientationCallBack;
	}
	
	public int getHWRotation() {
		return hwRotation;
	}
	
	public int findHWRotation(IEnginePermission permission){
		hwRotation = 0;
		// 7.0 부터는 readFrameBuffer 를 사용할 수 없어
		// hwRotation 판단할 필요 없으므로 0으로 설정한다.
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
			return hwRotation;
		}

		// detect only when ro.sf.hwrotation is valid.
		if (SystemProperties.get("ro.sf.hwrotation", null) == null) {
			return hwRotation;
		}
		try {
			Intent i = new Intent(context, MarkerService.class);
			context.startService(i);
			hwRotation = permission.hwRotation();
			context.stopService(i);
			return hwRotation;
		}
		catch(Exception e) {
			MLog.e(e.toString());
			return hwRotation;
		}	
	}

	public void onConfigurationChanged(Configuration newConfig) {
		Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int rotation = display.getRotation(); // 0,1,2,3
		MLog.v("onConfigurationChanged savedRotation : " + savedRotation + ", rotation : " + rotation);
		if(savedRotation != rotation){
			int temp = savedRotation;
			savedRotation = rotation;
			sendRotation(temp, rotation);
		}
	}
	
	public boolean checkRotation(int beforeRotation, int currentRotation){
		if(beforeRotation == currentRotation){
			return false;
		}
		
		switch(beforeRotation){
		case Surface.ROTATION_0:
			if(currentRotation == Surface.ROTATION_180){
				return false;
			}
			break;
		case Surface.ROTATION_180:
			if(currentRotation == Surface.ROTATION_0){
				return false;
			}
			break;
		case Surface.ROTATION_270:
			if(currentRotation == Surface.ROTATION_90){
				return false;
			}
			break;
		case Surface.ROTATION_90:
			if(currentRotation == Surface.ROTATION_270){
				return false;
			}
			break;
		}
		return true;
	}
	
	private void sendRotation(int beforeRotation, int rotation){
		if(orientationCallBack != null){
			orientationCallBack.onChanged(beforeRotation, rotation);
		}
	}
	
	public static interface OnOrientationListener{
		public void onChanged(int beforeRotation, int rotation);
	}
}
