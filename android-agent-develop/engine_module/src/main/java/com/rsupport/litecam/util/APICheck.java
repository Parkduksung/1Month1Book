package com.rsupport.litecam.util;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

import com.rsupport.litecam.record.RecordSet;

import android.os.Build;
import android.os.Build.VERSION_CODES;

/**
 * liteCam을 사용하기 위한 클래스로. API Version 체크를 진행합니다.
 * 설정값은 {@link RecordSet}의 설정값에 따라서 변경될 수 있습니다.
 *
 * @author taehwan
 */
public class APICheck {

    /**
     * liteCam Support Version check. Android
     * {@link VERSION_CODES#JELLY_BEAN_MR1} 이상이고, CPU가 2(Dual core) 이상인 경우에만 지원.
     *
     * @return
     */
    public static boolean isSupportedLiteCam() {
        if (isSupportedVersion() && isSupportedProcessors()) {
            return true;
        }

        return false;
    }

    /**
     * liteCam support version. {@link VERSION_CODES#JELLY_BEAN_MR1} 이상에서 동작합니다.
     *
     * @return supported is true, not supported is false
     */
    private static boolean isSupportedVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) { // 공식 speck
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { // 비공식 speck
            return true;
        }
        return false;
    }

    private static boolean isSupportedProcessors() {
        if (getNumCores() >= RecordSet.DUAL_CORE) {
            return true;
        }
        return false;
    }

    /**
     * liteCam 내부에서 사용하는 method로 Dualcore를 체크합니다.
     *
     * @return
     */
    public static boolean isDualCoreCheck() {
        if (getNumCores() == RecordSet.DUAL_CORE) {
            return true;
        }
        return false;
    }

    /**
     * liteCam 내부에서 사용하는 method로 GPU에서 image 변환 지원 여부를 확인합니다.
     *
     * @param isGPU
     * @return
     */
    public static boolean getImageTransformation(boolean isGPU) {
        if (isNewAPISupportVersion()) {
            return isGPU;
        }

        return false;
    }

    ;

    /**
     * liteCam GPU 사용 유무 판단, Muxer 최신버전 적용 가능 판단.<br>
     * Android 4.2.2 이하 버전은 GPU 사용이 불가능하므로 FALSE, 4.3 이상 버전은 CPU/GPU 사용 유무 확인.
     *
     * @return TRUE는 최신버전 Support, FALSE는 최신버전 Not support.
     */
    public static boolean isNewAPISupportVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ? true : false;
    }

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     *
     * @return The number of cores, or 1 if failed to get result
     */
    public static int getNumCores() {
        // Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                // Check if filename is "cpu", followed by a single digit number
                if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }

                return false;
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)

            RecordSet.CPU_COUNT = files.length;

            return files.length;

        } catch (Exception e) {
            return 1;
        }
    }
}
