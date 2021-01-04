package com.rsupport.srn30.screen.encoder;

import java.nio.ByteBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.Surface;

import com.rsupport.srn30.screen.encoder.gl.GLSurfaceDrawable;
import com.rsupport.srn30.screen.encoder.surface.OnSurfaceDrawable;
import com.rsupport.srn30.screen.encoder.surface.SurfaceReader;
import com.rsupport.util.rslog.MLog;

public class VirtualDisplayHelper {
	public static String VIRTUAL_DISPLAY_NAME = "mobizenDisplay";
	
	private Context context = null;
	private SurfaceReader surfaceReader = null;

	public VirtualDisplayHelper(Context context) {
		this.context = context;
	}

	public void onDestroy() {
		MLog.i("onDestroy");
		release();
		context = null;
	}
	
	public void release(){
		MLog.i("release");
		if(surfaceReader != null){
			surfaceReader.onDestroy();
			surfaceReader = null;
		}
	}

	public void initialized(Surface encoderInputSurface, int width, int height, int colorFormat) {
		surfaceReader = new SurfaceReader(context);
		surfaceReader.setSurfaceDrawable(new GLSurfaceDrawable(encoderInputSurface, width, height));
		surfaceReader.createInputSurface(width, height, colorFormat);
	}

	public Surface getImageReaderSurface() {
		return surfaceReader.getSurface();
	}
	
	class BitmapDrawable implements OnSurfaceDrawable{
		private Bitmap bitmap = null;
		private Surface encoderInputSurface = null;
		private Rect inOutRect = null;

		public BitmapDrawable(Surface encoderInputSurface) {
			this.encoderInputSurface = encoderInputSurface;
		}

		@Override
		public void initialized() {
			inOutRect = new Rect();
		}

		@Override
		public void onDrawable(ByteBuffer imageBuffer, int width, int height,
				int pixelStride, int rowStride, int rowPadding) {
			if(bitmap == null){
				bitmap = Bitmap.createBitmap(rowStride/pixelStride, height, Config.ARGB_8888);
			}

			if(encoderInputSurface.isValid() == true){
				Canvas canvas = encoderInputSurface.lockCanvas(inOutRect);
				bitmap.copyPixelsFromBuffer(imageBuffer);
				canvas.drawBitmap(bitmap, 0, 0, null);
				encoderInputSurface.unlockCanvasAndPost(canvas);
			}
		}

		@Override
		public void release() {
			bitmap.recycle();
			bitmap = null;
			encoderInputSurface = null;
			inOutRect = null;
		}

	}
}
