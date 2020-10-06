package com.rsupport.litecam.media.muxer;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;

import com.rsupport.litecam.util.LLog;

/**
 * Android 4.3 이상의 {@link android.media.MediaMuxer}를 사용한다.
 *
 * @author taehwan
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MediaMuxerWrapper extends IMediaMuxerWrapper {
    private MediaMuxer muxer;

    MediaMuxerWrapper(String path, int format, boolean isAudio) {
        restart(path, format);
        trackSize(isAudio);
    }

    @Override
    public int addTrack(Object format) {
        if (started) return -1;

        ++numTracksAdded;
        int trackIndex = muxer.addTrack((MediaFormat) format);

        if (allTracksAdded()) {
            start();
        }

        return trackIndex;
    }

    @Override
    public Object getVideoTrackInfo(MediaFormat format, Point size, int frameRate) {
        return format;
    }

    @Override
    public Object getAudioTrackInfo(MediaFormat format, int sampleRate, int channelCount, int bitrate) {
        return format;
    }

    @Override
    public void start() {
        if (started) return;

        muxer.start();
        started = true;
    }

    @Override
    public void stop() {
        if (muxer != null) { // 삼성 muxer 에러..... Warning이 표시되면 앱이 종료되지는 않음...
            if (!allTracksFinished()) LLog.e("Stopping Muxer before all tracks added!");
            if (!started) LLog.e("Stopping Muxer before it was stated");

            try {
                muxer.stop();
                muxer.release();

            } catch (IllegalStateException e) {
                LLog.e(true, "muxer error: " + e);
            }
            muxer = null;

            if (muxerWapperListener != null) {
                muxerWapperListener.stopMuxer();
            }
        }
        started = false;
        numTracksAdded = 0;
        numTracksFinished = 0;
    }

    @Override
    protected void restart(String filePath, int format) {
        stop();
        if (started) return;

        try {
            muxer = new MediaMuxer(filePath, format);

        } catch (IOException e) {
            throw new RuntimeException("MediaMuxer creation failed", e);
        }
    }

    @Override
    public void setOrientationHint(int degrees) {
        if (started) return;
        muxer.setOrientationHint(degrees);
    }

    @Override
    public void writeSampleData(int trackIndex, ByteBuffer byteBuf, BufferInfo bufferInfo) {
        if (!started) return;
        muxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
    }

    @Override
    public long getVideoPresentationTimeUs(long startWhen) {
        return (long) (System.currentTimeMillis() - startWhen) * 1000;
    }
}
