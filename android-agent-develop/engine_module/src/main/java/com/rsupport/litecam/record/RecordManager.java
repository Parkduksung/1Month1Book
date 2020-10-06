/*******************************************************************************
 *      ______   _____     __    __  _____    _____    _____     ______   _______
 *     / ___  | / ____|   / /   / / / __  |  / ___ |  / __  |   / ___  | |___  __|
 *    / /__/ / | |____   / /   / / / /  | | / /  | | / /  | |  / /__/ /     / /
 *   / ___  |  |____  | / /   / / / /__/ / / /__/ /  | |  | | / ___  |     / /
 *  / /   | |   ____| | | |__/ / /  ____/ /  ____/   | |_/ / / /   | |    / /
 * /_/    |_|  |_____/  |_____/ /__/     /__/        |____/ /_/    |_|   /_/
 *
 ********************************************************************************
 *
 * Copyright (c) 2014 RSUPPORT Co., Ltd. All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property
 * of RSUPPORT Company Limited and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to RSUPPORT
 * Company Limited and its suppliers and are protected by trade secret
 * or copyright law. Dissemination of this information or reproduction
 * of this material is strictly forbidden unless prior written permission
 * is obtained from RSUPPORT Company Limited.
 *
 * FileName : RecordManager.java
 * Author : Kwon Taehwan
 * Date : 2014-03
 * Purpose : liteCam Video/Audio 데이터를 Encoder 하기위한 Thread.
 *
 * [History]
 * - 2014-03 최초 작성
 *******************************************************************************/

package com.rsupport.litecam.record;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;

import com.rsupport.litecam.ScreenInfo;
import com.rsupport.litecam.ScreenInfo.CaptureFrame;
import com.rsupport.litecam.ScreenInfo.ResolutionInfo;
import com.rsupport.litecam.media.MP4MediaMuxer;
import com.rsupport.litecam.media.muxer.IMediaMuxerWrapper;
import com.rsupport.litecam.media.muxer.IMediaMuxerWrapper.IMuxerWapperListener;
import com.rsupport.litecam.util.APICheck;
import com.rsupport.litecam.util.AvailableCapacityCheck;
import com.rsupport.litecam.util.DirectoryManage;
import com.rsupport.litecam.util.LLog;
import com.rsupport.litecam.util.RecordScreen;
import com.rsupport.litecam.util.WatermarkImage;
import com.rsupport.util.log.RLog;

/**
 * Video/Audio 녹화를 위한 RecordManager는 화면과 사운드(MIC)를 통하여 인코딩을 시작한다.
 * {@link #create} 함수를 통해 create 한다.
 * Android Version 따라서 사용법이 달라지며, Android JellyBean 4.3 (API 18) 버전과 Android JellyBean 4.2.2 (API 17) 이하 버전으로 분리
 *
 * <p><b>JellyBean 4.3 (API 18) 이상</b>
 * Android API의 {@link android.media.MediaMuxer}와 {@link MediaCodec} 사용하고, GPU를 통한 화면 색상값 변환을 기본으로 한다.
 * GPU 코드는 {@link RecordHelperGPUInput} 에서 실제 화면 인코딩을 시작한다.
 * 4.3 이상은 CPU/GPU 모두 사용이 가능하며 {@link #create}의 isGPU flag 값을 설정한다.<br>
 * <b>GPU 사용</b> : TRUE<br>
 * <b>CPU 사용</b> : FALSE
 *
 *
 * <p>JellyBean 4.2.2 (API 17) 이하
 * Android API의 {@link MediaCodec}과 {@link MP4MediaMuxer} 사용하고, CPU를 통한 화면 색상값 변환을 기본으로 한다.
 * 4.2.2 이하 버전은 GPU를 통한 화면 변환이 불가능하여, CPU/GPU 선택이 불가능하다.
 *
 * <p>create 후 끝나면 {@link #initRecordManager}
 * 함수를 호출하여 초기화 한다. 초기화시 사운드는 제외할 수 있다.
 *
 *
 * <p>RecordManager is generally used like this:
 * <pre>
 * RecordManager recordManager = RecordManager.create(true/false);
 * recordManager.configure(context, screenSize, RecordFormat);
 *
 * ByteBuffer inputBuffers[] = recordManager.getInputBuffers(mCaptureAshmem.getMemorySize());
 * byte[] tempBuffer = new byte[inputBuffers[0].capacity()];
 * ByteBuffer inputBuffer;
 * int inputBufferIndex = -1; // 항상 -1을 시작으로 하면 된다.
 *
 * inputBufferIndex = recordManager.dequeueInputBuffer(-1);
 * if (inputBufferIndex >= 0) {
 * 	// inputBuffer에 채워넣을 준비 상태일 경우
 *  ...
 * 	inputBuffer = inputBuffers[inputBufferIndex];
 * 	inputBuffer.clear();
 * 	inputBuffer.put(tempBuffer);
 * 	inputBuffer.flip();
 * 	...
 * 	recordManager.queueInputBuffer(inputBufferIndex);
 *
 * } else if (inputBufferIndex == RecordManager.INFO_TRY_AGAIN_LATER) {
 * 	// Temp Buffer가 존재하지 않아 다시 시도해야 하는 경우입니다.
 * 	continue;
 * }
 *
 * // {@link #isExceededCapacity}을 이용하여 용량계산에 대한 return 처리.
 * switch (recordManager.isExceededCapacity()) {
 * 	case RecordManager.EXCEEDED_CAPACITY_SYSTEM:
 * 	...
 * 	break;
 *
 * 	case RecordManager.EXCEEDED_CAPACITY_FILE:
 * 	...
 * 	break;
 * }
 *
 * if (recordManager != null) {
 * 		recordManager.close();
 * }
 * </pre>
 *
 * <p>{@link #getInputBuffers}에 buffer의 사이즈를 넘겨준다. -1을 넘겨줄경우 (가로 x 세로 x 4)의 사이즈로 temp buffer를 초기화 한다.
 * 생성된 buffer 중 empty buffer에 대한 번호를 넘겨주는 것이 {@link #dequeueInputBuffer}이며 -1을 넘겨주어야 한다.
 * fill buffer를 한 후 {@link #queueInputBuffer}를 호출하여, encoder를 호출한다.
 *
 * <p><b>메모리 체크</b>
 * {@link android.media.MediaMuxer}에서 인코딩 중인 파일의 크기는 2 GB를 넘길 수 없다. 해당 용량을 체크하는 코드를 추가하여야 하며, 시스템상의 용량도
 * 최소 {@link AvailableCapacityCheck#REFERENCE_MEMORY} 이하로 떨어지는 경우 {@link LiteCamBinder#onCaptureStop}을 호출하여 Encoder를 정지해주어야 한다.
 *
 * <p>{@link #close}를 호출하여 Encoder를 종료한다.
 */
