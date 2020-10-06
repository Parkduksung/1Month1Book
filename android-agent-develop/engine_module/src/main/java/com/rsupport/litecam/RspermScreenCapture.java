package com.rsupport.litecam;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import com.rsupport.ScreenCapture;
import com.rsupport.litecam.ScreenInfo.ResolutionInfo;
import com.rsupport.litecam.util.RecordScreenHead;
import com.rsupport.rsperm.IRSPerm;
import com.rsupport.srn30.ASHM_SCREEN;
import com.rsupport.srn30.ASHM_SCREEN.Header;
import com.rsupport.util.MemoryFileEx;
import com.rsupport.util.log.RLog;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Screen data를 불러오기 위한 class
 * rsperm은 {@link com.rsupport.litecam.binder.Binder}에서 미리 bind 처리 후 해당 클래스에서 화면의 ScreenShort 처리
 * <p>
 * {@link RecordScreenHead}에서 screenshort의 head {@link #HEAD_SIZE}에 대한 분석을 처리
 *
 * @author taehwan
 */
public class RspermScreenCapture extends ScreenCapture {
    public static final String TAG = "CaptureAshmem";

    private IRSPerm mPerm = null;
    public MemoryFileEx mAshm = null;

    private int nCaptureWidth = 0;
    private int nCaptureHeight = 0;

    private Header mHeaderInfo;

    public RspermScreenCapture(IRSPerm perm) {
        mPerm = perm;
    }

    int ashmType;

    // run in worker thread ...
    public boolean create(Point size, int colorFormat) {
        nCaptureWidth = size.x;
        nCaptureHeight = size.y;

        return createAshmemory(colorFormat);
    }

    // run in worker thread ...
    public boolean create(ResolutionInfo info, int colorFormat) {
        nCaptureWidth = info.screenSize.x;
        nCaptureHeight = info.screenSize.y;

        return createAshmemory(colorFormat);
    }

    public void setBitmapSave(boolean isSave) {
        BITMAP_SAVE = isSave;
    }

    Header headerInfo;

    private boolean createAshmemory(int colorFormat) {
//		BITMAP_SAVE = true;

        boolean ret = false;

        ashmType = colorFormat;


        StringBuilder sb = new StringBuilder("ashm=screen");
        sb.append("&width=").append(nCaptureWidth);
        sb.append("&height=").append(nCaptureHeight);
        sb.append("&bitType=").append(ashmType);

        try {
            if (mAshm != null)
                mAshm.close();

            mAshm = new MemoryFileEx(mPerm.getFile(sb.toString()), -1);
            mPerm.initScreen(nCaptureWidth, nCaptureHeight);
            ret = mPerm.capture(nCaptureWidth, nCaptureHeight); // fill screen info.

        } catch (Exception e) {
            e.printStackTrace();
            RLog.e("get ashmem: " + e.toString());
            mAshm = null;
        }

        return ret;
    }

    public int getBytesPerLineWidth() {
        return mHeaderInfo.bytesPerLine / 4;
    }

    public void closeAshmemory() {
        if (mAshm != null) {
            mAshm.close();
            mAshm = null;
        }
    }

    public int getBufferSize() {
        return (mAshm.length() - HEAD_SIZE);
    }

    public byte[] getHead() {
        byte[] buffer = new byte[mAshm.length()];
        try {
            mAshm.readBytes(buffer, 0, 0, mAshm.length());
            return buffer;

        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return null;
    }

    private static final int HEAD_SIZE = 32;
    private boolean BITMAP_SAVE = false;
    private Context mContext;
    private int rotation = -1;

    public void screenshot(Context context, byte[] frameData, int len, int rotation) {
        this.rotation = rotation;
        screenshot(context, frameData, len);

    }

    public void screenshot(Context context, byte[] frameData, int len) {
        mContext = context;
        screenshot(frameData, len);
    }

    public void screenshot(byte[] frameData, int len) {
        assert (mAshm != null && mPerm != null);
        try {
            // ret 1: success, 0: fail.
            mPerm.capture(nCaptureWidth, nCaptureHeight);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int copy_len = len;
//		if(mAshm == null){
//			Log.d(TAG, "mAshm: null");
//			return;
//		}
        if ((mAshm.length() - HEAD_SIZE) < len) {
            copy_len = (mAshm.length() - HEAD_SIZE);
            Log.d(TAG, "copy_len: " + copy_len + ", req len: " + len);
        }

        // Log.i(TAG, String.format("Ashm: length.%d, ", mAshm.length()));
        try {
            mAshm.readBytes(frameData, HEAD_SIZE, 0, copy_len);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * capture image 저장 테스트
         * BITMAP_SAVE가 TRUE, ashmType이 RGB 일 경우 save
         */
        if (BITMAP_SAVE && ashmType == RecordScreenHead.RGB) {
            if (mContext == null) {
                BITMAP_SAVE = false;
                return;
            }

            byte[] buffer = new byte[mAshm.length()];
            try {
                mAshm.readBytes(buffer, 0, 0, mAshm.length());

                Header headerInfo = ASHM_SCREEN.get(mAshm);
                Log.i(TAG, headerInfo.toString());

                // save as jpeg file.
                String jpgFile = mContext.getFilesDir() + "/screenshot.jpg";
//				new File(jpgFile).delete();
                Bitmap.Config bmpcfg = Bitmap.Config.ARGB_8888;
                Bitmap bmp = Bitmap.createBitmap(headerInfo.width, headerInfo.height, bmpcfg);

                bmp.copyPixelsFromBuffer(ByteBuffer.wrap(frameData));
                if (rotation != -1 && Build.MODEL.equals("LG-F300K")) {
                    if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
                        bmp = imgRotate(bmp, rotation);
                    }
                }
                FileOutputStream out = new FileOutputStream(jpgFile);
                bmp.compress(Bitmap.CompressFormat.JPEG, 30, out);

                out.close();
                bmp.recycle();
                BITMAP_SAVE = false;

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

}