package com.rsupport.media.mediaprojection.record.codec;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;


import com.rsupport.util.log.RLog;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by taehwan on 5/28/15.
 * <p>
 * MediaCodec 17 이상에서 사용할 경우에 대한 정의
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class RSMediaCodec17 extends AbstractRSMediaCodec {

    protected static final String MIME_TYPE = "video/avc";

    private ByteBuffer[] inputBuffers;
    protected ByteBuffer[] outputBuffers;

    public RSMediaCodec17() {
        super();
        mediaCodecInfo = selectCodec(MIME_TYPE);
        colorFormat = selectColorFormat(mediaCodecInfo);
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
        if (mediaCodecInfo == null) {
            RLog.e("MediaCodecInfo is null");
            return false;
        }

        try {
            mediaCodec = MediaCodec.createByCodecName(mediaCodecInfo.getName());
        } catch (IOException e) {
            RLog.e("Fail MediaCodec.createByCodecName : " + e);
            return false;
        }
        if (mediaCodec == null) {
            RLog.e("Fail MediaCodec.createByCodecName : " + mediaCodecInfo.getName());
            return false;
        }

        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        mediaCodec.start();
        inputBuffers = mediaCodec.getInputBuffers();
        outputBuffers = mediaCodec.getOutputBuffers();
        return true;
    }

    @Override
    public void stop() {
        super.stop();

        inputBuffers = null;
        outputBuffers = null;
    }

    @Override
    public boolean dequeueOutputBuffer() throws Exception {
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);

        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            RLog.i("Encoder end of stream");
            return false;
        }

        if (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
            if (!onDequeueEvent(outputBuffer, bufferInfo)) {
                throw new RuntimeException("onDequeueEvent fail");
            }

            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);

        } else {
            switch (outputBufferIndex) {
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    // No output available yet
                    break;

                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    // this happens before th first frame is returned. Not called API 17(4.2) and API 16(4.1.x)
                    outputBuffers = mediaCodec.getOutputBuffers();
                    RLog.d("Encoder output buffers changed");
                    break;

                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    // Not expected for an encoder
                    // Not called API 17(4.2) and API 16(4.1.x)
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
    public boolean putEncoderData(ByteBuffer frameBuffer, long presentationTimeUs) {
        if (inputBuffers == null) {
            throw new RuntimeException("Called putEncoderData before preEncoder");
        }

        // input yuv data to codec.
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(TIMEOUT_US); // 50 ms
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];

            inputBuffer.clear();
            inputBuffer.put(frameBuffer);

            mediaCodec.queueInputBuffer(inputBufferIndex, 0, inputBuffer.capacity(), presentationTimeUs, 0);
        }
        return true;
    }

    @Override
    public boolean putEncoderData(byte[] frameData, long presentationTimeUs) {
        if (inputBuffers == null) {
            throw new RuntimeException("Called putEncoderData before preEncoder");
        }

        // input yuv data to codec.
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(TIMEOUT_US); // 50 ms.
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];

            inputBuffer.clear();
            inputBuffer.put(frameData);

            mediaCodec.queueInputBuffer(inputBufferIndex, 0, inputBuffer.capacity(), presentationTimeUs, 0);
        }
        return true;
    }

    private int selectColorFormat(MediaCodecInfo codecInfo) {
        if (codecInfo == null) {
            RLog.e("There is no encoder support " + MIME_TYPE);
            return 0;
        }

        int ret_colorFormat = selectColorFormat(codecInfo, MIME_TYPE);

        if (ret_colorFormat == 0) {
            RLog.e("couldn't find a good color format for %s/%s", codecInfo.getName(), MIME_TYPE);
        }

        return ret_colorFormat;   // not reached
    }

    /**
     * Retruns a color format that is supported by the codec and by this test
     * code. If no match is found, this throws a test failure -- the set of
     * formats known to the test should be expanded for new platforms.
     */
    private int selectColorFormat(MediaCodecInfo codecInfo, String mimeType) {
        try {
            MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
            for (int i = 0; i < capabilities.colorFormats.length; i++) {
                int colorFormat = capabilities.colorFormats[i];
                if (isRecognizedFormat(colorFormat)) {
                    RLog.d("Find a good color format for " + codecInfo.getName() + " / " + mimeType + " / " + colorFormat);
                    return colorFormat;
                }
            }
        } catch (IllegalArgumentException e) {
            RLog.e("IllegalArgumentException : " + e);
        }

        return 0;
    }

    /**
     * Returns true if this is a color format that this test code understands
     * (i.e. we know how to read and generate frames in this format).
     */
    private boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            // these are the formats we know how to handle for this test
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                RLog.d("COLOR_FormatYUV420Planar");
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
                RLog.d("COLOR_FormatYUV420PackedPlanar");
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                RLog.d("COLOR_FormatYUV420SemiPlanar");
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
                RLog.d("COLOR_FormatYUV420PackedSemiPlanar");
            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                RLog.d("COLOR_TI_FormatYUV420PackedSemiPlanar");
                return true;
            default:
                return false;
        }
    }
}
