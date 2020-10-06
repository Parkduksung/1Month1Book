package com.rsupport.sony;

import android.annotation.TargetApi;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;

// 14+
@TargetApi(14)
public class InjCommon {
    static boolean dbg = true;
    static String TAG = "Sony.in";

    public static int iTouchSync = 0;
    public static boolean bTouchSync = false;
    Wheel14plus wheel14;

    public InjCommon() {
        if (android.os.Build.VERSION.SDK_INT >= 14)
            wheel14 = new Wheel14plus();
    }

    long touchDownTime = -1;

    MotionEvent getTouchEvent(int action, int x, int y, int source) {
        if (dbg) Log.d(TAG, String.format("jexe.touch: %d, (%d, %d)", action, x, y));

        long tick = SystemClock.uptimeMillis();
        if (action == MotionEvent.ACTION_DOWN)
            touchDownTime = tick;

        MotionEvent me = MotionEvent.obtain(touchDownTime, tick, action, (float) x, (float) y, 0);
        me.setSource(source); // InputDevice.SOURCE_TOUCHSCREEN); // sdk12.

        return me;
    }

    MotionEvent getWheelEvent(int x, int y, int dy) {
        if (dbg) Log.d(TAG, String.format("jexe.wheel: (%d, %d), %d", x, y, dy));
        return wheel14.getWheelEvent(x, y, dy);
    }
    ////////////////////////////////////////////////////////////////////////////////////

    private long kbdDownTime = -1;

    public KeyEvent getKeyEvent(int action, int code, int scancode, int repeatCnt, int metaState) {

        long tick = SystemClock.uptimeMillis();
        if (action == KeyEvent.ACTION_DOWN && repeatCnt == 0)
            kbdDownTime = tick;
        return new KeyEvent(kbdDownTime, tick, action, code, repeatCnt, metaState, -1, scancode, 0, InputDevice.SOURCE_KEYBOARD);
    }

    final int[] mtProps = {0, 1};
    PointerCoords[] mtCoords = {new PointerCoords(), new PointerCoords()};

    @SuppressWarnings("deprecation")
    public MotionEvent getMultiTouchEvent(int action, int x, int y, int x2, int y2, int source) {

        mtCoords[0].x = x;
        mtCoords[0].y = y;
        mtCoords[0].pressure = 1;
        mtCoords[0].size = 1;

        mtCoords[1].x = x2;
        mtCoords[1].y = y2;
        mtCoords[1].pressure = 1;
        mtCoords[1].size = 1;

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                return MotionEvent.obtain(touchDownTime, SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_MOVE, 2, mtProps, mtCoords,
                        0, 1f, 1f, 0, 0, source, 0);

            case MotionEvent.ACTION_DOWN:
                touchDownTime = SystemClock.uptimeMillis();
                return MotionEvent.obtain(touchDownTime, touchDownTime,
                        MotionEvent.ACTION_DOWN, 1, mtProps, mtCoords,
                        0, 1f, 1f, 0, 0, source, 0);

            case 261://MotionEvent.ACTION_POINTER_2_DOWN:
                touchDownTime = SystemClock.uptimeMillis();
                return MotionEvent.obtain(touchDownTime, touchDownTime,
                        261, 2, mtProps, mtCoords,
                        0, 1f, 1f, 0, 0, source, 0);

            case 262://MotionEvent.ACTION_POINTER_2_UP:
                return MotionEvent.obtain(touchDownTime, SystemClock.uptimeMillis(),
                        262, 2, mtProps, mtCoords,
                        0, 1f, 1f, 0, 0, source, 0);

            case MotionEvent.ACTION_UP:
                return MotionEvent.obtain(touchDownTime, SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_UP, 1, mtProps, mtCoords,
                        0, 1f, 1f, 0, 0, source, 0);
        }
        return null;
    }

    public static void setFlags(int i1, int i2, int i3, int i4, int i5) {
        iTouchSync = i1;
        bTouchSync = (i1 != 0);
    }

}
