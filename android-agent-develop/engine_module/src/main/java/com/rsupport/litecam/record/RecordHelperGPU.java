package com.rsupport.litecam.record;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;

import com.rsupport.litecam.ScreenInfo;
import com.rsupport.litecam.media.muxer.IMediaMuxerWrapper;
import com.rsupport.litecam.util.LLog;
import com.rsupport.litecam.util.RecordScreen;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class RecordHelperGPU extends RecordManager {
    private RecordHelperGPUInput mRecordInput;

    public RecordHelperGPU() {
        super();
        mColorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
    }

    /**
     * Muxer 초기화 시 Muxer의 rotate를 {@link com.rsupport.litecam.record.ScreenInfo.ScreenInfo#rotate} 의 값을 참고하여,
     * {@link com.rsupport.litecam.util.RecordScreen#DisplayUtil()} 을 통해 실제 화면 회전 값을 가져와 셋팅한다.
     */
    @Override
    protected boolean initRecord() {
        muxerWrapper = IMediaMuxerWrapper.mediaMuxerInit(
                mRecordFormat.getString(RecordFormat.KEY_FILE_PATH),
                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4,
                mRecordFormat.getBoolean(RecordFormat.KEY_IS_AUDIORECORD));
        muxerWrapper.setOrientationHint(RecordScreen.getDegress(mResolutionInfo.rotate));

        videoTrackIndex = -1;
        if (!prepareEncoderVideo()) {
            throw new RuntimeException("prepareEncoderVideo error");
        }

        if (mRecordFormat.getBoolean(RecordFormat.KEY_IS_AUDIORECORD)) {
            try {
                audioTrackIndex = -1;
                if (!prepareEncoderAudio()) {
                    throw new RuntimeException("prepareEncoderAudio error");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    @Override
    protected long getVideoPresentationTimeUs() {
//		return (long) (System.currentTimeMillis() - mStartWhen) * 1000 * 1000;
        return muxerWrapper.getVideoPresentationTimeUs(mStartWhen) * 1000;
    }

    @Override
    protected void offerVideoDataEncoder() throws InterruptedException {
        try {
            mRecordInput = new RecordHelperGPUInput(mContext, mEncoderVideo.createInputSurface(), mResolutionInfo);
            mEncoderVideo.start();

            mRecordInput.prepareRenderer(mRecordFormat);

            boolean inputDone = false;
            boolean outputDone = false;

            ScreenInfo.CaptureFrame encData;
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            ByteBuffer[] outputBuffers = mEncoderVideo.getOutputBuffers();

            while (!outputDone) {
                if (!inputDone) {
                    if (eosReceived) {
                        mEncoderVideo.signalEndOfInputStream();
                        inputDone = true;

                    } else {
                        mSemaphore.acquire();

                        encData = getVideoData();
                        if (encData == null)
                            continue;

                        synchronized (mVideoInputBuffers) {
                            mRecordInput.generateSurfaceFrame(mVideoInputBuffers[encData.inputBufferIndex]);
                        }
                        mRecordInput.setPresentationTime(encData.time);

                        if (!mRecordInput.swapBuffers()) {
                            LLog.w("swapBuffer error");
                        }

                        synchronized (mVideoInputBuffers) {
                            mVideoInputBuffers[encData.inputBufferIndex].clear();
                        }
                    }
                }

                outputDone = videoQueueOutput(outputBuffers, bufferInfo);
            }

        } finally {
            LLog.d("Video record end");
            if (mRecordInput != null) {
                mRecordInput.release();
                mRecordInput = null;
            }
        }

    }

    @Override
    protected void muxerStart() {
        if (mEncoderVideo != null) {
            MediaFormat format = mEncoderVideo.getOutputFormat();
            // now that we have the Magic Goodies, start the muxer
            videoTrackIndex = muxerWrapper.addTrack(format);
        }

        if (mEncoderAudio != null) {
            MediaFormat format = mEncoderAudio.getOutputFormat();
            // Log.d(TAG, "encoder output format changed: " + format);
            // now that we have the Magic Goodies, start the muxer
            audioTrackIndex = muxerWrapper.addTrack(format);
        }
    }

    @Override
    protected void writeSampleData(int trackCheck, ByteBuffer byteBuf, BufferInfo bufferInfo) {
        if (muxerWrapper == null) return;

        switch (trackCheck) {
            case VIDEO_TRACK_INDEX:
                muxerWrapper.writeSampleData(videoTrackIndex, byteBuf, bufferInfo);
                break;

            case AUDIO_TRACK_INDEX:
                muxerWrapper.writeSampleData(audioTrackIndex, byteBuf, bufferInfo);
                break;
        }
    }

    @Override
    protected void offerVideoDataStreamEncoder() throws InterruptedException {
    }

}
