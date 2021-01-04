package com.rsupport.rsperm;

import android.text.TextUtils;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.rsupport.mobizen.injection.InjectionListener;
import com.rsupport.mobizen.injection.InjectionManager;
import com.rsupport.mobizen.injection.knox.KnoxInjection;
import com.rsupport.util.rslog.MLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class KnoxPermission extends ProjectionPermission implements InjectionListener {

    private final static int rcpMonkeyTouch = 24;
    private final static int rcpMonkeyWheel = 25;
    private final static int rcpMonkeyKeypad = 26;
    private int keyEventMetaState = 0;
    private static InjCommon inj = new InjCommon();

    private KnoxInjection injection = null;

    @Override
    protected void onBindSuccess(String key) {
        if(!TextUtils.isEmpty(key)){
            injection = (KnoxInjection) new InjectionManager().createInjection(getContext(), InjectionManager.PermissionType.KNOX, this);
            injection.requestPermission(key);
        }else {
            MLog.e("Activation Key Empty");
        }
    }

    @Override
    public String exec(String cmd) throws Exception {
        return null;
    }

    @Override
    public int getType() {
        return IEnginePermission.BIND_TYPE_KNOX_PROJECTION;
    }

    @Override
    public void inject(byte[] data, int offset, int length) throws Exception {
        if(injection == null || !injection.hasPermission()) {
            return;
        }

        int msgid = data[offset]&0xFF;
        ByteBuffer parser = ByteBuffer.wrap(data, 0, data.length).order(ByteOrder.LITTLE_ENDIAN); // 5 : skip rcpMsg.datasize
        parser.position(10);

        switch (msgid){
            case rcpMonkeyTouch:
                // touch :         injectWithPrimitive(action,         0,      x       y       x2      y2)
                InjCommon.rcpMonkeyTouch2Msg touch = InjCommon.rcpMonkeyTouch2Msg.obtain(parser);
                injectWithPrimitive(touch.action, 0, touch.x, touch.y, touch.x2, touch.y2);
                break;
            case rcpMonkeyWheel:
                // wheel :         injectWithPrimitive(action,        dy,      x       y       0        0)
                InjCommon.rcpMonkeyWheelMsg wheel = InjCommon.rcpMonkeyWheelMsg.obtain(parser);
                injectWithPrimitive(MotionEvent.ACTION_SCROLL, wheel.dy, wheel.x, wheel.y, 0, 0);
                break;

            case rcpMonkeyKeypad:
                // key   :         injectWithPrimitive(action+100,    code     scan,  repeat  meta      0)
                InjCommon.rcpMonkeyKeypadMsg keybd = InjCommon.rcpMonkeyKeypadMsg.obtain(parser);
                for (int i=0; i<keybd.count; ++i) {
                    if(keybd.getKeycode(i) == KeyEvent.KEYCODE_CTRL_LEFT){
                        keyEventMetaState = keybd.getAction(i) == KeyEvent.ACTION_DOWN ? KeyEvent.META_CTRL_ON|KeyEvent.META_CTRL_LEFT_ON : 0;
                    } else if(keybd.getKeycode(i) == KeyEvent.KEYCODE_SHIFT_LEFT){
                        keyEventMetaState = keybd.getAction(i) == KeyEvent.ACTION_DOWN ? KeyEvent.META_SHIFT_ON|KeyEvent.META_SHIFT_LEFT_ON : 0;
                    } else if(keybd.getKeycode(i) == KeyEvent.KEYCODE_SHIFT_RIGHT){
                        keyEventMetaState = keybd.getAction(i) == KeyEvent.ACTION_DOWN ? KeyEvent.META_SHIFT_ON|KeyEvent.META_SHIFT_RIGHT_ON : 0;
                    } else if(keybd.getKeycode(i) == KeyEvent.KEYCODE_ALT_LEFT){
                        keyEventMetaState = keybd.getAction(i) == KeyEvent.ACTION_DOWN ? KeyEvent.META_ALT_ON|KeyEvent.META_ALT_LEFT_ON : 0;
                    }
                    injectWithPrimitive(keybd.getAction(i) + 100, keybd.getKeycode(i), 0, 0, keyEventMetaState, 0);
                }
                break;
            default:
                MLog.e("invalid input msg->id: %d", msgid);
                break;
        }
    }

    private void injectWithPrimitive(int action, int i1, int i2, int i3, int i4, int i5) throws Exception { // scroll for ICS+
        if (action < 100) { // 100 = KeycodeActionOffset
            // 12+
            if (action == MotionEvent.ACTION_SCROLL){
                MotionEvent me = inj.getWheelEvent(i2, i3, i1);
                injection.injectMotionEvent(me, true);
                me.recycle();
            }
            else if (i4 <= 0 || i4 == 32768){
                MotionEvent me = inj.getTouchEvent(action, i2,i3, InputDevice.SOURCE_TOUCHSCREEN);
                injection.injectMotionEvent(me, true);
                me.recycle();
            }
            else{
                MotionEvent[] me = inj.getMultiTouchEvent(action, i2,i3,i4,i5,InputDevice.SOURCE_TOUCHSCREEN);
                for (MotionEvent event : me) {
                    injection.injectMotionEvent(event, true);
                    event.recycle();
                }
            }
        }
        else {
            injection.injectKeyEvent(inj.getKeyEvent(action-100, i1, i2, i3, i4), true);
        }
    }

    @Override
    public void onResultRequestPermission(int result) {
        if(result == InjectionListener.Companion.getPERMISSION_RESULT_OK()){
            MLog.i("Knox Activation OK");
        } else {
            MLog.i("Knox Activation Error");
        }
    }

}
