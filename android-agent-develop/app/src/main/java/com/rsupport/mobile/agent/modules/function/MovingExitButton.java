package com.rsupport.mobile.agent.modules.function;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.rsupport.mobile.agent.R;

import com.rsupport.mobile.agent.service.AgentMainService;
import com.rsupport.util.log.RLog;

/**
 * Created by Hyungu-PC on 2015-06-24.
 */
public class MovingExitButton {

    private Context m_Context;
    private Handler m_Handler;
    private WindowManager.LayoutParams mParams;
    private WindowManager floatingWindowManager;
    private View view;
    private ImageView exitButton;
    private float START_X;
    private float START_Y;
    private int PREV_X;
    private int PREV_Y;
    private int MAX_X = -1;
    private int MAX_Y = -1;

    private final int MIN_MOVE_POINT = 15;
    private final int MIN_MOVE_TIME = 300;

    public MovingExitButton(Context context, Handler handler) {
        m_Context = context;
        m_Handler = handler;

        initView();
    }

    private WindowManager.LayoutParams getFloatingParam() {
        mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getTopLayerType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.LEFT | Gravity.TOP;

        return mParams;
    }

    private int getTopLayerType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            return WindowManager.LayoutParams.TYPE_PHONE;  //softkey모델에서 softkey 영역 포함.(TYPE_SYSTEM_ERROR로 설정시 소프트키 영역 그리기 안됨.)
        }
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) m_Context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.exit_button_layout, null);
        exitButton = (ImageView) view.findViewById(R.id.exit_button);

        floatingWindowManager = (WindowManager) m_Context.getSystemService(Context.WINDOW_SERVICE);
        floatingWindowManager.addView(view, getFloatingParam());
        view.setOnClickListener(ButtonClickListener);
        view.setOnTouchListener(mViewTouchListener);
        RLog.i("MAX_Y : " + MAX_Y);
        setMaxPosition();
        mParams.y = MAX_Y / 5;
        floatingWindowManager.updateViewLayout(view, mParams);
    }

    private View.OnClickListener ButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            m_Handler.sendEmptyMessage(AgentMainService.AGENT_SERVICE_SHOW_EXIT_ACTITY);
        }
    };


    private long touchStartTime = 0;
    private View.OnTouchListener mViewTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int x, y;

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    setMaxPosition();
                    START_X = event.getRawX();
                    START_Y = event.getRawY();
                    touchStartTime = System.currentTimeMillis();
                    PREV_X = mParams.x;
                    PREV_Y = mParams.y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    x = (int) (event.getRawX() - START_X);
                    y = (int) (event.getRawY() - START_Y);
                    mParams.x = PREV_X + x;
                    mParams.y = PREV_Y + y;
                    floatingWindowManager.updateViewLayout(view, mParams);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    x = (int) (event.getRawX() - START_X);
                    y = (int) (event.getRawY() - START_Y);
                    setAutoMove();
                    break;

            }
            if (System.currentTimeMillis() - touchStartTime < MIN_MOVE_TIME
                    && getMoveDistance(PREV_X, PREV_Y, mParams.x, mParams.y) < MIN_MOVE_POINT) {
                return false;
            }
            return true;
        }
    };


    private static int getMoveDistance(int fromX, int fromY, int toX, int toY) {
        int ret = 0;

        int X = Math.abs(toX - fromX);
        int Y = Math.abs(toY - fromY);
        ret = (int) Math.sqrt(X ^ 2 + Y ^ 2);
//		Logger.i(className, "distance :: " + ret);
        return ret;
    }


    private void setAutoMove() {


//        if (mParams.x >= MAX_X) {
//            return;
//        }
        autoMove(mParams.x, 0);

//        if (mParams.x > (MAX_X/2)) {
//            if (mParams.x >= MAX_X) {
//                return;
//            }
//            autoMove(mParams.x, MAX_X);
//        } else {L
//
//        }
//        if (mParams.x > (MAX_X/2)) {
//            if (mParams.y > (MAX_Y/2)) {
//                autoMove(mParams.x, MAX_X , mParams.y, MAX_Y);
//            } else {
//                autoMove(mParams.x, MAX_X, mParams.y, 0);
//            }
//
//        } else {
//            if (mParams.y > (MAX_Y/2)) {
//                autoMove(mParams.x, 0 , mParams.y, MAX_Y);
//            } else {
//                autoMove(mParams.x, 0 , mParams.y, 0);
//            }
//
//        }
    }

    private void autoMove(final int fromX, final int toX) {
        new Thread() {
            public void run() {
                int start = toX - fromX;
                if (start > 0) {
                    start = fromX;
                }
                for (int i = start; i < toX; i = i + 2) {
                    if (i < 0) {
                        movehandler.sendEmptyMessage(Math.abs(i));
                    } else {
                        movehandler.sendEmptyMessage(i);
                    }

//                    try {
//                        sleep(1);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        }.start();
    }

    private Handler movehandler = new Handler() {
        public void handleMessage(Message msg) {
            if (view == null || !view.isShown()) return;
            mParams.x = msg.what;
            view.setDrawingCacheEnabled(false);
            floatingWindowManager.updateViewLayout(view, mParams);
            view.setDrawingCacheEnabled(true);

        }
    };

    private void setMaxPosition() {
        if (view == null) return;
        DisplayMetrics matrix = new DisplayMetrics();
        floatingWindowManager.getDefaultDisplay().getMetrics(matrix);

        MAX_X = matrix.widthPixels - view.getWidth();
        MAX_Y = matrix.heightPixels - view.getHeight();
    }

    public void hideView() {
        if (view == null || !view.isShown()) return;

        floatingWindowManager.removeViewImmediate(view);
    }

}
