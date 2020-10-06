package com.rsupport.knox;

import android.graphics.Bitmap;
import android.view.Surface;

import com.rsupport.ScreenCapture;
import com.rsupport.util.log.RLog;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

/**
 * Created by Hyungu-PC on 2015-09-07.
 * Knox 스크린 캡쳐 클래스.
 * 싱글턴이어야 재 실행시 에러가 없음.
 */
public class KnoxScreenCapture extends ScreenCapture {
    private int mScrSize = -1;
    private int rotation = -1;
    private int mWidth;
    private int mHeight;
    private static KnoxScreenCapture instance;

    static {
        System.loadLibrary("screencap");
    }

    private KnoxScreenCapture() {

    }

    static public KnoxScreenCapture getInstance() {
        if (instance == null) {
            instance = new KnoxScreenCapture();
        }
        return instance;
    }

    public void initialize(int width, int height) {
        mWidth = width;
        mHeight = height;

        RLog.i(">>> ScreenCap initializeScreen : mWidth : " + mWidth + " mHeight : " + mHeight);
        mScrSize = initializeScreen(width, height);
        RLog.i(">>> ScreenCap initializeScreen : " + mScrSize);
    }

    public int getScreenSize() {
        return mScrSize;
    }

    public int screenshot(byte[] frameData, int scrSize) {
        int capSize = captureScreen(frameData, scrSize);
//        log.i(">>> ScreenCap captureScreen : " + capSize);
        return capSize;

    }

    public boolean screenShotFile(byte[] frameData, int scrSize, String path, int width, int height, int rotation) {
        this.rotation = rotation;
        return screenShotFile(frameData, scrSize, path, width, height);
    }

    public boolean screenShotFile(byte[] frameData, int scrSize, String path, int width, int height) {
        int capSize = captureScreen(frameData, scrSize);
        if (capSize <= 0) {
            finalize();
            return false;
        }

        Bitmap.Config bmpcfg = Bitmap.Config.ARGB_8888;
        Bitmap bm = Bitmap.createBitmap(width, height, bmpcfg);
        bm.copyPixelsFromBuffer(ByteBuffer.wrap(frameData));
        if (rotation != -1 && (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270)) {
            bm = imgRotate(bm, rotation);
        }
        try {
            File file = new File(path);
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 30, fOut);

            fOut.flush();
            fOut.close();
            RLog.d(">>> ScreenCap dumpfile create : " + path);
            RLog.d(">>> ScreenCap dumpfile file.length() : " + file.length());
        } catch (Exception e) {
            e.printStackTrace();
            RLog.i(null, "dumpfile error!");
            return false;
        }
        finalize();
        return true;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void finalize() {
        RLog.i(">>> ScreenCap finalize <<<");
        finalizeScreen();
    }


    private native int initializeScreen(int width, int height);

    private native void finalizeScreen();

    private native int captureScreen(byte[] buffer, int count);


}
