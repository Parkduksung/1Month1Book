package com.rsupport.sony;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Environment;
import android.os.Handler;

import com.rsupport.media.IMediaCodecListener;
import com.rsupport.media.mediaprojection.Configuration;
import com.rsupport.media.mediaprojection.record.IRSEncoder;
import com.rsupport.media.mediaprojection.record.adjust.AdjustBitrate;
import com.rsupport.media.mediaprojection.record.adjust.AdjustFps;
import com.rsupport.media.mediaprojection.record.adjust.BitRateMonitor;
import com.rsupport.media.mediaprojection.record.codec.IRSMediaCodec;
import com.rsupport.media.mediaprojection.record.codec.RSMediaCodec18Surface;
import com.rsupport.media.mediaprojection.record.gl.VirtualDisplayGLSurface;
import com.rsupport.util.MemoryFileEx;
import com.rsupport.util.Screen;
import com.rsupport.util.log.RLog;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
public class EncoderSony implements IRSEncoder {

    private static final boolean FILE_OUTPUT_TEST = false;
    private static final int CAPTURE_DELAY = 75;

    private Context context;

    private Configuration configuration;
    private ExecutorService executorService;

    private IMediaCodecListener mediaCodecListener;

    private boolean isRunning = false;

    private RandomAccessFile writeSampleData;

    private IRSMediaCodec rsMediaCodecSony;

    private AdjustFps adjustFps; // FPS control
    private AdjustBitrate adjustBitrate; // Bitrate control
    /**
     * Bitrate Monitor 로써 Bitrate Time을 기준으로 {@link #adjustBitrate}에 callback 을 하여
     * Bitrate 컨트롤을 한다.
     */
    private BitRateMonitor bitRateMonitor;
    private long startTime = 0;
    private SonyScreenCapture capture = null;

    private final int HEAD_SIZE = 32;

    /**
     * EGL Surface draw.
     */
    private VirtualDisplayGLSurface glSurfaceDraw = null;

    private Future future;
    private int rotation;

    public EncoderSony(Context context) {
        this.context = context;
        executorService = Executors.newSingleThreadExecutor();

        adjustFps = new AdjustFps();
        adjustBitrate = new AdjustBitrate();
        bitRateMonitor = new BitRateMonitor();
        bitRateMonitor.setOnBitrateChangeListener(adjustBitrate);
        capture = new SonyScreenCapture(context);
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
//        if (surfaceReader != null) {
//            return surfaceReader.getNowPresentationTime();
//        }
        return 0;
    }

    @Override
    public MediaFormat getMediaFormat() {
        return rsMediaCodecSony.getMediaFormat();
    }

    @Override
    public void initialized() {
        if (configuration == null) {
            throw new NullPointerException("Configuration is null");
        }
        startTime = 0;
        final int width = configuration.mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
        final int height = configuration.mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
        final int frameRate = configuration.mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);

