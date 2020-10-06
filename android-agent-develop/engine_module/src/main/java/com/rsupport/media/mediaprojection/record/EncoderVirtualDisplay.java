package com.rsupport.media.mediaprojection.record;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;

import com.rsupport.media.IMediaCodecListener;
import com.rsupport.media.mediaprojection.Configuration;
import com.rsupport.media.mediaprojection.IVirtualDisplay;
import com.rsupport.media.mediaprojection.record.adjust.AdjustBitrate;
import com.rsupport.media.mediaprojection.record.adjust.AdjustFps;
import com.rsupport.media.mediaprojection.record.adjust.BitRateMonitor;
import com.rsupport.media.mediaprojection.record.codec.IRSMediaCodec;
import com.rsupport.media.mediaprojection.record.codec.RSMediaCodec21Surface;
import com.rsupport.media.mediaprojection.record.gl.VirtualDisplayGLSurface;
import com.rsupport.media.mediaprojection.record.surface.OnVirtualDisplayCallbackListener;
import com.rsupport.media.mediaprojection.record.surface.SurfaceReader;
import com.rsupport.rsperm.IRSPerm;
import com.rsupport.util.log.RLog;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by taehwan on 5/13/15.
 * <p/>
 * 4.4 VirtualDisplay와 5.0 이상의 MediaProjection을 사용할 경우 다음 클래스를 호출한다.
 */
@TargetApi(19)
public class EncoderVirtualDisplay implements IRSEncoder {

    public static final String VIRTUAL_DISPLAY_NAME = "display:VDRemoteDisplay";
    private static final boolean FILE_OUTPUT_TEST = false;

    private Context context;

    private SurfaceReader surfaceReader;

    private Configuration configuration;
    private ExecutorService executorService;

    private IRSPerm rsperm;
    private IMediaCodecListener mediaCodecListener;
    private IVirtualDisplay virtualDisplay;

    private boolean isRunning = false;

    private RandomAccessFile writeSampleData;

    private IRSMediaCodec rsMediaCodecForSurface;

    private AdjustFps adjustFps; // FPS control
    private AdjustBitrate adjustBitrate; // Bitrate control
    /**
     * Bitrate Monitor 로써 Bitrate Time을 기준으로 {@link #adjustBitrate}에 callback 을 하여
     * Bitrate 컨트롤을 한다.
     */
    private BitRateMonitor bitRateMonitor;

    /**
     * EGL Surface draw.
     */
    private VirtualDisplayGLSurface glSurfaceDraw = null;

    private Future future;
    private int rotation;

    public EncoderVirtualDisplay(Context context) {
        this.context = context;
        executorService = Executors.newSingleThreadExecutor();

        adjustFps = new AdjustFps();
        adjustBitrate = new AdjustBitrate();
        bitRateMonitor = new BitRateMonitor();
        bitRateMonitor.setOnBitrateChangeListener(adjustBitrate);

        if (FILE_OUTPUT_TEST) {
            File file = new File(String.format("/sdcard/temp_%d.h264", System.currentTimeMillis()));
            if (file.isFile()) {
                file.delete();
            }

            try {
                writeSampleData = new RandomAccessFile(file, "rw");
            } catch (IOException e) {
                //
            }
        }
    }

    @Override
    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    @Override
    public void setFps(int fps) {
        if (fps <= 0) return;
        adjustFps.setFps(fps);
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void setMediaCodecListener(IMediaCodecListener mediaCodecListener) {
        this.mediaCodecListener = mediaCodecListener;
    }

    @Override
    public long getPresentationTime() {
        if (surfaceReader != null) {
            return surfaceReader.getNowPresentationTime();
        }
        return 0;
    }

    @Override
    public MediaFormat getMediaFormat() {
        return rsMediaCodecForSurface.getMediaFormat();
    }

    public void setVirtualDisplay(IRSPerm rsperm) {
        this.rsperm = rsperm;
    }

    public void setVirtualDisplay(IVirtualDisplay virtualDisplay) {
        this.virtualDisplay = virtualDisplay;
    }


    @Override
    public void initialized() {
        if (configuration == null) {
            throw new NullPointerException("Configuration is null");
        }

        final int width = configuration.mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
        final int height = configuration.mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
        final int frameRate = configuration.mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);

        // Bitrate 내부 재 설정.
        adjustBitrate.setConfigure(width, height, frameRate, configuration.codecValue);
        configuration.mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, adjustBitrate.getCurrentBitrate());
        bitRateMonitor.setFrameRate(frameRate);