@SuppressLint("NewApi")
public abstract class RecordManager {
    private static final boolean IS_TEST = false;

    /**
     * Muxer에 데이터를 저장하기위한 변수로 Video 0, Audio 1 로 정의한다.
     * {@link #writeSampleData}, 변수와 4.2.2 이하 Muxer에서 사용한다.
     */
    protected static final int VIDEO_TRACK_INDEX = 0;
    protected static final int AUDIO_TRACK_INDEX = 1;

    /**
     * If a non-negative timeout had been specified in the call
     * to {@link #dequeueInputBuffer}, indicates that the call timed out.
     */
    public static final int INFO_TRY_AGAIN_LATER = -1;


    private static final int TIMEOUT_USEC = 10000;

    /**
     * Input Buffer 수를 정한다.
     * {@link #getInputBuffers}에서 Buffer를 초기화 할 때 사용된다.
     */
    private static final int INPUT_BUFFER_COUNT = 7;

    protected Context mContext;
    protected ResolutionInfo mResolutionInfo;

    protected IMediaMuxerWrapper muxerWrapper;

    /**
     * Thread 종료 확인을 위한 변수.
     * 기본값은 false이며 true시 모든 Encoder Thread가 종료된다.
     */
    protected boolean eosReceived;

    protected RecordFormat mRecordFormat;


    // ///////////////////////////////////////// Muxer
    /**
     * Encoder의 시작시간을 저장한다.
     * {@link #queueInputBuffer} 에서 시작과 동시에 시작시간을 기록한다.
     */
    protected long mStartWhen;

    /**
     * Muxer의 실제 Track 번호를 저장한다.
     * {@link #muxerStart} 에서 addTrack시에 Track의 번호가 결정된다.
     */
    protected int videoTrackIndex = -1;
    protected int audioTrackIndex = -1;


    // ///////////////////////////////////////// Video encoder
    /**
     * {@link MediaCodec}을 이용하여 Video Encoder를 생성
     */
    protected MediaCodec mEncoderVideo;

    /**
     * Video input/output Thread 변수
     * <p>{@link #offerVideoDataEncoder} 에서 video input
     * {@link #videoDataOutput} 에서 video output
     */
    protected Thread videoOfferThread;
    protected Thread videoOutThread;

    /**
     * video capture queue
     * 캡쳐한 data를 queue에 저장하는 변수
     */
    private Queue<ScreenInfo.CaptureFrame> mCaptureQueue;

    /**
     * video input에 대한 lock.
     */
    protected Semaphore mSemaphore;

    /**
     * video input에 대한 lock.
     */
    protected Semaphore muxerWapperSemaphore;

    /**
     * Video temp buffer 생성
     * <p>{@link #INPUT_BUFFER_COUNT}의 수만큼 buffer를 생성하며,
     * {@link #dequeueInputBuffer}의 함수에서 size에 따라서 buffer 크기 정한다.
     */
    protected ByteBuffer[] mVideoInputBuffers;

    /**
     * Video의 인코딩 color format의 정보를 가진다.
     */
    protected int mColorFormat;


    // ///////////////////////////////////////// Audio encoder
    /**
     * {@link MediaCodec}을 이용하여 Audio Encoder를 생성
     */
    protected MediaCodec mEncoderAudio;

    /**
     * Audio input/output Thread 변수
     * <p>{@link #offerAudioDataEncoder} 에서 Audio input
     * {@link #audioDataOutput} 에서 Audio output
     */
    protected Thread audioOfferThread;


    // ///////////////////////////////////////// Audio Recorder
    /**
     * {@link AudioRecord} API를 사용하여 Audio 녹음을 하기 위한 변수
     */
    private AudioRecord mAudioRecord = null;

    /**
     * {@link AudioRecord#getMinBufferSize}에서 계산되는 Audio min buffer size 값을 가지고 있다.
     * {@link #offerAudioDataEncoder}에서 Audio 데이터를 일정부분 가져오기 위해서 사용.
     */
    private int mAudioMinBufferSize;


    /**
     * Video/Audio encoder를 init 한다. init은 내부에서만 처리하며, {@link #prepareEncoderVideo}와
     * {@link #prepareEncoderAudio}에 대한 초기화를 진행한다.
     *
     * <p>GPU 처리 : {@link #mColorFormat}을 {@link CodecCapabilities#COLOR_FormatSurface}으로 설정하여 처리.
     * <p>CPU 처리 : {@link #mColorFormat}을 안드로이드 기기에 따라 다르게 처리
     *
     * @return
     */
    protected abstract boolean initRecord();

    /**
     * Video에 대한 처리를 담당하며, GPU/CPU 처리에 따라서 offer 가 달라 abstract로 정의
     *
     * @throws InterruptedException
     */
    protected abstract void offerVideoDataEncoder() throws InterruptedException;

    protected abstract void offerVideoDataStreamEncoder() throws InterruptedException;

