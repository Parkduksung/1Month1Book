package com.rsupport.media.mediaprojection;

import android.media.MediaFormat;

/**
 * Created by taehwan on 5/13/15.
 * <p/>
 * 녹화 설정 정보를 셋팅한다.
 */
public class Configuration {

    /**
     * H.264 codec value
     * width * height * fps * codec value
     */
    public static final float CODEC_VALUE = 0.14f;

    public float codecValue = CODEC_VALUE;

    public MediaFormat mediaFormat;

    /**
     * Virtual Width.
     */
    public int screenWidth;

    /**
     * Virtual height.
     */
    public int screenHeight;
}
