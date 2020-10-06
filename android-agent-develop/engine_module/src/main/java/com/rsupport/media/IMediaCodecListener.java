package com.rsupport.media;

import android.media.MediaCodec;
import android.media.MediaFormat;

import androidx.annotation.IntDef;

import java.nio.ByteBuffer;

/**
 * Created by taehwan on 6/2/15.
 */
public interface IMediaCodecListener {

    /**
     * Encoder 데이터에 대한 정보를 셋팅
     *
     * @param width     Encoder width.
     * @param height    Encoder height.
     * @param frameRate Encoder frameRate.
     */
    void onSendEncoderInfo(int width, int height, int frameRate);

    /**
     * Screen 데이터에 대한 정보
     *
     * @param width     Encoder width.
     * @param height    Encoder height.
     * @param frameRate Encoder frameRate.
     */
    void onSendScreenInfo(int width, int height, int frameRate);

    /**
     * MediaCodec의 Format change
     */
    void onSendFormatChanged(MediaFormat mediaFormat);

    /**
     * Byte data callback.
     */
    void onSendDequeueEvent(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo);

    int STATUS_START = 100;
    int STATUS_STOP = 200;
    int EVENT_ERROR_CAPTURE = 7000;
    int EVENT_ERROR_INITIALIZATION_FAILED = 8000;

    @IntDef({STATUS_START, STATUS_STOP, EVENT_ERROR_CAPTURE, EVENT_ERROR_INITIALIZATION_FAILED})
    @interface StatusType {
    }

    /**
     * 현재 진행 중인 상항을 알린다.
     */
    void onSendStatus(@StatusType int status);
}
