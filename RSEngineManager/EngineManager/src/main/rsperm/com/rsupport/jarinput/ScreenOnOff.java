package com.rsupport.jarinput;

import android.os.Build;
import android.os.SystemClock;
import android.view.KeyEvent;

import com.rsupport.rsperm.Reflection;
import com.rsupport.util.Config;
import com.rsupport.util.rslog.MLog;

import java.lang.reflect.Method;

class ScreenOnOff {
	
	
	 void handleScreenOn(int action, int code) {
		if (code == KeyEvent.KEYCODE_ENDCALL || code == KeyEvent.KEYCODE_POWER) {
			if (action == KeyEvent.ACTION_UP && !isScreenOn()) {
				onScreen();
			}
		}
	}
	 
	 
	 
	Method userActivityXXX;
	Method isScreenOn, isInteractive;
	Method wakeUp;
	Object powMgr;
	ScreenOnOff() {

		try {
			Class<?> cls = Reflection.getClass("android.os.IPowerManager");
			if (Build.VERSION.SDK_INT < 17)
				userActivityXXX = Reflection.getMethod(cls, "userActivityWithForce", long.class, boolean.class, boolean.class);
			else
				userActivityXXX = Reflection.getMethod(cls, "userActivity", long.class, int.class, int.class);
			isScreenOn = Reflection.getMethod(cls, "isScreenOn");
			isInteractive = Reflection.getMethod(cls, "isInteractive");
			wakeUp = Reflection.getMethod(cls, "wakeUp", long.class);

			powMgr = Reflection.getPowerManager();
		} catch (Exception e) {
			MLog.e(e.toString());
		}
	}
	
	private boolean onScreen() {

		try {
			if (isScreenOn())
				return false;
			if (wakeUp != null)
				wakeUp.invoke(powMgr,SystemClock.uptimeMillis());
			else if (Build.VERSION.SDK_INT <17)
				userActivityXXX.invoke(powMgr, SystemClock.uptimeMillis(), true, true);
			else
				userActivityXXX.invoke(powMgr, SystemClock.uptimeMillis(), 1, 1);
				
			if (Config.DBG) MLog.d("screen on");
			return true;
		} catch (Exception e) {
			MLog.e(e.toString());
		}

		return false;
	}

	private boolean isScreenOn() {
		try {
			if (isScreenOn != null)
				return (Boolean) isScreenOn.invoke(powMgr);
			if (isInteractive != null)
				return (Boolean) isInteractive.invoke(powMgr);
		} catch (Exception e) {
		}
		return true;
	}

}
