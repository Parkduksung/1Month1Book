package com.rsupport.litecam;

import android.graphics.Point;

import com.rsupport.litecam.ScreenInfo.ResolutionInfo;
import com.rsupport.litecam.record.RecordFormat;
import com.rsupport.litecam.util.AvailableCapacityCheck;
import com.rsupport.litecam.util.RecordScreenHead;

/**
 * <b>LiteCam을 사용하기 위한 interface</b><br>
 * <p>{@link LiteCam}은 Binder를 사용하지 않고, <b>byte[]</b>를 직접 전송하는 경우 사용한다.
 *
 * <p>uses-permission
 * <pre>
 * &lt;!-- Tell the system this app requires OpenGL ES 2.0. --&gt;
 * &lt;uses-feature android:glEsVersion="0x00020000" android:required="true" /&gt;
 *
 * &lt;uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /&gt;
 * &lt;uses-permission android:name="android.permission.RECORD_AUDIO"/&gt;
 * </pre>
 *
 * <p><b>Method 호출 순서</b>
 * {@link #isSupportLiteCam}, {@link #initCaptureInfo}, {@link #getColorFormatASHM}, {@link #queueInputBuffer}, {@link #stop} 순서로 사용한다.
 *
 * <p>{@link #isSupportLiteCam}을 호출하여 실제 Encoder가 사용가능한지 확인을 하여야 한다. (현재는 Dual코어 이상에서 사용이 가능하다.)
 * {@link #initCaptureInfo} 메소드를 호출하면 실제 인코딩 가능한 size를 return 하며, ashm 초기화시에 사용하면 된다.
 * {@link #getColorFormatASHM} ASHM 메모리 초기화시에 필요한 변수로, {@link #initCaptureInfo} 메소드가 호출되고 난 이후 호출하면 된다. ASHM에서 초기화 시에 사용할 색상값을 정의한다.
 * {@link #queueInputBuffer}을 호출하여 실제 byte를 queue에 전송한다.
 * 인코딩이 종료되면 {@link #stop}을 호출하여 인코딩을 종료한다.
 *
 * <p>isExceededCapacity is generally used like this:
 * <pre>
 * ILiteCam liteCam = LiteCam.init(getApplicationContext());
 *
 * // LiteCam Version 체크
 * if (!liteCam.isSupportLiteCam()) {
 * 	Toast.makeText(getApplicationContext(), "LiteCam record not supported!", Toast.LENGTH_SHORT).show();
 * 	return;
 * }
 *
 * // 이미지 사이즈를 모를 경우
 * Point captureSize = liteCam.initCaptureInfo(false, false, 0.9f);
 * // 이미지 사이즈를 직접 지정할 경우
 * // Point captureSize = liteCam.initCaptureInfo(false, false, 720, 1280, 0);
 *
 * // ASHM 초기화 시에 필요한 colorType이 -1 이면 인코딩 불가
 * liteCam.getColorFormatASHM();
 *
 * RecordFormat format = new RecordFormat();
 * ...
 * liteCam.initRecord(byte[], format);
 *
 * while (true) {
 *  ...
 *  int ret = liteCam.queueInputBuffer(byte[]);
 * switch (mRecordManager.isExceededCapacity()) {
 * case RecordManager.EXCEEDED_CAPACITY_SYSTEM:
 * 	... // stop 호출
 * 	break;
 * case RecordManager.EXCEEDED_CAPACITY_FILE:
 * 	... // stop 호출
 * 	break;
 * }
 *
 * // end
 * liteCam.stop();
 * </pre>
 */

public interface ILiteCam {
    /**
     * {@link #receiveData} 을 사용할 경우 return 되는 변수입니다.
     * temp Buffer가 부족하여 return 되며, 이번 byte를 무시하고, 다시 호출하면 됩니다.
     */
    public static final int INFO_TRY_AGAIN_LATER = -1;

    /**
     * {@link #receiveData} 을 사용할 경우 return 되는 변수입니다.
     * 시스템의 용량이 부족하여 return 되며, {@link AvailableCapacityCheck#REFERENCE_MEMORY} 이하가 되면 호출됩니다.
     * return 시 {@link #stop} 을  호출하여 Encoder 를 종료하면 됩니다.
     */
    public static final int EXCEEDED_CAPACITY_SYSTEM = -2;
    /**
     * {@link #receiveData} 을 사용할 경우 return 되는 변수입니다.
     * 시스템의 용량이 부족하여 return 되며, {@link AvailableCapacityCheck#WRITE_BASE_SIZE} 이상이 되면 호출됩니다.
     * return 시 {@link #stop} 을  호출하여 Encoder 를 종료하면 됩니다.
     */
    public static final int EXCEEDED_CAPACITY_FILE = -3;

