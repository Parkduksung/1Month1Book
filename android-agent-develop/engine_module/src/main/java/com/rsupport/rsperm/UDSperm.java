package com.rsupport.rsperm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.List;

import android.content.Context;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;
import android.view.Surface;

import com.rsupport.util.Net10;
import com.rsupport.util.Screen;
import com.rsupport.util.log.RLog;

public class UDSperm implements IRSPerm {

    final static String TAG = "UDSperm";
    Context mCxt;
    boolean mEnableInput;

    public UDSperm(Context cxt, boolean enableInput) {
        mCxt = cxt;
        mEnableInput = enableInput;
    }

    LocalSocket mSock;
    LocalSocket mJarSock;

    @Override
    public synchronized boolean bind(String pkgName) {
        unbind();
        pkgName = mCxt.getPackageName();

        try {
            LocalSocket sock = new LocalSocket();
            String address = pkgName + ".udsbinder";
            sock.connect(new LocalSocketAddress(address));
            mSock = sock;
            Log.i(TAG, "binded with " + address);
        } catch (IOException e) {
            Log.w(TAG, "bind screen: " + e.toString());
            return false;
        }

        if (mEnableInput) {
            try {
                String address = pkgName + ".jarinject";
                LocalSocket sock = new LocalSocket();
                sock.connect(new LocalSocketAddress(address));
                mJarSock = sock;
                Log.i(TAG, "binded with " + address);
            } catch (IOException e) {
                Log.e(TAG, "bind input: " + e.toString());
            }
        } else {
            Log.w(TAG, "input is disabled.");
        }
        return isBinded();
    }

    @Override
    public synchronized void unbind() {
        if (mSock != null) {
            try {
                mSock.shutdownInput();
                mSock.shutdownOutput();
                mSock.close();
            } catch (IOException e) {
            }
            mSock = null;
        }

        if (mJarSock != null) {
            try {
                mJarSock.shutdownInput();
                mJarSock.shutdownOutput();
                mJarSock.close();
            } catch (IOException e) {
            }
            mJarSock = null;
        }
        mCaptureReqCache = null;
    }

    @Override
    public synchronized boolean isBinded() {
        return (mSock != null && mSock.isConnected());
    }

    @Override
    public synchronized boolean loadJni(String soPath) {
        // U must execute the module previously.
        return true;
    }

    @Override
    public String getAddress() {
        return null;
    }

    @Override
    public synchronized FileDescriptor getFile(String args) throws Exception {
        mCaptureReqCache = null; // clear previous req
        if (mSock == null) {
            Log.e(TAG, "not binded.");
            return null;
        }
        // len(4B), cmdID(1B),

        ByteBuffer bb = RSPermHelper.buildRequest(true, 256, JNI_ASHMCreate, args);
        synchronized (csWrite) {
            mSock.getOutputStream().write(bb.array(), 0, bb.position());
            return Net10.recvFd(mSock.getFileDescriptor());
        }
    }

    @Override
    public synchronized boolean initScreen(int w, int h) throws Exception {
        ByteBuffer bb = RSPermHelper.buildRequest(true, 13, JNI_ASHMInitScreen, w, h);
        synchronized (csWrite) {
            mSock.getOutputStream().write(bb.array(), 0, 13);
            mSock.getInputStream().read(bb.array(), 0, 8);
        }
        assert (4 == bb.getInt(0));
        Log.i(TAG, "result of initScreen: " + bb.getInt(4));
        return bb.getInt(4) == COMMAND_OK;
    }

    private Object csWrite = new Object();
    private ByteBuffer mCaptureReqCache;

    @Override
    public synchronized boolean capture(int w, int h) throws Exception {
        if (mCaptureReqCache == null) {
            mCaptureReqCache = RSPermHelper.buildRequest(true, 30, JNI_ASHMCapture, w, h);
            assert mCaptureReqCache.position() == 13;
        }
        synchronized (csWrite) {
            mSock.getOutputStream().write(mCaptureReqCache.array(), 0, 13);
            mSock.getInputStream().read(mCaptureReqCache.array(), 20, 8);
        }
        assert (4 == mCaptureReqCache.getInt(20));
        return mCaptureReqCache.getInt(24) == COMMAND_OK;
    }

    @Override
    public synchronized void inject(byte[] data, int offset, int length) throws Exception {
        data[offset - 1] = (byte) length;
        --offset;
        ++length;
        mJarSock.getOutputStream().write(data, offset, length);
        // there is no reply packet.
    }

