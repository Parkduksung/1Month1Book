package com.rsupport.srn30.screen.encoder.surface;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.*;
import android.view.Surface;

import com.rsupport.srn30.adjust.AdjustFPS;
import com.rsupport.srn30.adjust.FPSMonitor;
import com.rsupport.util.rslog.MLog;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class SurfaceReader{
	private Handler handler = null;
	private ImageReader imageReader = null;
	private ReentrantLock imageReaderLock = null;
	private OnSurfaceDrawable surfaceDrawable = null;
	private HandlerThread handlerThread = null;
    private AdjustFPS adjustFPS = null;
	private FPSMonitor fpsMonitor = null;

	public SurfaceReader(Context context) {
		imageReaderLock = new ReentrantLock(true);
	}

	public void onDestroy() {
		MLog.i("onDestroy");
		imageReaderLock.lock();
		if(imageReader != null){
			imageReader.close();
			imageReader = null;
		}

		if(handlerThread != null){
			handlerThread.quit();
			handlerThread = null;
		}
		imageReaderLock.unlock();
		handler = null;
	}
	
	public Surface getSurface() {
		return imageReader.getSurface();
	}

    /**
     * {@link #createInputSurface(int, int, int, int)}
     */
    @Deprecated
	public Surface createInputSurface(int width, int height, int pixelFormat){
		return createInputSurface(width, height, pixelFormat, -1);
	}

	public Surface createInputSurface(int width, int height, int pixelFormat, int frameRate){
        if(frameRate > 0){
            adjustFPS = new AdjustFPS();
            adjustFPS.init(frameRate);
			fpsMonitor = new FPSMonitor();
			fpsMonitor.setChangeListener(adjustFPS);
        }
		handlerThread = new ImageReaderHandlerThread("SurfaceReader");
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper());
		imageReader = ImageReader.newInstance(width, height, pixelFormat, 2);
		imageReader.setOnImageAvailableListener(imageListener, handler);
		return imageReader.getSurface();
	}

	public void setSurfaceDrawable(OnSurfaceDrawable surfaceDrawable) {
		this.surfaceDrawable = surfaceDrawable;
	}

	private OnImageAvailableListener imageListener = new OnImageAvailableListener() {
		@Override
		public void onImageAvailable(ImageReader reader) {
			imageReaderLock.lock();
			if(imageReader != null){
				Image image = null;
				try {
					image = reader.acquireLatestImage();

                    if(adjustFPS != null && adjustFPS.isContinue(false)){
                        imageReaderLock.unlock();
                        return;
                    }

					if(image != null){
						if(fpsMonitor != null){
							fpsMonitor.startTime();
						}
						Image.Plane planes[] = image.getPlanes();
						Image.Plane plane = planes[0];
						ByteBuffer imageBuffer = plane.getBuffer();
						int width = image.getWidth();
						int height = image.getHeight();
						int pixelStride = plane.getPixelStride();
						int rowStride = plane.getRowStride();
						int rowPadding = rowStride - pixelStride * width;
//						log.w("w.%d, h.%d, pixelStride.%d, rowStride.%d, rowPadding.%d, ", width, height, pixelStride, rowStride, rowPadding);
						if(surfaceDrawable != null){
							surfaceDrawable.onDrawable(imageBuffer, width, height, pixelStride, rowStride, rowPadding);
						}
						if(fpsMonitor != null){
							fpsMonitor.endTime();;
							fpsMonitor.checkChangeFrameRate();
						}
					}
				} catch (Exception e) {
				} finally {
					if(image != null){
						image.close();
					}
				}
			}
			imageReaderLock.unlock();
		}
	};

    class ImageReaderHandlerThread extends HandlerThread{
		public ImageReaderHandlerThread(String name) {
			super(name);
		}

		@Override
		protected void onLooperPrepared() {
            if(surfaceDrawable != null){
                surfaceDrawable.initialized();
            }
		}

        @Override
        public void run() {
            super.run();
            if(surfaceDrawable != null){
                surfaceDrawable.release();
            }
        }
    }
}