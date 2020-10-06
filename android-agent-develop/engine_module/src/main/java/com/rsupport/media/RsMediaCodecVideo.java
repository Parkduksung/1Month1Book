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

package com.rsupport.media;

import android.content.Context;
import android.graphics.Point;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import com.rsupport.litecam.ScreenInfo;
import com.rsupport.litecam.ScreenInfo.CaptureFrame;
import com.rsupport.litecam.ScreenInfo.ResolutionInfo;
import com.rsupport.litecam.record.RecordManager;
import com.rsupport.litecam.record.RecordSet;
import com.rsupport.litecam.util.RecordScreenHead;
import com.rsupport.media.header.XVIDEOHEADER;
import com.rsupport.media.header.XVIDEOHEADERREC64;
import com.rsupport.util.Screen;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import config.EngineConfigSetting;

//@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
public class RsMediaCodecVideo extends RsMediaCodecBase implements ICodecListener {

    /*
     * Video Code Option Info
     */
    public Builder mBuilder;

    private static final int INFO_TRY_AGAIN_LATER = -1;

    private static final int INPUT_BUFFER_COUNT = 7;

    // Encoder (OMX, SoftEncoder)
    private static IRsVEncoder mIVideoEncoder;

    // Codec Listener
    private ICodecListener mCodecListener;

    // Thread for offer input video stream
    private Thread mVideoOfferThread;

    // Thread locker
    private Semaphore mSemaphore;

    // Queue input data
    private Queue<ScreenInfo.CaptureFrame> mCaptureQueue;

    // Queue input data schema
    private ScreenInfo.CaptureFrame mQVideoData;

    private ByteBuffer[] mVideoInputBuffers;

    private ResolutionInfo mResolutionInfo;

    private static RsMediaCodecVideo mRsCodec;

    private static Context mContext;

    private ByteBuffer[] mInputBuffers;

    private int mInputBufferIndex;

    private boolean eosReceived;

    private boolean mIsFirstInputBuffer = true;

    private long mStartEncodeTime = 0;

    private int mRotation;


    /**
     * @author sypark
     * <p>
     * set video encoder option
     */
    public static class Builder {

        public int VIDEOWIDTH = 0;
        public int VIDEOHEIGHT = 0;
        public int BIT_RATE = RecordSet.DEFAULT_AUDIO_BIT_RATE;
        public int FPS_RATE = RecordSet.DEFAULT_AUDIO_BIT_RATE;
        public int IFRAME_INTERVAL = RecordSet.IFRAME_INTERVAL;
        public int CAPTURE_COLORFORMAT = RecordScreenHead.RGB;
        public int BYTESPERLINE = 0;

        public ResolutionInfo RESOLUTIONINFO = null;

        public Builder setWIDTH(int width) {
            VIDEOWIDTH = width;
            return this;
        }

        public Builder setHEIGHT(int height) {
            VIDEOHEIGHT = height;
            return this;
        }

        public Builder setBIT_RATE(int bit_rate) {
            BIT_RATE = bit_rate;
            return this;
        }

        public Builder setFPS_RATE(int fps_rate) {
            FPS_RATE = fps_rate;
            return this;
        }

        public Builder setIFRAME_INTERVAL(int iframe_interval) {
            IFRAME_INTERVAL = iframe_interval;
            return this;
        }

        public Builder setCAPTURE_COLORFORMAT(int colorformat) {
            CAPTURE_COLORFORMAT = colorformat;
            return this;
        }

        public Builder setBYTEPERLINE(int bytesPerLine) {
            BYTESPERLINE = bytesPerLine;
            return this;
        }

        public Builder setRESOLUTIONINFO(ResolutionInfo resolutionInfo) {
            RESOLUTIONINFO = resolutionInfo;
            return this;
        }
    }

    private RsMediaCodecVideo() {
        initialize();
    }

    /**
     * initialize
     */
    public void initialize() {
        mSemaphore = new Semaphore(0);
        mCaptureQueue = new LinkedList<ScreenInfo.CaptureFrame>();
        eosReceived = false;
    }

