package com.rsupport.litecam.media.muxer;

import java.nio.ByteBuffer;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.os.Build;

import com.rsupport.litecam.media.MP4MediaFormat;
import com.rsupport.litecam.media.MP4MediaMuxer;
import com.rsupport.litecam.record.RecordSet;
import com.rsupport.litecam.util.LLog;

/**
 * Android 4.3 이상의 {@link android.media.MediaMuxer}를 사용한다.
 *
 * @author taehwan
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MP4MediaMuxerWrapper extends IMediaMuxerWrapper {
    private MP4MediaMuxer muxer;

    public MP4MediaMuxerWrapper(String path, int format, boolean isAudio) {
        format = MP4MediaMuxer.MUXER_OUTPUT_MPEG_4_VIDEO_AUDIO;
        if (!isAudio)
            format = MP4MediaMuxer.MUXER_OUTPUT_MPEG_4_VIDEO_ONLY;

        restart(path, format);
        trackSize(isAudio);
    }

    @Override
    public int addTrack(Object format) {
        if (started) return -1;

        ++numTracksAdded;
        int trackIndex = muxer.addTrack((MP4MediaFormat) format);

        if (allTracksAdded()) {
            start();
        }

        return trackIndex;
    }

    @Override
    public Object getVideoTrackInfo(MediaFormat format, Point size, int frameRate) {
        MP4MediaFormat mediaFormat = MP4MediaFormat.createVideoFormat(size.x, size.y);
        mediaFormat.setInteger(MP4MediaFormat.KEY_FRAME_RATE, RecordSet.FRAME_RATE);
        return mediaFormat;
    }

    @Override
    public Object getAudioTrackInfo(MediaFormat format, int sampleRate, int channelCount, int bitrate) {
        MP4MediaFormat mediaFormat = MP4MediaFormat.createAudioFormat(sampleRate, RecordSet.AUDIO_CHANNEL_COUNT);
        mediaFormat.setInteger(MP4MediaFormat.KEY_AUDIO_BIT_RATE, RecordSet.DEFAULT_AUDIO_BIT_RATE);
        return mediaFormat;
    }

    @Override
    public void start() {
        if (started) return;
        muxer.start();
        started = true;
    }

    @Override
    public synchronized void stop() {
        if (muxer != null) {
            if (!allTracksFinished()) LLog.e("Stopping Muxer before all tracks added!");
            if (!started) LLog.e("Stopping Muxer before it was stated");

            muxer.stop();
            muxer.release();
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

        muxer = new MP4MediaMuxer(filePath, format);
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
        return (long) (System.currentTimeMillis() - startWhen);
    }

}
