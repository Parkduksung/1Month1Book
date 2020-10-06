package com.rsupport.media.mediaprojection.record.adjust;

public class AdjustFps {
    private final int SECOND = 1000;

    private long delayTimeMs = SECOND / 30;
    private long startTime = 0;
    private long frameCount = 0;

    /**
     * fps 를 설정한다.
     */
    public void setFps(int maxFps) {
        this.delayTimeMs = SECOND / maxFps;
        startTime = 0;
        frameCount = 0;
    }


    public synchronized boolean isDelayed() {
        if (isFirstFrame()) {
            return false;
        }

        long nextTime = nextTime();
        if (System.currentTimeMillis() > nextTime()) {
            frameCount++;
            return false;
        }
        sleep(nextTime);
        return true;
    }

    private long nextTime() {
        return startTime + ((frameCount + 1) * delayTimeMs);
    }

    private boolean isFirstFrame() {
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private void sleep(long nextTime) {
        try {
            long sleepTime = nextTime - System.currentTimeMillis();
            if (sleepTime > 0) {
                Thread.sleep(sleepTime);
            }
        } catch (Exception e) {
        }
    }
}
