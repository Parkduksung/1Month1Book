package com.rsupport.srn30.screen.encoder.surface;

import java.nio.ByteBuffer;

public interface OnSurfaceDrawable {
	public void initialized();
	public void release();
	public void onDrawable(ByteBuffer imageBuffer, int width, int height, int pixelStride, int rowStride, int rowPadding);
}
