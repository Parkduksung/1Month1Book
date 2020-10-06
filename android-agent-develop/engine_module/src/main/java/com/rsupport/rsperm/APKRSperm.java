package com.rsupport.rsperm;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.widget.Toast;

import com.rsupport.engine.IBinderListener;
import com.rsupport.util.Net10;
import com.rsupport.util.Screen;
import com.rsupport.util.log.RLog;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileDescriptor;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

public class APKRSperm implements IRSPerm {

    final static String TAG = "APKRSperm";

    Context mCxt;

    private IBinderListener mBinderListener = null;

    /**
     * Rsperm version code 가 330 이상일때만 dumpsys 명령을 수행할 수 있다.
     */
    private int SUPPORTED_DUMP_SYS_RSPERM_MIN_VERSION_CODE = 330;

    public APKRSperm(Context cxt, @NotNull IBinderListener binderListener) {
        mCxt = cxt;
        mBinderListener = binderListener;
    }

    boolean bindRequested = false;
    private String packageName = null;


    @Override
    public synchronized boolean bind(String address) {
        boolean ret = false;
        this.packageName = address;

        // UI Thread 예외처리.
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.e(TAG, "called from UI thread.");
            return false;
        }

        if (bindRequested == true) {
            Log.i(TAG, "already binding...");
            return (mBinder != null);
        }

