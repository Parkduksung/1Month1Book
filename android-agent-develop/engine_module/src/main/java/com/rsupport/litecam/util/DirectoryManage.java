package com.rsupport.litecam.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;

import com.rsupport.litecam.record.RecordSet;

/**
 * 폴더와 임시 파일을 생성하며, 폴더는 사용자 설정에 따라 변경됩니다.
 * 임시 파일 이름은 {@link RecordSet#DEFAULT_FILE_NAME}과 {@link RecordSet#FILE_DATE_TYPE} 조합으로 .mp4 파일이 생성되어,
 * 해당 파일에 Encoder 된 데이터를 저장.
 *
 * @author taehwan
 */
public class DirectoryManage {

    public static boolean createDirectory(String path) {
        File filePath = new File(path);
        if (!filePath.exists()) {
            if (filePath.mkdirs() == false) {
                LLog.e(true, "createDirectory fail : " + path);
                return false;
            }
        }
        return true;
    }

    /**
     * directory create and temp file create
     *
     * @param path is Absolute path
     * @return Absolute path and temp file name
     */
    public static File createDirectoryAndTempFile(String path) {
        File filePath = new File(path);
        if (!filePath.exists()) {
            if (filePath.mkdirs() == false) {
                LLog.e(true, "createDirectory fail : " + path);
            }
        }
        return filePath;
    }

    /**
     * mp4를 저장해야할 temp 파일을 생성한다.
     *
     * @param path is Absolute directory.
     * @return temp file Absolute file path.
     */
    @SuppressLint("SimpleDateFormat")
    public static String createTempFile(File path, String tempFileName) {
        String tempFile = path.getPath();
        tempFile += "/" + tempFileName;
        return tempFile;
    }

    /**
     * mp4를 저장해야할 temp 파일을 생성한다.
     *
     * @param path is Absolute directory.
     * @return temp file Absolute file path.
     */
    @SuppressLint("SimpleDateFormat")
    public static String createTempFile(File path, String tempFileName, String dateType) {
        String tempFile = path.getPath();

        // currentTime Millis
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdfNow = new SimpleDateFormat(dateType);
        tempFile += "/" + tempFileName + sdfNow.format(date) + ".mp4";

        return tempFile;
    }
}