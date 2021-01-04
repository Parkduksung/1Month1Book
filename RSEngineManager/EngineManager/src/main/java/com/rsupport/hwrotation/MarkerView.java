package com.rsupport.hwrotation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

@SuppressLint("InlinedApi")
public class MarkerView extends View {
	private Bitmap mMarkerBmp;

	public MarkerView(Context context) {
		super(context);
		setFocusable(false);
		
		int w = 3, h = 1;

		mMarkerBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		for(int y=0; y < h; ++y)
			for(int x=0; x < w; ++x)
				mMarkerBmp.setPixel(x, y, Color.CYAN);
		int[] colors = {0xff000011, 0xff001100, 0xff110000}; // almost black.
		mMarkerBmp.setPixel(0, 0, colors[0]);
		mMarkerBmp.setPixel(1, 0, colors[1]);
		mMarkerBmp.setPixel(2, 0, colors[2]);
		setTopmost(context, w, h);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawBitmap(mMarkerBmp, 0, 0, null);
	}
    public void remove() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.removeView(this);
    }
    
    @SuppressLint("InlinedApi")
	private void setTopmost(Context cxt, int w, int h) {
		if (android.os.Build.VERSION.SDK_INT >= 16)
			this.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE 
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_FULLSCREEN);
		
    	WindowManager.LayoutParams params = new WindowManager.LayoutParams(
    			w,h,0,0,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT);
    	params.gravity = Gravity.LEFT | Gravity.TOP;
        
        WindowManager wm = (WindowManager) cxt.getSystemService(Context.WINDOW_SERVICE);
        wm.addView(this, params);
    }
    
    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }
}

