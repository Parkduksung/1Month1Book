/*
 *
 * <b>Copyright (c) 2012 RSUPPORT Co., Ltd. All Rights Reserved.</b><p>
 *
 * <b>NOTICE</b> :  All information contained herein is, and remains the property
 * of RSUPPORT Company Limited and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to RSUPPORT
 * Company Limited and its suppliers and are protected by trade secret
 * or copyright law. Dissemination of this information or reproduction
 * of this material is strictly forbidden unless prior written permission
 * is obtained from RSUPPORT Company Limited.<p>
 *
 * Author  : Park Sung Yeon <br>
 * Date    : 2014. .  <br>
 *
 */

package com.rsupport.media;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;

import com.rsupport.jni.RsMedia;
import com.rsupport.litecam.media.ImageProcessing;
import com.rsupport.litecam.record.RecordSet;
import com.rsupport.litecam.util.RecordScreenHead;
import com.rsupport.media.RsMediaCodecVideo.Builder;

import java.nio.ByteBuffer;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
public class RsMediaEncoderVOMX implements IRsVEncoder {

    private static final String VIDEO_MIME_TYPE = "video/avc";

    MediaCodec mVideoEncoder;

    ICodecListener mCodecListener;

    static RsMediaEncoderVOMX mEncoderOMX;

    int mColorFormat;

    private Builder mBuilder;

    private byte[] mYUVData;

    private int mYuvDataLen = 0;

    private long mFrameIndex = 0;
    boolean mIsFormatChanged = false;

    protected Thread mVideoOfferThread;

    private ByteBuffer[] mInputOrgBuffers;
    private ByteBuffer[] mOutputBuffers;
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    boolean eosReceived = true;

    private static Context mContext;

    private RsMediaEncoderVOMX() {
        initVariables();
    }

    public static IRsVEncoder createInstanceStatic(Context context) {
        mContext = context;
        return createInstanceStatic();
    }

    public static IRsVEncoder createInstanceStatic() {
        if (mEncoderOMX == null) {
            mEncoderOMX = new RsMediaEncoderVOMX();
        }
        return mEncoderOMX;
    }

    @Override
    public IRsVEncoder createInstance() {
        if (mEncoderOMX == null) {
            mEncoderOMX = new RsMediaEncoderVOMX();
        }
        return mEncoderOMX;
    }

    @Override
    public void start() {
        if (mVideoEncoder == null) return;

        try {
            mVideoEncoder.start();
        } catch (Exception e) {
            throw new RuntimeException("Video start exception " + e);
        }

        mInputOrgBuffers = mVideoEncoder.getInputBuffers();
        mBufferInfo = new MediaCodec.BufferInfo();
        mOutputBuffers = mVideoEncoder.getOutputBuffers();
    }

    @Override
    public void stop() {
        if (mVideoEncoder == null) return;

        eosReceived = true;

        setEOSCodec();
    }

    boolean mIsGalaxyTab412 = false;