    /**
     * Android version에 따라서 muxer 처리가 달라진다.
     * <p>Android 4.3 이상은 {@link android.media.MediaMuxer}로 처리하며,
     * Android 4.2.2 이하는 {@link MP4MediaMuxer}로 처리한다.
     *
     * <p>muxer 초기화 시 {@link #videoTrackIndex}와 {@link #audioTrackIndex}를 초기화 한다
     */
    protected abstract void muxerStart();

    /**
     * Video/Audio의 Encoder byte를 저장한다.
     * Android 4.3 이상은 {@link android.media.MediaMuxer}로 MP4 파일 저장한다.
     * Android 4.2.2 이하는 {@link MP4MediaMuxer}로 MP4 파일 저장한다.
     *
     * @param trackCheck track의 INDEX 번호로 {@link #VIDEO_TRACK_INDEX}와 {@link #AUDIO_TRACK_INDEX} 가 된다.
     * @param byteBuf    encoder 한 bytebuffer
     * @param bufferInfo encoder 한 {@link BufferInfo}를 넘겨준다.
     */
    protected abstract void writeSampleData(int trackCheck, ByteBuffer byteBuf, BufferInfo bufferInfo);

    /**
     * Video의 Presentation time을 계산한다.
     *
     * @return time
     */
    protected abstract long getVideoPresentationTimeUs();

    RandomAccessFile mRandomFile;

