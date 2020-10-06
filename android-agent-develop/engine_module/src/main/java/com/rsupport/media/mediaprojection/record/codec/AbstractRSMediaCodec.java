package com.rsupport.media.mediaprojection.record.codec;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;


import com.rsupport.util.log.RLog;

import java.nio.ByteBuffer;

/**
 * Created by taehwan on 5/28/15.
 * <p/>
 * MediaCodec을 구현하기 위한 추상 클래스
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public abstract class AbstractRSMediaCodec implements IRSMediaCodec, IRSMediaCodec.OnDequeueBufferListener {

    protected final int TIMEOUT_US = 50000;

    protected MediaFormat mediaFormat;
    protected MediaCodec mediaCodec;

    protected MediaCodec.BufferInfo bufferInfo;

    protected MediaCodecInfo mediaCodecInfo;
    protected int colorFormat;
    protected OnDequeueBufferListener onDequeueBufferListener;

    public AbstractRSMediaCodec() {
        bufferInfo = new MediaCodec.BufferInfo();
    }

    @Override
    public MediaFormat getMediaFormat() {
        return mediaFormat;
    }

    @Override
    public Surface getSurface() {
        return null;
    }

    @Override
    public void signalEndOfStream() {
        //  Surface 에서만 사용한다.
    }

    @Override
    public void stop() {
        RLog.i("stop");
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }

        mediaFormat = null;
    }

    @Override
    public void onDestroy() {
        RLog.i("onDestroy");
        stop();
        onDequeueBufferListener = null;
        bufferInfo = null;
    }

    @Override
    public void setOnDequeueBufferListener(OnDequeueBufferListener onDequeueBufferListener) {
        this.onDequeueBufferListener = onDequeueBufferListener;
    }

    @Override
    public int getColorFormat() {
        return colorFormat;
    }

    @Override
    public MediaCodec getMediaCodec() {
        return mediaCodec;
    }

    @Override
    public boolean putEncoderData(ByteBuffer frameBuffer, long presentationTimeUs) {
        return false;
    }

    @Override
    public boolean putEncoderData(byte[] frameData, long presentationTimeUs) {
        return false;
    }


    @Override
    public boolean onDequeueEvent(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo) {
        if (onDequeueBufferListener != null) {
            return onDequeueBufferListener.onDequeueEvent(outputBuffer, bufferInfo);
        }
        return false;
    }

    @Override
    public void onFormatChanged(MediaFormat mediaFormat) {
        if (onDequeueBufferListener != null) {
            onDequeueBufferListener.onFormatChanged(mediaFormat);
        }
    }

    /**
     * Returns the first codec capable of encoding the specified MIME type, or
     * null if no match was found.
     */
    @SuppressWarnings("deprecation")
    protected MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {
                continue;
            }

            for (String type : codecInfo.getSupportedTypes()) {
                if (type.equalsIgnoreCase(mimeType)) {
                    RLog.i("SelectCodec : " + codecInfo.getName());
                    return codecInfo;
                }
            }
        }
        return null;
    }
}