    /**
     * Create Instance
     *
     * @param context
     * @return
     */
    public static RsMediaCodecVideo createInstance(Context context) {
        mContext = context;
        return createInstance();
    }

    /**
     * Create Instance
     *
     * @return
     */
    public static RsMediaCodecVideo createInstance() {
        if (mRsCodec == null) {
            mRsCodec = new RsMediaCodecVideo();
        }

        mIVideoEncoder = __getEncoderInstance();

        return mRsCodec;
    }

    /**
     * Start video encoder
     */
    @Override
    public void start() {
//		if (mIVideoEncoder == null) return;
//		
//		mIVideoEncoder.start();
//		
//		mVideoOfferThread = new Thread(offerVideoEncoderRunnable, "Video offer thread");
//		mVideoOfferThread.start();
    }

    /**
     * Resume video encoder
     */
    @Override
    public void resume() {
    }

    /**
     * Pause video encoder
     */
    @Override
    public void pause() {
    }

    /**
     * Stop video encoder
     */
    @Override
    public void stop() {
        eosReceived = true;

        if (mIVideoEncoder != null) {
            mIVideoEncoder.stop();
        }

        if (mSemaphore != null) {
            mSemaphore.release();
//			mSemaphore = null;
        }
    }

    /**
     * Close video encoder
     */
    @Override
    public void close() {
    }

    /**
     * getColorForamt
     *
     * @return
     */
    public int getColorFormat() {
        return mIVideoEncoder.getColorFormat();
    }

    /**
     * Queueing input data
     */
    @Override
    public int queueInput(byte[] inData, int len, int codecType) {
        if (mInputBuffers == null) {
            mInputBuffers = getInputBuffers(inData.length);
        }

        mInputBufferIndex = dequeueInputBuffer(-1);

        if (mInputBufferIndex == RecordManager.INFO_TRY_AGAIN_LATER) {
            return INFO_TRY_AGAIN_LATER;
        }

        ByteBuffer inputBuffer = mInputBuffers[mInputBufferIndex];
        inputBuffer.clear();
        inputBuffer.put(inData);
        inputBuffer.flip();

        queueInputBuffer(mInputBufferIndex);

        return 0;
    }

    @Override
    public void setCodecListener(ICodecListener listener) {
        mCodecListener = listener;
        mIVideoEncoder.setCodecListener(this);
    }

    @Override
    public void setConfigure(RsMediaCodecVideo.Builder vBuilder, RsMediaCodecAudio.Builder aBuilder) {
        if (mCaptureQueue == null) initialize();

        mBuilder = vBuilder;

        mResolutionInfo = new ResolutionInfo();
        mResolutionInfo.dstScreenSize = new Point(mBuilder.VIDEOWIDTH, mBuilder.VIDEOHEIGHT);
        mResolutionInfo.screenSize = new Point(mBuilder.VIDEOWIDTH, mBuilder.VIDEOHEIGHT);
        mResolutionInfo.alignedScreenSize = new Point(mBuilder.VIDEOWIDTH, mBuilder.VIDEOHEIGHT);
        mResolutionInfo.bytePerLine = mBuilder.BYTESPERLINE;

        mBuilder.RESOLUTIONINFO = mResolutionInfo;

        mIVideoEncoder.configure(mBuilder);

        onVideoHeaderRec();
        onVideoHeader();
    }

    public Object getOutputFormat() {
        return mIVideoEncoder.getOutputFormat();
    }

    public boolean isRunEncoder() {
        boolean ret = false;

        if (mVideoOfferThread != null) {
            ret = true;
        }

        return ret;
    }



    /*
     *
     *
     * From below is class inner support functions.
     *
     *
     *
     */


    private int getCurrentRotation() {
        int ret = 0;
        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        ret = display.getRotation() * 90;
        return ret;
    }

    private Point getRealSize() {
        Point point = new Point();
        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(point);
        } else if (Build.VERSION.SDK_INT >= 13) {
            display.getSize(point);
        } else {
            point.x = display.getWidth();
            point.y = display.getHeight();
        }

