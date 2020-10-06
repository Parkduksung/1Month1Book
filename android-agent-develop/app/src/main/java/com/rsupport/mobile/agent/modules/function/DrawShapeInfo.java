package com.rsupport.mobile.agent.modules.function;

import android.graphics.Paint;

public class DrawShapeInfo {

    public short x = 0;
    public short y = 0;
    public short x2 = 0;
    public short y2 = 0;
    public Paint paint;

    public DrawShapeInfo(short x, short y, short x2, short y2, Paint paint) {
        this.x = x;
        this.y = y;
        this.x2 = x2;
        this.y2 = y2;
        this.paint = paint;
    }

}
