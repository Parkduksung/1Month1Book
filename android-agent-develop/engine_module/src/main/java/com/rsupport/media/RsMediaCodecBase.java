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

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;

import com.rsupport.litecam.util.LLog;

public abstract class RsMediaCodecBase {

    public abstract void start();

    public abstract void stop();

    public abstract void resume();

    public abstract void pause();

    public abstract void close();

    public abstract void setConfigure(RsMediaCodecVideo.Builder vBuilder, RsMediaCodecAudio.Builder aBuilder);

    public abstract int queueInput(byte[] bytes, int len, int codeType);

    public abstract void setCodecListener(ICodecListener listener);

    public static final int TIMEOUT_USEC = 10000;

    public static final int CODEC_VIDEO = 0;
    public static final int CODEC_AUDIO = 1;

    public static final int CREATE_CODEC_VIDOE = 1;
    public static final int CREATE_CODEC_AUDIO = 2;
    public static final int CREATE_CODEC_ALL = 3;

    public long getVideoPresentationTimeUs(long startTime) {
        return (System.currentTimeMillis() - startTime) * 800;
    }

    public long getAudioPresentationTimeUs(long startTime) {
        return (System.currentTimeMillis() - startTime) * 1000;
    }

    public static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {
                continue;
            }

            for (String type : codecInfo.getSupportedTypes()) {
                if (type.equalsIgnoreCase(mimeType)) {
                    LLog.i(true, "SelectCodec : " + codecInfo.getName());
                    return codecInfo;
                }
            }
        }
        return null;
    }

}
