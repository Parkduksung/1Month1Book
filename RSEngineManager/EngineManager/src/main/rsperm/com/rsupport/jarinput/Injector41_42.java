package com.rsupport.jarinput;

import android.annotation.TargetApi;
import android.hardware.input.IInputManager;
import android.os.Build;
import android.os.RemoteException;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.rsupport.util.Config;
import com.rsupport.util.rslog.MLog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class Injector41_42 implements MonkeyAgent.IInjector {

	InjCommon mInjCommon;

	public Injector41_42() throws Exception {
		init();
		mInjCommon = new InjCommon();
	}

	// -----------------------------------------------------------------------------------------------------------
	@Override
	public void touchInject(int action, int x, int y) {
		touch(mInjCommon.getTouchEvent(action, x, y, InputDevice.SOURCE_TOUCHSCREEN));
	}

	private void touch(InputEvent ie) {
		try {
			iInputMgr.injectInputEvent(ie, InjCommon.iTouchSync);
		} catch (RemoteException e) {
			MLog.e(e.toString());
		}

		// try {
		// injectInputEvent.invoke(mInputManager, ie, 0); // 0.noSync,
		// 2.waitResult
		// } catch (Exception e) {
		// log.e(e.toString());
		// }
	}

	// -----------------------------------------------------------------------------------------------------------
	@Override
	public void wheelInject(int x, int y, int dy) {

		if (Config.DBG) MLog.d("wheel (%d, %d, delta: %d", x, y, dy);
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
			return;

		try {
			iInputMgr.injectInputEvent(ke, InjCommon.iTouchSync);
		} catch (RemoteException e) {
		}
		// try {
		// injectInputEvent.invoke(mInputManager, ke, 2); // 0.noSync,
		// 2.waitResult
		// } catch (Exception e) {
		// log.e(e.toString());
		// }
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

	// ------------------------------------------------------------------------------------------------
	// reflections
	private static IInputManager iInputMgr;
	private static ScreenOnOff mScreen;

	static boolean init() throws Exception {
		MLog.i("injector41_42.init");
		mScreen = new ScreenOnOff();
		
		// injectInputEvent = clsInputManager.getMethod("injectInputEvent", InputEvent.class, int.class);
		// Method mid_getInstance = clsInputManager.getMethod("getInstance");
		// iInputMgr = mid_getInstance.invoke(null);
		try {
			Class<?> clsInputManager = Class.forName("android.hardware.input.InputManager");
			Method mid_getInstance = clsInputManager.getMethod("getInstance");

			Field f = clsInputManager.getDeclaredField("mIm");
			f.setAccessible(true);
			iInputMgr = (IInputManager) f.get( mid_getInstance.invoke(null) );
			MLog.d("init41/2.ok");
			return true;
		} catch (Exception e) {
			MLog.e("init41/2.failed");
			return false;
		}
	}
}