    private void initRandomFile() {
        try {
            File f = new File("/sdcard", "rsrecord.tmp");
            if (f.exists()) {
                f.delete();
                f.createNewFile();
            }
            mRandomFile = new RandomAccessFile(f, "rw");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * RecordManager 생성자
     * {@link #eosReceived} : Thread 종료를 위한 변수 (default : false)
     * {@link #mCaptureQueue} 초기화
     * {@link #mEncoderTotalSize} : encoder 중인 data 크기값을 저장 (default : 0)
     * {@link #mStartWhen} : 시작시간을 저장 (defalut : 0)
     * {@link #mSemaphore} : 동기화를 위한 Semaphore 초기화
     */
    protected RecordManager() {
        initRandomFile();

        mColorFormat = selectColorFormat(RecordSet.VIDEO_MIME_TYPE); // color com.rsupport.setting

        eosReceived = false;
        RecordSet.ENCODER_TOTAL_SIZE = 0;

        mStartWhen = 0;

        mSemaphore = new Semaphore(0);
        mCaptureQueue = new LinkedList<ScreenInfo.CaptureFrame>();
    }

    /**
     * RecordManager를 Android Version을 확인 하여 create 한다.<p>
     * <p>
     * JellyBean 4.3 (API 18) 이상 isGPU 변수를 통해 CPU/GPU를 구분하여 사용이 가능하며, {@link android.media.MediaCodec}, {@link android.media.MediaMuxer}를 이용한다.
     * JellyBean 4.2.2 (API 17) 이하는 CPU만 사용이 가능하며, {@link android.media.MediaCodec}, {@link com.rsupport.litecam.media.MP4MediaMuxer}를 이용한다.
     *
     * @param isGPU
     * @return RecordManger의 생성
     */
    public static RecordManager create(boolean isGPU) {
        RecordManager manager = null;
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR1 || Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN || !APICheck.getImageTransformation(isGPU)) {
            LLog.i("Record is cpu");
            manager = new RecordHelperCPU();

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            LLog.i("Record is gpu");
            manager = new RecordHelperGPU();
        }

        return manager;
    }

    public void setResolution(ResolutionInfo resolutionInfo) {
        mResolutionInfo = resolutionInfo;
    }

    /**
     * Record를 초기화 진행한다.<p>
     * <p>
     * 사전 작업으로 {@link DirectoryManage#createDirectoryAndTempFile}메소드를 통해 Temp 폴더, 파일을 생성하고,
     * {@link RecordScreen#getScreenSize}와 {@link AvailableCapacityCheck#getMemoryCheckInit}를 초기화 해야한다.<p>
     * <p>
     * Video는 {@link #create} 함수에 따라서 Android 4.3 이상과 4.2.2 이하 분리 및 GPU 사용 유무에 따라서 초기화가 진행.
     * {@link com.rsupport.litecam.record.ScreenInfo.ScreenInfo}의 값을 확인하여 저장한다.
     *
     * @param context
     * @param screenInfo
     * @param format
     * @return 초기화가 완료되면 TRUE, 실패하면 FALSE
     */
    public boolean config(Context context, ResolutionInfo resolutionInfo, RecordFormat format) {
        mContext = context;
        mResolutionInfo = resolutionInfo;
        mRecordFormat = format;

        Validation();

        // Temp 파일과 폴더를 생성한다.
        if (!(mRecordFormat.getString(RecordFormat.KEY_FILE_FULL_NAME, "").equals(""))) {
            RecordSet.FILE_PATH = DirectoryManage.createTempFile(DirectoryManage.createDirectoryAndTempFile(format.getString(RecordFormat.KEY_DIRECTORY_PATH)), mRecordFormat.getString(RecordFormat.KEY_FILE_FULL_NAME));

        } else {
            RecordSet.FILE_PATH = DirectoryManage.createTempFile(DirectoryManage.createDirectoryAndTempFile(format.getString(RecordFormat.KEY_DIRECTORY_PATH)), mRecordFormat.getString(RecordFormat.KEY_FILE_NAME), RecordSet.FILE_DATE_TYPE);
        }

        format.setString(RecordFormat.KEY_FILE_PATH, RecordSet.FILE_PATH);

        return initRecord();
    }

    /**
     * Record에 필요한 기본값을 정의합니다.
     */
    private void Validation() {
        mRecordFormat.setString(RecordFormat.KEY_FILE_NAME, mRecordFormat.getString(RecordFormat.KEY_FILE_NAME, RecordSet.DEFAULT_FILE_NAME));
        LLog.i(true, "File name : " + mRecordFormat.getString(RecordFormat.KEY_FILE_NAME));
        mRecordFormat.setString(RecordFormat.KEY_DIRECTORY_PATH, mRecordFormat.getString(RecordFormat.KEY_DIRECTORY_PATH, Environment.getExternalStorageDirectory().getPath() + "" + RecordSet.DEFAULT_FILE_PATH));
        LLog.i(true, "File path : " + mRecordFormat.getString(RecordFormat.KEY_DIRECTORY_PATH));

        // bitrate의 기본값이 0 이면
        // ((screen_width) * (screen_height) * 30(FPS) * 0.2) * 2;
//		mRecordFormat.setInteger(RecordFormat.KEY_VIDEO_BIT_RATE, (int) (mResolutionInfo.screenSize.x * mResolutionInfo.screenSize.y * 30 * 0.2) * 2);

        mRecordFormat.setInteger(RecordFormat.KEY_VIDEO_BIT_RATE, mRecordFormat.getInteger(RecordFormat.KEY_VIDEO_BIT_RATE, RecordSet.DEFAULT_VIDEO_BIT_RATE));
        LLog.i(true, "Video bitrate : " + mRecordFormat.getInteger(RecordFormat.KEY_VIDEO_BIT_RATE));
        mRecordFormat.setInteger(RecordFormat.KEY_SAMPLE_RATE, mRecordFormat.getInteger(RecordFormat.KEY_SAMPLE_RATE, RecordSet.DEFAULT_SAMPLE_RATE));
        LLog.i(true, "Audio samplerate : " + mRecordFormat.getInteger(RecordFormat.KEY_SAMPLE_RATE));
        mRecordFormat.setBoolean(RecordFormat.KEY_IS_AUDIORECORD, mRecordFormat.getBoolean(RecordFormat.KEY_IS_AUDIORECORD, RecordSet.DEFAULT_AUDIORECORD));
        LLog.i(true, "is Audio : " + mRecordFormat.getBoolean(RecordFormat.KEY_IS_AUDIORECORD));
        mRecordFormat.setWatermarkImage(RecordFormat.KEY_WATERMARK_IMAGE, mRecordFormat.getWatermarkImage(RecordFormat.KEY_WATERMARK_IMAGE, new WatermarkImage()));
    }

    /**
     * 시스템 용량 초과시 return. 용량은 {@link AvailableCapacityCheck#REFERENCE_MEMORY}
     */
    public static final int EXCEEDED_CAPACITY_SYSTEM = -2;

    /**
     * 녹화 중인 파일의 용량 초과시 return. 용량은 {@link AvailableCapacityCheck#WRITE_BASE_SIZE}
     */
    public static final int EXCEEDED_CAPACITY_FILE = -3;

    /**
     * 용량이 정상일때 return.
     */
    public static final int CAPACITY_OK = 0;

    /**
     * System의 용량과 녹화 진행 중인 파일의 용량을 체크.
     * System의 용량은 {@link AvailableCapacityCheck#REFERENCE_MEMORY}에 따라서 정해지며,
     * 용량을 초과시 {@link #EXCEEDED_CAPACITY_SYSTEM}이 return 되어 녹화 종료를 유도.<p>
     * <p>
     * 녹화 중인 파일의 용량을 체크하여 초과시 {@link #EXCEEDED_CAPACITY_FILE}을 return 하고,
     * {@link AvailableCapacityCheck#WRITE_BASE_SIZE}에 따라 정의
     *
     * <p>isExceededCapacity is generally used like this:
     * <pre>
     * switch (mRecordManager.isExceededCapacity()) {
     * case RecordManager.EXCEEDED_CAPACITY_SYSTEM:
     * 	...
     * 	break;
     * case RecordManager.EXCEEDED_CAPACITY_FILE:
     * 	...
     * 	break;
     * }</pre>
     *
     * @return 용량초과가 없으면 {@link #CAPACITY_OK}를 return.
     */
    public int isExceededCapacity() {
        if (!AvailableCapacityCheck.create().isRecordAvailableCapacitySize(mRecordFormat.getString(RecordFormat.KEY_DIRECTORY_PATH))) {
            return EXCEEDED_CAPACITY_SYSTEM;
        }

        if (isNowEncoderFileSizeExceeded()) {
            return EXCEEDED_CAPACITY_FILE;
        }

        return CAPACITY_OK;
    }

    /**
     * 저장되는 encoder data의 실제 사이즈를 확인한다.
     * Android {@link android.media.MediaMuxer} 에서 2GB의 용량을 초과할 경우 그냥 block 처리되므로 2GB가 넘지 않도록 처리.
     *
     * @return 현재 Encoder의 data size가 {@link com.rsupport.litecam.util.AvailableCapacityCheck#WRITE_BASE_SIZE} 작으면 TRUE.
     */
    private boolean isNowEncoderFileSizeExceeded() {
        return RecordSet.ENCODER_TOTAL_SIZE > AvailableCapacityCheck.WRITE_BASE_SIZE ? true : false;
    }

    /**
     * Video Encoder를 하기 위한 Temp buffer 생성한다.
     * size가 -1이면 Screen 사이즈의 (width * height * 4)를 Temp buffer의 사이즈가 된다.
     *
     * @param size
     * @return 생성된 ByteBuffer를 return 한다.
     */
    public ByteBuffer[] getInputBuffers(int size) {
        if (size == -1)
            size = mResolutionInfo.screenSize.x * mResolutionInfo.screenSize.y * 4;

        mVideoInputBuffers = new ByteBuffer[INPUT_BUFFER_COUNT];

        for (int i = 0; i < INPUT_BUFFER_COUNT; ++i) {
            try {
                mVideoInputBuffers[i] = ByteBuffer.allocateDirect(size);

            } catch (java.lang.IllegalArgumentException e) {
                mVideoInputBuffers[i] = ByteBuffer.allocate(size);
            }
        }

        return mVideoInputBuffers;
    }

    /**
     * Return the index는 input buffer가 비어있는 index를 return 한다.
     * -1이 return 될 경우 {@link #INFO_TRY_AGAIN_LATER} 함수로 매칭하여, 다시 시도하면 된다.
     *
     * @param index 는 -1 값을 넘겨주어야 한다.
     */
    public int dequeueInputBuffer(int index) {
        if (index == -1)
            index = 0;
        while (index < INPUT_BUFFER_COUNT) {
            if (mVideoInputBuffers[index].hasRemaining()) {
                return index;
            }

            ++index;
        }

        return -1;
    }

    /**
     * Video를 초기화. 스크린의 사이즈 x, y와 "AVC"를 인코딩 준비한다.
     * 색상 값 GPU : {@link MediaCodecInfo.CodecCapabilities#COLOR_FormatSurface}
     * 색상 값 CPU : 해당 단말기에서 요구하는 색상값을 검색하여 처리 {@link com.rsupport.litecam.ScreenInfo#selectColorFormat}
     *
     * @return boolean TRUE is success, FALSE is fail.
     */
    protected boolean prepareEncoderVideo() {
        // Video MediaFormat com.rsupport.setting
        MediaFormat format = MediaFormat.createVideoFormat(RecordSet.VIDEO_MIME_TYPE, mResolutionInfo.alignedScreenSize.x, mResolutionInfo.alignedScreenSize.y);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mRecordFormat.getInteger(RecordFormat.KEY_VIDEO_BIT_RATE));
        format.setInteger(MediaFormat.KEY_FRAME_RATE, RecordSet.FRAME_RATE);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, mColorFormat);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, RecordSet.IFRAME_INTERVAL);
        LLog.i(true, "video format : " + format);
        try {
            mEncoderVideo = MediaCodec.createEncoderByType(RecordSet.VIDEO_MIME_TYPE);
        } catch (IOException e) {
            LLog.e(true, "Codec '" + RecordSet.VIDEO_MIME_TYPE + "' failed configuration. ", e);
            return false;
        }

        try {
            mEncoderVideo.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        } catch (IllegalStateException e) {
//			throw new RuntimeException("codec '" + RecordInfo.VIDEO_MIME_TYPE + "' failed configuration. " + e);
            LLog.e(true, "Codec '" + RecordSet.VIDEO_MIME_TYPE + "' failed configuration. ", e);
            return false;
        } catch (Exception e) {
            LLog.e(true, "Codec '" + RecordSet.VIDEO_MIME_TYPE + "' failed configuration. ", e);
            return false;
        }

        return true;
    }

    /**
     * Audio를 초기화.
     * Audio는 MIC를 이용하여 녹음을 진행해야 하며, Android API {@link AudioRecord}를 이용하여 녹음한다.
     * 녹음한 데이터는 PCM 데이터가 출력되며, 이를 다시 {@link MediaCodec}을 통해 AAC로 인코딩하여 Muxing 한다.
     * Muxer는 create에서 설정된 값에 따라 Version이 결정된다.<p>
     * <p>
     * Muxer를 사용하기 위해서는 {@link MediaFormat#KEY_CHANNEL_COUNT} 값이 2가 되어야 한다.<p>
     * <p>
     * Encoder 준비가 완료되면 {@link MediaCodec#start}를 호출하여 MediaCodec을 실행한다.
     *
     * @return boolean TRUE is success, FALSE is fail.
     */
    protected boolean prepareEncoderAudio() throws IOException {
        mAudioMinBufferSize = AudioRecord.getMinBufferSize(
                mRecordFormat.getInteger(RecordFormat.KEY_SAMPLE_RATE),
                RecordSet.RECORD_CHANNEL_CONFIG,
                RecordSet.RECORD_AUDIO_FORMAT);

        try {
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, // source
                    mRecordFormat.getInteger(RecordFormat.KEY_SAMPLE_RATE), // sample rate, hz
                    RecordSet.RECORD_CHANNEL_CONFIG, // channels
                    RecordSet.RECORD_AUDIO_FORMAT, // audio format 1:29
                    mAudioMinBufferSize * 3); // buffer size (bytes)
        } catch (java.lang.IllegalArgumentException e) {
            LLog.e("AudioRecord : " + e);
            mAudioRecord.release();
            return false;
        }

        MediaFormat audioFormat = MediaFormat.createAudioFormat(
                RecordSet.AUDIO_MIME_TYPE,
                mRecordFormat.getInteger(RecordFormat.KEY_SAMPLE_RATE),
                RecordSet.AUDIO_CHANNEL_COUNT);

        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, RecordSet.DEFAULT_AUDIO_BIT_RATE);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);

        mEncoderAudio = MediaCodec.createByCodecName(selectCodec(RecordSet.AUDIO_MIME_TYPE).getName());
        LLog.i(true, "Audio format : " + audioFormat);

        try {
            mEncoderAudio.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        } catch (IllegalStateException e) {
//			throw new RuntimeException("codec '" + RecordInfo.AUDIO_MIME_TYPE + "' failed configuration. " + e);
            LLog.e(true, "Codec '" + RecordSet.AUDIO_MIME_TYPE + "' failed configuration. ", e);
            mAudioRecord.release();
            mEncoderAudio.release();
            return false;
        }

        mEncoderAudio.start(); // init success

        return true;
    }

