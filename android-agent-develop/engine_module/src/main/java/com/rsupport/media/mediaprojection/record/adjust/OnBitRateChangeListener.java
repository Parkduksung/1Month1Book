package com.rsupport.media.mediaprojection.record.adjust;

import android.annotation.TargetApi;
import android.os.Build;

/**
 * Created by taehwan on 5/19/15.
 * <p>
 * Bitrate 컨트롤 callback
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public interface OnBitRateChangeListener {

    void onUpperEvent();

    void onLowerEvent();
}
