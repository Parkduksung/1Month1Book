package com.rsupport.media.mediaprojection.record.codec;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;


import com.rsupport.util.log.RLog;

import java.io.IOException;

/**
 * Created by taehwan on 5/28/15.
 * <p/>
 * Android JellyBean 4.3 이상에서 동작하는 Surface Encoder
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class RSMediaCodec18Surface extends RSMediaCodec17 {

    private Surface surface;

    public RSMediaCodec18Surface() {
        super();
    }

    @Override
    public boolean preEncoder() {
        if (mediaCodecInfo == null) {
            RLog.e("MediaCodecInfo is null");
            return false;
        }

        try {
            mediaCodec = MediaCodec.createByCodecName(mediaCodecInfo.getName());
        } catch (IOException e) {
            RLog.e("Fail MediaCodec.createByCodecName : " + e);
            return false;
        } catch (IllegalArgumentException e) {
            RLog.e("Fail MediaCodec.createByCodecName : " + e);
            return false;
        }

        if (mediaCodec == null) {
            RLog.e("Fail MediaCodec.createByCodecName : " + mediaCodecInfo.getName());
            return false;
        }

        // Change surface color format
        colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);

        mediaCodec.configure(getMediaFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        surface = mediaCodec.createInputSurface();
        mediaCodec.start();
        outputBuffers = mediaCodec.getOutputBuffers();
        return true;
    }

    @Override
    public Surface getSurface() {
        return surface;
    }

    @Override
    public void signalEndOfStream() {
        try {
            mediaCodec.signalEndOfInputStream();
        } catch (IllegalStateException e) {
            RLog.e("Fail MediaCodec signalEndOfStream : " + e);
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (surface != null) {
            surface.release();
            surface = null;
        }
    }
}
