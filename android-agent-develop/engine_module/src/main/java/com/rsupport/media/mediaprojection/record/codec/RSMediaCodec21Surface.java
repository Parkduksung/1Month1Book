package com.rsupport.media.mediaprojection.record.codec;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.text.TextUtils;
import android.view.Surface;

import com.rsupport.util.CodecUtils;
import com.rsupport.util.log.RLog;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by taehwan on 5/28/15.
 * <p/>
 * Android API 21이상의 Surface 에서 사용할 Encoder 정의.
 */
@androidx.annotation.RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class RSMediaCodec21Surface extends AbstractRSMediaCodec {

    private static final String MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;
    private Surface surface;

    public RSMediaCodec21Surface() {
        super();
        colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
    }

    @Override
    public void initEncoder(int width, int height, int bitRate, int frameRate, int iFrameInterval) {
        RLog.d("initEncoder width.%d, height.%d, bitrate.%d, frameRate.%d, iFrameInterval.%d",
                width, height, bitRate, frameRate, iFrameInterval);
        mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval);
    }

    @Override
    public boolean preEncoder() {
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        String codec = null;
        try {
            codec = mediaCodecList.findEncoderForFormat(mediaFormat);
            if (codec == null) {
                RLog.i("findEncoderForFormat fail");
                throw new RuntimeException();
            }
        } catch (RuntimeException e) {
            mediaCodecInfo = selectCodec(MIME_TYPE);
            if (mediaCodecInfo != null) {
                codec = mediaCodecInfo.getName();
            }
        }

        if (TextUtils.isEmpty(codec)) {
            mediaCodecInfo = CodecUtils.selectAVCCodec();
            if (mediaCodecInfo != null) {
                codec = mediaCodecInfo.getName();
            }
        }

        RLog.i("Find codec name : " + codec);
        if (codec == null) {
            // Don't run the encoder.
            RLog.i("Not support MediaCodec " + mediaFormat);
            return false;
        }

        try {
            mediaCodec = MediaCodec.createByCodecName(codec);
        } catch (IOException e) {
            RLog.e("Fail MediaCodec.createByCodecName : " + e);
            return false;
        } catch (IllegalArgumentException e) {
            RLog.e("Fail MediaCodec.createByCodecName : " + e);
            return false;
        } catch (Exception e) {
            RLog.e("Fail MediaCodec.createByCodecName : " + e);
            return false;
        }

        RLog.i("Find mediaCodec name : " + mediaCodec);

        try {
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (MediaCodec.CodecException e) {
            RLog.e("MediaCodec exception ", mediaFormat.toString());
            return false;
        }

        RLog.i("Find mediaCodec configure : " + mediaFormat);
        surface = mediaCodec.createInputSurface();
        mediaCodec.start();

        RLog.i("Find mediaCodec start");
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
    public boolean dequeueOutputBuffer() throws Exception {
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);

        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            RLog.i("Encoder end of stream");
            return false;
        }

        if (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
            if (!onDequeueEvent(outputBuffer, bufferInfo)) {
                throw new RuntimeException("onDequeueEvent fail");
            }

            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);

        } else {
            switch (outputBufferIndex) {
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    // No output available yet
                    break;

                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    // Subsequent data will conform to new format.
                    mediaFormat = mediaCodec.getOutputFormat();
                    RLog.d("Encoder output format changed : " + mediaFormat);
                    onFormatChanged(mediaFormat);
                    break;

                default:
                    RLog.e("Unexpected result from encoder.dequeueOutputBuffers : " + outputBufferIndex);
                    break;
            }
        }

        return true;
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
