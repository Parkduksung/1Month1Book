package com.rsupport.media.mediaprojection.record.adjust;


import com.rsupport.util.log.RLog;

/**
 * Created by taehwan on 5/19/15.
 * <p/>
 * Bitrate 모니터
 */
public class BitRateMonitor {

    /**
     * 네트워크에 보내지는 frame 수
     */
    private final int MAX_CHECK_SEND_FRAME = 10;

    /**
     * Bitrate를 낮추는 기준 FPS
     */
    private final int LOWER_FPS = 1000 / 20;

    /**
     * Bitrate를 높이는 기준 FPS
     */
    private final int UPPER_FPS = 1000 / 29;

    private int lowerSendDuration = LOWER_FPS;
    private int upperSendDuration = UPPER_FPS;


    /**
     * 실제 FPS 조절에 대한 callback
     */
    private OnBitRateChangeListener listener;

    private int sendFrameCount = 0;
    private long startTime = 0;
    private long endTime = 0;
    private long totalDuration = 0;

    public void setFrameRate(int frameRate) {
        this.lowerSendDuration = 1000 / (int) (frameRate * 0.65);
        this.upperSendDuration = 1000 / (int) (frameRate * 0.95);
    }

    public void setOnBitrateChangeListener(OnBitRateChangeListener listener) {
        this.listener = listener;
    }

    /**
     * 진행중 걸린 시간.
     *
     * @return {@link #endTime} - {@link #startTime}
     */
    public long getDuration() {
        return endTime - startTime;
    }

    /**
     * 시작 시간을 기억한다.
     */
    public void startTime() {
        startTime = System.currentTimeMillis();
    }

    /**
     * 종료 시간을 기억한다.
     */
    public void endTime() {
        endTime = System.currentTimeMillis();
    }

    /**
     * {@link #MAX_CHECK_SEND_FRAME} 만큼 걸린 시간을 측정한다.
     */
    private void sumDuration() {
        totalDuration += getDuration();
        sendFrameCount++;
    }

    /**
     * {@link #MAX_CHECK_SEND_FRAME} 보내는 동안의 평균 시간.
     *
     * @return 전송중 평균 시간.
     */
    private long getDurationAVG() {
        return totalDuration / MAX_CHECK_SEND_FRAME;
    }

    /**
     * 진행 중 FPS가 낮아졌는지 높아졌는지에 대한 callback 처리
     */
    public void checkChangeFrameRate() {
        long durationAverage = getDurationAVG();
        if (sendFrameCount < MAX_CHECK_SEND_FRAME && durationAverage < lowerSendDuration) {
            sumDuration();

        } else {
            if (durationAverage > lowerSendDuration) {
                RLog.w("Bitrate lower. Delayed time is %d durationAVG %d, lowerSendDuration %d", sendFrameCount, durationAverage, lowerSendDuration);
                if (listener != null) {
                    listener.onLowerEvent();
                }

            } else if (durationAverage < upperSendDuration) {
                if (listener != null) {
                    listener.onUpperEvent();
                }
            }

            totalDuration = getDuration();
            sendFrameCount = 0;
        }
    }

    public void onDestory() {
        listener = null;
    }
}
