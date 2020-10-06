package com.rsupport.util;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.rsupport.util.log.RLog;

public class Screen {

    public static Point resolution(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();

        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        int rotation = d.getRotation();
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;

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

    public static boolean isLandscape(Point displaySize, Display display) {
        int rotation = display.getRotation();
        RLog.i("rotation : " + rotation + " X::::: " + displaySize.x + " Y ::::" + displaySize.y);
        switch (rotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                if (displaySize.x > displaySize.y) { // Rotation이 0, 180 인데 실제 가로가 더 넓은 경우
                    return true;
                }
                break;

            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                if (displaySize.x < displaySize.y) { // Rotation이 90, 270 인데 가로디폴트 단말
                    return true;
                }

                break;

        }
        return false;
    }

    public static boolean isScreenOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn;

        if (Build.VERSION.SDK_INT >= 20) {
            isScreenOn = pm.isInteractive();
        } else {
            isScreenOn = pm.isScreenOn();
        }

        return isScreenOn;
    }

}