        return point;
    }


    private void onVideoHeader() {
        if (mCodecListener == null) return;

        XVIDEOHEADER vHeader = new XVIDEOHEADER();

        int fWidth = getRealSize().x;
        int fHeight = getRealSize().y;

        mRotation = getCurrentRotation();

        if (mRotation == 1 || mRotation == 3) {
            fWidth = getRealSize().y;
            fHeight = getRealSize().x;
        }

        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        vHeader.isLandscape = Screen.isLandscape(getRealSize(), display) ? 1 : 0;
        vHeader.frameWidth = fWidth;
        vHeader.frameHeight = fHeight;
        vHeader.framepersecond = mBuilder.FPS_RATE;
        vHeader.rotation = mRotation;
        vHeader.videoWidth = mBuilder.VIDEOWIDTH;
        vHeader.videoHeight = mBuilder.VIDEOHEIGHT;

        byte[] bytes = new byte[vHeader.size()];
        vHeader.push(bytes, 0);

        mCodecListener.onVideoHeader(bytes);
    }

    private void onVideoHeaderRec() {
        if (mCodecListener == null) return;

        XVIDEOHEADERREC64 vHeader = new XVIDEOHEADERREC64();

        vHeader.framepersecond = mBuilder.FPS_RATE;
        vHeader.videoWidth = mBuilder.VIDEOWIDTH;
        vHeader.videoHeight = mBuilder.VIDEOHEIGHT;

        byte[] bytes = new byte[vHeader.size()];
        vHeader.push(bytes, 0);

        mCodecListener.onVideoHeaderRec(bytes);
    }

    private static IRsVEncoder __getEncoderInstance() {
        IRsVEncoder encoder = null;
        int apilevel = Build.VERSION.SDK_INT;
        // 4.1.2 colorformat issue and 4.1.2 to soft encoder
        if (apilevel > 16 && !EngineConfigSetting.isSoftEncoding) {
            encoder = RsMediaEncoderVOMX.createInstanceStatic();
        } else {
            encoder = RsMediaEncoderVSoft.createInstanceStatic(mContext);
        }

        return encoder;
    }

    private void clearVariables() {
        if (mRsCodec != null) {
            mRsCodec = null;
        }

//		if (mVideoOfferThread != null) {
//			mVideoOfferThread.interrupt();
//			mVideoOfferThread = null;
//		}

        if (mResolutionInfo != null) {
            mResolutionInfo = null;
        }

        if (mVideoInputBuffers != null) {
            mVideoInputBuffers = null;
        }

        if (mCaptureQueue != null) {
            mCaptureQueue.clear();
            mCaptureQueue = null;
        }
    }

    private void procStoping() {
        initVariables();
        mRsCodec = null;
        eosReceived = true;
    }

    private void initVariables() {
        eosReceived = false;
        mIsFirstInputBuffer = true;
        mStartEncodeTime = 0;
    }

    private Runnable offerVideoEncoderRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                offerVideoEncoder();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    int captureImgLength;

    private void offerVideoEncoder() throws InterruptedException {
        if (mBuilder.CAPTURE_COLORFORMAT == RecordScreenHead.RGB) {
            captureImgLength = mBuilder.VIDEOWIDTH * mBuilder.VIDEOHEIGHT * 4;
        } else {
            captureImgLength = mBuilder.VIDEOWIDTH * mBuilder.VIDEOHEIGHT * 3 / 2;
        }

        while (!eosReceived) {
            procInputVideo();

            if (mRotation != getCurrentRotation()) {
                mRotation = getCurrentRotation();
                onVideoHeader();
            }

        }
    }

    private ScreenInfo.CaptureFrame getVideoData() {
        synchronized (mCaptureQueue) {
            if (!mCaptureQueue.isEmpty())
                return mCaptureQueue.poll();
            return null;
        }
    }

    private ByteBuffer[] getInputBuffers(int size) {
        if (size == -1)
            size = mResolutionInfo.screenSize.x * mResolutionInfo.screenSize.y * 4;

        mVideoInputBuffers = new ByteBuffer[INPUT_BUFFER_COUNT];

        for (int i = 0; i < INPUT_BUFFER_COUNT; ++i) {
            try {
                if (EngineConfigSetting.isSoftEncoding) {
                    //삼성 프린터 3세대 에서 allocateDirect 작동안함
                    mVideoInputBuffers[i] = ByteBuffer.allocate(size);
                } else {
                    mVideoInputBuffers[i] = ByteBuffer.allocateDirect(size);
                }

            } catch (java.lang.IllegalArgumentException e) {
                mVideoInputBuffers[i] = ByteBuffer.allocate(size);
            }
        }

        return mVideoInputBuffers;
    }

    private int dequeueInputBuffer(int index) {
        if (index == -1)
            index = 0;
        while (index < INPUT_BUFFER_COUNT) {
            if (mVideoInputBuffers[index].hasRemaining()) {
                return index;
            }
            ++index;
        }
        return -1;
    }

    private void queueInputBuffer(int index) {
        if (eosReceived) return;

        if (mStartEncodeTime == 0) {
            mStartEncodeTime = System.currentTimeMillis();
            startEncoder();
        }

        synchronized (mCaptureQueue) {
            if (mVideoInputBuffers[index].hasRemaining()) {
                mVideoInputBuffers[index].flip();
            }
            mCaptureQueue.offer(new CaptureFrame(index, getVideoPresentationTimeUs(mStartEncodeTime)));
        }

        mSemaphore.release();
    }

    private void startEncoder() {
        mIVideoEncoder.start();

        mVideoOfferThread = new Thread(offerVideoEncoderRunnable, "Screen Capture offer thread");
        mVideoOfferThread.start();
    }

    private boolean procInputVideo() throws InterruptedException {
        boolean ret = false;

        while (true) {
            mSemaphore.acquire();

            if (eosReceived) return true;

            mQVideoData = getVideoData();
            if (mQVideoData != null) {
                synchronized (mVideoInputBuffers) {
                    byte[] data = mVideoInputBuffers[mQVideoData.inputBufferIndex].array();
                    mIVideoEncoder.queueInput(data, 0, captureImgLength, false);
                }
                mVideoInputBuffers[mQVideoData.inputBufferIndex].clear();
                break;
            }
        }

        return ret;
    }

    @Override
    public void onVDeqeueFormatChanged(MediaFormat format) {
        mCodecListener.onVDeqeueFormatChanged(format);
    }

    @Override
    public void onVDeqeueFormatChanged(byte[] data) {
        mCodecListener.onVDeqeueFormatChanged(data);
    }

    @Override
    public void onVDeqeueFormatChanged(byte[] data, int offset, int size) {
        mCodecListener.onVDeqeueFormatChanged(data, offset, size);
    }

    @Override
    public void onVDequeueOutput(byte[] data) {
        mCodecListener.onVDequeueOutput(data);
    }

    @Override
    public void onVDequeueOutput(byte[] data, int offset, int size) {
        mCodecListener.onVDequeueOutput(data, offset, size);
    }

    @Override
    public void onVDequeueOutput(ByteBuffer byteBuffer, BufferInfo bufferInfo) {
        mCodecListener.onVDequeueOutput(byteBuffer, bufferInfo);
    }

    @Override
    public void onVideoHeader(byte[] data) {
    }

    @Override
    public void onVideoHeaderRec(byte[] data) {
    }

    @Override
    public void onVStart() {
        mCodecListener.onVStart();
    }

    @Override
    public void onVStop() {
        mCodecListener.onVStop();
        procStoping();
    }

    @Override
    public void onVError() {
        mCodecListener.onVError();
    }

    @Override
    public void onADeqeueFormatChanged(MediaFormat format) {
        mCodecListener.onADeqeueFormatChanged(format);
    }

    @Override
    public void onADequeueOutput(byte[] data) {
        mCodecListener.onADequeueOutput(data);
    }

    @Override
    public void onADequeueOutput(ByteBuffer byteBuffer, BufferInfo bufferInfo) {
        mCodecListener.onADequeueOutput(byteBuffer, bufferInfo);
    }

    @Override
    public void onAStart() {
        mCodecListener.onAStart();
    }

    @Override
    public void onAStop() {
        mCodecListener.onAStop();
    }

    @Override
    public void onAError() {
        mCodecListener.onAError();
    }

}
