package com.rsupport.litecam;

import java.nio.ByteBuffer;

import android.content.Context;
import android.graphics.Point;
import android.os.Binder;

import com.rsupport.litecam.ScreenInfo.ResolutionInfo;
import com.rsupport.litecam.record.RecordFormat;
import com.rsupport.litecam.record.RecordManager;
import com.rsupport.litecam.record.RecordSet;
import com.rsupport.litecam.util.APICheck;
import com.rsupport.litecam.util.AvailableCapacityCheck;
import com.rsupport.litecam.util.DirectoryManage;
import com.rsupport.litecam.util.LLog;
import com.rsupport.litecam.util.RecordFrameRate;
import com.rsupport.litecam.util.RecordScreen;
import com.rsupport.litecam.util.RecordScreenHead;

/**
 * <b>LiteCam을 사용하기 위한 실제 class</b>로 {@link ILiteCam}을 사용하여 호출하면 된다.
 */
public class LiteCam implements ILiteCam {
    private final boolean isLog = true;
    private Context mContext;

    private RecordFrameRate recordFrameRate;

    /**
     * {@link RspermScreenCapture}를 처리하고, 실제 Video/Audio를 녹화하기 위한 클래스 변수
     */
    private RecordManager mRecordManager;

    /**
     * 화면의 가로/세로 사이즈와 rotate 값을 저장하고 있는 변수
     */
    private ResolutionInfo mResolution;

    private ByteBuffer mInputBuffers[];
    private int mInputBufferIndex = -1;

    private LiteCam(Context context) {
        mContext = context;
        mInputBuffers = null;
    }

    /**
     * ScreenManager를 초기화 하고, Binder가 없는 경우 사용.
     *
     * @param context
     */
    public static ILiteCam init(Context context) {
        return new LiteCam(context);
    }

    @Override
    public boolean isSupportLiteCam() {
        return APICheck.isSupportedLiteCam();
    }

    @Override
    public boolean isRecordAvailableCapacityCheck(String filePath) {
        if (DirectoryManage.createDirectory(filePath) == true) {
            return AvailableCapacityCheck.create().isRecordAvailableCapacitySize(filePath);
        }
        return false;
    }

    @Override
    public boolean isCaptureAvailableCapacityCheck(String filePath) {
        if (DirectoryManage.createDirectory(filePath) == true) {
            return AvailableCapacityCheck.create().isCaptureAvailableCapacitySize(filePath);
        }
        return false;
    }

    @Override
    public ResolutionInfo getResolutionInfo() {
        return mResolution;
    }

    @Override
    public Point initCaptureInfo(boolean isWatermark, boolean isGPU, String ratioValue) {
        initCapture(isWatermark, isGPU);

        // 현재 화면의 사이즈를 가져온다. 상대적인 비율에 따라서 화면 사이즈를 계산.
        mResolution = RecordScreen.getResolution(mContext, ratioValue);
        return mResolution.screenSize;
    }

    @Override
    public Point initCaptureInfo(boolean isWatermark, boolean isGPU, float ratio) {
        initCapture(isWatermark, isGPU);

        // 현재 화면의 사이즈를 가져온다. 상대적인 비율에 따라서 화면 사이즈를 계산.
        mResolution = RecordScreen.getResolution(mContext, ratio);
        return mResolution.screenSize;
    }

    @Override
    public Point initCaptureInfo(boolean isWatermark, boolean isGPU, int width, int height, int rotation) {
        initCapture(isWatermark, isGPU);

        // 현재 화면의 사이즈를 가져온다. 상대적인 비율에 따라서 화면 사이즈를 계산.
        mResolution = RecordScreen.getResolution(mContext, width, height, rotation);
        return mResolution.screenSize;
    }

    private void initCapture(boolean isWatermark, boolean isGPU) {
        RecordSet.ISWatermark = isWatermark;
        RecordSet.ISGPU = isGPU;

        /**
         * Android 4.3 이상은 GPU 사용 여부에 따라서 초기화가 달라짐. Android 4.2.2 이하는 CPU만 사용
         * 가능.
         */
        mRecordManager = RecordManager.create(isGPU);
    }

    @Override
    public int getColorFormatASHM() {
        LLog.i("getColorFormat : " + mRecordManager.getColorFormat());
        return (RecordSet.RS_COLOR_FORMAT = RecordScreenHead.getRSColorFormat(mRecordManager.getColorFormat()));
    }

