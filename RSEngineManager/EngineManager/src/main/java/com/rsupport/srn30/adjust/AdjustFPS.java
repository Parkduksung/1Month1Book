package com.rsupport.srn30.adjust;


import com.rsupport.util.rslog.MLog;

/**<pre>*******************************************************************************
 *       ______   _____    __    __ _____   _____   _____    ______  _______
 *      / ___  | / ____|  / /   / // __  | / ___ | / __  |  / ___  ||___  __|
 *     / /__/ / | |____  / /   / // /  | |/ /  | |/ /  | | / /__/ /    / /
 *    / ___  |  |____  |/ /   / // /__/ // /__/ / | |  | |/ ___  |    / /
 *   / /   | |   ____| || |__/ //  ____//  ____/  | |_/ // /   | |   / /
 *  /_/    |_|  |_____/ |_____//__/    /__/       |____//_/    |_|  /_/
 *
 ********************************************************************************</pre>
 *
 * <b>Copyright (c) 2012 RSUPPORT Co., Ltd. All Rights Reserved.</b><p>
 *
 * <b>NOTICE</b> :  All information contained herein is, and remains the property
 * of RSUPPORT Company Limited and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to RSUPPORT
 * Company Limited and its suppliers and are protected by trade secret
 * or copyright law. Dissemination of this information or reproduction
 * of this material is strictly forbidden unless prior written permission
 * is obtained from RSUPPORT Company Limited.<p>
 *
 * FileName: AdjustFPS.java<br>
 * Author  : kwcho<br>
 * Date    : 2014. 8. 22.오후 12:15:04<br>
 * Purpose : FPS 를 조절 한다.<p>
 *
 * [History]<p>
 */
public class AdjustFPS implements FPSMonitor.OnChangeListener{
    private final int MAX_FPS = 30;
    private final int MIN_FPS = 18;
    private int maxFPS = 30;
    private int currentFPS = maxFPS;

    private long encoderPeriodNanos = 1000000000 / currentFPS;
    private long nextEncoderFrameTime = -1;
    private long encoderPeriodThresholdNanos = encoderPeriodNanos;

    private ThreadYield threadYield = null;
    private int fps = 23;
	/**
	 * fps 를 설정한다.
	 * @param maxFps
	 */
	public void init(int maxFps) {
        init(null, maxFps);
	}

	public void init(ThreadYield threadYield, int maxFps) {
        this.threadYield = threadYield;
        this.fps = maxFps;
        if(fps <= 0){
            MLog.w("error fps " + fps);
            fps = MAX_FPS;
        }
        this.maxFPS = fps;
        this.currentFPS = fps;
        changeFPS(fps);
	}

    private void changeFPS(int fps){
        MLog.d("changeFPS : " + fps);
        encoderPeriodNanos = 1000000000/fps;
        encoderPeriodThresholdNanos = encoderPeriodNanos;
    }

	/**
	 * FPS 시간을 Check 할때 호출한다.
	 * 반환 값이 true 이면 다음로직을 실행하면 안되며, false 일때 다음 로직을 실행 해야 한다.
     * default sleep 사용함.
	 * @return true 이면 시간안됨, false 이면 FPS 시간이 됨.
	 */
	public boolean isContinue(){
		return isContinue(true);
	}

    /**
     * FPS 시간을 Check 할때 호출한다.
     * 반환 값이 true 이면 다음로직을 실행하면 안되며, false 일때 다음 로직을 실행 해야 한다.
     * default sleep 사용함.
     *
     * @param useSleep
     * @return true 이면 시간안됨, false 이면 FPS 시간이 됨.
     */
    public boolean isContinue(boolean useSleep){
        long curNanotime = System.nanoTime();
        if(nextEncoderFrameTime <= 0){
            nextEncoderFrameTime = curNanotime;
        }
        if(curNanotime >= nextEncoderFrameTime - encoderPeriodThresholdNanos){
            while(true) {
                nextEncoderFrameTime += encoderPeriodNanos;
                if (nextEncoderFrameTime > curNanotime){
                    break;
                }
            }
            if(threadYield != null && useSleep == true){
                // sleepTime 이 없으면 cpu 를 많이 사용하므로 60FPS 기준으로 1FPS 시간만큰 sleep 을 준다.
                try { threadYield.yield(); } catch (Exception e) {	}
            }
            return false;
        }

        if(useSleep == true){
            if(threadYield != null){
                threadYield.none();
            }
            try {
                long sleepTimeMs = encoderPeriodNanos / 1000000 / 2;
                Thread.sleep(sleepTimeMs, 999999);
            }
            catch (InterruptedException e) {
            }
        }
        return true;
    }

    /**
	 * 종료 할때 호출.
	 */
	public void onDestroy(){
	}

    @Override
    public void onUpperEvent() {
        if(currentFPS < maxFPS){
            changeFPS(++currentFPS);
        }
    }

    @Override
    public void onLowerEvent() {
        if(currentFPS> MIN_FPS){
            changeFPS(--currentFPS);
        }
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

    public static class ThreadYield {
        private boolean isYield = false;
        private int currentCount = 0;
        private int yieldTimeMs = 15;
        private int yieldCount = 2;

        public ThreadYield(int yieldCount, int yieldTimeMs){
            this.yieldCount = yieldCount;
            this.yieldTimeMs= yieldTimeMs;
        }

        public void none(){
            isYield = false;
            currentCount = 0;
        }

        public void yield() throws InterruptedException{
            if(isYield == true){
                currentCount++;
                if(currentCount >= yieldCount){
                    long sleepTimeMs = yieldTimeMs > 1 ? yieldTimeMs - 1 : yieldTimeMs;
                    Thread.sleep(sleepTimeMs, 999999);
                    currentCount = 0;
                }
            }
            isYield = true;
        }
    }
}
