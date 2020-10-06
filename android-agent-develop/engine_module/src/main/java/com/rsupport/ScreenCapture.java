package com.rsupport;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.Surface;

/**
 * Created by hyun on 2016. 12. 2..
 */

public class ScreenCapture {


    protected Bitmap imgRotate(Bitmap bmp, int rotation) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();


        Matrix matrix = new Matrix();
        if (rotation == Surface.ROTATION_90) {
            matrix.postRotate(270);
        } else if (rotation == Surface.ROTATION_270) {
            matrix.postRotate(90);
        }

        Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
        bmp.recycle();

        return resizedBitmap;
    }
}
