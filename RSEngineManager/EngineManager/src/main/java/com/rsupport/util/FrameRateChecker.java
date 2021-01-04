package com.rsupport.util;

import com.rsupport.util.rslog.MLog;

/**
 * Created by kwcho on 12/30/15.
 */
public class FrameRateChecker {

    private final long FRAME_NANO_TIME = 1 * 1000 * 1000 * 1000;

    private long nextNanoTime = 0;
    private int calledCount = 0;
    private long startNanoTime = 0;
    private int totalCalledCount = 0;

    private OnFrameRateUpdateListener onFrameRateUpdateListener = null;

    public void reset(){
        nextNanoTime = 0;
        calledCount = 0;
        startNanoTime = 0;
        totalCalledCount = 0;
    }

    public void check(){
        long currentNanoTime = System.nanoTime();
        calledCount++;
        totalCalledCount++;
        if(nextNanoTime <= 0){
            startNanoTime = currentNanoTime;
            nextNanoTime = currentNanoTime + FRAME_NANO_TIME;
        }

        if(currentNanoTime >= nextNanoTime){
            nextNanoTime = currentNanoTime + FRAME_NANO_TIME;
            if(onFrameRateUpdateListener != null){
                long durationNanoTime = currentNanoTime - startNanoTime;
                float avgCount = (float)totalCalledCount / ((float)durationNanoTime / (float)FRAME_NANO_TIME);
                onFrameRateUpdateListener.update(calledCount, avgCount);
            }
            calledCount = 0;
        }
    }

    public void setOnFrameRateUpdateListener(OnFrameRateUpdateListener onFrameRateUpdateListener) {
        this.onFrameRateUpdateListener = onFrameRateUpdateListener;
    }

    public interface OnFrameRateUpdateListener{
        public void update(int frameRate, float frameRateAverage);
    }

}
