package com.rsupport.mobile.agent.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public class DispUtil {

    public static Point getScreenPixel(Context cxt) {
        Display display = ((WindowManager) cxt.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        try {
            display.getRealSize(size);
        } catch (NoSuchMethodError e) {
            size.x = display.getWidth();
            size.y = display.getHeight();
        }

        return size;
    }

    public static int getResolution(Context cxt) {
        Display display = ((WindowManager) cxt.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        return display.getRotation();
    }
}
