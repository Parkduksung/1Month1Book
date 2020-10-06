/**
 * Gallery of tutorial
 */
package com.rsupport.mobile.agent.ui.tutorial;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;


public class TutorialGallery extends Gallery {

    public static final int TYPE_KEY = 0;
    public static final int TYPE_MOTION = 1;
    private int type = TYPE_MOTION;

    public TutorialGallery(Context context) {
        this(context, null);
        setSoundEffectsEnabled(false);
    }

    public TutorialGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSoundEffectsEnabled(false);
    }

    public TutorialGallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setSoundEffectsEnabled(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (type == TYPE_MOTION) {
            return super.onTouchEvent(event);
        } else {
            return true;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (type == TYPE_MOTION) {
            return onTouchEvent(ev);
        }
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        if (type == TYPE_MOTION) {
            int keyCode;
            if (e2.getX() > e1.getX()) {
                keyCode = KeyEvent.KEYCODE_DPAD_LEFT;
            } else {
                keyCode = KeyEvent.KEYCODE_DPAD_RIGHT;
            }
            onKeyDown(keyCode, null);
            return true;
        } else {
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    public void setScrollType(int type) {
        this.type = type;
    }

    public int getScrollType() {
        return type;
    }

}
