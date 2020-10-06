package com.rsupport.litecam.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

/**
 * <b>사용가능한 용량을 체크하는 class.</b><br>
 * {@link #create}를 통해 초기화 가능하며, 파일을 저장하는 경로를 전달하면 된다. 이후 사용시에는 {@link #get}을 통해 접근하여 사용할 수 있다.<p>
 * <p>
 * 시스템의 최소 메모리는 {@link #REFERENCE_MEMORY} 만큼 존재하여야 하며, 1개 파일의 최대 용량은 {@link #WRITE_BASE_SIZE}를 초과하지 않아야 한다.<br>
 * 시스템의 용량 체크는 {@link #isAvailableExternalMemorySize} 을 호출하여 메모리 확인이 가능하다.
 *
 * @author taehwan
 */
public class AvailableCapacityCheck {
    // 싱글톤
    private static AvailableCapacityCheck mAvailableCapacityCheck;

    /**
     * 녹화를 하기 위한 Android 시스템의 최하 메모리 요구 사항으로 500 MB로 정의.
     */
    public static final long RECORD_REFERENCE_MEMORY = 0x1F400000L; // 500 MB
//	public static final long RECORD_REFERENCE_MEMORY = 0x38400000L * 100; // 500 MB

    /**
     * 스크린샷 위한 Android 시스템의 최하 메모리 요구 사항으로 10 MB로 정의.
     */
    public static final long CAPTURE_REFERENCE_MEMORY = 0xA00000L; // 10 MB
//	public static final long CAPTURE_REFERENCE_MEMORY = 0x38400000L * 100; // 10 MB

    /**
     * Android {@link android.media.MediaMuxer}에서 저장가능한 최대 용량이 2GB인점을 생각하여 1.86GB로 정의.
     */
    public static final long WRITE_BASE_SIZE = 0x77359400L; // 1.86 GB
//	public static final long WRITE_BASE_SIZE = 0x100000L;

    /**
     * @deprecated SD카드가 존재하는지 체크하는 변수로 현재는 제거.
     */
    private boolean isExternalStorage;

    /**
     * 파일이 저장되는 경로를 넘겨받아 {@link AvailableCapacityCheck}를 초기화
     *
     * @param directoryPath
     * @return
     */
    public static AvailableCapacityCheck create() {
        if (mAvailableCapacityCheck == null) {
            mAvailableCapacityCheck = new AvailableCapacityCheck();
        }
        return mAvailableCapacityCheck;
    }

    /**
     * @deprecated 해당 메소드는 SDCARD 사용 여부를 체크.
     * 확장 SDCARD인 경우 예외가 필요하여 우선. 제외.
     */
    private void isExternalStorage() {
        String state = Environment.getExternalStorageState();
        isExternalStorage = false;

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            isExternalStorage = true;
        }
    }

    /**
     * 활성화된 메모리 size를 계산하며, {@link StatFs} 사용.
     *
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressWarnings("deprecation")
    private long getAvailableCapacity(String filePath) {
//		if (!isExternalStorage)
//			return -1;

        long blockSize = 0;
        long avaiableBlock = 0;

        StatFs statFs = new StatFs(filePath);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = statFs.getBlockSizeLong();
            avaiableBlock = statFs.getAvailableBlocksLong();

        } else {
            blockSize = statFs.getBlockSize();
            avaiableBlock = statFs.getAvailableBlocks();
        }

        return blockSize * avaiableBlock;
    }

    /**
     * 실제 남아있는 메모리를 체크하여 {@link #REFERENCE_MEMORY} 초과시 TRUE를 리턴하여 프로그램을 종료할 수있도록 유도.
     *
     * @return TRUE라면 남은 공간이 부족하여 프로그램 종료, FALSE라면 정상.
     */
    public boolean isRecordAvailableCapacitySize(String filePath) {
//		if (!isExternalStorage)
//			return false;

        long size = getAvailableCapacity(filePath);
        if (size > RECORD_REFERENCE_MEMORY && size != -1) {
            return true;
        }

        return false;
    }

    /**
     * 실제 남아있는 메모리를 체크하여 {@link #REFERENCE_MEMORY} 초과시 TRUE를 리턴하여 프로그램을 종료할 수있도록 유도.
     *
     * @return TRUE라면 남은 공간이 부족하여 프로그램 종료, FALSE라면 정상.
     */
    public boolean isCaptureAvailableCapacitySize(String filePath) {
//		if (!isExternalStorage)
//			return false;

        long size = getAvailableCapacity(filePath);
        if (size > CAPTURE_REFERENCE_MEMORY && size != -1) {
            return true;
        }

        return false;
    }
}
