package com.rsupport.util;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.rsupport.util.rslog.MLog;

public class Screen {
	
	static public Rect screenRect;
	
	static public Rect getScreenRect(Context cxt, int hwRotation) {
		if(screenRect == null){
			Point size = Screen.resolution(cxt);
			if ((hwRotation %2) == 0)
				screenRect = new Rect(0,0,size.x,size.y);
			else
				screenRect = new Rect(0,0,size.y,size.x);
		}
		return screenRect;
	}

	public static Point resolution(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display d = wm.getDefaultDisplay();

		DisplayMetrics metrics = new DisplayMetrics();
		d.getMetrics(metrics);

		int rotation = d.getRotation();
		int widthPixels = metrics.widthPixels;
		int heightPixels = metrics.heightPixels;

		// 두개화면
		if (Build.PRODUCT.toLowerCase().startsWith("n-05e")) {
			widthPixels = 540;
			heightPixels = 960;
			return new Point(widthPixels, heightPixels);
		}
		
		if (14 <= Build.VERSION.SDK_INT && Build.VERSION.SDK_INT < 17)
		try {
		    widthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(d);
		    heightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
		} catch (Exception ignored) {
		}
		if (Build.VERSION.SDK_INT >= 17)
		try {
		    Point realSize = new Point();
		    Display.class.getMethod("getRealSize", Point.class).invoke(d, realSize);
		    widthPixels = realSize.x;
		    heightPixels = realSize.y;
		} catch (Exception ignored) {
		}

		if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270)
			return new Point(heightPixels, widthPixels);

		return new Point(widthPixels, heightPixels);
	}

	/**
	 * vd용 display 사이즈를 구한다.
	 * @param context android context
	 * @return 설정된 display size
	 * @since 1.0.0.0
	 */
	public static Point virtualDisplayResolution(Context context) {
		return virtualDisplayResolution(context,Integer.MAX_VALUE);
	}

	/**
	 * vd용 제한된 display 사이즈를 구한다.
	 * @param context android context
	 * @param maxResolution 최대 size
	 * @return 설정된 display size
	 * @since 1.0.0.0
	 */
	public static Point virtualDisplayResolution(Context context, int maxResolution) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display d = wm.getDefaultDisplay();

		DisplayMetrics metrics = new DisplayMetrics();
		d.getMetrics(metrics);

		int widthPixels = metrics.widthPixels;
		int heightPixels = metrics.heightPixels;

		if (14 <= Build.VERSION.SDK_INT && Build.VERSION.SDK_INT < 17) {
			try {
				widthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(d);
				heightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
			} catch (Exception ignored) {
			}
		} else if (Build.VERSION.SDK_INT >= 17){
			try {
				Point realSize = new Point();
				Display.class.getMethod("getRealSize", Point.class).invoke(d, realSize);
				widthPixels = realSize.x;
				heightPixels = realSize.y;
			} catch (Exception ignored) {
			}
		}
		MLog.i("widthPixels.%d, heightPixels.%d", widthPixels, heightPixels);
		int minPixels = Math.min(widthPixels, heightPixels);
		// 작은 사이즈가 maxResolution 보다 크면 제한 한다.
		if(minPixels > maxResolution){
			if(d.getRotation() == Surface.ROTATION_90 || d.getRotation() == Surface.ROTATION_270){
				float ratio = (float)widthPixels/(float)heightPixels;
				heightPixels = maxResolution;
				widthPixels = (int)(maxResolution * ratio);
			}
			else{
				float ratio = (float)heightPixels/(float)widthPixels ;
				widthPixels = maxResolution;
				heightPixels = (int)(maxResolution * ratio);
			}
		}
		return new Point(widthPixels, heightPixels);
	}
}
