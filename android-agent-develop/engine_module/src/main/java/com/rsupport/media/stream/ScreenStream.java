package com.rsupport.media.stream;

import android.content.Context;

import com.rsupport.media.ICodecListener;

public interface ScreenStream {

    void setEncoderListener(ICodecListener encoderListener);

    int start();

    void setFps(int fps);

    /**
     * VD(MediaProjection) 사용시 화면 회전에 따른 reload 사용.
     */
    void startStreamReload();

    void stop();

    int resume();

    void pause();

    void close();

    void changeRotation(Context context, int savedRotation, int rotation);
}