    /**
     * 녹화를 하기 위한 최소 용량 체크
     *
     * @param filePath
     * @return
     */
    public abstract boolean isRecordAvailableCapacityCheck(String filePath);

    /**
     * 캡쳐를 위한 최소 용량 체크
     *
     * @param filePath
     * @return
     */
    public abstract boolean isCaptureAvailableCapacityCheck(String filePath);

    /**
     * LiteCam API 사용 가능 여부 확인.
     * 사용 가능하면 true, 그렇지 않으면 false가 return.
     */
    public abstract boolean isSupportLiteCam();

    /**
     * Capture 할 사이즈를 정확하게 알지 못하는 경우에 사용하며, Watermark와 GPU 사용 여부를 지정하여 Capture에 대한 정보를 초기화 합니다.
     * 화면의 사이즈를 String으로 처리하며, 240 ~ 2160의 숫자와 0.5의 half 값을 전송합니다.
     * 해당 메소드는 {@link #initRecord}를 호출하기 전에 Screen의 사이즈와 Watermark 사용 여부, GPU 사용 여부를 우선 확인합니다.
     *
     * @param isWatermark
     * @param isGPU
     * @param ratio
     * @return 인코딩 가능한 사이즈가 return 되므로, ASHM 초기화시에 사용하시면 됩니다.
     */
    public abstract Point initCaptureInfo(boolean isWatermark, boolean isGPU, String ratioValue);

    /**
     * Capture 할 사이즈를 정확하게 알지 못하는 경우에 사용하며, Watermark와 GPU 사용 여부를 지정하여 Capture에 대한 정보를 초기화 합니다.
     * 배율을 지정할 경우에 사용하는 변수로 배율은 30 ~ 90%(0.3f ~ 0.9f) 사이의 값을 사용하면 됩니다. 그 이상과 그 이하의 경우 오류가 나는 기기가 발생할 수 있어 0.3~0.9f로 사이즈를 지정합니다.
     * 해당 메소드는 {@link #initRecord}를 호출하기 전에 Screen의 사이즈와 Watermark 사용 여부, GPU 사용 여부를 우선 확인합니다.
     *
     * @param isWatermark
     * @param isGPU
     * @param ratio
     * @return 인코딩 가능한 사이즈가 return 되므로, ASHM 초기화시에 사용하시면 됩니다.
     */
    public abstract Point initCaptureInfo(boolean isWatermark, boolean isGPU, float ratio);

    /**
     * Capture 할 사이즈를 직접 지정할 경우에 사용하며, Watermark와 GPU 사용 여부를 지정하여 Capture에 대한 정보를 초기화 합니다.
     * 내부에서는 30~90% 사이의 값으로 재 조정 될 수 있습니다.
     * 해당 메소드는 {@link #initRecord}를 호출하기 전에 Screen의 사이즈와 Watermark 사용 여부, GPU 사용 여부를 우선 확인합니다.
     *
     * @param isWatermark
     * @param isGPU
     * @param width
     * @param height
     * @param rotation
     * @return 인코딩 가능한 사이즈가 return 되므로, ASHM 초기화시에 사용하시면 됩니다.
     */
    public abstract Point initCaptureInfo(boolean isWatermark, boolean isGPU, int width, int height, int rotation);

    /**
     * 해당 단말기에서 ASHM을 이용하여 Capture 가능한 색상값을 return 합니다. {@link #initCaptureInfo} 함수를 호출 한 다음 해당 메소드를 사용할 수 있습니다.
     * 색상 정의는 ASHM의 사용가능한 색상값이 return 되므로 ASHM 초기화시에 사용하면 됩니다.
     *
     * @return -1일 경우 오류
     */
    public abstract int getColorFormatASHM();

    /**
     * liteCam을 사용하기 위한 메소드로, {@link RecordFormat}을 직접 초기화 하지 않을 경우 사용합니다.
     * init시에는 ASHM을 통하여 캡쳐한 첫번째 프레임을 함께 전송해주어야 합니다. byte head를 직접 확인하여, 내부 인코딩에 사용할 이미지 사이즈를 재 조정하게 됩니다.
     *
     * <p><b>초기화 값은 아래와 같습니다.</b><br>
     * <b>파일 저장 폴더</b> : Environment.getExternalStorageDirectory().getPath() + "/Mobizen"<br>
     * <b>file name</b> : {@link RecordFormat#KEY_FILE_NAME} 대로 초기화<br>
     * <b>Video bit rate</b> : 3145728<br>
     * <b>Audio sample rate</b> : 8000<br>
     * <b>Audio 인코딩 사용</b> : true<br>
     * <b>Watermark</b> : WATERMARK_SMALL = "mark_mobizen_100.png", WATERMARK_MEDIUM = "mark_mobizen_200.png", WATERMARK_LARGE = "mark_mobizen_300.png"<br>
     *
     * @param headByte    srn30에서 capture한 프레임의 head 포함된 모든 byte를 전송합니다. 이때 색상값은 {@link RecordScreenHead}의 색상값이 됩니다.
     * @param isWatermark
     */
    public abstract void initRecord(byte[] headByte);

