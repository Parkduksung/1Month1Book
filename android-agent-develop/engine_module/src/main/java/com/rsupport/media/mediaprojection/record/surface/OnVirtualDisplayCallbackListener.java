package com.rsupport.media.mediaprojection.record.surface;

/**
 * Created by taehwan on 5/13/15.
 */
public interface OnVirtualDisplayCallbackListener {

    /**
     * Start Callback
     *
     * @return
     */
    boolean startCallback();

    /**
     * Record stop callback
     */
    void stopCallback();
}
