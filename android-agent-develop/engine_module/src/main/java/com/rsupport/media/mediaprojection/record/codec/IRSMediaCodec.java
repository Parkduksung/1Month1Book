package com.rsupport.media.mediaprojection.record.codec;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;

import java.nio.ByteBuffer;

/**
 * Created by taehwan on 5/28/15.
 * <p/>
 * MediaCodec을 정의한다.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public interface IRSMediaCodec {

    /**
     * MediaFormat 초기화
     */
    void initEncoder(int width, int height, int bitRate, int frameRate, int iFrameInterval);

    /**
     * MediaFormat return
     */
    MediaFormat getMediaFormat();

    /**
     * MediaCodec stop.
     */
    void stop();

    /**
     * MediaCodec release
     */
    void onDestroy();

    /**
     * Encoder 초기화
     */
    boolean preEncoder();

    /**
     * MediaCodec에서 생성된 Surface return.
     */
    Surface getSurface();

    /**
     * Dequeue Buffer listener 셋팅.
     */
    void setOnDequeueBufferListener(OnDequeueBufferListener onDequeueBufferListener);

    /**
     * Dequeue output 데이터 처리
     */
    boolean dequeueOutputBuffer() throws Exception;

    /**
     * Encoder 데이터를 {@link ByteBuffer}로 처리한다.
     */
    boolean putEncoderData(ByteBuffer frameBuffer, long presentationTimeUs);


    /**
     * Encoder 데이터를 {@link byte}로 처리한다.
     */
    boolean putEncoderData(byte[] frameData, long presentationTimeUs);

    /**
     * Surface에 대한 종료 이벤트 처리
     */
    void signalEndOfStream();

    /**
     * @return 설정된 Color format 값을 return 한다.
     */
    int getColorFormat();

    /**
     * @return 초기화 된 MediaCodec을 return 한다.
     */
    MediaCodec getMediaCodec();


    interface OnDequeueBufferListener {
        /**
         * Output Buffer에 대한 이벤트.
         */
        boolean onDequeueEvent(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo);

        /**
         * Format changed에 대한 이벤트
         */
        void onFormatChanged(MediaFormat mediaFormat);
    }
}
