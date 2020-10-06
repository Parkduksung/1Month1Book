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

import android.content.Context;
import android.graphics.Point;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.Display;
import android.view.WindowManager;

import com.rsupport.litecam.LiteCam;
import com.rsupport.litecam.RspermScreenCapture;
import com.rsupport.litecam.ScreenInfo.ResolutionInfo;
import com.rsupport.litecam.binder.Binder;
import com.rsupport.litecam.record.RecordSet;
import com.rsupport.litecam.util.RecordScreen;
import com.rsupport.litecam.util.RecordScreenHead;
import com.rsupport.media.ICodecAdapter;
import com.rsupport.media.ICodecListener;
import com.rsupport.media.RsMediaCodec;
import com.rsupport.media.RsMediaCodecVideo;
import com.rsupport.rsperm.IRSPerm;
import com.rsupport.util.log.RLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import config.EngineConfigSetting;

public class RsScreenCaptureStream implements ScreenStream {

    Context mContext;
    //	RsMediaCodecVideo mCodecVideo;
    RsMediaCodec mCodec;
    Thread captureThread = null;
    CaptureRunnable captureRunnable = null;
    Point realCaptureSize = null;
    RspermScreenCapture screenCapture = null;
    boolean ashmLength = false;
    byte[] frameData = null;
    private IRSPerm rsperm;

    boolean mIsWaterMark = false;
    boolean mIsGPU = false;
    private ResolutionInfo mResolution;
    private static final boolean FILE_OUTPUT_TEST = false;
    private RandomAccessFile writeSampleData;

    private final int INTERVAL_FRAME = 30;
    private final int INTERVAL_FRAME_SAMSUNGPRINTER_3TH = 170;
    private final int INTERVAL_FRAME_HCI_TV = 80;

    public RsScreenCaptureStream(Context context, IRSPerm rsperm) {
        mContext = context;
        this.rsperm = rsperm;

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

        initialize();
    }


    private void initialize() {
        captureRunnable = new CaptureRunnable();
        screenCapture = new RspermScreenCapture(rsperm);

        mFrameType = FRAMEDATA_FROM_ENGINE;
//		mFrameType = FRAMEDATA_FROM_FILE;

        mStreamState = STREAM_STATE_READY;

        initRandomFile();

        initVariables();
    }

    private void initVariables() {
        this.mIsPause = false;
        this.mIsStop = false;
        this.mIsClose = false;
    }

