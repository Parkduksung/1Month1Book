/*
 * @ author kyeom
 * @ date   2010.09.01
 *
 *  Topmost screen layer
 */

package com.rsupport.mobile.agent.modules.function;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.rsupport.mobile.agent.R;
import com.rsupport.mobile.agent.utils.DisplaySize;
import com.rsupport.mobile.agent.utils.NavigationBar;
import com.rsupport.mobile.agent.utils.TouchPointConverter;
import com.rsupport.mobile.agent.utils.WindowDisplay;

import config.EngineConfigSetting;

import com.rsupport.mobile.agent.constant.GlobalStatic;
import com.rsupport.util.log.RLog;


public class TopmostText {
    protected static WindowManager windowManager;

    private Context context;
    protected TextView textView;
    private String windowText;
    private int textColor;
    private DisplaySize displaySize;
    private NavigationBar navigationBar;

    public TopmostText(Context context) {
        this(context, context.getResources().getString(R.string.topmost_text));
    }

    public TopmostText(Context context, String text) {
        this(context, text, Color.RED);
    }

    public TopmostText(Context context, String text, int textColor) {
        this.context = context;
        windowText = text;
        this.textColor = textColor;

        createWindow();
        initDrawable();
        WindowDisplay windowDisplay = new WindowDisplay(windowManager);
        displaySize = windowDisplay;
        navigationBar = windowDisplay;
        touchPointConverter = new TouchPointConverter(displaySize, navigationBar);
    }

    private void createWindow() {
        windowManager = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
    }

    private void initDrawable() {
        imgLaserPoint = context.getResources().getDrawable(R.drawable.mouse_curser_point);
        imgClick = context.getResources().getDrawable(R.drawable.mouse_cursor_click);
        imgLeft = context.getResources().getDrawable(R.drawable.mouse_cursor_left);
        imgRight = context.getResources().getDrawable(R.drawable.mouse_cursor_right);
        imgUp = context.getResources().getDrawable(R.drawable.mouse_cursor_up);
        imgDown = context.getResources().getDrawable(R.drawable.mouse_cursor_down);
    }

    public static int Dip2Pixel(Context context, float dip) {
        int ret = 0;
        final float scale = context.getResources().getDisplayMetrics().density;
        float px = (int) (dip * scale + 0.5f);
        ret = (int) px;
        return ret;
    }

    protected View getView() {
        View ret = null;
        if (textView == null) {
            textView = new TextView(context);
            textView.setText(getText());
            textView.setTextSize(17);
            if (EngineConfigSetting.isSamsungPrinter()) {
                textView.setTextSize(34);
            }
            textView.setTextColor(getTextColor());
            textView.setGravity(Gravity.RIGHT);
            textView.setPadding(0, 0, 20, 0);

        }
        ret = textView;
        return ret;
    }

    public static final int REMOTESTATE_CLICK = 0;
    public static final int REMOTESTATE_DRAG = 1;
    public static final int REMOTESTATE_LASER = 2;
    public static final int REMOTESTATE_RELEASED = 3;
    public static final int REMOTESTATE_VISIBLE = 4;
    public static final int REMOTESTATE_INVISIBLE = 5;
    //arrow 0 원 1

    public void drawRemoteStateImage(int type, int x, int y) {
        RLog.i("drawClickImage x : " + x + " y : " + y);
        setDrawInfo(type, x, y);
        invalidateHandler.sendEmptyMessage(0);
    }

    public void onMouseUp() {
        clearCanvasHandler.sendEmptyMessageDelayed(0, 150);
    }

    private Handler invalidateHandler = new Handler() {
        public void handleMessage(Message msg) {
            cursorView.setVisibility(View.VISIBLE);
            procDraw();
        }
    };

