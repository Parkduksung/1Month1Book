package com.rsupport.media.mediaprojection.record.adjust;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.os.Build;
import android.os.Bundle;

import com.rsupport.util.log.RLog;


/**
 * Created by taehwan on 5/19/15.
 * <p/>
 * Bitrate를 컨트롤 한다. Android Kitkat 부터 Bitrate 컨트롤이 가능한다.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class AdjustBitrate implements OnBitRateChangeListener {

    /**
     * Default Bitrate index.
     */
    private final int DROP_BITRATE_INDEX = 3;

    /**
     * 시작 Bitrate index.
     */
    private final int START_BITRATE_INDEX = 7;

    /**
     * Max bitrate index
     */
    private final int MAX_BITRATE_INDEX = START_BITRATE_INDEX + 3;

    /**
     * H.264 codec value
     * width * height * fps * codec value
     */
    private final float CODEC_VALUE = 0.12f;

    /**
     * Base bitrate offset
     */
    private int BITRATE_OFFSET = 1024 * 256;

    /**
     * Bitrate 가 변경되고 다음 bitrate 변경 요청이 왔을때 해당 시간이 지난 후 적용.
     */
    private final int BITRATE_UPPER_CHANGE_TIME = 1000 * 10;

    /**
     * Bitrate 가 변경되고 다음 bitrate 변경 요청이 왔을때 해당 시간이 지난 후 적용.
     */
    private final int BITRATE_LOWER_CHANGE_TIME = 1000 * 2;

    // 복원시 필요함.
    private int sumBitRateIndex = START_BITRATE_INDEX;
    // 복원시 필요함.
    private int currentBitRateIndex = START_BITRATE_INDEX;
    // 복원시 필요함.
    private int changeEventCount = 0;
    // 복원시 필요함.
    private int bitrateIndex = START_BITRATE_INDEX;
    // 복원시 필요함.
    private int topBitRateIndex = 0;

    private long bitrateChangeTime = 0;

    private Bundle params = null;
    private MediaCodec mediaCodec = null;

    /**
     * width * height * fps * codec value
     */
    private int baseBitrate = 0;

    public AdjustBitrate() {
        params = new Bundle();
    }

    public void setConfigure(int width, int height, int fps, float codecValue) {
        baseBitrate = (int) ((width * height * fps) * codecValue);
        BITRATE_OFFSET = baseBitrate / 10;

        RLog.d("Width %d, Height %d, FPS %d, BaseBitrate %d, BitrateOffset %d", width, height, fps, baseBitrate, BITRATE_OFFSET);
    }

    /**
     * MediaCodec을 셋팅
     */
    public void setMediaCodec(MediaCodec mediaCodec) {
        this.mediaCodec = mediaCodec;
    }

    /**
     * 현재 설정된 bitrate index
     *
     * @return bitrate index
     */
    public int getCurrentBitRateIndex() {
        return currentBitRateIndex;
    }

    /**
     * 현재 설정된 bitrate를 반환한다.
     */
    public int getCurrentBitrate() {
        if (baseBitrate == 0) {
            baseBitrate = (int) ((640 * 480 * 30) * CODEC_VALUE);
        }

        int currentBitrate = baseBitrate + (BITRATE_OFFSET * (currentBitRateIndex - START_BITRATE_INDEX));
        return (currentBitrate < BITRATE_OFFSET * 2) ? BITRATE_OFFSET * 2 : currentBitrate;
    }

    /**
     * bitrate 설정을 Index로 설정한다.
     */
    public void setBitrateIndex(int bitrateIndex) {
        if (bitrateIndex > MAX_BITRATE_INDEX) {
            bitrateIndex = MAX_BITRATE_INDEX;
        }
        this.bitrateIndex = bitrateIndex;
    }

    public void onDestory() {
        params = null;
        mediaCodec = null;
        bitrateChangeTime = 0;
        bitrateIndex = 0;
    }

    @Override
    public void onUpperEvent() {
        if (bitrateChangeTime > 0 && System.currentTimeMillis() - bitrateChangeTime > BITRATE_UPPER_CHANGE_TIME) {
            bitrateChangeTime = System.currentTimeMillis();
            if (bitrateIndex < MAX_BITRATE_INDEX) {
                bitrateIndex++;
                if (topBitRateIndex < bitrateIndex) {
                    topBitRateIndex = bitrateIndex;
                }
            }

            int beforeUseIndex = currentBitRateIndex;

            changeEventCount++;
            sumBitRateIndex += bitrateIndex;
            int avgBitrateIndex = sumBitRateIndex / (changeEventCount + 1);
            currentBitRateIndex = (avgBitrateIndex + topBitRateIndex) / 2;

            if (currentBitRateIndex != beforeUseIndex) {
                RLog.i("CurrentBitrate %d", getCurrentBitrate());
                changeBitrate();
            }
        }
    }

    @Override
    public void onLowerEvent() {
        if (bitrateIndex > DROP_BITRATE_INDEX) {
            bitrateIndex = DROP_BITRATE_INDEX;
            currentBitRateIndex = bitrateIndex;
            changeEventCount++;
            sumBitRateIndex += bitrateIndex;
            changeBitrate();
            RLog.i("CurrentBitrate %d", getCurrentBitrate());
            bitrateChangeTime = System.currentTimeMillis();

        } else {
            if (System.currentTimeMillis() - bitrateChangeTime > BITRATE_LOWER_CHANGE_TIME) {
                if (bitrateIndex > 0) {
                    bitrateIndex--;
                    changeEventCount++;
                    currentBitRateIndex = bitrateIndex;
                    sumBitRateIndex += bitrateIndex;

                    changeBitrate();
                    RLog.i("CurrentBitrate %d", getCurrentBitrate());
                }
            }
        }
    }

    private void changeBitrate() {
        if (mediaCodec != null) {
            params.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, getCurrentBitrate());
            mediaCodec.setParameters(params);
        }
    }

    public int getSumBitRateIndex() {
        return sumBitRateIndex;
    }

    public void setSumBitRateIndex(int sumBitRateIndex) {
        this.sumBitRateIndex = sumBitRateIndex;
    }

    public void setCurrentBitRateIndex(int currentBitRateIndex) {
        this.currentBitRateIndex = currentBitRateIndex;
    }

    public int getChangeEventCount() {
        return changeEventCount;
    }

    public void setChangeEventCount(int changeEventCount) {
        this.changeEventCount = changeEventCount;
    }

    public int getTopBitRateIndex() {
        return topBitRateIndex;
    }

    public void setTopBitRateIndex(int topBitRateIndex) {
        this.topBitRateIndex = topBitRateIndex;
    }

    public int getBitrateIndex() {
        return bitrateIndex;
    }
}