    //	RandomAccessFile mRandomFile;
    private void initRandomFile() {
        try {
//            File f = new File("/sdcard", "rsrecord2.tmp");
            FileOutputStream os = mContext.openFileOutput("rsrecord2.tmp", Context.MODE_PRIVATE);
            os.close();

//            if (f.exists()) {
//                f.delete();
//                f.createNewFile();
//            }
            File f = new File(mContext.getFilesDir(), "rsrecord2.tmp");
            mRandomFile = new RandomAccessFile(f, "rw");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getCurrentRotate() {
        int ret = 0;
        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        return display.getRotation();
    }


    public Point initResolutionInfo(String ratioValue) {
        if (EngineConfigSetting.isHCIBuild()) {
            mResolution = RecordScreen.getResolution(mContext, 0.7f);
        } else if (EngineConfigSetting.isSamsungPrinter()) {
            //삼성프린터 4세대 캡쳐사이즈 조정
            if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
                mResolution = RecordScreen.getResolution(mContext, 0.9f);
            } else {
                mResolution = RecordScreen.getResolution720P(mContext, ratioValue);
            }
        } else {
            mResolution = RecordScreen.getResolution720P(mContext, ratioValue);
        }

        RLog.i("ScreenSize :  X ::" + mResolution.screenSize.x + " Y :: " + mResolution.screenSize.y);
        return mResolution.screenSize;
    }

    private static int mStreamState = 0;

    private final int STREAM_STATE_READY = 0;
    private final int STREAM_STATE_RUNNING = 1;
    private final int STREAM_STATE_PAUSING = 2;
    private final int STREAM_STATE_PAUSED = 3;
    private final int STREAM_STATE_STOPING = 4;

    private static int mReqCommand = 0;

    private final int REQ_COMMAND_NULL = 0;
    private final int REQ_COMMAND_START = 1;
    private final int REQ_COMMAND_RESUME = 2;
    private final int REQ_COMMAND_STOP = 3;

    @Override
    public int start() {
        int ret = 0;

        RLog.e("VideoStreamTrace RsScreenCaptureStream start() start");

        if (mStreamState == STREAM_STATE_RUNNING) return ret;

        if (mStreamState == this.STREAM_STATE_STOPING) {
            mReqCommand = REQ_COMMAND_START;
            return ret;
        }

        mReqCommand = REQ_COMMAND_NULL;

        mCodec = RsMediaCodec.createInstance(mContext, RsMediaCodec.CREATE_CODEC_VIDOE);

        if (mCodec == null) return -1;

        realCaptureSize = initResolutionInfo("480");

        mCodec.setCodecListener(new EncoderObserver());

        if ((-1) == prepareCaptureInput()) return -1;

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        captureThread = new Thread(captureRunnable, "recorder");
        captureThread.start();

        return ret;
    }

    @Override
    public void setFps(int fps) {

    }

    @Override
    public void startStreamReload() {
        // Do noting.
    }

    @Override
    public int resume() {
        int ret = 0;

        if (mStreamState == STREAM_STATE_RUNNING) return ret;

        if (mStreamState == STREAM_STATE_PAUSING) {
            mReqCommand = REQ_COMMAND_RESUME;
        }

        mStreamState = STREAM_STATE_RUNNING;
        mReqCommand = REQ_COMMAND_NULL;

        captureThread = new Thread(captureRunnable, "recorder");
        captureThread.start();

        return 0;
    }

    public int getColorFormatASHM() {
        return (RecordSet.RS_COLOR_FORMAT = RecordScreenHead.getRSColorFormat(mCodec.getColorFormat()));
    }

    class CaptureRunnable implements Runnable {
        public int resultCode = 0;

        @Override
        public void run() {
            initVariables();
            procCaptureInput();
        }
    }

    private int prepareCaptureInput() {
        int ret = -1;

        if (mFrameType == FRAMEDATA_FROM_ENGINE) {
//			if (!binder.isBinderAlive()) {
//				RLog.e("VideoStreamTrace RsScreenCaptureStream binder.isBinderAlive() error");
//				return ret;
//			}

            int colorFormat = -1;
            if ((colorFormat = getColorFormatASHM()) == -1) {
                RLog.e("VideoStreamTrace RsScreenCaptureStream getColorFormatASHM error");
                return ret;
            }

            ashmLength = screenCapture.create(realCaptureSize, colorFormat);
            if (!ashmLength) {
                RLog.e("VideoStreamTrace RsScreenCaptureStream screenCapture.create error");
                return ret;
            }

            mResolution = RecordScreenHead.setScreenSize(RecordScreenHead.get(screenCapture.getHead()), mResolution, RecordSet.ISGPU);
            mCodec.setConfigure(new RsMediaCodecVideo.Builder()
                    .setWIDTH(mResolution.screenSize.x)
                    .setHEIGHT(mResolution.screenSize.y)
                    .setFPS_RATE(getFPS())
//                    .BIT_RATE(2560 * 1000 * GlobalStatic.TEST_BITLATE[GlobalStatic.getBitrateTest(mContext)])
//                    .setBIT_RATE((int) ((mResolution.screenSize.x * mResolution.screenSize.y * 15) /** 0.4f*/))
                    .setBIT_RATE((getBitRate()))
                    .setCAPTURE_COLORFORMAT(colorFormat)
                    .setIFRAME_INTERVAL(getIFrame())
                    .setBYTEPERLINE(mResolution.bytePerLine), null);

            RLog.i("VideoStreamTrace RsScreenCaptureStream setConfigure");

            frameData = new byte[screenCapture.getBufferSize()];
        } else if (mFrameType == FRAMEDATA_FROM_FILE) {
//			mCodecVideo.configure(1280, 800, RecordScreenHead.RGB);
        }

        return 0;
    }

    private int getFPS() {
        if (EngineConfigSetting.isSamsungPrinter()) {
            if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
                return 20;
            }
        }
        return 5;
    }