    /**
     * Record를 시작하는 메소드로, {@link RecordFormat}을 정의하면 된다. {@link RspermScreenCapture}를
     * 초기화 하고, {@link #ScreenRecordTask}를 실행하여 Record를 시작한다.
     *
     * @param format       {@link RecordFormat}을 참고하여 작성.
     * @param isWatermark는 TEST용으로 사용되는 변수.
     */
    @Override
    public void initRecord(byte[] headByte) {
        RecordFormat format = new RecordFormat();
        RecordScreenHead head = RecordScreenHead.get(headByte);
        LLog.i("ScreenHead : " + head.toString());

        mResolution = RecordScreenHead.setScreenSize(head, mResolution, RecordSet.ISGPU);

        recordFrameRate = new RecordFrameRate(RecordSet.BANCHMARK_FPS_AUTO);

        /**
         * 화면의 context, 화면 사이즈, {@link RecordFormat}을 등록하여 초기화.
         */
        mRecordManager.config(mContext, mResolution, format);
    }

    /**
     * Record를 시작하는 메소드로, {@link RecordFormat}을 정의하면 된다. {@link Binder}가 문제가 없으면
     * {@link RspermScreenCapture}를 초기화 하고, {@link #ScreenRecordTask}를 실행하여 Record를
     * 시작한다.
     *
     * @param format       {@link RecordFormat}을 참고하여 작성.
     * @param isWatermark는 TEST용으로 사용되는 변수.
     */
    @Override
    public void initRecord(byte[] headByte, RecordFormat format) {
        RecordScreenHead head = RecordScreenHead.get(headByte);
        LLog.i("ScreenHead : " + head.toString());

        recordFrameRate = new RecordFrameRate(format.getInteger(RecordFormat.KEY_FRAME_RATE, RecordSet.BANCHMARK_FPS_AUTO));

        mResolution = RecordScreenHead.setScreenSize(head, mResolution, RecordSet.ISGPU);

        /**
         * 화면의 context, 화면 사이즈, {@link RecordFormat}을 등록하여 초기화.
         */
        mRecordManager.config(mContext, mResolution, format);
    }

    @Override
    public String getRecordFilePath() {
        return RecordSet.FILE_PATH;
    }

    @Override
    public long getRecordSize() {
        return RecordSet.ENCODER_TOTAL_SIZE;
    }

    /**
     * ScreenCapture를 처리하고, Encoder를 처리하기 위한 Thread. 실제 처리 방법은
     * {@link RspermScreenCapture#screenshot} 함수와 {@link RecordManager} 함수를 이용하여 사용.
     * <p>
     * <p>
     * {@link AvailableCapacityCheck}를 통해 현재 시스템의 가용 메모리를 확인하고,
     * {@link RecordManager#isEncoderTotalSize}를 통해 현재 녹화 중인 파일의 용량을 체크한다.
     *
     * @throws InterruptedException
     * @author taehwan
     */
    @Override
    public int queueInputBuffer(byte[] buffer) throws InterruptedException {
        if (mRecordManager == null) {
            return EXCEEDED_CAPACITY_SYSTEM;
        }

        if (mInputBuffers == null) {
            mInputBuffers = mRecordManager.getInputBuffers(buffer.length);
        }

        recordFrameRate.captureTime();

        mInputBufferIndex = mRecordManager.dequeueInputBuffer(-1);
        if (mInputBufferIndex == RecordManager.INFO_TRY_AGAIN_LATER) {
            recordFrameRate.setNextFrameSleep();
            return INFO_TRY_AGAIN_LATER;
        }

        ByteBuffer inputBuffer = mInputBuffers[mInputBufferIndex];
        inputBuffer.clear();
        inputBuffer.put(buffer);
        inputBuffer.flip();

        mRecordManager.queueInputBuffer(mInputBufferIndex);

        // 시스템의 SD카드 메모리 체크와 현재 녹화 중인 파일의 용량 체크를 진행.
        switch (mRecordManager.isExceededCapacity()) {
            case RecordManager.EXCEEDED_CAPACITY_SYSTEM:
                LLog.i(isLog, "There is not enough system memory.");
                return EXCEEDED_CAPACITY_SYSTEM;

            case RecordManager.EXCEEDED_CAPACITY_FILE:
                LLog.i(isLog, "Recording file size exceeds 2 GB was.");
                return EXCEEDED_CAPACITY_FILE;
        }

        recordFrameRate.setNextFrameSleep();
        return 0;
    }

    /**
     * Capture 를 정지하는 메소드.
     */
    @Override
    public void stop() {
        if (mRecordManager != null) {
            // thread가 종료되면 close 호출
            mRecordManager.stop();
            mRecordManager = null;

            mInputBuffers = null;
        }
    }

}
