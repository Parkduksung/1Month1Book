package com.rsupport.jarinput;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class Wheel14plus {

	private PointerProperties props[] = { new PointerProperties() }; // SDK14+
	private PointerCoords coords[] = { new PointerCoords() }; // SDK9+
	
	MotionEvent getWheelEvent(int x, int y, int dy) {
	long tick = SystemClock.uptimeMillis();

	props[0].id = 0;
	props[0].toolType = 3;
	coords[0].setAxisValue(0, x);
	coords[0].setAxisValue(1, y); // fix at 2013/04/15
	coords[0].setAxisValue(9, dy);
	return MotionEvent.obtain(tick, tick, MotionEvent.ACTION_SCROLL, // sdk12+
			1, props, coords, 0, 0, 0f, 0f, 0, 0, InputDevice.SOURCE_MOUSE, 0);
	}
}

