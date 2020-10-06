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
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

public class RsMediaCodec extends RsMediaCodecBase implements ICodecListener {

    private static RsMediaCodecVideo mCodecVideo;
    private static RsMediaCodecAudio mCodecAudio;

    private static RsMediaCodec mCodec;
    private static Context mContext;

    private ICodecListener mCodecListener;

    private RsMediaCodec() {
        init();
    }

    private void init() {
    }

    public static RsMediaCodec createInstance(Context context, int flag) {
        mContext = context;

        if (mCodec == null) {
            mCodec = new RsMediaCodec();
        }

        if ((flag & CREATE_CODEC_VIDOE) > 0) {
            if (mCodecVideo == null) {
                mCodecVideo = RsMediaCodecVideo.createInstance(mContext);
                mCodecVideo.setCodecListener(mCodec);
            }
        }

        if ((flag & CREATE_CODEC_AUDIO) > 0) {
            if (mCodecAudio == null) {
                mCodecAudio = RsMediaCodecAudio.createInstance(mContext);
            }
        }

        return mCodec;
    }

    public static RsMediaCodecVideo getInstanceVideo() {
        return mCodecVideo;
    }

    public static RsMediaCodecAudio getInstanceAudio() {
        return mCodecAudio;
    }

    @Override
    public void start() {
        if (mCodecVideo != null) {
            mCodecVideo.start();
        }
        if (mCodecAudio != null) {
            mCodecAudio.start();
        }
    }

    @Override
    public void stop() {
        if (mCodecVideo != null) {
            mCodecVideo.stop();
        }
        if (mCodecAudio != null) {
            mCodecAudio.stop();
        }
    }

    @Override
    public void resume() {
        if (mCodecVideo != null) {
            mCodecVideo.resume();
        }
        if (mCodecAudio != null) {
            mCodecAudio.resume();
        }
    }

    @Override
    public void pause() {
        if (mCodecVideo != null) {
            mCodecVideo.pause();
        }
        if (mCodecAudio != null) {
            mCodecAudio.pause();
        }
    }

    @Override
    public void close() {
        if (mCodecVideo != null) {
            mCodecVideo = null;
        }
        if (mCodecAudio != null) {
            mCodecAudio = null;
        }
    }


    @Override
    public void setConfigure(RsMediaCodecVideo.Builder videoBuilder, RsMediaCodecAudio.Builder audioBuilder) {
        if (videoBuilder != null && mCodecVideo != null) {
            mCodecVideo.setConfigure(videoBuilder, null);
        }
        if (audioBuilder != null && mCodecAudio != null) {
            mCodecAudio.setConfigure(null, audioBuilder);
        }
    }

    @Override
    public int queueInput(byte[] data, int len, int codecType) {
        if (codecType == CODEC_VIDEO) {
            if (mCodecVideo == null) return 0;
            mCodecVideo.queueInput(data, len, codecType);
        }
        if (codecType == CODEC_AUDIO) {
            if (mCodecAudio == null) return 0;
            mCodecAudio.queueInput(data, len, codecType);
        }
        return 0;
    }

    @Override
    public void setCodecListener(ICodecListener listener) {
        mCodecListener = listener;
        if (mCodecVideo != null) {
            mCodecVideo.setCodecListener(this);
        }
        if (mCodecAudio != null) {
            mCodecAudio.setCodecListener(this);
        }
    }

    @Override
    public void onVDeqeueFormatChanged(byte[] data) {
        if (mCodecListener == null) return;
        mCodecListener.onVDeqeueFormatChanged(data);
    }

    @Override
    public void onVDeqeueFormatChanged(byte[] data, int offset, int size) {
        if (mCodecListener == null) return;
        mCodecListener.onVDeqeueFormatChanged(data, offset, size);
    }

    @Override
    public void onVDeqeueFormatChanged(MediaFormat format) {
        if (mCodecListener == null) return;
        mCodecListener.onVDeqeueFormatChanged(format);
    }

    @Override
    public void onVDequeueOutput(byte[] data) {
        if (mCodecListener == null) return;
        mCodecListener.onVDequeueOutput(data);
    }

    @Override
    public void onVDequeueOutput(byte[] data, int offset, int size) {
        if (mCodecListener == null) return;
        mCodecListener.onVDequeueOutput(data, offset, size);
    }

    @Override
    public void onVDequeueOutput(ByteBuffer byteBuffer, BufferInfo bufferInfo) {
        if (mCodecListener == null) return;
        mCodecListener.onVDequeueOutput(byteBuffer, bufferInfo);
    }

    @Override
    public void onVStart() {
        if (mCodecListener == null) return;
        mCodecListener.onVStart();
    }

    @Override
    public void onVStop() {
        mCodecVideo = null;
        if (mCodecListener == null) return;
        mCodecListener.onVStop();
    }

    @Override
    public void onVError() {
        if (mCodecListener == null) return;
        mCodecListener.onVError();
    }

    @Override
    public void onADeqeueFormatChanged(MediaFormat format) {
        if (mCodecListener == null) return;
        mCodecListener.onADeqeueFormatChanged(format);
    }

    @Override
    public void onADequeueOutput(byte[] data) {
        if (mCodecListener == null) return;
        mCodecListener.onADequeueOutput(data);
    }

    @Override
    public void onADequeueOutput(ByteBuffer byteBuffer, BufferInfo bufferInfo) {
        if (mCodecListener == null) return;
        mCodecListener.onADequeueOutput(byteBuffer, bufferInfo);
    }

    @Override
    public void onVideoHeader(byte[] data) {
        if (mCodecListener == null) return;
        mCodecListener.onVideoHeader(data);
    }

    @Override
    public void onVideoHeaderRec(byte[] data) {
        if (mCodecListener == null) return;
        mCodecListener.onVideoHeaderRec(data);
    }

    @Override
    public void onAStart() {
        if (mCodecListener == null) return;
        mCodecListener.onAStart();
    }

    @Override
    public void onAStop() {
        if (mCodecListener == null) return;
        mCodecListener.onAStop();
    }

    @Override
    public void onAError() {
        if (mCodecListener == null) return;
        mCodecListener.onAError();
    }

    public int getColorFormat() {
        return mCodecVideo.getColorFormat();
    }

}
