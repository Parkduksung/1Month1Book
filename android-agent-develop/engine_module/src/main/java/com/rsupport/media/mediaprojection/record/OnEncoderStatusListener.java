package com.rsupport.media.mediaprojection.record;

import androidx.annotation.IntDef;

/**
 * Created by taehwan on 5/13/15.
 */
public interface OnEncoderStatusListener {

    public static final int TYPE_ERROR = 1000;

    @IntDef({TYPE_ERROR})
    public @interface StatusType {
    }

    public void status(@StatusType int type);

    /**
     * Start Callback
     *
     * @return
     */
    public boolean startCallback();
}