    /**
     * liteCam을 사용하기 위한 메소드로, {@link RecordFormat}을 직접 초기화 하는 경우에 사용합니다.
     * init시에는 ASHM을 통하여 캡쳐한 첫번째 프레임을 함께 전송해주어야 합니다. byte head를 직접 확인하여, 내부 인코딩에 사용할 이미지 사이즈를 재 조정하게 됩니다.
     *
     * <p>isExceededCapacity is generally used like this: <br>
     * FILE_FULL_NAME과 FILE_NAME을 동시에 사용할 경우 FILE_FULL_NAME이 기본으로 사용되며, FILE_NAME만 지정하면 기본 Type에 따라 파일명이 지정됩니다.
     * <pre>
     * RecordFormat format = new RecordFormat();
     * format.setString({@link RecordFormat#KEY_FILE_PATH}, Environment.getExternalStorageDirectory().getPath() + "/Mobizen");
     * format.setString({@link RecordFormat#KEY_FILE_FULL_NAME}, "mobizen.mp4");
     * format.setString({@link RecordFormat#KEY_FILE_NAME}, "mobizen_");
     * format.setInteger({@link RecordFormat#KEY_VIDEO_BIT_RATE}, 3145728);
     * format.setInteger({@link RecordFormat#KEY_SAMPLE_RATE}, 8000);
     * format.setBoolean({@link RecordFormat#KEY_IS_AUDIORECORD}, true);
     *
     * // Watermark 추가
     * WatermarkImage watermark = new WatermarkImage();
     * watermark.WATERMARK_SMALL = "mark_mobizen_100.png";
     * watermark.WATERMARK_MEDIUM = "mark_mobizen_200.png";
     * watermark.WATERMARK_LARGE = "mark_mobizen_300.png";
     * format.setWatermarkImage({@link RecordFormat#KEY_WATERMARK_IMAGE}, watermark);
     * </pre>
     *
     * @param headByte    srn30에서 capture한 프레임의 head 포함된 모든 byte를 전송합니다. 이때 색상값은 {@link RecordScreenHead}의 색상값이 됩니다.
     * @param format
     * @param isWatermark
     */
    public abstract void initRecord(byte[] headByte, RecordFormat format);

    /**
     * {@link #initRecord}가 호출 된 후에 호출하면 됩니다.
     *
     * @return 파일의 전체 경로와 파일명이 return 됩니다.
     */
    public abstract String getRecordFilePath();


    /**
     * {@link #queueInputBuffer}가 호출 된 후에 호출하면 됩니다.
     *
     * @return 현재까지 인코딩 된 Video의 사이즈가 return 됩니다.
     */
    public abstract long getRecordSize();


    /**
     * ASHM에서 캡쳐한 head가 포함되지 않은 byte를 queue에 쌓아 처리합니다.
     * 모든 초기화가 끝이 나면 호출하면 되며 {@link #initRecord}를 호출 한 후 사용합니다. 내부에서 Encoder 설정에 따라서 모든 Encoder를 실행하게 됩니다.
     *
     * @param buffer
     * @return {@link #INFO_TRY_AGAIN_LATER} 는 현재 사용가능한 Buffer가 없을 경우 호출되며, 무시하고 다음 데이터를 넣어주시면 됩니다.(FPS는 저하 될 수 있다.)
     * {@link #EXCEEDED_CAPACITY_SYSTEM} 시스템의 메모리가 부족하면 호출되며, {@link AvailableCapacityCheck#REFERENCE_MEMORY}
     * 에 정의되어 있는 공간 이하가 되면 녹화를 중지해야 합니다.
     * {@link #EXCEEDED_CAPACITY_FILE}는 현재 녹화 중인 파일의 용량을 체크하며, {@link AvailableCapacityCheck#WRITE_BASE_SIZE}
     * 를 초과할 경우 녹화를 중지해야 합니다.
     *
     * <p>isExceededCapacity is generally used like this:
     * <pre>
     * switch (mRecordManager.isExceededCapacity()) {
     * case ILiteCam.EXCEEDED_CAPACITY_SYSTEM:
     * 	... // stop 호출
     * 	break;
     * case ILiteCam.EXCEEDED_CAPACITY_FILE:
     * 	... // stop 호출
     * 	break;
     * }</pre>
     */
    public abstract int queueInputBuffer(byte[] buffer) throws InterruptedException;

    public abstract ResolutionInfo getResolutionInfo();

    /**
     * Encoder를 종료할때 사용되며, Muxer와 Encoder가 종료된다.
     */
    public abstract void stop();
}