    @Override
    public boolean configure(RsMediaCodecVideo.Builder builder) {
        mBuilder = builder;

        mColorFormat = getCodecColorFormat();

        MediaFormat format = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, mBuilder.VIDEOWIDTH, mBuilder.VIDEOHEIGHT);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mBuilder.BIT_RATE);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, mBuilder.FPS_RATE);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, mColorFormat);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, mBuilder.IFRAME_INTERVAL);

        try {
            mVideoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        try {
            mVideoEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        mYuvDataLen = mBuilder.VIDEOWIDTH * mBuilder.VIDEOHEIGHT * 3 / 2;

        if (mBuilder.CAPTURE_COLORFORMAT == RecordScreenHead.RGB) {
            mYUVData = new byte[mYuvDataLen];
        }


        // SHV-E230S
        mIsGalaxyTab412 = ((Build.MODEL.contains("SHV-E230S")) ? true : false);

        initVariables();

        mCodecListener.onVStart();

        return true;
    }

    @Override
    public void setCodecListener(ICodecListener codecListener) {
        mCodecListener = codecListener;
    }

    @Override
    public void queueInput(byte[] data, int offset, int size, boolean finish) {
        if (eosReceived) return;

        int inputBufferIndex = mVideoEncoder.dequeueInputBuffer(-1);
        ByteBuffer inputBuffer = mInputOrgBuffers[inputBufferIndex];
        inputBuffer.clear();

        if (finish) {
            mVideoEncoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
        } else {
            if (mBuilder.CAPTURE_COLORFORMAT == RecordScreenHead.RGB) {
                procColorConverter(mYUVData, data, mColorFormat);
                inputBuffer.put(mYUVData, offset, mYuvDataLen);
            } else {
                inputBuffer.put(data, offset, mYuvDataLen);
            }

            if (eosReceived) return;

            mVideoEncoder.queueInputBuffer(inputBufferIndex, 0, mYuvDataLen, (((mFrameIndex++) * 5) * 1000), 0);
        }

        videoQueueOutputStream();
    }

    @Override
    public int getColorFormat() {
        return getCodecColorFormat();
    }

    @Override
    public Object getOutputFormat() {
        return mVideoEncoder.getOutputFormat();
    }




    /*
     *
     *
     * From below is class inner support functions.
     *
     *
     *
     */


    private void setEOSCodec() {
        int inputBufferIndex = mVideoEncoder.dequeueInputBuffer(-1);
        ByteBuffer inputBuffer = mInputOrgBuffers[inputBufferIndex];
        inputBuffer.clear();

        mVideoEncoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);

        videoQueueOutputStream();
    }

    private void initVariables() {
        mFrameIndex = 0;
        mIsFormatChanged = false;
        eosReceived = false;
    }

    private synchronized boolean videoQueueOutputStream() {
        if (mVideoEncoder == null) return false;

        if (mOutputBuffers == null) {
            mOutputBuffers = mVideoEncoder.getOutputBuffers();
        }

        while (true) {
            int outputBufferIndex = mVideoEncoder.dequeueOutputBuffer(mBufferInfo, -1);
            if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {

            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {

                mOutputBuffers = mVideoEncoder.getOutputBuffers();

            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

                mIsFormatChanged = true;
                mCodecListener.onVDeqeueFormatChanged(mVideoEncoder.getOutputFormat());

            } else if (outputBufferIndex < 0) {

            } else {
                ByteBuffer outputBuffer = mOutputBuffers[outputBufferIndex];
                if (outputBuffer == null) {
                    throw new RuntimeException("encoderOutputBuffer " + outputBufferIndex + " was null");
                }

                if (mBufferInfo.size > 0 && mBufferInfo.size < 127 && !mIsFormatChanged) {
                    setSpsPps(outputBufferIndex);
                    mBufferInfo.size = 0;
                }

                if (((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0)) {
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    outputBuffer.position(mBufferInfo.offset);
                    outputBuffer.limit(mBufferInfo.offset + mBufferInfo.size);

                    mCodecListener.onVDequeueOutput(outputBuffer, mBufferInfo);
//					mCodecListener.onVDequeueOutput(byteArray);
                }

                mVideoEncoder.releaseOutputBuffer(outputBufferIndex, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    stopVideo();
                    return true;
                }

                if (eosReceived) continue;

                break;
            }
        }

        return false;
    }

    private void stopVideo() {
        try {
            if (mVideoEncoder != null) {
                mVideoEncoder.stop();
                mVideoEncoder.release();
                mVideoEncoder = null;
                mCodecListener.onVStop();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    byte[] sps;
    byte[] pps;
    byte[] csd = new byte[128];

    private void setSpsPps(int outBufferIndex) {
        try {
            if (mBufferInfo.size > 0 && mBufferInfo.size < 127) {
                int sppsLen = 0;
                int len = 0, p = 4, q = 4;
                len = mBufferInfo.size;
                mOutputBuffers[outBufferIndex].get(csd, 0, len);
                if (len > 0 && csd[0] == 0 && csd[1] == 0 && csd[2] == 0 && csd[3] == 1) {
                    // Parses the SPS and PPS, they could be in two different packets and in a different order
                    //depending on the phone so we don't make any assumption about that
                    while (p < len) {
                        while (!(csd[p + 0] == 0 && csd[p + 1] == 0 && csd[p + 2] == 0 && csd[p + 3] == 1) && p + 3 < len)
                            p++;
                        if (p + 3 >= len) p = len;
                        if ((csd[q] & 0x1F) == 7) {
                            sps = new byte[p - q];
                            System.arraycopy(csd, q, sps, 0, p - q);
                            sppsLen += p - q + 4;
                        } else {
                            pps = new byte[p - q];
                            System.arraycopy(csd, q, pps, 0, p - q);
                            sppsLen += p - q + 4;
                        }
                        p += 4;
                        q = p;
                    }
                }

                byte[] spps = new byte[sppsLen];
                System.arraycopy(csd, 0, spps, 0, sppsLen);

                mIsFormatChanged = true;

                mCodecListener.onVDeqeueFormatChanged(spps);
            }
        } catch (Exception e) {
        }
    }


    private void procColorConverter(byte[] yuvBytes, byte[] sources, int colorFormat) {
        if (mIsGalaxyTab412) {
            RsMedia.encodeYUV420SPReverse(yuvBytes, sources, mBuilder.RESOLUTIONINFO.screenSize.x, mBuilder.RESOLUTIONINFO.screenSize.y);
        } else {
            ImageProcessing.convertColor(sources, RecordSet.RS_COLOR_FORMAT, mBuilder.RESOLUTIONINFO, yuvBytes, colorFormat);
        }
    }

    private int getCodecColorFormat() {
        int colorFormat = RsMediaUtils.selectColorFormat(RecordSet.VIDEO_MIME_TYPE);
        if (colorFormat == -1) colorFormat = 21;

        return colorFormat;
    }

}
