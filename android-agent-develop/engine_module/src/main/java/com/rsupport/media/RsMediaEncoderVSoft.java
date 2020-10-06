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

import com.rsupport.jni.RsMedia;
import com.rsupport.litecam.util.RecordScreenHead;
import com.rsupport.media.RsMediaCodecVideo.Builder;
import com.rsupport.util.log.RLog;

public class RsMediaEncoderVSoft implements IRsVEncoder {

    ICodecListener mCodecListener;

    static RsMediaEncoderVSoft mEncoderVSoft;

    int mColorFormat;

    private Builder mBuilder;

    private static RsMedia mRsMedia;

    private long mFrameIndex = 0;

    boolean mIsFormatChanged = false;

    private byte[] mYUVData;

    private int mYuvDataLen = 0;

    boolean eosReceived = true;

    private byte[] mOutputBuf = new byte[1024 * 1000];
    private byte[] mSPPSBuf = new byte[1024];

    private int mSPPSBufLength;

    private static Context mContext;

    private RsMediaEncoderVSoft() {
        initVariables();
    }

    public static IRsVEncoder createInstanceStatic(Context context) {
        mContext = context;
        return createInstanceStatic();
    }

    public static IRsVEncoder createInstanceStatic() {
        if (mEncoderVSoft == null) {
            mEncoderVSoft = new RsMediaEncoderVSoft();
        }

        mRsMedia = new RsMedia();

        return mEncoderVSoft;
    }

    @Override
    public IRsVEncoder createInstance() {
        if (mEncoderVSoft == null) {
            mEncoderVSoft = new RsMediaEncoderVSoft();
        }

        mRsMedia = new RsMedia();

        return mEncoderVSoft;
    }

    @Override
    public void start() {
        if (mRsMedia == null) return;

        mRsMedia.initRsMedia(RsMedia.DECTYPE_SYNC);
        mSPPSBufLength = mRsMedia.initXEncoder(mSPPSBuf, mBuilder.VIDEOWIDTH, mBuilder.VIDEOHEIGHT, mBuilder.BIT_RATE, mBuilder.FPS_RATE);

        mCodecListener.onVDeqeueFormatChanged(mSPPSBuf, 0, mSPPSBufLength);
    }

    @Override
    public void stop() {
        eosReceived = true;

        RLog.i("Stop :::closeXEncoder");
//		mRsMedia.closeXEncoder();
        mRsMedia = null;

        mCodecListener.onVStop();
    }

    @Override
    public boolean configure(Builder builder) {
        initVariables();

        mBuilder = builder;

        mYuvDataLen = mBuilder.VIDEOWIDTH * mBuilder.VIDEOHEIGHT * 3 / 2;

        if (mBuilder.CAPTURE_COLORFORMAT == RecordScreenHead.RGB) {
            mYUVData = new byte[mYuvDataLen];
        }

        mCodecListener.onVStart();

        return false;
    }

    @Override
    public void setCodecListener(ICodecListener codecListener) {
        mCodecListener = codecListener;
    }

    @Override
    public void queueInput(byte[] data, int offset, int size, boolean finish) {
        if (eosReceived) return;

        int len = 0;

        if (mBuilder.CAPTURE_COLORFORMAT == RecordScreenHead.RGB) {
//			RsMedia.encodeYUV420SPReverse(mYUVData, data, mBuilder.RESOLUTIONINFO.screenSize.x, mBuilder.RESOLUTIONINFO.screenSize.y);
            RsMedia.encodeYUV420SP(mYUVData, data, mBuilder.RESOLUTIONINFO.screenSize.x, mBuilder.RESOLUTIONINFO.screenSize.y);
            len = mRsMedia.writeXEncoder(mYUVData, mYuvDataLen, mOutputBuf);
        } else {
            len = mRsMedia.writeXEncoder(data, mYuvDataLen, mOutputBuf);
        }

        if (mSPPSBufLength > 0) {

            mCodecListener.onVDequeueOutput(mOutputBuf, mSPPSBufLength, (len - mSPPSBufLength));
            mSPPSBufLength = 0;

        } else {

            mCodecListener.onVDequeueOutput(mOutputBuf, 0, len);

        }
    }

    @Override
    public int getColorFormat() {
        return 21; // NV12
    }

    @Override
    public Object getOutputFormat() {
        return null;
    }


    private void initVariables() {
        mFrameIndex = 0;
        mIsFormatChanged = false;
        eosReceived = false;
    }

}
