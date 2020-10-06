package com.rsupport.mobile.agent.modules.function;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.rsupport.mobile.agent.utils.ComputeCoordinate;
import com.rsupport.mobile.agent.utils.NavigationBar;
import com.rsupport.mobile.agent.utils.WindowDisplay;
import com.rsupport.mobile.agent.utils.Converter;
import com.rsupport.mobile.agent.utils.Utility;
import com.rsupport.util.log.RLog;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;


public class ScreenDraw {
    private final String className = "ScreenDraw";

    public static final int rcpDrawFreeLine = 0;
    public static final int rcpDrawLine = 1;
    public static final int rcpDrawRectangle = 2;
    public static final int rcpDrawEllipse = 3;
    public static final int rcpDrawArrow = 4;
    public static final int rcpDrawEraser = 8;

    private LinkedHashMap<float[], Paint> drawHashMinusSHeight;
    private LinkedHashMap<float[], Paint> drawHashOriginal;
    private float[] pointsMinusSHeight;
    private float[] pointsOriginal;
    private WindowManager windowManager;
    private DrawingView drawingView;
    private ActivityManager am;
    private Display display;
    private Context context;
    private Paint paint;
    private Rect rect;
    private String oldTopActivity;
    private String newTopActivity;
    private int oldOrientation;

    private int newOrientation;
    private short oldHeight;
    private short nowHeight;
    private short statusBarHeight;
    private boolean isStop;
    private int m_drawType;

    private Vector vecLines = new Vector();
    private Vector vecLinesOrigin = new Vector();
    private Vector vecArrows = new Vector();
    private Vector vecArrowsOrigin = new Vector();
    private Vector vecRects = new Vector();
    private Vector vecRectsOrigin = new Vector();
    private Vector vecEllipses = new Vector();
    private Vector vecEllipsesOrigin = new Vector();
    private final ComputeCoordinate computeCoordinate = new ComputeCoordinate();

    public ScreenDraw(Context context) {
        this.context = context;
        drawHashMinusSHeight = new LinkedHashMap<float[], Paint>();
        drawHashOriginal = new LinkedHashMap<float[], Paint>();

        rect = new Rect();
        statusBarHeight = getStatusBarHeight();
        am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        createWindow();
    }

    private void createWindow() {

        try {
            if (Build.VERSION.SDK_INT <= 16) {
                Class<?> clsWindow = Class.forName(Utility.getDecodeString("YW5kcm9pZC52aWV3LldpbmRvd01hbmFnZXJJbXBs"));
                windowManager = (WindowManager) clsWindow.getMethod(Utility.getDecodeString("Z2V0RGVmYXVsdA=="), (Class[]) null).invoke(null, (Object[]) null);

            } else {
                //4.2.1대응
                windowManager = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
            }
        } catch (Exception e) {
            RLog.e(e.getLocalizedMessage());
        }
        showTopmostView();
    }

    private View getView() {
        if (drawingView == null) {
            drawingView = new DrawingView(context);
        }
        return drawingView;
    }

