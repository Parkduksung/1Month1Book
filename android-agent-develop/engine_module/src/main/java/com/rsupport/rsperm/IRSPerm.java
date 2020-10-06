package com.rsupport.rsperm;

import android.app.ActivityManager;
import android.graphics.Point;
import android.view.Surface;

import java.io.FileDescriptor;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public interface IRSPerm {
    public boolean bind(String address);

    public void unbind();

    public boolean isBinded();

    public boolean loadJni(String soPath);

    /**
     * @return 접속 Address, rsperm 을 사용하면 rsperm package name 그렇지 않으면 uds address
     */
    public String getAddress();

    public FileDescriptor getFile(String args) throws Exception;

    public boolean initScreen(int w, int h) throws Exception;

    public boolean capture(int w, int h) throws Exception;

    public void inject(byte[] data, int offset, int length) throws Exception;

    public void injectTouchEvent(int action, int x, int y, int x2, int y2) throws Exception;

    public void injectKeyEvent(int action, int key) throws Exception;

    public boolean screenshot(String imgPath) throws Exception;

    public String exec(String cmd) throws Exception;

    public int hwrotation() throws Exception;

    public boolean putSFloat(String name, float value) throws Exception;

    public boolean putSInt(String name, int value) throws Exception;

    public boolean putSLong(String name, long value) throws Exception;

    public boolean putSString(String name, String value) throws Exception;

    public boolean putGFloat(String name, float value) throws Exception;

    public boolean putGInt(String name, int value) throws Exception;

    public boolean putGLong(String name, long value) throws Exception;

    public boolean putGString(String name, String value) throws Exception;

    public boolean createVirtualDisplay(String vdname, int width, int height, int dpi, int flags, Surface surf);

    public List<ActivityManager.RunningAppProcessInfo> getRunningProcesses();

    /**
     * Dumpsys 명령을 수행할 수 있는지를 판단한다.
     *
     * @return dumpsys 명령어를 실행할 수 있으면 true, 그렇지 않으면 false`
     */
    boolean hasDumpsys();


    final static int COMMAND_OK = 0;

    static final int JNI_ASHMInject = 19;
    static final int JNI_ASHMCreate = 20;
    static final int JNI_ASHMCapture = 21;
    static final int JNI_ASHMScreenshot = 22; // dump to file
    static final int JNI_Execute = 23;
    static final int JNI_GetHWRotation = 24;
    static final int JNI_ASHMInitScreen = 25;

}

class RSPermHelper {
    static ByteBuffer buildRequest(boolean uds, int bufSize, int type, Object... args) {
        ByteBuffer bb = ByteBuffer.allocate(bufSize).order(ByteOrder.LITTLE_ENDIAN);
        if (uds)
            bb.position(4);
        bb.put((byte) type);
        for (Object arg : args) {
            if (arg instanceof Integer)
                bb.putInt((Integer) arg);
            else if (arg instanceof Byte)
                bb.put((Byte) arg);
            else if (arg instanceof String)
                bb.put(((String) arg).getBytes());
            else if (arg instanceof Point)
                bb.putInt(((Point) arg).x).putInt(((Point) arg).y);
        }
        if (uds) {
            int size = bb.position();
            bb.putInt(0, size - 4);
        }
        return bb;
    }
}