    private int getIFrame() {
        if (EngineConfigSetting.isSamsungPrinter()) {
            if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
                return 1;
            }
        }
        return 50;
    }

    private int getBitRate() {
        if (EngineConfigSetting.isSoftEncoding) {
            if (EngineConfigSetting.isHCIBuild()) {
                return mResolution.screenSize.x * mResolution.screenSize.y * 8;
            } else if (EngineConfigSetting.isSamsungPrinter() && VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
                return mResolution.screenSize.x * mResolution.screenSize.y * 15;
            } else {
                return mResolution.screenSize.x * mResolution.screenSize.y * 4;
            }
        } else {
            return mResolution.screenSize.x * mResolution.screenSize.y * 15;
        }
    }

    private void procCaptureInput() {
        mStreamState = STREAM_STATE_RUNNING;

        while (!mIsStop && !mIsPause) {

            screenCapture.screenshot(frameData, frameData.length);

            if (frameData == null || mCodec == null) break;

            if (mIsClose) return;

            try {

                int ret = mCodec.queueInput(frameData, frameData.length, RsMediaCodec.CODEC_VIDEO);

                if (EngineConfigSetting.isSoftEncoding && Build.VERSION.SDK_INT < 10) {
                    Thread.sleep(INTERVAL_FRAME_SAMSUNGPRINTER_3TH);
                } else if (EngineConfigSetting.isHCIBuild()) {
                    Thread.sleep(INTERVAL_FRAME_HCI_TV);
                } else {
                    Thread.sleep(INTERVAL_FRAME);
                }

                switch (ret) {
                    case LiteCam.EXCEEDED_CAPACITY_SYSTEM:
                        Thread.currentThread().interrupt();
                        break;

                    case LiteCam.EXCEEDED_CAPACITY_FILE:
                        Thread.currentThread().interrupt();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }

        procPausing();
    }

    private void procPausing() {
        if (mStreamState != STREAM_STATE_PAUSING) return;

        if (mReqCommand == REQ_COMMAND_RESUME) {
            resume();
        } else {
            mStreamState = STREAM_STATE_PAUSED;
        }
    }

    private void procStoping() {
        if (mIsClose) {
            close();
            return;
        }

        if (mStreamState != STREAM_STATE_STOPING) return;

        if (mReqCommand == REQ_COMMAND_START) {
            start();
        }

        mStreamState = STREAM_STATE_READY;
    }

    private static int mFrameType = 0;

    private final int FRAMEDATA_FROM_ENGINE = 0;
    private final int FRAMEDATA_FROM_FILE = 1;

    //    private final int SCREENSHOT_BUFFERSIZE = 1280 * 800 * 4;
    private final int SCREENSHOT_BUFFERSIZE = 1920 * 1080 * 4;

    private RandomAccessFile mRandomFile;

    private byte[] getFrameData() {
        byte[] ret = null;
        if (mFrameType == FRAMEDATA_FROM_ENGINE) {
            ret = getFrameDataEngine();
        } else if (mFrameType == FRAMEDATA_FROM_FILE) {
            ret = getFrameDataFile();
        }
        return ret;
    }

    private byte[] getFrameDataEngine() {
        screenCapture.screenshot(frameData, frameData.length);
        return frameData;
    }

    private byte[] getFrameDataFile() {
        if (frameData == null || frameData.length != SCREENSHOT_BUFFERSIZE) {
            frameData = new byte[SCREENSHOT_BUFFERSIZE];
        }
        try {
            int read = mRandomFile.read(frameData, 0, SCREENSHOT_BUFFERSIZE);
            if (read <= 0) frameData = null;
            Thread.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return frameData;
    }

    private boolean mIsStop = false;
    private boolean mIsPause = false;
    private boolean mIsClose = false;

    ICodecListener mEncoderListener;

    public void setEncoderListener(ICodecListener encoderListener) {
        mEncoderListener = encoderListener;
    }

    int mRotate = 0;

    byte[] bytesTempBuffer = new byte[1024 * 1024];

    class EncoderObserver extends ICodecAdapter {

        @Override
        public void onVDeqeueFormatChanged(byte[] data) {
            mEncoderListener.onVDeqeueFormatChanged(data);
        }

        @Override
        public void onVDeqeueFormatChanged(byte[] data, int offset, int size) {
            mEncoderListener.onVDeqeueFormatChanged(data, offset, size);
        }

        @Override
        public void onVDeqeueFormatChanged(MediaFormat format) {
            mEncoderListener.onVDeqeueFormatChanged(format);
        }

        @Override
        public void onVDequeueOutput(byte[] data) {
            mEncoderListener.onVDeqeueFormatChanged(data);
        }


        @Override
        public void onVDequeueOutput(byte[] data, int offset, int size) {
            if (writeSampleData != null) {
//                ByteBuffer tempBuffer = null;
                try {
//                    tempBuffer = byteBuffer.duplicate();
//                    byte[] data = new byte[bufferInfo.size];
//                    tempBuffer.get(data);
                    try {
                        writeSampleData.write(data, 0, size);
                    } catch (IOException e) {

                    }
                } finally {
//                    if (tempBuffer != null) {
//                        tempBuffer.clear();
//                    }
//
                }
            }
            mEncoderListener.onVDequeueOutput(data, offset, size);

        }

        @Override
        public void onVDequeueOutput(ByteBuffer byteBuffer, BufferInfo bufferInfo) {
            if (writeSampleData != null) {
                ByteBuffer tempBuffer = null;
                try {
                    tempBuffer = byteBuffer.duplicate();
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
            mEncoderListener.onVDequeueOutput(byteBuffer, bufferInfo);
        }

        @Override
        public void onVideoHeader(byte[] data) {
            mEncoderListener.onVideoHeader(data);
        }

        @Override
        public void onVideoHeaderRec(byte[] data) {
            mEncoderListener.onVideoHeaderRec(data);
        }

        @Override
        public void onVStart() {
            mEncoderListener.onVStart();
        }

        @Override
        public void onVStop() {
            mEncoderListener.onVStop();
            procStoping();
        }

        @Override
        public void onVError() {
            mEncoderListener.onVError();
        }

    }

    public void pause() {
        if (mStreamState == STREAM_STATE_READY) return;

        mIsPause = true;

        mStreamState = STREAM_STATE_PAUSING;
    }

    @Override
    public void stop() {
        if (mStreamState == STREAM_STATE_READY) return;
        if (mStreamState == STREAM_STATE_STOPING) return;

        mStreamState = STREAM_STATE_STOPING;

        mIsStop = true;

        mCodec.stop();
    }

    @Override
    public void close() {
        mIsClose = true;

        if (mStreamState == STREAM_STATE_RUNNING) {
            stop();
        } else if (mStreamState == STREAM_STATE_STOPING) {
        } else {
            if (mCodec != null) {
                mCodec = null;
            }
        }
    }

    @Override
    public void changeRotation(Context context, int savedRotation, int rotation) {
    }
}
