package com.rsupport.media.mediaprojection.record.surface;

import android.annotation.TargetApi;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

import com.rsupport.litecam.util.LLog;
import com.rsupport.media.mediaprojection.record.adjust.AdjustFps;
import com.rsupport.util.log.RLog;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class SurfaceReader {
    private Handler handler = null;
    private ImageReader imageReader = null;
    private ReentrantLock imageReaderLock = null;
    private OnSurfaceDrawable surfaceDrawable = null;

    private HandlerThread handlerThread = null;

    private OnVirtualDisplayCallbackListener recordStateListener = null;

    /**
     * 녹화 중인지 확인하기 위한 signal
     */
    private boolean isStart = false;

    private long startTime = 0;

    /**
     * 녹화 종료 signal 처리
     */
    private boolean endOfStream = false;

    private AdjustFps adjustFps;


    public SurfaceReader(AdjustFps adjustFps) {
        imageReaderLock = new ReentrantLock(true);
        isStart = false;
        endOfStream = false;
        this.adjustFps = adjustFps;
    }

    /**
     * Record is stop
     */
    public void stop() {
        endOfStream = true;
    }

    public void onDestroy() {
        imageReaderLock.lock();
        LLog.i("VirtualDisplay release");
        stop();
        adjustFps = null;

        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }

        if (handlerThread != null) {
            handlerThread.quit();
            handlerThread = null;
        }

        surfaceDrawable = null;
        handler = null;

        isStart = false;
        endOfStream = false;

        imageReaderLock.unlock();
    }

    public Surface getSurface() {
        return imageReader.getSurface();
    }

    public void setStartListener(OnVirtualDisplayCallbackListener listener) {
        this.recordStateListener = listener;
    }

    /**
     * Create input surface(Virtual Display 에서 사용 할 ImageReader
     */
    public Surface createInputSurface(int width, int height, int pixelFormat) {
        handlerThread = new ImageReaderHandlerThread("SurfaceReader");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        imageReader = ImageReader.newInstance(width, height, pixelFormat, 2);
        imageReader.setOnImageAvailableListener(imageListener, handler);
        return imageReader.getSurface();
    }

    private OnImageAvailableListener imageListener = new OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            imageReaderLock.lock();

            if (adjustFps == null || imageReader == null || recordStateListener == null) {
                imageReaderLock.unlock();
                return;
            }

            /**
             * 녹화 종료 체크.
             */
            if (endOfStream && isStart) {
                RLog.i("Stream end");
                isStart = false;

                if (recordStateListener != null) {
                    recordStateListener.stopCallback();
                }

                imageReaderLock.unlock();
                return;
            }

            if (!isStart) {
                RLog.i("Stream started");
                isStart = recordStateListener.startCallback();
                if (startTime == 0) {
                    startTime = System.currentTimeMillis();
                }
            }

            if (imageReader != null) {
                Image image = null;
                try {
                    image = reader.acquireLatestImage();

                    if (adjustFps.isDelayed()) {
                        imageReaderLock.unlock();
                        if (image != null) {
                            image.close();
                        }
                        return;
                    }

                    if (image != null) {
                        Plane plane = image.getPlanes()[0];
                        ByteBuffer imageBuffer = plane.getBuffer();
                        int width = image.getWidth();
                        int height = image.getHeight();

                        int pixelStride = plane.getPixelStride();
                        int rowStride = plane.getRowStride();
                        int rowPadding = rowStride - pixelStride * width;

                        if (surfaceDrawable != null) {
                            surfaceDrawable.onDrawable(imageBuffer, width, height, pixelStride, rowStride, rowPadding, getNowPresentationTime());
                        }
                    }

                } catch (Exception e) {
                    RLog.e(e);

                } finally {
                    if (image != null) {
                        image.close();
                    }
                }
            }
            imageReaderLock.unlock();
        }
    };

    /**
     * SurfaceView에서 사용하는 PresentationTime.
     *
     * @return
     */
    public long getNowPresentationTime() {
        return (System.currentTimeMillis() - startTime) * 1000 * 1000;
    }

    public void setSurfaceDrawable(OnSurfaceDrawable surfaceDrawable) {
        this.surfaceDrawable = surfaceDrawable;
    }

    class ImageReaderHandlerThread extends HandlerThread {
        public ImageReaderHandlerThread(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            if (surfaceDrawable != null) {
                surfaceDrawable.initialized();
            }
        }
    }
}