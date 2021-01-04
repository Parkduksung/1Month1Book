package com.rsupport.jarinput;

import android.content.Context;
import android.os.IBinder;
import android.view.IWindowManager;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.rsupport.util.Config;
import com.rsupport.util.rslog.MLog;

import java.lang.reflect.Method;

class Injector23_40 implements MonkeyAgent.IInjector {

	InjCommon mInjCommon;

	public Injector23_40() throws Exception {
		init();
		mInjCommon = new InjCommon();
	}

	// -----------------------------------------------------------------------------------------------------------
	@Override
	public void touchInject(int action, int x, int y) {
		touch(mInjCommon.getTouchEvent(action, x, y, InputDevice.SOURCE_TOUCHSCREEN));
	}
	
	private void touch(InputEvent ie) {
		Injector23_40.iWndMgr.injectPointerEvent((MotionEvent)ie, InjCommon.bTouchSync);
//		try {
//			Injector40.injectPointerEvent.invoke(Injector40.wndMgr, ie, false);
//		} catch (Exception e) {
//			log.e(e.toString());
//		}
	}

	// -----------------------------------------------------------------------------------------------------------
	@Override
	public void wheelInject(int x, int y, int dy) {

		if (Config.DBG) MLog.d("wheel (%d, %d, delta: %d", x, y, dy);
		if (android.os.Build.VERSION.SDK_INT>=14)
			touch(mInjCommon.getWheelEvent(x, y, dy));
	}

	// -----------------------------------------------------------------------------------------------------------
	@Override
	public void keyInject(int action, int code, int scancode, int repeat, int meta) {

		mScreen.handleScreenOn(action, code);
		//if (code == KeyEvent.KEYCODE_ENDCALL)
		//	code = KeyEvent.KEYCODE_POWER;
		
		KeyEvent ke = mInjCommon.getKeyEvent(action, code, scancode, repeat, meta);
		if (ke == null)
			return ;

		Injector23_40.iWndMgr.injectKeyEvent((KeyEvent)ke, false);
//		try {
//			Injector40.injectKeyEvent.invoke(Injector40.wndMgr, ke, false);
//		} catch (Exception e) {
//			log.e(e.toString());
//		}
	}

	// ---------------------------------------------------------- multi-touch
	@Override
	public void multiTouchEvent(int action, int x, int y, int x2, int y2) {
		
		if (action == MotionEvent.ACTION_MOVE) {
			touch(mInjCommon.getMultiTouchEvent(MotionEvent.ACTION_MOVE, x, y, x2, y2, InputDevice.SOURCE_TOUCHSCREEN));
		} else if (action == MotionEvent.ACTION_DOWN) {
			touch(mInjCommon.getMultiTouchEvent(MotionEvent.ACTION_DOWN, x, y, x2, y2, InputDevice.SOURCE_TOUCHSCREEN));
			touch(mInjCommon.getMultiTouchEvent(MotionEvent.ACTION_POINTER_2_DOWN, x, y, x2, y2, InputDevice.SOURCE_TOUCHSCREEN));
		} else // if (action == MotionEvent.ACTION_UP)
		{
			touch(mInjCommon.getMultiTouchEvent(MotionEvent.ACTION_POINTER_2_UP, x, y, x2, y2, InputDevice.SOURCE_TOUCHSCREEN));
			touch(mInjCommon.getMultiTouchEvent(MotionEvent.ACTION_UP, x, y, x2, y2, InputDevice.SOURCE_TOUCHSCREEN));
		}
	}
	
	//------------------------------------------------------------------------------------------------
	// reflections
	private static ScreenOnOff mScreen;
	private static IWindowManager iWndMgr;
	static boolean init()  throws Exception {
		Class<?> ServiceManager = Class.forName("android.os.ServiceManager");
		Method getService = ServiceManager.getMethod("getService", String.class);

		Class<?> IWindowManager_Stub = Class.forName("android.view.IWindowManager$Stub");
		Method WMasInterface = IWindowManager_Stub.getMethod("asInterface", IBinder.class);
		Object wmBinder = getService.invoke(null,new Object[] { Context.WINDOW_SERVICE });
		iWndMgr = (android.view.IWindowManager)WMasInterface.invoke(null, new Object[] { wmBinder });
		mScreen = new ScreenOnOff();

		return true;
	}

}