        // Bitrate 내부 재 설정.
        adjustBitrate.setConfigure(width, height, frameRate, configuration.codecValue);
        configuration.mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, adjustBitrate.getCurrentBitrate());
        bitRateMonitor.setFrameRate(frameRate);

        final int bitrate = configuration.mediaFormat.getInteger(MediaFormat.KEY_BIT_RATE);
        final int iFrameInterval = configuration.mediaFormat.getInteger(MediaFormat.KEY_I_FRAME_INTERVAL);

        rsMediaCodecSony = new RSMediaCodec18Surface();
        rsMediaCodecSony.initEncoder(width, height, bitrate, frameRate, iFrameInterval);
        rsMediaCodecSony.setOnDequeueBufferListener(dequeueBufferListener);

        try {
            while (!capture.isConnected()) {
                Thread.sleep(20);
            }
            capture.init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        final Runnable capture = new Runnable() {
            @Override
            public void run() {
                if (rsMediaCodecSony.preEncoder()) {

                    adjustBitrate.setMediaCodec(rsMediaCodecSony.getMediaCodec());
                    if (frameRate > 0) {
                        adjustFps.setFps(frameRate);
                    }

                    glSurfaceDraw = new VirtualDisplayGLSurface(rsMediaCodecSony.getSurface(), configuration.screenWidth, configuration.screenHeight);
                    glSurfaceDraw.initialized();


                    startCapture();
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

    private boolean startCapture() {
        if (isRunning) {
            return true;
        }
        RLog.i("StartVirtualDisplay record");

        future = executorService.submit(encoderLoop);
        isRunning = true;
        onSendStatus(IMediaCodecListener.STATUS_START);
        return true;
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
//        if (surfaceReader != null) {
//            surfaceReader.stop();
//        }
        isRunning = false;

    }

    private void release() {
//        if (surfaceReader != null) {
//            surfaceReader.onDestroy();
//            surfaceReader = null;
//        }
//
//        if (glSurfaceDraw != null) {
//            glSurfaceDraw.release();
//            glSurfaceDraw = null;
//        }

        isRunning = false;
        capture.close();
        if (rsMediaCodecSony != null) {
            rsMediaCodecSony.onDestroy();
            rsMediaCodecSony = null;
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

        executorService = null;
        configuration = null;
        future = null;

        RLog.i("Encoder release");
        onSendStatus(IMediaCodecListener.STATUS_STOP);
    }

    public MemoryFileEx mAshm = null;
    /**
     * Encoder 데이터 처리
     */
    private Runnable encoderLoop = new Runnable() {
        @Override
        public void run() {
            RLog.i("encoderLoop");
            isRunning = true;
            boolean isScreenOn = Screen.isScreenOn(context);

            try {
                while (isRunning) {
                    if (startTime == 0) {
                        startTime = System.currentTimeMillis();
                    }
                    if (capture.getFileDescriptorSize() <= 0) {
                        continue;
                    }

//                    int capResult = capture.capture();

//                    if (capResult != 0) {
//
//                        if (!isScreenOn && Screen.isScreenOn(context)) {
//                            capture.close();
//                            capture.init();
//                        }
//                    }
                    capture.capture();
                    byte[] imageBuffer = new byte[capture.getFileDescriptorSize()];
                    capture.getFileDescriptorSize();
                    FileDescriptor fd = capture.getFileDescriptor();
                    FileInputStream inputStream = new FileInputStream(fd);
                    inputStream.read(imageBuffer);
//
                    Bitmap.Config config = Bitmap.Config.ARGB_8888;
                    Bitmap bm = Bitmap.createBitmap(capture.getRect().width(), capture.getRect().height(), config);
                    bm.copyPixelsFromBuffer(ByteBuffer.wrap(imageBuffer));

                    try {
                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/image.jpg");
                        file.deleteOnExit();
                        FileOutputStream fOut = new FileOutputStream(file);
                        bm.compress(Bitmap.CompressFormat.JPEG, 30, fOut);

                        fOut.flush();
                        fOut.close();
                        RLog.d(">>> ScreenCap dumpfile file.length() : " + file.length());
                    } catch (Exception e) {
                        e.printStackTrace();
                        RLog.i(null, "dumpfile error!");
                    }


                    int width = capture.getRect().width();
                    int height = capture.getRect().height();
                    int pixelStride = 1;
                    int rowStride = width;
                    int rowPadding = rowStride - pixelStride * width;

                    if (glSurfaceDraw != null) {

//                        glSurfaceDraw.onDrawable(imageBuffer, width, height, pixelStride, rowStride, rowPadding, getNowPresentationTime());
                    }
                    if (!rsMediaCodecSony.dequeueOutputBuffer()) {
                        break;
                    }

                    inputStream.close();
                    Thread.sleep(CAPTURE_DELAY);

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

    public long getNowPresentationTime() {
        return (System.currentTimeMillis() - startTime) * 1000000;
    }
}
