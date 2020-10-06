package com.rsupport.litecam.media.muxer;

import java.nio.ByteBuffer;

import android.graphics.Point;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;

import com.rsupport.litecam.util.APICheck;


public abstract class IMediaMuxerWrapper {
    protected IMuxerWapperListener muxerWapperListener;

    /**
     * 사용하는 버전에 따라서 muxer 호출이 다르기에 아래와 같이 정의
     */
    public static final int ANDROID_MUXER = 0;
    public static final int RS_MUXER = 1;

    /**
     * muxer가 시작되면 true로 변경. {@link #start} 함수에서 호출
     */
    public boolean started = false;
    protected int numTracks = 0;
    protected int numTracksAdded = 0;
    protected int numTracksFinished = 0;

    public static IMediaMuxerWrapper mediaMuxerInit(String filePath, int format, boolean isAudio) {
        IMediaMuxerWrapper muxerWapper = null;

        if (APICheck.isNewAPISupportVersion()) {
            muxerWapper = new MediaMuxerWrapper(filePath, format, isAudio);

        } else {
            muxerWapper = new MP4MediaMuxerWrapper(filePath, format, isAudio);
        }

        return muxerWapper;
    }

    /**
     * Track size audio를 사용할 경우 track 2. 사용하지 않을 경우 track 1.
     *
     * @param isAudio
     */
    protected void trackSize(boolean isAudio) {
        if (isAudio) {
            numTracks = 2;
        } else {
            numTracks = 1;
        }
    }

    /**
     * Adds a track with the specified format.
     *
     * @param format The media format for the track.
     * @return The track index for this newly added track, and it should be used
     * in the {@link #writeSampleData}.
     */
    public abstract int addTrack(Object format);

    /**
     * {@link #addTrack}을 호출하기 전에 호출해야 하며, Muxer version에 따라서 format을 설정한다.
     *
     * @param format    Video의 format을 설정
     * @param size
     * @param frameRate
     * @return return 된 결과를 {@link #addTrack}에서 사용하면 된다.
     */
    public abstract Object getVideoTrackInfo(MediaFormat format, Point size, int frameRate);

    /**
     * {@link #addTrack}을 호출하기 전에 호출해야 하며, Muxer version에 따라서 format을 설정한다.
     *
     * @param format       Audio의 format을 설정
     * @param sampleRate
     * @param channelCount
     * @param bitrate
     * @return return 된 결과를 {@link #addTrack}에서 사용하면 된다.
     */
    public abstract Object getAudioTrackInfo(MediaFormat format, int sampleRate, int channelCount, int bitrate);

    /**
     * Encoder 영상의 회전을 지정합니다.
     *
     * @param degrees
     */
    public abstract void setOrientationHint(int degrees);

    /**
     * muxer finish 하기 위한 호출.
     * {@link #allTracksFinished}를 확인하여 자동으로 {@link #stop}을 호출한다.
     */
    public void finishTrack() {
        if (!started) return;

        ++numTracksFinished;

        if (allTracksFinished()) {
            stop();
        }
    }

    /**
     * 모든 track 이 입력되었는지 체크. 트랙 카운트는 {@link #trackSize}에서 미리 결정.
     *
     * @return
     */
    protected boolean allTracksAdded() {
        return (numTracks == numTracksAdded);
    }

    /**
     * all track is finished. track count is {@link #trackSize}
     *
     * @return
     */
    protected boolean allTracksFinished() {
        return (numTracksAdded == numTracksFinished);
    }

    public abstract void start();

    public abstract void stop();

    protected abstract void restart(String filePath, int format);

    public abstract void writeSampleData(int trackIndex, ByteBuffer byteBuf, BufferInfo bufferInfo);

    /**
     * Video의 Presentation time을 계산한다.
     *
     * @return time
     */
    public abstract long getVideoPresentationTimeUs(long startWhen);

    /**
     * Audio의 Presentation time을 계산한다.
     *
     * @return time
     */
    public long getAudioPresentationTimeUs(long startWhen) {
        return (long) (System.currentTimeMillis() - startWhen) * 1000;
    }

    /**
     * Muxer stop callback listener 등록
     */
    public void setMuxerWapperListener(IMuxerWapperListener listener) {
        muxerWapperListener = listener;
    }

    /**
     * Muxer 종료 callback Listener
     */
    public interface IMuxerWapperListener {
        public void stopMuxer();
    }
}
