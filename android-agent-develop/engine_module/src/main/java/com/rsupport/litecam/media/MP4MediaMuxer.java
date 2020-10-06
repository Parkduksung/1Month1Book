package com.rsupport.litecam.media;

import java.nio.ByteBuffer;
import java.util.Map;

import android.media.AudioFormat;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaMuxer.OutputFormat;

public class MP4MediaMuxer {
    public static final int MUXER_OUTPUT_MPEG_4_VIDEO_AUDIO = 0;
    public static final int MUXER_OUTPUT_MPEG_4_VIDEO_ONLY = 1;

    public static final int AUDIO_DATA = 0;
    public static final int VIDEO_DATA = 1;

    // Muxer internal states.
    private static final int MUXER_STATE_UNINITIALIZED = -1;
    private static final int MUXER_STATE_INITIALIZED = 0;
    private static final int MUXER_STATE_STARTED = 1;
    private static final int MUXER_STATE_STOPPED = 2;

    private int mState = MUXER_STATE_UNINITIALIZED;

    private int mLastTrackIndex = -1;


    public MP4MediaMuxer(String path, int format) {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        if (format == MUXER_OUTPUT_MPEG_4_VIDEO_AUDIO) {
            nativeInitMP4Writer(path, true);

        } else {
            nativeInitMP4Writer(path, false);
        }

        mState = MUXER_STATE_INITIALIZED;
    }

    public int addTrack(MP4MediaFormat format) {
        Map<String, Object> formatMap = format.getMap();

        String[] keys = null;
        Object[] values = null;

        if (format != null) {
            keys = new String[formatMap.size()];
            values = new Object[formatMap.size()];

            int i = 0;
            for (Map.Entry<String, Object> entry : formatMap.entrySet()) {
                keys[i] = entry.getKey();
                values[i] = entry.getValue();
                ++i;
            }
        }

        int type = nativeAddTrack(keys, values);
        mState = MUXER_STATE_INITIALIZED;
        return type;
    }

    /**
     * Sets the orientation hint for output video playback.
     * <p>This method should be called before {@link #start}. Calling this
     * method will not rotate the video frame when muxer is generating the file,
     * but add a composition matrix containing the rotation angle in the output
     * video if the output format is
     * {@link OutputFormat#MUXER_OUTPUT_MPEG_4} so that a video player can
     * choose the proper orientation for playback. Note that some video players
     * may choose to ignore the composition matrix in a video during playback.
     * By default, the rotation degree is 0.</p>
     *
     * @param degrees the angle to be rotated clockwise in degrees.
     *                The supported angles are 0, 90, 180, and 270 degrees.
     */
    public void setOrientationHint(int degrees) {
        if (degrees != 0 && degrees != 90 && degrees != 180 && degrees != 270) {
            throw new IllegalArgumentException("Unsupported angle: " + degrees);
        }
        if (mState == MUXER_STATE_INITIALIZED) {
            nativeSetOrientationHint(degrees);
        } else {
            throw new IllegalStateException("Can't set rotation degrees due" +
                    " to wrong state.");
        }
    }

    /**
     * Starts the muxer.
     * <p>Make sure this is called after {@link #addTrack} and before
     * {@link #writeSampleData}.</p>
     */
    public void start() {
        if (mState == MUXER_STATE_INITIALIZED) {
            nativeStart();
            mState = MUXER_STATE_STARTED;
        } else {
            throw new IllegalStateException("Can't start due to wrong state.");
        }
    }

    /**
     * Stops the muxer.
     * <p>Once the muxer stops, it can not be restarted.</p>
     */
    public void stop() {
        if (mState == MUXER_STATE_STARTED) {
            nativeStop();
            mState = MUXER_STATE_STOPPED;
        } else {
            throw new IllegalStateException("Can't stop due to wrong state.");
        }
    }

    public void writeSampleData(int trackIndex, ByteBuffer byteBuf, BufferInfo bufferInfo) {
        if (trackIndex < 0 || trackIndex > VIDEO_DATA) {
            throw new IllegalArgumentException("trackIndex is invalid");
        }

        if (byteBuf == null) {
            throw new IllegalArgumentException("byteBuffer must not be null");
        }

        if (bufferInfo == null) {
            throw new IllegalArgumentException("bufferInfo must not be null");
        }

        if (bufferInfo.size < 0 || bufferInfo.offset < 0 || (bufferInfo.offset + bufferInfo.size) > byteBuf.capacity() || bufferInfo.presentationTimeUs < 0) {
            throw new IllegalArgumentException("bufferInfo must specify a" +
                    " valid buffer offset, size and presentation time");
        }

        if (mState != MUXER_STATE_STARTED) {
            return;
        }

        nativeWriteSampleData(trackIndex, byteBuf, bufferInfo.offset, bufferInfo.size, bufferInfo.presentationTimeUs, bufferInfo.flags);
    }


    /**
     * Make sure you call this when you're done to free up any resources
     * instead of relying on the garbage collector to do this for you at
     * some point in the future.
     */
    public void release() {
        if (mState == MUXER_STATE_STARTED) {
            stop();
        }

        nativeRelease();
        mState = MUXER_STATE_UNINITIALIZED;
    }

    /*
     * 실제 재생할 client의 채널 수를 확인하여 decoder에 호출하기 위한 함수 AudioTrack과 함께 사용하기 위해서
     * Stereo와 mono 중에서 선택. 실제 flag 값이 다르기에 대응되는 숫자로 변경
     */
    public static int getChannelConfig(int sampleChannelConfig) {
        switch (sampleChannelConfig) {
            case AudioFormat.CHANNEL_OUT_STEREO:
                return 2; // stereo channel 2

            case AudioFormat.CHANNEL_OUT_MONO:
                return 1; // mono channel 1

            default:
                return 2; // default channel 2
        }
    }

    private native void nativeInitMP4Writer(String filePath, boolean isAudio);

    private native int nativeAddTrack(String[] keys, Object[] values);

    private native void nativeSetOrientationHint(int degrees);

    private native void nativeWriteSampleData(int trackIndex, ByteBuffer byteBuf, int offset, int size, long presentationTimeUs, int flags);

    private native void nativeStart();

    private native void nativeStop();

    private native void nativeRelease();

    //static definition
    static {
        System.loadLibrary("muxer");
    }
}
