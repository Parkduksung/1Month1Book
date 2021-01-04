package com.rsupport.srn30.adjust;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Choreographer;
import android.view.WindowManager;

import com.rsupport.util.rslog.MLog;

import java.util.concurrent.Semaphore;

/**
 * Created by kwcho on 12/23/15.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ChoreographerHelper implements FPSMonitor.OnChangeListener{
    private final int MIN_FPS = 8;
    private int maxFPS = 30;
    private int currentFPS = maxFPS;

    private long refreshPeriodNanos = 1000000000 / 60;
    private long encoderPeriodNanos = 1000000000 / currentFPS;

    private long nextEncoderFrameTime = -1;
    private long refreshOvershootNanos = refreshPeriodNanos * 3 / 4;
    private long encoderPeriodThresholdNanos = encoderPeriodNanos;
    private boolean isDroppingFrames = false;
    private boolean isAlive = false;
    private Context context = null;
    private Choreographer choreographer = null;
    private Handler handler = null;
    private boolean isRelease = false;
    public ChoreographerHelper(Context context){
        this.context = context;
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        refreshPeriodNanos = (long)(1000000000 / windowManager.getDefaultDisplay().getRefreshRate());
    }

    public boolean initialized(final int fps, Handler handler){
        isRelease = false;
        isAlive = false;
        if(handler == null || fps <= 0){
            MLog.w("handler is " + handler + " or fps " + fps);
            return false;
        }
        this.maxFPS = fps;
        this.currentFPS = fps;
        this.handler = handler;
        if(Looper.myLooper() == handler.getLooper()){
            isAlive = init(fps);
            return isAlive;
        }
        else{
            final Semaphore semaphore = new Semaphore(0);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    isAlive = init(fps);
                    semaphore.release();
                }
            });
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                MLog.e(e);
                return false;
            }
        }
        return isAlive;
    }

    @Override
    public void onUpperEvent() {
        if(currentFPS < maxFPS){
            currentFPS++;
            changeFPS(currentFPS);
        }
    }

    @Override
    public void onLowerEvent() {
        if(currentFPS> MIN_FPS){
            changeFPS(--currentFPS);
        }
    }

    private boolean init(int fps){
        choreographer = Choreographer.getInstance();
        MLog.i("init choreographer("+Thread.currentThread().getId()+") : " + choreographer.hashCode());
        changeFPS(fps);
        choreographer.postFrameCallback(frameCallback);
        return true;
    }

    @Override
    public int onUpperEvent(int increase) {
        if(currentFPS + increase < maxFPS){
            changeFPS(currentFPS += increase);
        } else if (currentFPS < maxFPS) {
            changeFPS(currentFPS = maxFPS);
        }

        return currentFPS;
    }

    @Override
    public int onLowerEvent(int decrease) {
        if(currentFPS - decrease > MIN_FPS){
            changeFPS(currentFPS -= 10);
        } else if (currentFPS > MIN_FPS) {
            changeFPS(currentFPS = MIN_FPS);
        }

        return currentFPS;
    }

    private void changeFPS(int fps){
        encoderPeriodNanos = 1000000000/fps;
        refreshOvershootNanos = refreshPeriodNanos * 3 / 4;
        encoderPeriodThresholdNanos = encoderPeriodNanos;
    }

    public void release(){
        isRelease = true;
        MLog.i("release choreographer("+Thread.currentThread().getId()+") : " + choreographer);
        if(choreographer != null){
            choreographer.removeFrameCallback(frameCallback);
        }
    }

    public boolean isRendering(){
        if(Looper.myLooper() != handler.getLooper()){
            throw new RuntimeException("looper is not same.");
        }
        long curNanotime = System.nanoTime();
        if(nextEncoderFrameTime < 0){
            nextEncoderFrameTime = curNanotime;
        }
        if(curNanotime >= nextEncoderFrameTime - encoderPeriodThresholdNanos){
            while(true) {
                nextEncoderFrameTime += encoderPeriodNanos;
                if (nextEncoderFrameTime > curNanotime){
                    break;
                }
            }
            return true;
        }
        return false;
    }

    private Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            if (isDroppingFrames) {
                if (System.nanoTime() - frameTimeNanos >= refreshOvershootNanos) {
                    // Skipping late refresh frame
                }
                // next frameTimeNs >= mNextEncoderFrameTime.
                else if (refreshPeriodNanos + frameTimeNanos >= nextEncoderFrameTime) {
                    isDroppingFrames = false;
                }
            } else if (frameTimeNanos >= nextEncoderFrameTime) {
                isDroppingFrames = true;
                while (nextEncoderFrameTime <= frameTimeNanos) {
                    nextEncoderFrameTime += encoderPeriodNanos;
                }
            }

            if(isRelease == false){
                choreographer.postFrameCallback(this);
            }
        }
    };
}