    private Handler clearCanvasHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (!cursorView.isShown()) return;
            cursorView.setVisibility(View.GONE);
            windowManager.updateViewLayout(cursorView, cursorparams);
        }
    };

    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int TOP = 3;
    public static final int BOTTOM = 4;

    int type = -1;
    protected int x = 0, y = 0, oldX = 0, oldY = 0;
    private int originX = 0;
    int mDirection = 0;

    protected Drawable imgLeft, imgRight, imgUp, imgDown, imgClick, imgLaserPoint;
    private TouchPointConverter touchPointConverter;

    public void setDrawInfo(int type, int x, int y) {
        cursorparams.gravity = getGravity();
        this.type = type;
        originX = x;
        this.x = touchPointConverter.convertXIfNavigationLeft(x);
        this.y = y;
    }

    private void procDraw() {
        switch (type) {
            case REMOTESTATE_CLICK:
                oldX = originX;
                oldY = y;
                drawRemoteClick();
                break;
            case REMOTESTATE_DRAG:
                drawRemoteDrag();
                break;
            case REMOTESTATE_LASER:
                drawRemoteLaser();
                break;
            default:
                drawClear();
                break;
        }
    }

    private void drawClear() {
        RLog.i("drawRemote_Clear");
    }

    protected void drawRemoteClick() {
        cursorView.setBackgroundDrawable(imgClick);
        cursorparams.x = x - imgClick.getIntrinsicWidth() / 2;
        cursorparams.y = y - imgClick.getIntrinsicHeight() / 2;
        windowManager.updateViewLayout(cursorView, cursorparams);
    }


    private int convertDrawableWidthLaserOffset(Drawable drawable) {
        if (navigationBar.getNavigationDirection() == NavigationBar.LEFT) {
            return 0;
        }
        return drawable.getIntrinsicWidth();
    }

    private void drawRemoteDrag() {
        procDragMouse();
    }

    private void drawRemoteLaser() {
        cursorView.setBackgroundDrawable(imgLaserPoint);
        cursorparams.x = x - convertDrawableWidthLaserOffset(imgLaserPoint);
        cursorparams.y = y - imgLaserPoint.getIntrinsicHeight();
        windowManager.updateViewLayout(cursorView, cursorparams);
    }

    private void procDragMouse() {
        mDirection = getDirection();
        Drawable drawable = getDirectionImage();
        cursorView.setBackgroundDrawable(drawable);
        cursorparams.x = x - drawable.getIntrinsicWidth() / 2;
        cursorparams.y = y - drawable.getIntrinsicHeight() / 2;
        windowManager.updateViewLayout(cursorView, cursorparams);
        oldX = originX;
        oldY = y;
        RLog.i("drawRemote_Drag");
    }

    private Drawable getDirectionImage() { // 방향을 주고
        Drawable cursor = null;
        switch (mDirection) {
            case LEFT:
                cursor = imgLeft;
                break;
            case RIGHT:
                cursor = imgRight;
                break;
            case TOP:
                cursor = imgUp;
                break;
            case BOTTOM:
                cursor = imgDown;
                break;
        }
        return cursor;
    }

    private int getDirection() { // 방향을 구하고
        int gapX = oldX - originX;
        int gapY = oldY - y;

        if (gapX == 0 && gapY == 0) {
            RLog.i("drawRemote_mDirection : " + mDirection);
            return mDirection;
        }

        int direction = 0;
        if (Math.abs(gapX) >= Math.abs(gapY)) {
            if (gapX > 0) direction = LEFT;
            else direction = RIGHT;
        } else {
            if (gapY > 0) direction = TOP;
            else direction = BOTTOM;
        }
        if (direction == 0) direction = mDirection;

        RLog.i("drawRemote_Direction : " + direction);
        return direction;
    }

    private static WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.FILL_PARENT;
        params.gravity = Gravity.BOTTOM;
        //params.x = 20;
        if (GlobalStatic.IS_SAMSUNGPRINTER_BUILD) {
            params.y = 5;
        } else {
            params.y = 20;
        }

        params.format = PixelFormat.TRANSLUCENT;
        params.type = getTopLayerType();
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        return params;
    }

    protected WindowManager.LayoutParams cursorparams;

    protected WindowManager.LayoutParams getMainLayoutParams() {
        cursorparams = new WindowManager.LayoutParams();
        cursorparams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        cursorparams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        cursorparams.gravity = getGravity();
        cursorparams.type = getTopLayerType();
        cursorparams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        cursorparams.format = PixelFormat.TRANSLUCENT;
        return cursorparams;
    }

    private int getGravity() {
        if (navigationBar.getNavigationDirection() == NavigationBar.LEFT) {
            return Gravity.TOP | Gravity.RIGHT;
        }
        return Gravity.TOP | Gravity.LEFT;
    }

    private static int getTopLayerType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            return WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY | WindowManager.LayoutParams.TYPE_PHONE;  //softkey모델에서 softkey 영역 포함.(TYPE_SYSTEM_ERROR로 설정시 소프트키 영역 그리기 안됨.)
        }
    }

    protected boolean showTopmostView() {
        if (windowManager != null) {

            if (!getView().isShown()) {

                windowManager.addView(getView(), getLayoutParams());
            }
            if (!addCursorView().isShown())
                windowManager.addView(addCursorView(), getMainLayoutParams());

            return true;
        } else {
            return false;
        }
    }

    protected ImageView cursorView;

    protected ImageView addCursorView() {
        if (cursorView == null) {
            cursorView = new ImageView(context);
            cursorView.setVisibility(View.GONE);
        }
        return cursorView;
    }

    public static boolean isHide = false;

    protected void hideTopmostView() {
        stopWindowHandler.sendEmptyMessage(0);
        isHide = true;
    }

    private Handler stopWindowHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (textView != null) windowManager.removeView(textView);
            if (cursorView != null) windowManager.removeView(cursorView);
        }
    };

    public void inVisiblTopmostView(boolean control) {
        if (control) {
            inVisiblTopmostView.sendEmptyMessage(0);
            RLog.i("TopmostView_GONE");
        } else {
            inVisiblTopmostView.sendEmptyMessage(1);
            RLog.i("TopmostView_VISIBLE");
        }
    }

    private Handler inVisiblTopmostView = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                //getViewExist().setVisibility(View.GONE);
                textView.setTextColor(Color.TRANSPARENT);
            } else {
                //getViewExist().setVisibility(View.VISIBLE);
                textView.setTextColor(getTextColor());
            }
        }
    };

    /**
     * Android 는 화면출력시 Double buffering 방법을 이용한다.
     * 즉 화면의 변화가 일어날때 background 에서 먼저 그린 후 front 로 가져오는데,
     * 이때 화면의 변화량이 아주 적으면 background 에서 그린 화면을 front 로 가져오지 못하는 현상이 발생한다.
     * RemoteCall 에서 key event로 제어할때 이런현상이 두드러지며, 이를 해결하기 위해서
     * windowManager에 연속적인 화면 event 를 발생시킨다.
     */
    protected void startTextHandleThread() {
        isHide = false;
        new Thread(new Runnable() {
            public void run() {
                while (!isHide) {
                    try {
                        textHandler.sendEmptyMessage(0);
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        RLog.e(Log.getStackTraceString(e));
                    }
                }
            }
        }).start();
    }

    private final Handler textHandler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                if (textView != null && windowManager != null && !isHide) {
                    textView.setText(getText());
                    //windowManager.updateViewLayout(getView(), getLayoutParams());
                }
            } catch (Exception e) {
                RLog.e(Log.getStackTraceString(e));
            }
        }
    };

    public boolean show() {

        return showTopmostView();
    }

    public void showMovingText() {
        if (show()) {
            startTextHandleThread();
            //textHandler.sendEmptyMessage(0);
        }
    }

    public void hide() {
        hideTopmostView();
    }

    public void setText(String text) {
        windowText = text;
        textHandler.sendEmptyMessage(0);
    }

    public String getText() {
        return windowText;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getTextColor() {
        return textColor;
    }
}