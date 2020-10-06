package com.rsupport.sony;

import android.content.Context;
import android.os.RemoteControl;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;


import com.rsupport.util.log.RLog;

import static com.rsupport.litecam.binder.Binder.TAG;

/**
 * Created by hyun on 2017. 6. 21..
 */

public class SonyManager {
    static private SonyManager instance;
    private RemoteControl mRemote;

    private boolean isConnected;


    private SonyManager() {
    }

    public static synchronized SonyManager getInstance() {
        if (instance == null) {
            instance = new SonyManager();
        }
        return instance;
    }

    public synchronized void bind(Context context) {
        try {
            if (mRemote == null) {
                mRemote = RemoteControl.getRemoteControl(context, mListener);
            }
        } catch (RemoteControl.RemoteControlException e) {
            e.printStackTrace();
        }
    }

    public boolean bindGet(Context context, Long timeOut) {
        bind(context);
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeOut && !isConnected()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return isConnected();
    }

    private RemoteControl.ICallbacks mListener = new RemoteControl.ICallbacks() {

        @Override
        public void authorizationChanged(boolean srnOK, boolean inpOK) {
            RLog.i("first : " + srnOK + " second :" + inpOK);
//			isConnected = srnOK;
        }

        @Override
        public void connectionStatus(int status) {
            RLog.i(TAG, "the connection to the service has completed: " + status);
            if (status == 0) {
                isConnected = true;

            } else {
                isConnected = false;

            }
        }

        @Override
        public void deviceInfoChanged() {
            RLog.i(TAG, "the frame buffer orientation, flip mode, etc has changed.");
        }
    };

    public synchronized void nullSonymRemote() {
        if (mRemote != null) {
            mRemote.release();
            mRemote = null;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    // touch :         injectWithPrimitive(action,         0,      x       y       x2      y2)
    // wheel :         injectWithPrimitive(action,        dy,      x       y       0        0)
    // key   :         injectWithPrimitive(action+100,    code     scan,  repeat  meta      0)
    private InjCommon inj = new InjCommon();

    public void injectWithPrimitive(int action, int i1, int i2, int i3, int i4, int i5) throws Exception { // scroll for ICS+
        if (action < 100) { // 100 = KeycodeActionOffset
            if (action == MotionEvent.ACTION_SCROLL) { // 12+
                mRemote.injectMotionEvent(inj.getWheelEvent(i2, i3, i1)); // x,y,dy

            } else if (i4 <= 0) {
                mRemote.injectMotionEvent(inj.getTouchEvent(action, i2, i3, InputDevice.SOURCE_TOUCHSCREEN)); // touch.

            } else {
                mRemote.injectMotionEvent(inj.getMultiTouchEvent(action, i2, i3, i4, i5, InputDevice.SOURCE_TOUCHSCREEN)); // touch.
            }

        } else {
            mRemote.injectKeyEvent(inj.getKeyEvent(action - 100, i1, i2, i3, i4));
        }
    }

    public synchronized void injectKeyEvent(KeyEvent ev) {
        try {
            if (mRemote != null) {
                mRemote.injectKeyEvent(ev);
            }
        } catch (RemoteControl.ServiceExitedException e) {
            e.printStackTrace();
        }
    }

    public boolean isServiceAvailable(Context context) {
        return RemoteControl.serviceAvailable(context);
    }
}
