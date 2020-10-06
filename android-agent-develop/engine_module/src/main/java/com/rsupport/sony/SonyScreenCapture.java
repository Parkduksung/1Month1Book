package com.rsupport.sony;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.ParcelFileDescriptor;
import android.os.RemoteControl;
import android.util.Log;

import com.rsupport.ScreenCapture;
import com.rsupport.rsperm.Proto;

import java.io.FileDescriptor;

/**
 * Created by hyun on 2016. 12. 2..
 */

public class SonyScreenCapture extends ScreenCapture {

    private final String TAG = "SonyCapture";
    private Context context;
    private RemoteControl mRemote;

    private boolean incremental = true;
    private Rect rcSrn = new Rect();
    private int pixelFormat = 0;
    private int pixelPerLine = 0;
    private int mSrnSize = 0;

    private RemoteControl.MemoryAreaInformation memInfo;                // 화면이 저장되는 메모리
    private ParcelFileDescriptor mfdScreen;
    private FileDescriptor mFileDescriptor;
    private boolean isConnected = false;

    public SonyScreenCapture(Context context) {
        this.context = context;

        try {
            // 객체 선언시 화면공유 권한 팝업이 올라온다.
            mRemote = RemoteControl.getRemoteControl(context, mListener);
            Log.i(TAG, "sony remotecontrol is connected.");
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }


    private RemoteControl.ICallbacks mListener = new RemoteControl.ICallbacks() {

        @Override
        public void authorizationChanged(boolean srnOK, boolean inpOK) {
            Log.i(TAG, "first : " + srnOK + " second :" + inpOK);
//			isConnected = srnOK;
        }

        @Override
        public void connectionStatus(int status) {
            Log.i(TAG, "the connection to the service has completed: " + status);
            if (status == 0) {
                isConnected = true;

            } else {
                isConnected = false;

            }
        }

        @Override
        public void deviceInfoChanged() {
            Log.i(TAG, "the frame buffer orientation, flip mode, etc has changed.");
        }
    };

    public synchronized boolean init() throws Exception {
        if (mRemote == null || !isConnected) {
            Log.e(TAG, "Not connected with Sony.RC" + mRemote + " : " + isConnected);
            return false;
        }

        Log.i(TAG, "Connected with Sony.RC");

        // wait is needed???
        if (mfdScreen == null) {
            RemoteControl.DeviceInfo deviceInfo = mRemote.getDeviceInfo();                        // 디바이스 정보 객체 생성

            rcSrn.left = 0;
            rcSrn.top = 0;
            rcSrn.right = deviceInfo.frameBufferWidth;
            rcSrn.bottom = deviceInfo.frameBufferHeight;
            pixelPerLine = deviceInfo.frameBufferStride; // pixelsPerLine.
            pixelFormat = deviceInfo.fbPixelFormat;

            Log.v(TAG, String.format("DeviceInfo: %dx%d,  %dx%d, ppl.%d, pxlFmt.%d, bufsize.%d, orien:%d",
                    deviceInfo.fbWidth, deviceInfo.fbHeight,
                    deviceInfo.frameBufferWidth, deviceInfo.frameBufferHeight,
                    deviceInfo.frameBufferStride, deviceInfo.fbPixelFormat,
                    deviceInfo.frameBufferSize,
                    deviceInfo.displayOrientation
            ));

            memInfo = mRemote.getFrameBufferFd(PixelFormat.RGBA_8888, true);   // 포맷 설정
            mSrnSize = memInfo.getSize();
            mfdScreen = memInfo.getParcelFd();
            mFileDescriptor = mfdScreen.getFileDescriptor();

            Log.v(TAG, String.format("shared mem: %s", mfdScreen.toString()));

            // 1. set ashmem to native.
            // 2. set display info(w,h,ppl,pxlfmt)
            try {
                mRemote.grabScreen(incremental, rcSrn);

            } catch (Exception e) {
                Log.w(TAG, "first grap: " + e.toString());

                incremental = false;
                Log.e(TAG, "Capture fail. incremental " + incremental);
            }
        }

        return true;
    }

    public FileDescriptor getFileDescriptor() {
        return mFileDescriptor;
    }

    public int getFileDescriptorSize() {
        return mSrnSize;
    }

    public boolean isConnected() {
        return isConnected;
    }


    /**
     * SonyRCService에서 캡쳐 호출.
     * - NDK에서 호출하게 됨.
     */
    public int capture() {
        try {
            mRemote.grabScreen(incremental, rcSrn);
            return 0; // android::NO_ERROR

        } catch (Exception e) {
            Log.e(TAG, "capture: " + e.toString());
            e.printStackTrace();
        }
        return -1;
    }

    public Rect getRect() {
        return rcSrn;
    }

    public int getPixelFormat() {
        return pixelFormat;
    }

    public int getPixelPerLine() {
        return pixelPerLine;
    }

    public synchronized void close() {
        try {
            if (mfdScreen != null) {
                mfdScreen.close();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error - SonyPerm.close : " + Log.getStackTraceString(e));
        }

        try {
            if (mRemote != null) {
                mRemote.release();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error - SonyPerm.close : " + Log.getStackTraceString(e));
        }

        memInfo = null;
        mfdScreen = null;
        mRemote = null;
    }

}