    @Override
    public synchronized boolean screenshot(String imgPath) throws Exception {
        ByteBuffer bb = RSPermHelper.buildRequest(true, 256, JNI_ASHMScreenshot, imgPath);
        synchronized (csWrite) {
            mSock.getOutputStream().write(bb.array(), 0, bb.position());
            mSock.getInputStream().read(bb.array(), 0, 8);
        }
        assert (4 == bb.getInt(0));
        Log.i(TAG, "result of screenshot: " + bb.getInt(4));
        return bb.getInt(4) == COMMAND_OK;
    }

    @Override
    public synchronized int hwrotation() throws Exception {
        ByteBuffer bb = RSPermHelper.buildRequest(true, 32, JNI_GetHWRotation, Screen.resolution(mCxt));
        synchronized (csWrite) {
            mSock.getOutputStream().write(bb.array(), 0, bb.position());
            mSock.getInputStream().read(bb.array(), 0, 8);
        }
        assert (4 == bb.getInt(0));
        Log.i(TAG, "result of hwrotation: " + bb.getInt(4));
        return bb.getInt(4);
    }

    @Override
    public synchronized String exec(String cmd) throws Exception {
        ByteBuffer bb = RSPermHelper.buildRequest(true, 256, JNI_Execute, cmd);
        synchronized (csWrite) {
            mSock.getOutputStream().write(bb.array(), 0, bb.position());

            mSock.getInputStream().read(bb.array(), 0, 4);
            int size = bb.getInt(0);
            Log.i(TAG, "result of exec: " + size);

            byte[] reply = new byte[size];
            mSock.getInputStream().read(reply);
            return new String(reply, "UTF-8");
        }
        //Settings.Secure.putString(getContentResolver(), name, value);
    }

    public static Process exec(String launcherPath, boolean enableInput) {
        String suPath[] = {
                "/system/xbin/su",
                "/sbin/su",
        };
        File su = null;
        for (String path : suPath) {
            File f = new File(path);
            if (f.canExecute()) {
                su = f;
                break;
            }
        }
        if (su == null) return null;

        Process process = null;
        try {
            process = Runtime.getRuntime().exec(su.getAbsolutePath());
            StringBuilder sb = new StringBuilder();
            sb.append(launcherPath);
            if (enableInput) sb.append(" -jarinput");
            sb.append("\n");
            process.getOutputStream().write(sb.toString().getBytes());
            process.getOutputStream().flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

            for (String line = in.readLine(); line != null; line = in.readLine()) {
                RLog.e("read from exe: " + line);
                if (line.contains("[ERRO]")) {
                    RLog.e(line);
                    process.destroy();
                    process = null;
                    break;
                }
                if (line.contains("[INFO]")) {
                    RLog.i(line);
                    break;
                }
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return process;
    }

    @Override
    public synchronized boolean putSFloat(String name, float value) throws Exception {
        return new UDSSettings(mCxt.getPackageName()).putSFloat(this, name, value);
    }

    @Override
    public synchronized boolean putSInt(String name, int value) throws Exception {
        return new UDSSettings(mCxt.getPackageName()).putSInt(this, name, value);
    }

    @Override
    public synchronized boolean putSLong(String name, long value) throws Exception {
        return new UDSSettings(mCxt.getPackageName()).putSLong(this, name, value);
    }

    @Override
    public synchronized boolean putSString(String name, String value) throws Exception {
        return new UDSSettings(mCxt.getPackageName()).putSString(this, name, value);
    }

    @Override
    public synchronized boolean putGFloat(String name, float value) throws Exception {
        return new UDSSettings(mCxt.getPackageName()).putGFloat(this, name, value);
    }

    @Override
    public synchronized boolean putGInt(String name, int value) throws Exception {
        return new UDSSettings(mCxt.getPackageName()).putGInt(this, name, value);
    }

    @Override
    public synchronized boolean putGLong(String name, long value) throws Exception {
        return new UDSSettings(mCxt.getPackageName()).putGLong(this, name, value);
    }

    @Override
    public synchronized boolean putGString(String name, String value) throws Exception {
        return new UDSSettings(mCxt.getPackageName()).putGString(this, name, value);
    }

    @Override
    public synchronized void injectTouchEvent(int action, int x, int y, int x2, int y2) throws Exception {

    }

    @Override
    public synchronized void injectKeyEvent(int action, int key) throws Exception {

    }

    public synchronized boolean createVirtualDisplay(String vdname, int width, int height, int dpi, int flags, Surface surf) {
//		if (isBinded() == false) return false;
//		try {
//			return mBinder.createVirtualDisplay(vdname, width, height, dpi, surf, flags);
//		}
//		catch (Exception e) {
//			log.e(e.toString());
        return false;
//		}

    }

    @Override
    public synchronized List getRunningProcesses() {
        return null;
    }

    @Override
    public boolean hasDumpsys() {
        return false;
    }
}