        final int bitrate = configuration.mediaFormat.getInteger(MediaFormat.KEY_BIT_RATE);
        final int iFrameInterval = configuration.mediaFormat.getInteger(MediaFormat.KEY_I_FRAME_INTERVAL);

        rsMediaCodecForSurface = new RSMediaCodec21Surface();
        rsMediaCodecForSurface.initEncoder(width, height, bitrate, frameRate, iFrameInterval);
        rsMediaCodecForSurface.setOnDequeueBufferListener(dequeueBufferListener);

        final Runnable capture = new Runnable() {
            @Override
            public void run() {
                if (rsMediaCodecForSurface.preEncoder()) {
                    adjustBitrate.setMediaCodec(rsMediaCodecForSurface.getMediaCodec());

                    if (frameRate > 0) {
                        adjustFps.setFps(frameRate);
                    }
                    surfaceReader = new SurfaceReader(adjustFps);
                    glSurfaceDraw = new VirtualDisplayGLSurface(rsMediaCodecForSurface.getSurface(), configuration.screenWidth, configuration.screenHeight);
                    glSurfaceDraw.setRotation(rotation);
                    surfaceReader.setSurfaceDrawable(glSurfaceDraw);
                    surfaceReader.setStartListener(recordVirtualDisplayListener);

                    Surface sourceSurface = surfaceReader.createInputSurface(
                            configuration.screenWidth,
                            configuration.screenHeight,
                            PixelFormat.RGBA_8888);
                    if (virtualDisplay != null) {
                        createVirtualDisplay(virtualDisplay, sourceSurface, configuration.screenWidth, configuration.screenHeight);

                    } else if (rsperm != null) {
                        createVirtualDisplay(rsperm, sourceSurface, configuration.screenWidth, configuration.screenHeight);
                    }

                } else {
                    RLog.e("Initialization fail");
                    onSendStatus(IMediaCodecListener.EVENT_ERROR_INITIALIZATION_FAILED);
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                new Handler(context.getMainLooper()).postDelayed(capture, 500);
            }
        }).start();
    }

    private IRSMediaCodec.OnDequeueBufferListener dequeueBufferListener = new IRSMediaCodec.OnDequeueBufferListener() {
        @Override
        public boolean onDequeueEvent(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo) {
            bitRateMonitor.startTime();
            if (writeSampleData != null) {
                ByteBuffer tempBuffer = null;
                try {
                    tempBuffer = outputBuffer.duplicate();
                    byte[] data = new byte[bufferInfo.size];
                    tempBuffer.get(data);
                    try {
                        writeSampleData.write(data, 0, bufferInfo.size);
                    } catch (IOException e) {

                    }
                } finally {
                    if (tempBuffer != null) {
                        tempBuffer.clear();
                    }
                }
            }

            onSendDequeueEvent(outputBuffer, bufferInfo);

            bitRateMonitor.endTime();
            bitRateMonitor.checkChangeFrameRate();
            return true;
        }

        @Override
        public void onFormatChanged(MediaFormat mediaFormat) {
            onSendFormatChanged(mediaFormat);
            RLog.i(mediaFormat.toString());
        }
    };

    @Override
    public void stop() {
        if (surfaceReader != null) {
            surfaceReader.stop();
        }
    }

    private void release() {
        if (surfaceReader != null) {
            surfaceReader.onDestroy();
            surfaceReader = null;
        }

        if (glSurfaceDraw != null) {
            glSurfaceDraw.release();
            glSurfaceDraw = null;
        }

        isRunning = false;

        if (rsMediaCodecForSurface != null) {
            rsMediaCodecForSurface.onDestroy();
            rsMediaCodecForSurface = null;
        }

        if (writeSampleData != null) {
            try {
                writeSampleData.close();
            } catch (IOException e) {
                //
            }
        }

        if (bitRateMonitor != null) {
            bitRateMonitor.onDestory();
            bitRateMonitor = null;
        }

        if (adjustBitrate != null) {
            adjustBitrate.onDestory();
            adjustBitrate = null;
        }

        if (rsperm != null && rsperm.isBinded()) {
            rsperm.createVirtualDisplay(VIRTUAL_DISPLAY_NAME, 0, 0, 0, 0, null);
        }
        if (virtualDisplay != null) {
            virtualDisplay.release();
        }

        executorService = null;
        configuration = null;
        future = null;

        RLog.i("Encoder release");
        onSendStatus(IMediaCodecListener.STATUS_STOP);
    }

    /**
     * 이미지가 들어오면 시작한다.
     */
    private boolean startVirtualDisplay() {
        if (isRunning) {
            return true;
        }
        RLog.i("StartVirtualDisplay record");

        future = executorService.submit(encoderLoop);
        isRunning = true;
        onSendStatus(IMediaCodecListener.STATUS_START);
        return true;
    }

    private boolean createVirtualDisplay(IRSPerm rsperm, Surface surface, int width, int height) {
        DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        Display display = displayManager.getDisplay(0);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR;
        boolean result = rsperm.createVirtualDisplay(VIRTUAL_DISPLAY_NAME, width, height, displayMetrics.densityDpi, flags, surface);

        RLog.i("createVirtualDisplay.%b, width.%d, height.%d, dpi.%f", result, width, height, displayMetrics.density);
        return result;
    }

    private boolean createVirtualDisplay(IVirtualDisplay virtualDisplay, Surface surface, int width, int height) {
        DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        Display display = displayManager.getDisplay(0);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        boolean result = virtualDisplay.createVirtualDisplay(VIRTUAL_DISPLAY_NAME,
                width,
                height,
                displayMetrics.densityDpi,
                surface,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC | DisplayManager.VIRTUAL_DISPLAY_FLAG_SECURE);

        RLog.i("createVirtualDisplay.%b, width.%d, height.%d, dpi.%f", result, width, height, displayMetrics.density);
        return result;
    }

    OnVirtualDisplayCallbackListener recordVirtualDisplayListener = new OnVirtualDisplayCallbackListener() {

        @Override
        public boolean startCallback() {
            return startVirtualDisplay();
        }

        @Override
        public void stopCallback() {
            rsMediaCodecForSurface.signalEndOfStream();
        }
    };

    /**
     * Encoder 데이터 처리
     */
    private Runnable encoderLoop = new Runnable() {
        @Override
        public void run() {
            RLog.i("encoderLoop");
            isRunning = true;
            try {
                while (isRunning) {
                    if (!rsMediaCodecForSurface.dequeueOutputBuffer()) {
                        break;
                    }
                }

            } catch (Exception e) {
                RLog.e(e);
                onSendStatus(IMediaCodecListener.EVENT_ERROR_CAPTURE);

            } finally {
                isRunning = false;
                RLog.i("encoderLoop end");
                release();
            }
        }
    };

    public void onSendFormatChanged(MediaFormat mediaFormat) {
        if (mediaCodecListener != null) {
            mediaCodecListener.onSendFormatChanged(mediaFormat);
        }
    }

    public void onSendDequeueEvent(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
        if (mediaCodecListener != null) {
            mediaCodecListener.onSendDequeueEvent(byteBuffer, bufferInfo);
        }
    }

    public void onSendStatus(@IMediaCodecListener.StatusType int status) {
        if (mediaCodecListener != null) {
            mediaCodecListener.onSendStatus(status);
        }
    }
}