    /**
     * Returns the first codec capable of encoding the specified MIME type, or
     * null if no match was found.
     */
    private MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {
                continue;
            }

            for (String type : codecInfo.getSupportedTypes()) {
                if (type.equalsIgnoreCase(mimeType)) {
                    LLog.i(true, "SelectCodec : " + codecInfo.getName());
                    return codecInfo;
                }
            }
        }
        return null;
    }

    /**
     * Retruns a color format that is supported by the codec and by this test
     * code. If no match is found, this throws a test failure -- the set of
     * formats known to the test should be expanded for new platforms.
     */
    protected int selectColorFormat(String mimeType) {
        try {
            MediaCodecInfo codecInfo = selectCodec(mimeType);
            if (codecInfo == null) {
                throw new RuntimeException("Unable to find an appropriate codec for " + mimeType);
            }

            MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
            for (int i = 0; i < capabilities.colorFormats.length; i++) {
                int colorFormat = capabilities.colorFormats[i];
                if (isRecognizedFormat(colorFormat)) {
                    LLog.d(true, "Find a good color format for " + codecInfo.getName() + " / " + mimeType + " / " + colorFormat);
                    return colorFormat;
                }
            }
        } catch (IllegalArgumentException e) {
            LLog.e(true, "IllegalArgumentException : " + e);
        }

        return -1;
    }

    /**
     * Returns true if this is a color format that this test code understands
     * (i.e. we know how to read and generate frames in this format).
     */
    private boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            // these are the formats we know how to handle for this test
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                LLog.d("COLOR_FormatYUV420Planar");
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
                LLog.d("COLOR_FormatYUV420PackedPlanar");
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                LLog.d("COLOR_FormatYUV420SemiPlanar");
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
                LLog.d("COLOR_FormatYUV420PackedSemiPlanar");
            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                LLog.d("COLOR_TI_FormatYUV420PackedSemiPlanar");
                return true;
            default:
                return false;
        }
    }


    /**
     * RSperm에서 사용하기 위한 ColorFormat으로 기기마다 조금씩 차이가 난다.
     * 색상 값 GPU : {@link MediaCodecInfo.CodecCapabilities#COLOR_FormatSurface}
     * 색상 값 CPU : 해당 단말기에서 요구하는 색상값을 검색하여 처리 {@link com.rsupport.litecam.ScreenInfo#selectColorFormat}
     *
     * @return 검색된 Color 값을 return 한다.
     */
    public int getColorFormat() {
        return mColorFormat;
    }

    /**
     * queueInputBuffer에서 첫 번째 프레임이 전송되면 시작
     */
    private void startEncoder() {
        videoOfferThread = new Thread(offerVideoDataEncoderRunnable, "Video offer thread");
        videoOfferThread.setUncaughtExceptionHandler(EncodingThreadExceptionHandler);
        videoOfferThread.start();

        if (mRecordFormat.getBoolean(RecordFormat.KEY_IS_AUDIORECORD) && mAudioRecord != null) {
            if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
                mAudioRecord.startRecording();
            }

            audioOfferThread = new Thread(offerAudioDataEncoderRunnable, "Audio offer thread");
            audioOfferThread.start();
        }
    }

    private void startEncoderStream() {
        videoOfferThread = new Thread(offerVideoDataStreamEncoderRunnable, "Video offer thread");
        videoOfferThread.setUncaughtExceptionHandler(EncodingThreadExceptionHandler);
        videoOfferThread.start();
    }

    /**
     * Video input Thread 시작
     * Video 데이터를 Encoder에 저장하는 Thread.<p>
     * <p>
     * Android 4.2.2 이하는 {@link RecordHelperCPU}의 Thread 만을 실행
     * Android 4.3 이상은 CPU/GPU 사용 여부에 따라서 RecordHelper를 사용하도록 처리
     */
    private Runnable offerVideoDataEncoderRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                offerVideoDataEncoder();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable offerVideoDataStreamEncoderRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                offerVideoDataStreamEncoder();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Video data가 Empty가 아닌 경우 poll 함수를 통해 최신의 데이터를 가져와 return 한다.
     *
     * @return queue가 empty 라면 null을 return
     */
    protected ScreenInfo.CaptureFrame getVideoData() {
        synchronized (mCaptureQueue) {
            if (!mCaptureQueue.isEmpty())
                return mCaptureQueue.poll();
            return null;
        }
    }

    /**
     * 실제 Encoder해야 할 Byte를 Temp buffer에 채워두고, 해당 메소드를 호출하면 된다.
     * index는 {@link #dequeueInputBuffer} 의 번호를 넘겨주면 되며, 해당 메소드가 시작하면, 아래와 같은 내용이 최초 1번 실행된다.
     * Audio 사용 유무에 따라서 {@link AudioRecord#startRecording()} 함수가 호출.
     * {@link #mStartWhen} 변수에 millis 을 저장
     * {@link #startEncoder} 를 호출하여, Video output Thread, Audio In/Out Thread를 실행한다.
     *
     * @param index
     */
    public void queueInputBuffer(int index) {
        if (eosReceived) return;

        if (mStartWhen == 0) {
            mStartWhen = System.currentTimeMillis(); // Encoder start Time
            startEncoder();
        }

        RLog.v("---------------------------- queueInputBuffer : " + index);

        synchronized (mCaptureQueue) {
            if (mVideoInputBuffers[index].hasRemaining()) {
                mVideoInputBuffers[index].flip();
            }
            mCaptureQueue.offer(new CaptureFrame(index, getVideoPresentationTimeUs()));
        }
        mSemaphore.release();
    }

    public void queueInputBufferStream(int index) {
        if (eosReceived) return;

        if (mStartWhen == 0) {
            mStartWhen = System.currentTimeMillis(); // Encoder start Time
            startEncoderStream();
        }

        RLog.v("---------------------------- queueInputBuffer : " + index);

        synchronized (mCaptureQueue) {
            if (mVideoInputBuffers[index].hasRemaining()) {
                mVideoInputBuffers[index].flip();
            }
            mCaptureQueue.offer(new CaptureFrame(index, getVideoPresentationTimeUs()));
        }
        mSemaphore.release();
    }

    /**
     * 인코딩된 데이터를 Muxer 처리하는 함수로, {@link MeidaCodec}을 따라서 정의
     * Android 4.2.2 이하 버전의 경우 {@link MediaCodec#INFO_OUTPUT_FORMAT_CHANGED}이 호출되지 않아
     * {@link #offerVideoDataEncoder}에서 {@link #muxerStart}를 미리 호출하여 {@link MP4MediaMuxer}를 실행.
     * Android 4.3 이상 버전의 경우 API 문서상에 따라서 프로그램이 실행되며, Muxer는 {@link android.media.MediaMuxer}를 실행한다.<p>
     * <p>
     * {@link #IS_TEST}가 TRUE이면 test 파일을 저장.
     */
    protected boolean videoQueueOutput(ByteBuffer[] outputBuffers, MediaCodec.BufferInfo bufferInfo) {
        /**
         * H.264 Out test code
         */
        BufferedOutputStream outputStream = null;
        if (IS_TEST) {
            File f = new File(Environment.getExternalStorageDirectory(), "video_encoded.h264");
            try {
                f.delete();
                outputStream = new BufferedOutputStream(new FileOutputStream(f));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /**
         * H.264 Out test code
         */

        int outputBufferIndex = mEncoderVideo.dequeueOutputBuffer(bufferInfo, -1);
        if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
            // no output available yet

        } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            // not expected for an encoder
//				outputBuffers = mEncoderVideo.getOutputBuffers();

        } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            // should happen before receiving buffers, and should only happen once
            muxerStart();

        } else if (outputBufferIndex < 0) {
            LLog.w(true, "unexpected result from encoder.dequeueOutputBuffer : ", outputBufferIndex);

        } else {
            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
            if (outputBuffer == null) {
                throw new RuntimeException("encoderOutputBuffer " + outputBufferIndex + " was null");
            }

            if (bufferInfo.size != 0) {
                if (!muxerWrapper.started) {
                    LLog.e("Muxer not stated. dropping video");
                    //throw new RuntimeException("muxer hasn't started");

                } else {
                    // got a buffer
                    if (((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) && APICheck.isNewAPISupportVersion()) {
                        // ignore this -- we passed the CSD into MediaMuxer when
                        // we got the format change notification
                        LLog.i("Got codec config buffer " + bufferInfo.size + " ignoring");
                        bufferInfo.size = 0;
                    }

                    if (bufferInfo.size > 0) {
                        // adjust the ByteBuffer values to match BufferInfo (not needed?)
                        outputBuffer.position(bufferInfo.offset);
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                        if (bufferInfo.presentationTimeUs < 0) {
                            bufferInfo.presentationTimeUs = 0;
                        }

                        LLog.i(false, "MPEG4Write", "Video " + bufferInfoToString(bufferInfo));
                        RecordSet.ENCODER_TOTAL_SIZE += bufferInfo.size;
                        writeSampleData(VIDEO_TRACK_INDEX, outputBuffer, bufferInfo);
                    }
                }
            }

            /**
             * H.264 Out test code
             */
            if (IS_TEST) {
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);

                try {
                    outputStream.write(outData, 0, outData.length);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            /**
             * H.264 Out test code
             */

            mEncoderVideo.releaseOutputBuffer(outputBufferIndex, false);

            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                LLog.i("End of stream");

                stopVideo();

                LLog.w("video muxer finishTrack");
                muxerWrapper.finishTrack();
                return true;
            }
        }

        /**
         * H.264 Out test code
         */
        if (IS_TEST) {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /**
         * H.264 Out test code
         */

        return false;
    }

    byte[] sps;
    byte[] pps;

    protected boolean videoQueueOutputStream(ByteBuffer[] outputBuffers, MediaCodec.BufferInfo bufferInfo) {
        int outputBufferIndex = mEncoderVideo.dequeueOutputBuffer(bufferInfo, -1);
        RLog.v("---------------------- bufferInfo : " + bufferInfo.size);
        if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
            // no output available yet

        } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            // not expected for an encoder
//			outputBuffers = mEncoderVideo.getOutputBuffers();

        } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            // should happen before receiving buffers, and should only happen once

            // sypark
