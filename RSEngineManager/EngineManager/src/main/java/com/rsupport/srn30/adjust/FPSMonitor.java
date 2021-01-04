package com.rsupport.srn30.adjust;

import com.rsupport.util.rslog.MLog;

/**
 * Created by kwcho on 6/8/15.
 */
public class FPSMonitor {

    private int baseFPS = 20;

    private int lowerWorkingDuration = 1000/30;
    private int upperWorkingDuration = 1000/40;

    private int sendFrameCount = 0;
    private long startTime = 0;
    private long endTime = 0;
    private long totalDuration = 0;

    private int increase = 1;

    private OnChangeListener onChangeListener = null;

    public FPSMonitor(){
    }

    public void setChangeListener(OnChangeListener onChangeListener){
        this.onChangeListener = onChangeListener;
    }

    /**
     * 시작시간을 마킹한다.
     */
    public void startTime() {
        startTime = System.currentTimeMillis();
    }

    /**
     * 종료 시간을 마킹한다.
     */
    public void endTime() {
        endTime = System.currentTimeMillis();
    }

    private long getDuration() {
        return endTime - startTime;
    }

    private void sumDuration() {
        totalDuration += getDuration();
        sendFrameCount++;
    }

    private long getDurationAVG(){
        return totalDuration/baseFPS;
    }

    private int upperCount = 0;

    /**
     * {@link com.rsupport.srn30.adjust.OnBitRateChangeListener} 에 변경 event 를 전달한다.
     */
    public void checkChangeFrameRate(int y){
        if(sendFrameCount < baseFPS){
            sumDuration();
        }else{
            int fps = 0;
            long durationAVG = getDurationAVG();
            if(durationAVG > lowerWorkingDuration){
                upperCount = 0;
                if(onChangeListener != null){
                    onChangeListener.onLowerEvent();
                }
            }
            else if(durationAVG < upperWorkingDuration){
                upperCount++;
                if(onChangeListener != null){
                    onChangeListener.onUpperEvent();

                }
            }
            else{
                if(upperCount > 0 && upperCount < 3){
                    if(onChangeListener != null){
                        onChangeListener.onLowerEvent();
                    }
                }
                upperCount = 0;
            }
            totalDuration = getDuration();
            sendFrameCount = 0;
        }
    }

    public void checkChangeFrameRate(){
        if(sendFrameCount < baseFPS){
            sumDuration();
        }else{
            long durationAVG = getDurationAVG();
            if(durationAVG > lowerWorkingDuration){
                upperCount = 0;
                if(onChangeListener != null){
                    onChangeListener.onLowerEvent(10);
                }
            }
            else if(durationAVG < upperWorkingDuration){
                upperCount++;
                if(onChangeListener != null) {
                    //onChangeListener.onUpperEvent();
                    onChangeListener.onUpperEvent(increase);
                }
            }
            else{
                if(upperCount > 0 && upperCount < 3){
                    if(onChangeListener != null){
                       onChangeListener.onLowerEvent(10);
                    }
                }
                upperCount = 0;
            }
            totalDuration = getDuration();
            sendFrameCount = 0;

            if (upperCount != 0) {
                increase = (increase * 2 >= 8 ? 8 : increase * 2);
            } else {
                increase = 1;
            }
        }
    }

    public static interface OnChangeListener{
        /**
         * frame rate 를 올려야 될때 호출된다.
         */
        public void onUpperEvent();

        /**
         * frame rate 를 내려야 될때 호출된다.
         */
        public void onLowerEvent();

        /**
         * frame rate 를 올려야 될때 호출된다.
         */
        public int onUpperEvent(int increase);

        /**
         * frame rate 를 내려야 될때 호출된다.
         */
        public int onLowerEvent(int decrease);
    }
}