        try {
            if (mBinder == null) {
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }
                String mClassname = "com.rsupport.rsperm.i";
                Intent intent1 = new Intent();
                intent1.setClassName(address, mClassname);
                List<ResolveInfo> list = mCxt.getPackageManager().queryIntentServices(intent1, PackageManager.MATCH_DEFAULT_ONLY);
                if (list.size() <= 0) {
                    Toast.makeText(mCxt, "There is no rsperm-v2 package", Toast.LENGTH_LONG).show();
                    return false;
                }

                Intent intent = new Intent();
                intent.setClassName(address, mClassname);
                Log.i(TAG, "BindSrnCaller bind : " + address + " : " + mClassname);
                ret = mCxt.bindService(intent, srnConn, Context.BIND_AUTO_CREATE);
                if (ret) bindRequested = true;
                mBinderListener.onBindService(ret);
            }
        } catch (Exception e) {
            Log.e(TAG, "bind error : " + e.getLocalizedMessage());
        }
        return ret;
    }

    @Override
    public synchronized void unbind() {
        try {
            if (mBinder != null && bindRequested == true) {
                RLog.v("-------------------- APKRSperm unbind");
                mCxt.unbindService(srnConn);
                mBinderListener.onUnBindService(true);
            }
            bindRequested = false;
            mBinder = null;
            mCaptureReqCache = null;
        } catch (Exception e) {
            e.printStackTrace();
            mBinderListener.onUnBindService(false);
        } finally {

        }
    }

    boolean isVer2 = true;

    @Override
    public synchronized boolean loadJni(String jniPath) {
        if (!new File(jniPath).exists()) {
            Log.e(TAG, "Null file: " + jniPath);
            return false;
        }
        if (mBinder == null) {
            Log.e(TAG, "not binded");
            return false;
        }

        try {
            int ret = mBinder.testDummy1(jniPath);
            Log.i(TAG, String.format("[%s], ret:%d", jniPath, ret));
            if (ret == COMMAND_OK) {
                String query = "features";
                byte[] answer = mBinder.query(query.getBytes(), query.length());
                int features = Integer.parseInt(new String(answer));
                isVer2 = (features & 0x0100) != 0;

                Log.i(TAG, String.format("rsperm features: %08x", features));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getAddress() {
        return packageName;
    }

    @Override
    public boolean hasDumpsys() {
        try {
            PackageInfo packageInfo = mCxt.getPackageManager().getPackageInfo(getAddress(), PackageManager.GET_SERVICES);
            if (packageInfo.versionCode >= SUPPORTED_DUMP_SYS_RSPERM_MIN_VERSION_CODE) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public synchronized FileDescriptor getFile(String args) throws Exception {
        mCaptureReqCache = null; // clear previous req
        if (mBinder == null) return null;
        if (isVer2) {
            ParcelFileDescriptor parcelFileDescriptor = mBinder.getFile(args, 0, 0, 0);
            if (parcelFileDescriptor != null) {
                return parcelFileDescriptor.getFileDescriptor();
            }
            return null;
        }

        String udsaddr = mCxt.getPackageName() + ".udsashm";
        LocalServerSocket svrSock = new LocalServerSocket(udsaddr);
        args = "udsaddr=" + udsaddr + "&" + args;
        ByteBuffer bb = RSPermHelper.buildRequest(false, 256, JNI_ASHMCreate, args);
        if (mBinder.testDummy2(bb.array(), bb.position()) != 0)
            return null;

        LocalSocket sock = svrSock.accept();
        FileDescriptor theFd = Net10.recvFd(sock.getFileDescriptor());
        sock.close();
        svrSock.close();
        return theFd;
    }

    @Override
    public synchronized boolean initScreen(int w, int h) throws Exception {
        ByteBuffer bb = RSPermHelper.buildRequest(false, 13, JNI_ASHMInitScreen, w, h);
        return mBinder.testDummy2(bb.array(), bb.position()) == COMMAND_OK;
    }

    private ByteBuffer mCaptureReqCache;

    @Override
    public synchronized boolean capture(int w, int h) throws Exception {

        if (isVer2)
            return mBinder.capture(w, h, 0) == COMMAND_OK; // rsperm v2+

        if (mCaptureReqCache == null) {
            mCaptureReqCache = RSPermHelper.buildRequest(false, 13, JNI_ASHMCapture, w, h);
            assert mCaptureReqCache.position() == 13;
        }
        return mBinder.testDummy2(mCaptureReqCache.array(), mCaptureReqCache.position()) == COMMAND_OK;
    }

    private boolean isFunctionKey(int code) {
        return code == KeyEvent.KEYCODE_HOME
                || code == KeyEvent.KEYCODE_SEARCH
                || code == KeyEvent.KEYCODE_MENU
                || code == KeyEvent.KEYCODE_ENDCALL
                || code == KeyEvent.KEYCODE_BACK
                || code == KeyEvent.KEYCODE_POWER
                || code == KeyEvent.KEYCODE_APP_SWITCH;
    }

    final int KEY_OFFSET = 100;
    final int KEY_ACTION_DOWN = KEY_OFFSET + 0; //KeyEvent.ACTION_DOWN;
    final int KEY_ACTION_UP = KEY_OFFSET + 1; //KeyEvent.ACTION_UP;

    @Override
    public synchronized void inject(byte[] data, int offset, int length) throws Exception {
        //if (isVer2) mBinder.injectWithBytes(data, offset, length);
        --offset;
        ++length;
        data[offset] = (byte) JNI_ASHMInject;
        mBinder.testDummy2(Arrays.copyOfRange(data, offset, offset + length), length);
    }

    public synchronized void injectTouchEvent(int action, int x, int y, int x2, int y2) throws Exception {
        mBinder.injectWithPrimitive(action, 0, x, y, x2, y2);
    }

    public synchronized void injectKeyEvent(int action, int key) throws Exception {
//			mBinder.injectWithPrimitive(action, key, 0, 0, 0, 0);
        switch (action) {
            case KEY_ACTION_DOWN:
                mBinder.injectWithPrimitive(KEY_ACTION_DOWN, key, key, getRepeat(action, key), getMetaState(action, key), 0);
            case KEY_ACTION_UP:
                mBinder.injectWithPrimitive(KEY_ACTION_UP, key, key, getRepeat(action, key), getMetaState(action, key), 0);
                break;
            default:
                Log.d("RemoteCall", "Invalid key action: " + action);
        }
    }

    private static int kbdRepeatCnt = -1;

    protected int getRepeat(int action, int code) {
        if (isFunctionKey(code)) {
            if (action == KEY_ACTION_DOWN) {
                return ++kbdRepeatCnt;
            } else {
                kbdRepeatCnt = -1; // KeycodeAction
                return 0;
            }
        } else {
            return 0;
        }
    }

    private static int metaState = 0;

    protected int getMetaState(int action, int code) {
        if (isFunctionKey(code)) {
            return 0;
        } else {
            if (code == KeyEvent.KEYCODE_SHIFT_LEFT) {
                if (action == KEY_ACTION_DOWN)
                    metaState |= (KeyEvent.META_SHIFT_LEFT_ON | KeyEvent.META_SHIFT_ON);
                else
                    metaState &= ~(KeyEvent.META_SHIFT_LEFT_ON | KeyEvent.META_SHIFT_ON);
            } else if (code == KeyEvent.KEYCODE_ALT_LEFT) {
                if (action == KEY_ACTION_DOWN)
                    metaState |= (KeyEvent.META_ALT_LEFT_ON | KeyEvent.META_ALT_ON);
                else
                    metaState &= ~(KeyEvent.META_ALT_LEFT_ON | KeyEvent.META_ALT_ON);
            } else if (code == KeyEvent.KEYCODE_CTRL_LEFT) {
                if (action == KEY_ACTION_DOWN)
                    metaState |= (KeyEvent.META_CTRL_LEFT_ON | KeyEvent.META_CTRL_ON);
                else
                    metaState &= ~(KeyEvent.META_CTRL_LEFT_ON | KeyEvent.META_CTRL_ON);
            }
            return metaState;
        }
    }


    @Override
    public synchronized boolean isBinded() {
        return (bindRequested && mBinder != null);
    }

    public com.rsupport.rsperm.IDummy mBinder;
    private com.rsupport.rsperm.IDummyCallback mSrnEventStub = new com.rsupport.rsperm.IDummyCallback.Stub() {
        public int getInt(byte[] data) {
            return -1234;
        }

        @Override
        public void onEvent(byte[] data) {
            ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            int infoType = bb.getInt();
            Log.i(TAG, "Infotype: " + infoType);

            try {
                for (; ; ) {
                    Log.i(TAG, String.format("value: %d", bb.getInt()));
                }
            } catch (BufferUnderflowException e) {
            }
        }
    };
    private ServiceConnection srnConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "rsperm is disconnected");
            mBinderListener.onServiceDisconnected();
            mBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.i(TAG, "rsperm is connected");
            try {
                mBinder = com.rsupport.rsperm.IDummy.Stub.asInterface(binder);
                mBinder.registerCallback(mSrnEventStub);
                mBinderListener.onServiceConnected();
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (SecurityException se) {
                se.printStackTrace();
                if (mBinderListener != null) {
                    mBinderListener.onServiceDisconnected();
                }
                mBinder = null;
            }
            Log.d(TAG, "binded screen");
        }
    };

    @Override
    public synchronized boolean screenshot(String imgPath) throws Exception {
        ByteBuffer bb = RSPermHelper.buildRequest(false, 256, JNI_ASHMScreenshot, imgPath);
        return mBinder.testDummy2(bb.array(), bb.position()) == COMMAND_OK;
    }

    @Override
    public synchronized String exec(String cmd) throws Exception {
        return null;
    }

    @Override
    public synchronized int hwrotation() throws Exception {
        ByteBuffer bb = RSPermHelper.buildRequest(false, 30, JNI_GetHWRotation, Screen.resolution(mCxt));
        return mBinder.testDummy2(bb.array(), bb.position());
    }

    @Override
    public synchronized boolean putSFloat(String name, float value) throws Exception {
        if (mBinder != null) {
            return mBinder.putSFloat(name, value);
        }
        return false;
    }

    @Override
    public synchronized boolean putSInt(String name, int value) throws Exception {
        if (mBinder != null) {
            return mBinder.putSInt(name, value);
        }
        return false;
    }

    @Override
    public synchronized boolean putSLong(String name, long value) throws Exception {
        if (mBinder != null) {
            return mBinder.putSLong(name, value);
        }
        return false;
    }

    @Override
    public synchronized boolean putSString(String name, String value) throws Exception {
        if (mBinder != null) {
            return mBinder.putSString(name, value);
        }
        return false;
    }

    @Override
    public synchronized boolean putGFloat(String name, float value) throws Exception {
        if (mBinder != null) {
            return mBinder.putGFloat(name, value);
        }
        return false;
    }

    @Override
    public synchronized boolean putGInt(String name, int value) throws Exception {
        if (mBinder != null) {
            return mBinder.putGInt(name, value);
        }
        return false;
    }

    @Override
    public synchronized boolean putGLong(String name, long value) throws Exception {
        if (mBinder != null) {
            return mBinder.putGLong(name, value);
        }
        return false;
    }

    @Override
    public synchronized boolean putGString(String name, String value) throws Exception {
        if (mBinder != null) {
            return mBinder.putGString(name, value);
        }
        return false;
    }

    @Override
    public synchronized boolean createVirtualDisplay(String vdname, int width, int height, int dpi, int flags, Surface surf) {
        if (!isBinded()) return false;
        try {
            return mBinder.createVirtualDisplay(vdname, width, height, dpi, surf, flags);
        } catch (Exception e) {
            RLog.e(e.toString());
            return false;
        }

    }

    @Override
    public synchronized List<ActivityManager.RunningAppProcessInfo> getRunningProcesses() {
        if (!isBinded()) return null;
        try {
            return mBinder.getRunningProcesses();
        } catch (Exception e) {
            RLog.e(e.toString());
            throw new RuntimeException("no Method");
        }
    }
}
