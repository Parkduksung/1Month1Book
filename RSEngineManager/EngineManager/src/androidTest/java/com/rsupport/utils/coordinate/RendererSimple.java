package com.rsupport.utils.coordinate;

import android.util.Log;

import java.util.Random;

/**
 * Created by kwcho on 12/23/15.
 */
public class RendererSimple {
    private static final String TAG = "RsupS";
    private int renderCount = 0;
    private int count = 0;
    private int maxWorkingTimeMs = 0;
    private int minWorkingTimeMs = 0;
    private int forceWorkingTimeMs = -1;
    private OnFinishListener finishListener = null;
    private int durationSecond = 10;

    public RendererSimple() {
        minWorkingTimeMs = 0;
        maxWorkingTimeMs = 0;
    }

    public RendererSimple(int maxWorkingTimeMs) {
        this. minWorkingTimeMs = 0;
        this.maxWorkingTimeMs = maxWorkingTimeMs;
    }

    public RendererSimple(int minWorkingTimeMs, int maxWorkingTimeMs) {
        this. minWorkingTimeMs = minWorkingTimeMs;
        this.maxWorkingTimeMs = maxWorkingTimeMs;
    }

    public void reset(){
        count = 0;
        renderCount = 0;
    }

    public void setRandomWorkingTimeMs(int maxWorkingTimeMs){
        this.minWorkingTimeMs = 0;
        this.maxWorkingTimeMs = maxWorkingTimeMs;
    }

    public void setRandomWorkingTimeMs(int minWorkingTimeMs, int maxWorkingTimeMs){
        this.minWorkingTimeMs = minWorkingTimeMs;
        this.maxWorkingTimeMs = maxWorkingTimeMs;
    }

    public void setForceWorkingTimeMs(int forceWorkingTimeMs) {
        this.forceWorkingTimeMs = forceWorkingTimeMs;
    }

    public void setDurationSecond(int durationSecond) {
        this.durationSecond = durationSecond;
    }

    public void doRendering(){
        long sleepNanoTime = 0;

        if(maxWorkingTimeMs > 0){
            sleepNanoTime = (new Random().nextInt(maxWorkingTimeMs) + minWorkingTimeMs) * 1000 * 1000;
        }
        else{
            sleepNanoTime = forceWorkingTimeMs * 1000 * 1000;
        }
        if(sleepNanoTime > 1000000){
            try {
                Thread.sleep(sleepNanoTime / 1000000, 100000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        renderCount++;
    }

    public int getRenderCount() {
        return renderCount;
    }

    public void setFinishListener(OnFinishListener finishListener) {
        this.finishListener = finishListener;
    }

    public void startTick() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    count++;
                    int renderCount = getRenderCount();
                    float fps = ((float) renderCount / (float) count);
                    Log.e(TAG, "fps : " + fps + ", renderCount : " + renderCount + ", count : " + count);
                    if(count >= durationSecond){
                        if(finishListener != null){
                            finishListener.onFinish();
                        }
                        break;
                    }
                }
            }
        }).start();
    }

    public static interface OnFinishListener{
        public void onFinish();
    }
}
