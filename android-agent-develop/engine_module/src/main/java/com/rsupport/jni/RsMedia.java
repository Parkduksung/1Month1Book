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

package com.rsupport.jni;

import android.graphics.Bitmap;
import android.view.Surface;

public class RsMedia {

    public static final int DECTYPE_SYNC = 0;
    public static final int DECTYPE_QTHREAD = 1;

    public static final int BITMAP_VIEW = 0;
    public static final int SURFACE_VIEW = 1;

    static {
        System.loadLibrary("rsmediaex");
    }

    public native int initRsMedia(int type);

    public native void initSurface(Surface surface);

    public native void initSurfaceEx(Surface surface, int angle);

    public native int startDecoderByPath(String path);

    public native void stopCloseDecoderByPath();

    public native int initSDecoder(int vWidth, int vHeight);

    public int writeSDecoder(byte[] inbuf, int inbufSize) {
        return writeSDecoder(inbuf, inbufSize, DECTYPE_SYNC);
    }

    public native int writeSDecoder(byte[] inbuf, int inbufSize, int decType);

    public native int setFrameAngle(int angle);

    public native int getFrameRGB(byte[] outputBytes, int len);

    public native int startRender();

    public native int stopRender();

    public native int closeSDecoder();

    public native int surfaceChanged(int width, int height);

    public native static int encodeYUV420SP(byte[] yuv420sp, byte[] argb, int width, int height);

    public native static int encodeYUV420SPReverse(byte[] yuv420sp, byte[] argb, int width, int height);

    public native int initSEncoder();

    public native int writeSEncoder(byte[] inbuf, int inbufSize, byte[] outbuf);

    public native int closeSEncoder();

    public native int initXEncoder(byte[] outbuf, int width, int height, int bitrate, int fps);

    public native int writeXEncoder(byte[] inbuf, int inbufSize, byte[] outbuf);

    public native int closeXEncoder();

    public native int getFrameBitmap(Bitmap bitmap);

    public native int getVideoWidth();

    public native int getVideoHeight();

}
