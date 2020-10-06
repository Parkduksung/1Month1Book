package com.rsupport.litecam.util;

import java.util.ArrayList;

import com.rsupport.litecam.record.RecordSet;

public class RecordFrameRate {

    /**
     * Frame rate standard time.
     */
    private static final int MILLISECOND = 1000;

    /**
     * Default frameRate Auto
     */
    private int frameRate = RecordSet.BANCHMARK_FPS_AUTO;

    /**
     * 이전 프레임과의 차이 시간
     */
    private float timeDelta = 0.0f;

    /**
     * capture 시간.
     */
    private long capturePrevTime = -1;

    /**
     * 누적 시간 기록
     */
    private long accumulatedTime = 0;

    /**
     * 모든 frame의 count
     */
    private long captureCount = 0;

    /**
     * 모든 frame의 누적 시간
     */
    private float timeElapsedAll = 0.0f;

    /**
     * 모든 frame의 평균 FPS
     */
    private float averageCaptureFPS = 0;


    /**
     * Second frame time elapsed
     */
    private float timeElapsed = 0.0f;

    /**
     * Second frame count
     */
    private int count = 0;

    /**
     * Max FPS
     */
    private float maxFPS = 0.0f;

    public RecordFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    /**
     * 1 sec 당 FPS 를 출력해야 할 경우에 사용.
     */
    private void printFPS() {
        if (!LLog.getUsed()) {
            return;
        }

        timeElapsed += timeDelta;
        if (timeElapsed >= 1.0f) { // 1 초 이상일 경우 처리.
            float fps = (float) count / timeElapsed;
            LLog.i("FPS : " + fps);

            count = 0;
            timeElapsed = 0.0f;

            if (fps > maxFPS) {
                maxFPS = fps;
                LLog.e("max fps : " + maxFPS);
            }
        }

        ++count;
    }

    /**
     * Capture 시간 계산
     */
    public void captureTime() {
        if (capturePrevTime < 0) {
            capturePrevTime = System.currentTimeMillis();
        }

        // current millis second.
        long nowTime = System.currentTimeMillis();
        ++captureCount; // all count
        timeDelta = (nowTime - capturePrevTime) * 0.001f; // timeDelta(1번생성 후 흐른 시간) 1초단위로 바꿔준다.

        printFPS(); // FPS를 출력할 경우에

        timeElapsedAll += timeDelta;

        if (timeElapsedAll > 0) {
            averageCaptureFPS = (float) captureCount / timeElapsedAll;
        }

        LLog.d("averageCaptureFPS " + averageCaptureFPS + " captureCount: " + captureCount);

        capturePrevTime = nowTime;
    }

    /**
     * NextFrame 을 가져오기 전에 sleep을 처리하여 FPS를 조정한다.
     *
     * @throws InterruptedException
     */
    public void setNextFrameSleep() throws InterruptedException {
        // FPS Auto mode
        if (frameRate == 0 && averageCaptureFPS > 0) {
            long defTime = (long) (MILLISECOND / averageCaptureFPS);
            LLog.d("defTime: " + defTime);
            defTime *= 1.1;
            long sleepTime = (long) (defTime - (timeDelta * MILLISECOND));
            LLog.d("sleepTime: " + sleepTime + " defTime: " + defTime + " timeDelta: " + (timeDelta * MILLISECOND));
            Thread.sleep(Math.abs(sleepTime));

            // FPS user select mode
        } else if (frameRate > 0) {
            long defTime = (long) (MILLISECOND / frameRate);
            long sleepTime = (long) (defTime - (timeDelta * MILLISECOND));

            LLog.d("defTime " + defTime + " now SleepTime: " + sleepTime);
            if (sleepTime > 0) {
                if (accumulatedTime > 0) {
                    accumulatedTime -= sleepTime;

                } else {
                    Thread.sleep(sleepTime);
                }

            } else {
                accumulatedTime += Math.abs(sleepTime);
                LLog.d("now SleepTime: " + sleepTime + " accumulatedTime: " + accumulatedTime);
            }
        }

    }

    /**
     * record Frame rate를 가져온다.
     *
     * @param context
     * @return
     */
    public static ArrayList<Integer> getFrameRateOptionInfo() {
        ArrayList<Integer> list = new ArrayList<Integer>();
        APICheck.getNumCores();

        for (int i = 0; i < RecordSet.DEFAULT_FRAME_RATE.length; ++i) {
            int fps = RecordSet.DEFAULT_FRAME_RATE[i];
            if (RecordSet.CPU_COUNT == RecordSet.DUAL_CORE && fps < RecordSet.BANCHMARK_DUAL_CORE_FPS_LIMITE) {
                list.add(fps);
            }

            if (RecordSet.CPU_COUNT == RecordSet.QUAD_CORE && fps < RecordSet.BANCHMARK_QUAD_CORE_FPS_LIMITE) {
                list.add(fps);
            }
        }

        return list;
    }
}
