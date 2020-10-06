package com.rsupport.media.mediaprojection.record.surface;

import java.nio.ByteBuffer;

public interface OnSurfaceDrawable {

    /**
     * Surface initialized
     */
    void initialized();

    /**
     * onDrawable
     */
    void onDrawable(ByteBuffer imageBuffer, int width, int height, int pixelStride, int rowStride, int rowPadding, long presentationTime);
}
