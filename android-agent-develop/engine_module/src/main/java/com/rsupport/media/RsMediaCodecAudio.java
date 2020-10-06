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
import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import com.rsupport.litecam.record.RecordSet;

import java.nio.ByteBuffer;

@TargetApi(16)
public class RsMediaCodecAudio extends RsMediaCodecBase {

    boolean mIsAudioRecord = false;

    private static Context mContext;
    private static RsMediaCodecAudio mCodecAudio;
    private static MediaCodec mAudioEncoder;

    private ICodecListener mCodecListener;
    protected boolean eosReceived = false;

    BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    ByteBuffer[] mInputBuffers;
    ByteBuffer[] mOutputBuffers;

    boolean mIsStopVideo = false;

    private Builder mBuilder;

    public static class Builder {

        private String MIME_TYPE_AUDIO = "audio/mp4a-latm";
        private int SAMPLE_RATE = RecordSet.DEFAULT_SAMPLE_RATE;
        private int BIT_RATE = RecordSet.DEFAULT_AUDIO_BIT_RATE;
        private int CHANNEL_COUNT = 1;
        private int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;

        public Builder MIME_TYPE_AUDIO(String mimeName) {
            MIME_TYPE_AUDIO = mimeName;
            return this;
        }

        public Builder SAMPLE_RATE(int sample_rate) {
            SAMPLE_RATE = sample_rate;
            return this;
        }

        public Builder BIT_RATE(int bit_rate) {
            BIT_RATE = bit_rate;
            return this;
        }

        public Builder CHANNEL_COUNT(int count) {
            CHANNEL_COUNT = count;
            return this;
        }

        public Builder CHANNEL_CONFIG(int channelConfig) {
            CHANNEL_CONFIG = channelConfig;
            return this;
        }
    }

    private RsMediaCodecAudio() {
        init();
    }

    public static RsMediaCodecAudio createInstance(Context context) {
        mContext = context;
        if (mCodecAudio == null) {
            mCodecAudio = new RsMediaCodecAudio();
        }
        return mCodecAudio;
    }

    public static MediaCodec getInstanceCodec() {
        return mAudioEncoder;
    }

    private void init() {
        mIsAudioRecord = false;
        eosReceived = false;
        mCodecListener = null;
    }

    @Override
    public void start() {
        try {
            mAudioEncoder.start();
        } catch (Exception e) {
            e.printStackTrace();
            mCodecListener.onAError();
        }
    }

    @Override
    public synchronized int queueInput(byte[] data, int dataLength, int codecType) {
        if (mIsStopVideo) return 0;
        if (mAudioEncoder == null) return 0;
        ByteBuffer inputBuffer;
        int inputBufferIndex = mAudioEncoder.dequeueInputBuffer(TIMEOUT_USEC);

        if (inputBufferIndex >= 0) {
            inputBuffer = mInputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(data, 0, dataLength);

            if (eosReceived) {
                mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, dataLength, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            } else {
                mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, dataLength, getAudioPresentationTimeUs(startTime), 0);
            }
        }

        audioQueueOutput(mOutputBuffers, mBufferInfo);

        return 0;
    }

    @Override
    public void stop() {
        eosReceived = true;

        if (!mIsStopVideo && mAudioEncoder != null) {
            byte[] byteDummy = new byte[10];
            queueInput(byteDummy, 10, RsMediaCodec.CODEC_AUDIO);
        }

        mCodecAudio = null;
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void close() {
    }

    @Override
    public void setCodecListener(ICodecListener listener) {
        mCodecListener = listener;
    }

    long startTime = 0;

    @Override
    public void setConfigure(RsMediaCodecVideo.Builder vBuilder, RsMediaCodecAudio.Builder aBuilder) {
//		SAMPLE_RATE = aBuilder.SAMPLE_RATE;
//		BIT_RATE = aBuilder.BIT_RATE;
        eosReceived = false;

        mBuilder = aBuilder;

        prepareEncoderAudio();
    }

    protected boolean prepareEncoderAudio() {
        MediaFormat audioFormat = MediaFormat.createAudioFormat(
                mBuilder.MIME_TYPE_AUDIO,
                mBuilder.SAMPLE_RATE,
                mBuilder.CHANNEL_COUNT
        );

        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBuilder.BIT_RATE);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384);

//		String strAudioMime = selectCodec(RecordSet.AUDIO_MIME_TYPE).getName();
//		mEncoderAudio = MediaCodec.createByCodecName(strAudioMime);

        try {
            mAudioEncoder = MediaCodec.createEncoderByType(mBuilder.MIME_TYPE_AUDIO);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        try {
            mAudioEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mAudioEncoder.start();
        } catch (IllegalStateException e) {
            mCodecListener.onAError();
            return false;
        }

        mIsStopVideo = false;

        mCodecListener.onAStart();

        mInputBuffers = mAudioEncoder.getInputBuffers();
        mOutputBuffers = mAudioEncoder.getOutputBuffers();

        startTime = System.currentTimeMillis();

        return true;
    }

    private boolean audioQueueOutput(ByteBuffer[] outputBuffers, MediaCodec.BufferInfo bufferInfo) {

        int outputBufferIndex = mAudioEncoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
        if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {

        } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

            mCodecListener.onADeqeueFormatChanged(mAudioEncoder.getOutputFormat());

        } else if (outputBufferIndex >= 0 && bufferInfo.size > 0) {
            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
            if (outputBuffer == null) {
                throw new RuntimeException("EncoderOutputBuffer " + eosReceived + " was null");
            }

            outputBuffer.position(bufferInfo.offset);
            outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

            mCodecListener.onADequeueOutput(outputBuffer, bufferInfo);

            mAudioEncoder.releaseOutputBuffer(outputBufferIndex, false);
        }

        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            stopAudio();
            return true;
        }

        return false;
    }

    public void stopAudio() {
        mIsStopVideo = true;

        try {
            if (mAudioEncoder != null) {
                mAudioEncoder.stop();
                mAudioEncoder.release();
                mAudioEncoder = null;
                mCodecListener.onAStop();
                mCodecListener = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mCodecListener.onAError();
        }
    }

}