//			MediaFormat format = mEncoderVideo.getOutputFormat();
//			ByteBuffer spsb = format.getByteBuffer("csd-0");
//			ByteBuffer ppsb = format.getByteBuffer("csd-1");
//			sps = new byte[spsb.capacity()-4];
//			spsb.position(4);
//			spsb.get(sps, 0, sps.length);
//			pps = new byte[ppsb.capacity()-4];
//			ppsb.position();
//			ppsb.get(pps, 0, pps.length);
//			
//			try {
//				mRandomFile.write(spsb.array(), 0, spsb.capacity());
//				mRandomFile.write(ppsb.array(), 0, ppsb.capacity());
//			} catch (IOException e) {
//				e.printStackTrace();
//			}

        } else if (outputBufferIndex < 0) {
            LLog.w(true, "unexpected result from encoder.dequeueOutputBuffer : ", outputBufferIndex);

        } else {
            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
            if (outputBuffer == null) {
                throw new RuntimeException("encoderOutputBuffer " + outputBufferIndex + " was null");
            }

            if (bufferInfo.size != 0) {
                if (((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0)) {
                    bufferInfo.size = 0;
                }

                if (bufferInfo.size > 0) {
                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    outputBuffer.position(bufferInfo.offset);
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

                    // saveTestFile

//					byte[] byteArray = new byte[bufferInfo.offset + bufferInfo.size];
//					outputBuffer.get(byteArray);
//					
//					try {
//						mRandomFile.write(byteArray, 0, byteArray.length);
//					} catch (IOException e) {
//						e.printStackTrace();
//					}

                    RLog.v("------------------------------------- videoQueueOutputStream : doing");

                }
            }

            mEncoderVideo.releaseOutputBuffer(outputBufferIndex, false);

            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                LLog.i("End of stream");

                stopVideo();

                LLog.w("video muxer finishTrack");
                muxerWrapper.finishTrack();
                return true;
            }
        }

        return false;
    }

    /**
     * Audio input Thread.
     */
    protected Runnable offerAudioDataEncoderRunnable = new Runnable() {

        @Override
        public void run() {
            offerAudioDataEncoder();
        }
    };

    /**
     * {@link AudioRecord}와 {@link MediaCodec}을 함께 사용하여, 사운드를 녹음하고
     * 인코딩을 위해 {@link MediaCodec#queueInputBuffer}를 호출하여 데이터를 put 해준다.<p>
     * <p>
     * 시스템상의 사운드를 녹음할 수 없어, MIC로 음성을 녹음한다.
     */
    protected synchronized void offerAudioDataEncoder() {
        ByteBuffer[] inputBuffers = mEncoderAudio.getInputBuffers();
        ByteBuffer inputBuffer;

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer[] outputBuffers = mEncoderAudio.getOutputBuffers();

        boolean inputDone = false;
        boolean outputDone = false;

        int inputBufferIndex;
        long presentationTimeUs = 0;

        final int readSize = mAudioMinBufferSize / 2;


        while (!outputDone) {
            if (!inputDone) {
                inputBufferIndex = mEncoderAudio.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufferIndex >= 0) {
                    inputBuffer = inputBuffers[inputBufferIndex];
                    inputBuffer.clear();

                    int inputLength = mAudioRecord.read(inputBuffer, readSize);
                    if (inputLength >= 0) {
                        presentationTimeUs = muxerWrapper.getAudioPresentationTimeUs(mStartWhen);
                        LLog.i(false, "Read Audio size : " + inputLength + " minBufferSize : " + mAudioMinBufferSize + " presentationTimeUs long : " + presentationTimeUs);

                        if (eosReceived) {
                            mEncoderAudio.queueInputBuffer(inputBufferIndex, 0, inputLength, presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            inputDone = true;

                        } else {
                            mEncoderAudio.queueInputBuffer(inputBufferIndex, 0, inputLength, presentationTimeUs, 0);
                        }
                    }
                }
            }

            outputDone = audioQueueOutput(outputBuffers, bufferInfo);
        }
    }

    /**
     * Encoder된 Audio data를 Muxer 처리.
     * 인코딩된 데이터를 Muxer 처리하는 함수
     * Android 4.2.2 이하는 {@link MP4MediaMuxer}을 사용하여 Muxing.
     * Android 4.3 이상 {@link android.media.MediaMuxer}를 사용하여 Muxing.
     */
    private boolean audioQueueOutput(ByteBuffer[] outputBuffers, MediaCodec.BufferInfo bufferInfo) {
        long lastPresentationTimeUs = 0;

        int outputBufferIndex = mEncoderAudio.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
        if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
            // INFO_TRY_AGIN_LATER

        } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//			outputBuffers = mEncoderAudio.getOutputBuffers();
            // INFO_OUTPUT_BUFFERS_CHANGED

        } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            // INFO_OUTPUT_FORMAT_CHANGED

        } else if (outputBufferIndex >= 0 && bufferInfo.size > 0) {
            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
            if (outputBuffer == null) {
                throw new RuntimeException("EncoderOutputBuffer " + eosReceived + " was null");
            }

            outputBuffer.position(bufferInfo.offset);
            outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

            if (lastPresentationTimeUs > bufferInfo.presentationTimeUs) {
                bufferInfo.presentationTimeUs = lastPresentationTimeUs;
            }

            LLog.i(false, "MPEG4Write", "Audio " + bufferInfoToString(bufferInfo));
            RecordSet.ENCODER_TOTAL_SIZE += bufferInfo.size;
            writeSampleData(AUDIO_TRACK_INDEX, outputBuffer, bufferInfo);
            lastPresentationTimeUs = bufferInfo.presentationTimeUs;

            mEncoderAudio.releaseOutputBuffer(outputBufferIndex, false);
        }

        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            LLog.i("End of stream");
            stopAudio();

            LLog.w("audio muxer finishTrack");
            muxerWrapper.finishTrack();
            return true;
        }

        return false;
    }

    @SuppressLint("DefaultLocale")
    private String bufferInfoToString(BufferInfo info) {
        return String.format("[flags: %d, presentationTimeUs: %d, size: %d]", info.flags, info.presentationTimeUs, info.size);
    }

    /**
     * RecordManager의 모든 Thread를 종료한다.
     * {@link #eosReceived} 변수가 true로 변경되어 동작하던 Thread를 종료하도록 한다.<p>
     * <p>
     * Video Thread 종료
     * {@link #closeVideoEncoder()}를 호출,
     * {@link #mCaptureQueue}에 저장되어 있는 데이터를 remove.<p>
     * <p>
     * Audio Thread 종료
     * {@link #mAudioRecord} Audio record 종료,
     * {@link #mEncoderAudio} Encoder 종료
     */
    public void stop() {
        if (mRandomFile != null) {
            try {
                RLog.v("------------------------------------- RecordManager stop and file close");
                mRandomFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        eosReceived = true;
        mSemaphore.release();

        if (muxerWrapper != null) {
            muxerWrapper.setMuxerWapperListener(muxerWapperListener);

            try {
                muxerWapperSemaphore = new Semaphore(0);
                muxerWapperSemaphore.acquire();

            } catch (InterruptedException e) {
                e.printStackTrace();
                LLog.e(true, "InterruptedException : " + e);
            }
        }

        LLog.i(true, "Record end");
    }

    private IMuxerWapperListener muxerWapperListener = new IMuxerWapperListener() {

        @Override
        public void stopMuxer() {
            LLog.i(true, "stopMuxer");
            muxerWapperSemaphore.release();
        }
    };

    /**
     * Audio의 Encoder를 종료한다.
     */
    private void stopAudio() {
        if (mEncoderAudio != null) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
            LLog.i("audioRecord.stop close");

            mEncoderAudio.stop();
            mEncoderAudio.release();
            mEncoderAudio = null;
        }
    }

    /**
     * Video의 Encoder를 종료한다.
     */
    private void stopVideo() {
        try {
            if (mEncoderVideo != null) {
                mEncoderVideo.stop();
                mEncoderVideo.release();
                mEncoderVideo = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        while (!mCaptureQueue.isEmpty()) {
            mCaptureQueue.remove();
        }
        mCaptureQueue = null;
    }

    protected Thread.UncaughtExceptionHandler EncodingThreadExceptionHandler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread th, Throwable ex) {
            RLog.v("Uncaught exception: " + ex);
        }
    };
}
