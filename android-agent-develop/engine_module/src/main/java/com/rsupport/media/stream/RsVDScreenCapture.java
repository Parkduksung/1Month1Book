/*
 *
 * <b>Copyright (c) 2012 RSUPPORT Co., Ltd. All Rights Reserved.</b><p>
 *
 * <b>NOTICE</b> :  All information contained herein is, and remains the property
 * of RSUPPORT Company Limited and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to RSUPPORT
 * Company Limited and its suppliers and are protected by trade secret
 * or copyright law. Dissemination of this information or reproduction
 * of this material is strictly forbidden unless prior written permission
 * is obtained from RSUPPORT Company Limited.<p>
 *
 * Author  : Park Sung Yeon <br>
 * Date    : 2014. .  <br>
 *
 */

package com.rsupport.media.stream;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.rsupport.litecam.binder.Binder;
import com.rsupport.media.mediaprojection.IScreenCapturable;
import com.rsupport.media.mediaprojection.IVirtualDisplay;
import com.rsupport.media.mediaprojection.ProjectionPermission;
import com.rsupport.rsperm.IRSPerm;
import com.rsupport.util.log.RLog;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.Buffer;

@TargetApi(19)
public class RsVDScreenCapture {

    private Context mContext;
    private Binder binder = null;
    private IRSPerm mRsperm;
    int mWidth;
    int mHeight;
    private int mRotation = 0;
    private ImageReader mImageReader;
    private Handler mHandler;

    private CaptureCallback captureCallback = CaptureCallback.empty;


    private ProjectionPermission projectionPermission;
    private IVirtualDisplay virtualDisplay;

    public RsVDScreenCapture(Context context) {
        mContext = context;
        initialize();
    }

    public void startScreenCapture(@NotNull CaptureCallback captureCallback, int width, int height) {
        this.captureCallback = captureCallback;
        if (initialize()) {
            setUpVirtualDisplay(width, height);
        }
    }


    private boolean initialize() {
        binder = Binder.getInstance();
        mRsperm = binder.getBinder();

        if (mRsperm == null) {
            try {
                noRsperm();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        mRsperm.isBinded();
        initDeviceInfo();

        return true;
    }

    private void noRsperm() throws Exception {
        if (projectionPermission == null) {
            projectionPermission = new ProjectionPermission(mContext);
        }

        if (!projectionPermission.isBound()) {
            RLog.i("ProjectionPermission.isBound " + projectionPermission.isBound());
            projectionPermission.bind();
            Thread.sleep(500);
        }

        IScreenCapturable capturable = projectionPermission.createScreenCapturable();
        virtualDisplay = (IVirtualDisplay) capturable.initialized();
    }


    public void stopScreenSharing() {
        if (mRsperm != null && mRsperm.isBinded()) {
            mRsperm.createVirtualDisplay("VDRemoteDisplay", 0, 0, 0, 0, null); // release previous if existed.
        }
    }

    private void setUpVirtualDisplay(int width, int height) {
        Display disp = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        disp.getMetrics(metrics);

        Point size = new Point(metrics.widthPixels, metrics.heightPixels);

        RLog.i("Header size.x : " + size.x + "size.y : " + size.y);
        RLog.i("Setting up a VirtualDisplay: %s, density: %d", size.toString(), metrics.densityDpi);
        Surface surface = getSurface(size.x, size.y);
        stopScreenSharing();

        int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR;
        boolean ret = false;
        if (mRsperm != null) {
            ret = mRsperm.createVirtualDisplay("VDRemoteDisplay", size.x, size.y, metrics.densityDpi, flags, surface);
        } else if (virtualDisplay != null) {
            ret = virtualDisplay.createVirtualDisplay("VDRemoteDisplay", size.x, size.y, metrics.densityDpi, surface, flags);
        }
        assert ret;
    }


    Surface getSurface(int width, int height) {
        if (mImageReader != null) {
            if (mImageReader.getWidth() != width || mImageReader.getHeight() != height) {
                Image i = mImageReader.acquireLatestImage();
                if (i != null) i.close();
                mImageReader.setOnImageAvailableListener(null, null);
                mImageReader.close();
                mImageReader = null;
            }
        }

        if (mImageReader == null) {
            mHandler = new Handler(mContext.getMainLooper());
//            final HandlerThread backgroundHandler = new HandlerThread("Capture");
//            backgroundHandler.start();
//            mHandler = new Handler(backgroundHandler.getLooper());
            mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
            mImageReader.setOnImageAvailableListener(new NewImageAvailableListener(), mHandler);
        }
        return mImageReader.getSurface();
    }

    class ScreenQueData {
        byte[] screenData;
        int offset;
        int size;

        public ScreenQueData(byte[] data, int offset, int size) {
            screenData = data;
            this.offset = offset;
            this.size = size;
        }
    }

    class NewImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            try {
                Image image = reader.acquireLatestImage();
                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    Image.Plane plane = planes[0];
                    int stride = plane.getRowStride();
                    int width = image.getWidth();
                    int height = image.getHeight();
                    int format = image.getFormat();


                    RLog.i(">>>VD_Screen_Capture");

                    final Bitmap bitmap;
                    String jpgFile = mContext.getFilesDir() + "/screenshot.jpg";
                    new File(jpgFile).delete();
                    FileOutputStream out = new FileOutputStream(jpgFile);
                    final Buffer buffer = plane.getBuffer();
                    int rowPadding = planes[0].getRowStride() - planes[0].getPixelStride() * width;

                    bitmap = Bitmap.createBitmap((width + (planes[0].getRowStride() - plane.getPixelStride() * width) / plane.getPixelStride()), height, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);
                    int newWidth = 480;
                    int newHeight = 800;
                    if (mRotation == Surface.ROTATION_90 || mRotation == Surface.ROTATION_270) {
                        if (width > height) {
                            newWidth = 800;
                            newHeight = 480;
                        }
                    } else {
                        if (width > height) {
                            newHeight = 480;
                            newWidth = 800;
                        }
                    }

                    float scaleWidth = ((float) newWidth) / bitmap.getWidth();
                    float scaleHeight = ((float) newHeight) / bitmap.getHeight();

                    Matrix matrix = new Matrix();
                    matrix.postScale(scaleWidth, scaleHeight);
                    Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);

                    out.close();
                    bitmap.recycle();
                    resizedBitmap.recycle();
                    image.close();
                    buffer.clear();
                    captureCallback.onSuccess();
                }
            } catch (Exception e) {
                e.printStackTrace();
                captureCallback.onFailure();
            } finally {
                if (virtualDisplay != null) {
                    virtualDisplay.release();
                }
                if (mImageReader != null) {
                    mImageReader.close();
                }
                if (projectionPermission != null) {
                    projectionPermission.onDestory();
                    projectionPermission = null;
                }
                stopScreenSharing();
            }
        }
    }


    private void initDeviceInfo() {
        Display dis = ((WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        dis.getSize(size);
        mWidth = 480;
        mHeight = 800;

//	    mWidth = size.x;
//	    mHeight = size.y;
    }

    public interface CaptureCallback {
        void onSuccess();

        void onFailure();

        CaptureCallback empty = new CaptureCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure() {
            }
        };
    }
}
