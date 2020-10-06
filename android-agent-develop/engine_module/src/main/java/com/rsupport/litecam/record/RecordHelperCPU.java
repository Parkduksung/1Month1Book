package com.rsupport.litecam.record;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaMuxer;

import com.rsupport.litecam.ScreenInfo;
import com.rsupport.litecam.media.ImageProcessing;
import com.rsupport.litecam.media.muxer.IMediaMuxerWrapper;
import com.rsupport.litecam.util.APICheck;
import com.rsupport.litecam.util.LLog;
import com.rsupport.litecam.util.RecordScreen;
import com.rsupport.litecam.util.RecordScreenHead;
import com.rsupport.util.log.RLog;


@SuppressLint("NewApi")
public class RecordHelperCPU extends RecordManager {

    public RecordHelperCPU() {
        super();
    }

    @Override
    protected boolean initRecord() {
        if (mColorFormat <= 0) {
            if ((mColorFormat = selectColorFormat(RecordSet.VIDEO_MIME_TYPE)) <= 0) {
                LLog.e(true, "ColorFormat error : " + mColorFormat);
                return false;
            }
        }

        muxerWrapper = IMediaMuxerWrapper.mediaMuxerInit(
                mRecordFormat.getString(RecordFormat.KEY_FILE_PATH),
                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4,
                mRecordFormat.getBoolean(RecordFormat.KEY_IS_AUDIORECORD));
        muxerWrapper.setOrientationHint(RecordScreen.getDegress(mResolutionInfo.rotate));

        if (!prepareEncoderVideo()) {
            throw new RuntimeException("prepareEncoderVideo error");
        }

        if (mRecordFormat.getBoolean(RecordFormat.KEY_IS_AUDIORECORD)) {
            try {
                if (!prepareEncoderAudio()) {
                    throw new RuntimeException("prepareEncoderAudio error");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (RecordSet.ISWatermark) {
            String apkFilePath = null;
            ApplicationInfo appInfo = null;
            PackageManager packMgmr = mContext.getPackageManager();
            try {
                LLog.w("mContext.getPackageName() : " + mContext.getPackageName());
                appInfo = packMgmr.getApplicationInfo(mContext.getPackageName(), 0);
            } catch (NameNotFoundException e) {
                throw new RuntimeException("Unable to locate assets, aborting...");
            }

            apkFilePath = appInfo.sourceDir;
            ImageProcessing.nativeOverlayImageLoad(apkFilePath,
                    mRecordFormat.getWatermarkImage(RecordFormat.KEY_WATERMARK_IMAGE).getWatermarkName(mContext, mRecordFormat, mRecordFormat.getString(RecordFormat.KEY_VIDEO_RATIO)), mResolutionInfo.rotate);
        }

        return true;
    }

    @Override
    protected long getVideoPresentationTimeUs() {
        return muxerWrapper.getVideoPresentationTimeUs(mStartWhen);
    }

    @Override
    protected void offerVideoDataEncoder() throws InterruptedException {
        try {
            mEncoderVideo.start();

        } catch (Exception e) {
            throw new RuntimeException("Video start exception " + e);
        }

        ScreenInfo.CaptureFrame encData;
        ByteBuffer[] inputBuffers = mEncoderVideo.getInputBuffers();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer[] outputBuffers = mEncoderVideo.getOutputBuffers();

        int frameBufferSize = mResolutionInfo.dstScreenSize.x * mResolutionInfo.dstScreenSize.y * 3 / 2;
        byte[] yuvTempBuffer = new byte[frameBufferSize];

        boolean inputDone = false;
        boolean outputDone = false;

        long lastTime = 0;

        // 4.2.2 이하 버전에는 MediaCodec.INFO_OUTPUT_FORMAT_CHANGED를 호출하지 않아 미리 실행
        if (!APICheck.isNewAPISupportVersion())
            muxerStart();

        while (!outputDone) {
            if (!inputDone) {
                try {
                    int inputBufferIndex = mEncoderVideo.dequeueInputBuffer(-1);

                    if (inputBufferIndex >= 0) {
                        if (eosReceived) {
                            // Send an empty frame with the end-of-stream flag set.  If we set EOS
                            // on a frame with data, that frame data will be ignored, and the
                            // output will be short one frame.
                            mEncoderVideo.queueInputBuffer(inputBufferIndex, 0, 0, (long) lastTime, MediaCodec.BUFFER_FLAG_END_OF_STREAM);

                            inputDone = true;

                        } else {
                            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                            inputBuffer.clear();

                            mSemaphore.acquire();

                            encData = getVideoData();
                            if (encData == null)
                                continue;

                            synchronized (mVideoInputBuffers) {
                                if (RecordSet.ISWatermark) {
                                    ImageProcessing.imageOverlay(mVideoInputBuffers[encData.inputBufferIndex].array(), mResolutionInfo, yuvTempBuffer, mColorFormat);
                                    inputBuffer.put(yuvTempBuffer);
                                    mVideoInputBuffers[encData.inputBufferIndex].clear();

                                } else {
                                    LLog.d(false, "inputBuffer.capacity() : " + inputBuffer.capacity() + " capture.capacity() : " + mVideoInputBuffers[encData.inputBufferIndex].capacity());
                                    if (RecordSet.RS_COLOR_FORMAT == RecordScreenHead.RGB) {
                                        ImageProcessing.convertColor(mVideoInputBuffers[encData.inputBufferIndex].array(), RecordSet.RS_COLOR_FORMAT, mResolutionInfo, yuvTempBuffer, mColorFormat);
                                        inputBuffer.put(yuvTempBuffer);

                                    } else { // RGB를 제외한 NV12, I420일 경우
                                        inputBuffer.put(mVideoInputBuffers[encData.inputBufferIndex].array(), 0, frameBufferSize);
//									try {
//										inputBuffer.put(mVideoInputBuffers[encData.inputBufferIndex].array(), 0, frameBufferSize);
//										
//									} catch (java.lang.IndexOutOfBoundsException e) { // IndexOutOfBoundsException 처리
//										ImageProcessing.convertColor(mVideoInputBuffers[encData.inputBufferIndex].array(), RecordSet.RS_COLOR_FORMAT, mResolutionInfo, yuvTempBuffer, mColorFormat);
//										inputBuffer.put(yuvTempBuffer, 0, frameBufferSize);
//										
//									} catch (BufferOverflowException e) { // BufferOverflowException 처리
//										ImageProcessing.convertColor(mVideoInputBuffers[encData.inputBufferIndex].array(), RecordSet.RS_COLOR_FORMAT, mResolutionInfo, yuvTempBuffer, mColorFormat);
//										inputBuffer.put(yuvTempBuffer, 0, frameBufferSize);
//									}
                                    }
                                    mVideoInputBuffers[encData.inputBufferIndex].clear();
                                }
                            }

                            mEncoderVideo.queueInputBuffer(inputBufferIndex, 0, frameBufferSize, (long) encData.time, 0);
                            lastTime = encData.time;
                        }
                    }

                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            outputDone = videoQueueOutput(outputBuffers, bufferInfo);
        }
    }

    @Override
    protected void muxerStart() {
        if (mEncoderVideo != null) {
            Object format = muxerWrapper.getVideoTrackInfo(
                    APICheck.isNewAPISupportVersion() ? mEncoderVideo.getOutputFormat() : null,
                    mResolutionInfo.alignedScreenSize,
                    RecordSet.FRAME_RATE);
            // now that we have the Magic Goodies, start the muxer
            videoTrackIndex = muxerWrapper.addTrack(format);
        }

        if (mEncoderAudio != null) {
            Object format = muxerWrapper.getAudioTrackInfo(
                    APICheck.isNewAPISupportVersion() ? mEncoderAudio.getOutputFormat() : null,
                    mRecordFormat.getInteger(RecordFormat.KEY_SAMPLE_RATE),
                    RecordSet.AUDIO_CHANNEL_COUNT,
                    RecordSet.DEFAULT_AUDIO_BIT_RATE);
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
        try {
            mEncoderVideo.start();

        } catch (Exception e) {
            throw new RuntimeException("Video start exception " + e);
        }

        ScreenInfo.CaptureFrame encData;
        ByteBuffer[] inputBuffers = mEncoderVideo.getInputBuffers();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer[] outputBuffers = mEncoderVideo.getOutputBuffers();

        int frameBufferSize = mResolutionInfo.dstScreenSize.x * mResolutionInfo.dstScreenSize.y * 3 / 2;
        byte[] yuvTempBuffer = new byte[frameBufferSize];

        boolean inputDone = false;
        boolean outputDone = false;

        long lastTime = 0;

        while (!outputDone) {
            if (!inputDone) {
                try {
                    int inputBufferIndex = mEncoderVideo.dequeueInputBuffer(-1);

                    if (inputBufferIndex >= 0) {
                        if (eosReceived) {
                            // Send an empty frame with the end-of-stream flag set.  If we set EOS
                            // on a frame with data, that frame data will be ignored, and the
                            // output will be short one frame.
                            mEncoderVideo.queueInputBuffer(inputBufferIndex, 0, 0, (long) lastTime, MediaCodec.BUFFER_FLAG_END_OF_STREAM);

                            inputDone = true;

                        } else {
                            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                            inputBuffer.clear();

                            mSemaphore.acquire();

                            encData = getVideoData();
                            if (encData == null)
                                continue;

                            synchronized (mVideoInputBuffers) {
                                if (RecordSet.ISWatermark) {
                                    ImageProcessing.imageOverlay(mVideoInputBuffers[encData.inputBufferIndex].array(), mResolutionInfo, yuvTempBuffer, mColorFormat);
                                    inputBuffer.put(yuvTempBuffer);
                                    mVideoInputBuffers[encData.inputBufferIndex].clear();

                                } else {
                                    LLog.d(false, "inputBuffer.capacity() : " + inputBuffer.capacity() + " capture.capacity() : " + mVideoInputBuffers[encData.inputBufferIndex].capacity());
                                    if (RecordSet.RS_COLOR_FORMAT == RecordScreenHead.RGB) {
                                        ImageProcessing.convertColor(mVideoInputBuffers[encData.inputBufferIndex].array(), RecordSet.RS_COLOR_FORMAT, mResolutionInfo, yuvTempBuffer, mColorFormat);
                                        inputBuffer.put(yuvTempBuffer);
                                        RLog.v("------------------------------------- offerVideoDataStreamEncoder : doing");

                                    } else { // RGB를 제외한 NV12, I420일 경우
                                        inputBuffer.put(mVideoInputBuffers[encData.inputBufferIndex].array(), 0, frameBufferSize);
//									try {
//										inputBuffer.put(mVideoInputBuffers[encData.inputBufferIndex].array(), 0, frameBufferSize);
//										
//									} catch (java.lang.IndexOutOfBoundsException e) { // IndexOutOfBoundsException 처리
//										ImageProcessing.convertColor(mVideoInputBuffers[encData.inputBufferIndex].array(), RecordSet.RS_COLOR_FORMAT, mResolutionInfo, yuvTempBuffer, mColorFormat);
//										inputBuffer.put(yuvTempBuffer, 0, frameBufferSize);
//										
//									} catch (BufferOverflowException e) { // BufferOverflowException 처리
//										ImageProcessing.convertColor(mVideoInputBuffers[encData.inputBufferIndex].array(), RecordSet.RS_COLOR_FORMAT, mResolutionInfo, yuvTempBuffer, mColorFormat);
//										inputBuffer.put(yuvTempBuffer, 0, frameBufferSize);
//									}
                                    }
                                    mVideoInputBuffers[encData.inputBufferIndex].clear();
                                }
                            }

                            mEncoderVideo.queueInputBuffer(inputBufferIndex, 0, frameBufferSize, (long) encData.time, 0);
                            lastTime = encData.time;
                        }
                    }

                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            outputDone = videoQueueOutputStream(outputBuffers, bufferInfo);
        }
    }

}