    private WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        params.x = 20;
        params.y = 20;
        params.format = PixelFormat.TRANSLUCENT;
        params.type = getTopLayerType();
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        ;
        return params;
    }

    private int getTopLayerType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            return WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY | WindowManager.LayoutParams.TYPE_PHONE;  //softkey모델에서 softkey 영역 포함.(TYPE_SYSTEM_ERROR로 설정시 소프트키 영역 그리기 안됨.)
        }
    }

    private boolean showTopmostView() {
        if (windowManager != null) {
            windowManager.addView(getView(), getLayoutParams());
            return true;
        } else {
            return false;
        }
    }

    public void hide() {
        stopWindowHandler.sendEmptyMessage(0);
    }

    private Handler stopWindowHandler = new Handler() {
        public void handleMessage(Message msg) {
            windowManager.removeViewImmediate(getView());
        }
    };

    public void show() {
        drawHandler.sendEmptyMessage(0);
    }

    private Handler drawHandler = new Handler() {
        public void handleMessage(Message msg) {
            getView().invalidate();
        }
    };


    public synchronized void onComputeCoordinateChanged(ComputeCoordinate computeCoordinate) {
        this.computeCoordinate.apply(computeCoordinate);
        statusBarHeight = getStatusBarHeight();
    }

    public synchronized void setDrawData(byte[] bytes) {
        if (paint == null) return;
        switch (m_drawType) {
            case rcpDrawFreeLine:
            case rcpDrawEraser:
                setPoints(bytes);
                break;
            case rcpDrawLine:
                setLinePoints(bytes);
                break;
            case rcpDrawArrow:
                setArrowPoints(bytes);
                break;
            case rcpDrawRectangle:
                setRectPoints(bytes);
                break;
            case rcpDrawEllipse:
                setEllipsePoints(bytes);
                break;
        }
    }


    private DrawShapeInfo getDrawShapeInfo(byte[] bytes) {
        short x = readShortLittleEndian(bytes, 0);
        short y = (short) (readShortLittleEndian(bytes, 2));
        short x2 = readShortLittleEndian(bytes, 4);
        short y2 = (short) (readShortLittleEndian(bytes, 6));


        Point from = new Point(computeCoordinate.compute(x, y));
        Point to = new Point(computeCoordinate.compute(x2, y2));
        return new DrawShapeInfo((short) from.x, (short) (from.y - statusBarHeight), (short) to.x, (short) (to.y - statusBarHeight), paint);
    }

    private DrawShapeInfo getOriginDrawShapeInfo(byte[] bytes) {
        short x = readShortLittleEndian(bytes, 0);
        short y = (short) (readShortLittleEndian(bytes, 2));
        short x2 = readShortLittleEndian(bytes, 4);
        short y2 = (short) (readShortLittleEndian(bytes, 6));

        Point from = new Point(computeCoordinate.compute(x, y));
        Point to = new Point(computeCoordinate.compute(x2, y2));

        return new DrawShapeInfo((short) from.x, (short) from.y, (short) to.x, (short) to.y, paint);
    }

    private DrawShapeInfo getLeftTopRightBottomDrawShapeInfo(DrawShapeInfo drawShapeInfo) {
        short left = drawShapeInfo.x > drawShapeInfo.x2 ? drawShapeInfo.x2 : drawShapeInfo.x;
        short right = drawShapeInfo.x > drawShapeInfo.x2 ? drawShapeInfo.x : drawShapeInfo.x2;
        short top = drawShapeInfo.y > drawShapeInfo.y2 ? drawShapeInfo.y2 : drawShapeInfo.y;
        short bottom = drawShapeInfo.y > drawShapeInfo.y2 ? drawShapeInfo.y : drawShapeInfo.y2;

        drawShapeInfo.x = left;
        drawShapeInfo.y = top;
        drawShapeInfo.x2 = right;
        drawShapeInfo.y2 = bottom;

        return drawShapeInfo;
    }

    private void setLinePoints(byte[] bytes) {
        vecLines.add(getDrawShapeInfo(bytes));
        vecLinesOrigin.add(getOriginDrawShapeInfo(bytes));
    }

    private void setArrowPoints(byte[] bytes) {
        vecArrows.add(getDrawShapeInfo(bytes));
        vecArrowsOrigin.add(getOriginDrawShapeInfo(bytes));
    }

    private void setRectPoints(byte[] bytes) {
        vecRects.add(getLeftTopRightBottomDrawShapeInfo(getDrawShapeInfo(bytes)));
        vecRectsOrigin.add(getLeftTopRightBottomDrawShapeInfo(getOriginDrawShapeInfo(bytes)));
    }

    private void setEllipsePoints(byte[] bytes) {
        vecEllipses.add(getLeftTopRightBottomDrawShapeInfo(getDrawShapeInfo(bytes)));
        vecEllipsesOrigin.add(getLeftTopRightBottomDrawShapeInfo(getOriginDrawShapeInfo(bytes)));
    }

    private void setPoints(byte[] pointBytes) {
        pointsMinusSHeight = new float[pointBytes.length - 4];
        pointsOriginal = new float[pointBytes.length - 4];

        Point startCoordinate = computeCoordinate.compute(readShortLittleEndian(pointBytes, 0), readShortLittleEndian(pointBytes, 2));

        pointsMinusSHeight[0] = startCoordinate.x;
        pointsMinusSHeight[1] = startCoordinate.y - statusBarHeight;


        pointsOriginal[0] = startCoordinate.x;
        pointsOriginal[1] = startCoordinate.y;

        RLog.d("setPoints start: " + pointsMinusSHeight[0] + " , " + pointsMinusSHeight[1] + " , " + pointsOriginal[0] + " , "
                + pointsOriginal[1]);


        for (int i = 4, n = pointBytes.length - 4; i < n; i += 4) {
            Point coordinate = computeCoordinate.compute(readShortLittleEndian(pointBytes, i), readShortLittleEndian(pointBytes, i + 2));

            short x = (short) coordinate.x;
            short yMinusStatusHeight = (short) (coordinate.y - statusBarHeight);

            pointsMinusSHeight[i - 2] = x;
            pointsMinusSHeight[i - 1] = yMinusStatusHeight;
            pointsMinusSHeight[i] = x;
            pointsMinusSHeight[i + 1] = yMinusStatusHeight;

            short y = (short) coordinate.y;
            pointsOriginal[i - 2] = x;
            pointsOriginal[i - 1] = y;
            pointsOriginal[i] = x;
            pointsOriginal[i + 1] = y;

            RLog.d(i + " : " + x + " , " + y);
        }

        Point endCoordinate = computeCoordinate.compute(readShortLittleEndian(pointBytes, pointBytes.length - 4), readShortLittleEndian(pointBytes, pointBytes.length - 2));

        pointsMinusSHeight[pointsMinusSHeight.length - 2] = endCoordinate.x;
        pointsMinusSHeight[pointsMinusSHeight.length - 1] = endCoordinate.y - statusBarHeight;

        pointsOriginal[pointsOriginal.length - 2] = pointsMinusSHeight[pointsMinusSHeight.length - 2];
        pointsOriginal[pointsOriginal.length - 1] = endCoordinate.y;

        drawHashMinusSHeight.put(pointsMinusSHeight, paint);
        drawHashOriginal.put(pointsOriginal, paint);

        RLog.d("setPoints end: " + pointsMinusSHeight[pointsMinusSHeight.length - 2] + " , " + pointsOriginal[pointsOriginal.length - 2]);
    }

    public void setDrawInfo(byte[] drawInfoBytes) {
        int index = 0;
        m_drawType = Converter.readByteToIntLittleEndian(drawInfoBytes[index]);
        index += 1;

        byte[] r = new byte[4];
        r[3] = drawInfoBytes[index];
        byte[] g = new byte[4];
        g[3] = drawInfoBytes[index + 1];
        byte[] b = new byte[4];
        b[3] = drawInfoBytes[index + 2];

        int color = Color.rgb(Converter.getIntFromBytes(r), Converter.getIntFromBytes(g), Converter.getIntFromBytes(b));

        index += 4;
        char thickness = (char) drawInfoBytes[index];

        paint = new Paint();
        paint.setAntiAlias(true);

        if (m_drawType == rcpDrawEraser) {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            paint.setStrokeWidth((float) thickness + 50);
        } else {
            paint.setColor(color);
            paint.setStrokeWidth((float) thickness + 10);
        }

        switch (m_drawType) {
            case rcpDrawRectangle:
            case rcpDrawEllipse:
                paint.setStyle(Paint.Style.STROKE);
                break;
        }

        Log.d("rcpDraw", "type : " + m_drawType + " , " + r[3] + " , " + g[3] + " , " + b[3] + ", " + ((int) thickness));
    }

    private void startStatusBarHeightListener() {
        oldHeight = getDrawScreenTopPos();
        oldTopActivity = getTopActivityName();
        oldOrientation = getOrientation();

        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                while (!isStop) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    checkDrawScreenTopPos();
                    checkTopActivityName();
                    checkScreenOrientation();
                }
                Looper.loop();
            }
        }).start();
    }

    private void checkDrawScreenTopPos() {
        nowHeight = getDrawScreenTopPos();
        if (oldHeight != nowHeight) {
            oldHeight = nowHeight;
            show();
            returnStatusBarSize();
            RLog.i("h : " + nowHeight);
        }
    }

    private void checkTopActivityName() {
        newTopActivity = getTopActivityName();
        if (!oldTopActivity.equals(newTopActivity)) {
            RLog.i("o : " + oldTopActivity + ", n : " + newTopActivity);
            oldTopActivity = newTopActivity;
            new Thread(new Runnable() {
                public void run() {
                    erasePoints();
                }
            }).start();
        }
    }

    private void checkScreenOrientation() {
        newOrientation = getOrientation();
        if (oldOrientation != newOrientation) {
            RLog.i("o : " + oldOrientation + ", n : " + newOrientation);
            oldOrientation = newOrientation;
            new Thread(new Runnable() {
                public void run() {
                    erasePoints();
                }
            }).start();
        }
    }

    private int getOrientation() {
        return display.getOrientation();
    }

    private String getTopActivityName() {
        List<RunningTaskInfo> info = am.getRunningTasks(1);
        ComponentName topActivity = info.get(0).topActivity;
        return topActivity.getClassName();
    }

    private short getStatusBarHeight() {
//		Rect statusBarRect = new Rect();
//		((Activity)context).getWindow().getDecorView().getWindowVisibleDisplayFrame(statusBarRect);
//		return (short)statusBarRect.top;
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return (short) result;
    }

    public short getDrawScreenTopPos() {
        rect.setEmpty();
        getView().getWindowVisibleDisplayFrame(rect);

        return (short) rect.top;
    }

    public void startDrawScreen() {
        isStop = false;
        startStatusBarHeightListener();
        returnStatusBarSize();
        startScreenChangeChecking();
    }

    private void startScreenChangeChecking() {
//		ServiceBind.getInstance(null).setScreenChangeNoty(true);
    }

    public void stopDrawScreen() {
        isStop = true;
        windowManager.removeView(getView());
        stopScreenChangeChecking();
    }

    private void stopScreenChangeChecking() {
//		ServiceBind.getInstance(null).setScreenChangeNoty(false);
    }

    public synchronized void erasePoints() {
        RLog.i("erasePoints");
        drawHashMinusSHeight.clear();
        drawHashOriginal.clear();
        vecLines.clear();
        vecLinesOrigin.clear();
        vecArrows.clear();
        vecArrowsOrigin.clear();
        vecRects.clear();
        vecRectsOrigin.clear();
        vecEllipses.clear();
        vecEllipsesOrigin.clear();
        show();
    }

    public short readShortLittleEndian(byte[] b, int n) {
        // 2 bytes
        int low = b[n] & 0xff;
        int high = b[n + 1] & 0xff;
        return (short) (high << 8 | low);
    }

    public void returnStatusBarSize() {
        int sLeft = rect.left;            //0
        int sTop = rect.top;            //38
        int sWidth = rect.width();        //480
//		int sHeight = rect.height();	//762

        RLog.i("l : " + sLeft + ", h : " + 0 + ", w : " + sWidth + ", t : " + sTop);

        byte[] statusBarPacket = new byte[16];

        int startPos = 0;
        System.arraycopy(Converter.getBytesFromIntLE(sLeft), 0, statusBarPacket, startPos, 4);
        startPos += 4;
        System.arraycopy(Converter.getBytesFromIntLE(0), 0, statusBarPacket, startPos, 4);
        startPos += 4;
        System.arraycopy(Converter.getBytesFromIntLE(sWidth), 0, statusBarPacket, startPos, 4);
        startPos += 4;
        System.arraycopy(Converter.getBytesFromIntLE(sTop), 0, statusBarPacket, startPos, 4);
        startPos += 4;

    }

    protected class DrawingView extends View {
        Paint mPaint;

        private WindowDisplay windowDisplay;

        public DrawingView(Context context) {
            super(context);
            windowDisplay = new WindowDisplay(windowManager);
        }

        public void onDraw(Canvas canvas) {
            RLog.i("DrawingView onDraw");

            canvas.save();

            if (windowDisplay.getNavigationDirection() == NavigationBar.LEFT && getWidth() < windowDisplay.getWidth()) {
                int offset = windowDisplay.getWidth() - getWidth();
                canvas.translate(-computeCoordinate.compute(offset, offset).x, 0);
            }
            drawToView(canvas);

            canvas.restore();
        }
    }

    public synchronized void drawToView(Canvas canvas) {
        RLog.i("drawToView");
        if (statusBarHeight == getDrawScreenTopPos()) {
            drawNormal(canvas);
        } else {
            drawOrigin(canvas);
        }
    }

    private void drawNormal(Canvas canvas) {
        Iterator<float[]> iter = drawHashMinusSHeight.keySet().iterator();
        while (iter.hasNext()) {
            float[] drawPoints = (float[]) iter.next();
            canvas.drawLines(drawPoints, drawHashMinusSHeight.get(drawPoints));
        }
        int size = vecLines.size();
        for (int i = 0; i < size; i++) {
            DrawShapeInfo drawInfo = (DrawShapeInfo) vecLines.elementAt(i);
            canvas.drawLine(drawInfo.x, drawInfo.y, drawInfo.x2, drawInfo.y2, drawInfo.paint);
        }
        size = vecArrows.size();
        for (int i = 0; i < size; i++) {
            DrawShapeInfo drawInfo = (DrawShapeInfo) vecArrows.elementAt(i);
            drawArrow(canvas, drawInfo, drawInfo.paint);
        }
        size = vecRects.size();
        for (int i = 0; i < size; i++) {
            DrawShapeInfo drawInfo = (DrawShapeInfo) vecRects.elementAt(i);
            RLog.i("drawInfo.x, drawInfo.y, drawInfo.x2, drawInfo.y2," + drawInfo.x + "  :::  " + drawInfo.y + "  :::  " + drawInfo.x2 + "  :::  " + drawInfo.y2);
            canvas.drawRect(drawInfo.x, drawInfo.y, drawInfo.x2, drawInfo.y2, drawInfo.paint);
        }
        size = vecEllipses.size();
        for (int i = 0; i < size; i++) {
            DrawShapeInfo drawInfo = (DrawShapeInfo) vecEllipses.elementAt(i);
            canvas.drawOval(new RectF(drawInfo.x, drawInfo.y, drawInfo.x2, drawInfo.y2), drawInfo.paint);
        }
    }

    private void drawOrigin(Canvas canvas) {
        Iterator<float[]> iter = drawHashOriginal.keySet().iterator();
        while (iter.hasNext()) {
            float[] drawPoints = (float[]) iter.next();
            canvas.drawLines(drawPoints, drawHashOriginal.get(drawPoints));
        }
        int size = vecLinesOrigin.size();
        for (int i = 0; i < size; i++) {
            DrawShapeInfo drawInfo = (DrawShapeInfo) vecLinesOrigin.elementAt(i);
            canvas.drawLine(drawInfo.x, drawInfo.y, drawInfo.x2, drawInfo.y2, drawInfo.paint);
        }
        size = vecArrowsOrigin.size();
        for (int i = 0; i < size; i++) {
            DrawShapeInfo drawInfo = (DrawShapeInfo) vecArrowsOrigin.elementAt(i);
            drawArrow(canvas, drawInfo, drawInfo.paint);
        }
        size = vecRectsOrigin.size();
        for (int i = 0; i < size; i++) {
            DrawShapeInfo drawInfo = (DrawShapeInfo) vecRectsOrigin.elementAt(i);
            canvas.drawRect(drawInfo.x, drawInfo.y, drawInfo.x2, drawInfo.y2, drawInfo.paint);
            RLog.i("drawInfo.x, drawInfo.y, drawInfo.x2, drawInfo.y2," + drawInfo.x + "  :::  " + drawInfo.y + "  :::  " + drawInfo.x2 + "  :::  " + drawInfo.y2);
        }
        size = vecEllipsesOrigin.size();
        for (int i = 0; i < size; i++) {
            DrawShapeInfo drawInfo = (DrawShapeInfo) vecEllipsesOrigin.elementAt(i);
            canvas.drawOval(new RectF(drawInfo.x, drawInfo.y, drawInfo.x2, drawInfo.y2), drawInfo.paint);
        }
    }

    private void drawArrow(Canvas canvas, DrawShapeInfo drawInfo, Paint paint) {
        Point[] arrPoint = new Point[4];
        Point start = new Point(drawInfo.x, drawInfo.y);
        Point end = new Point(drawInfo.x2, drawInfo.y2);
        getTrianglePoint(start, end, arrPoint);
        drawArrow(canvas, start, end, arrPoint, paint);
    }

    private boolean getTrianglePoint(Point start, Point end, Point[] arrow) {
        if (arrow == null || arrow.length < 4) return false;

        int nWidth = (int) (23 * 1.4);
        int nHeight = (int) (30 * 1.3);

        double theta, Cx, Cy, w_sin_th, w_cos_th;

        theta = Math.atan2((double) (end.y - start.y), (double) (end.x - start.x));

        Cx = end.x - nHeight * Math.cos(theta);
        Cy = end.y - nHeight * Math.sin(theta);

        w_sin_th = nWidth * Math.sin(theta) / 2.;
        w_cos_th = nWidth * Math.cos(theta) / 2.;

        arrow[0] = new Point();
        arrow[0].set(end.x, end.y);
        arrow[1] = new Point();
        arrow[1].set((short) (Cx - w_sin_th + 0.5), (short) (Cy + w_cos_th + 0.5));
        arrow[2] = new Point();
        arrow[2].set((short) (Cx + w_sin_th + 0.5), (short) (Cy - w_cos_th + 0.5));
        arrow[3] = new Point();
        arrow[3].set((short) Cx, (short) Cy);

        return true;
    }

    private void drawArrow(Canvas canvas, Point start, Point end, Point[] arrow, Paint paint) {
        Path path = new Path();

        path.moveTo(arrow[0].x, arrow[0].y); // 화살표머리
        path.lineTo(arrow[1].x, arrow[1].y); // 화살표 왼쪽 끝점
        path.lineTo(arrow[2].x, arrow[2].y); // 화살표 오른쪽 끝점

        canvas.drawLine(start.x, start.y, arrow[3].x, arrow[3].y, paint);

        canvas.drawPath(path, paint);
    }

}
