package com.rsupport.media.mediaprojection.record;

import android.media.MediaFormat;

import com.rsupport.media.IMediaCodecListener;
import com.rsupport.media.mediaprojection.Configuration;

/**
 * Created by taehwan on 5/13/15.
 * <p/>
 * MediaCodec을 정의 하기 위한 Interface
 */
public interface IRSEncoder {

    /**
     * fps 를 설정한다.
     *
     * @param fps
     */
    void setFps(int fps);

    int TYPE_VIDEO_CAPTURE_SURFACE = 0x00000002;

    void setConfiguration(Configuration configuration);

    /**
     * encoder 상태를 수신할 수 있는 listener를 설정한다.
     */
    void setMediaCodecListener(IMediaCodecListener mediaCodecListener);

    /**
     * encoder 동작 시간을 반환한다. (micro second)
     */
    long getPresentationTime();

    /**
     * MediaFormat 을 반환한다.
     *
     * @return 설정된 mediaFormat
     */
    MediaFormat getMediaFormat();

    /**
     * 초기화를 수행한다.
     */
    void initialized();

    /**
     * Rotation 정보를 처리한다.
     */
    void setRotation(int rotation);

    /**
     * 정지.
     */
    void stop();
}
