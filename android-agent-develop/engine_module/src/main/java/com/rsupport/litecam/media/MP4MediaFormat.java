package com.rsupport.litecam.media;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;

@SuppressLint("UseValueOf")
public class MP4MediaFormat {
    private Map<String, Object> mMap;

    public static final String KEY_WIDTH = "video-width";
    public static final String KEY_HEIGHT = "video-height";
    public static final String KEY_FRAME_RATE = "video-fps";

    public static final String KEY_SAMPLE_RATE = "sound-sample-rate";
    public static final String KEY_CHANNEL_COUNT = "sound-channel-config";
    public static final String KEY_AUDIO_BIT_RATE = "sound-bitrate";

    @SuppressWarnings({"rawtypes", "unchecked"})
    public MP4MediaFormat() {
        mMap = new HashMap();
    }

    /**
     * Creates a minimal video format.
     *
     * @param mime   The mime type of the content.
     * @param width  The width of the content (in pixels)
     * @param height The height of the content (in pixels)
     */
    public static final MP4MediaFormat createVideoFormat(int width, int height) {
        MP4MediaFormat format = new MP4MediaFormat();
        format.setInteger(KEY_WIDTH, width);
        format.setInteger(KEY_HEIGHT, height);

        return format;
    }

    /**
     * Creates a minimal audio format.
     *
     * @param mime         The mime type of the content.
     * @param sampleRate   The sampling rate of the content.
     * @param channelCount The number of audio channels in the content.
     */
    public static final MP4MediaFormat createAudioFormat(
            int sampleRate,
            int channelCount) {
        MP4MediaFormat format = new MP4MediaFormat();
        format.setInteger(KEY_SAMPLE_RATE, sampleRate);
        format.setInteger(KEY_CHANNEL_COUNT, channelCount);

        return format;
    }

    Map<String, Object> getMap() {
        return mMap;
    }

    public final int getInteger(String name) {
        return ((Integer) mMap.get(name)).intValue();
    }

    public final void setInteger(String name, int value) {
        mMap.put(name, new Integer(value));
    }
}
