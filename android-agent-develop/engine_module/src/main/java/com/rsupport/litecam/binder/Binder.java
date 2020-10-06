package com.rsupport.litecam.binder;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.rsupport.engine.IBinderListener;
import com.rsupport.litecam.RspermScreenCapture;
import com.rsupport.litecam.util.LLog;
import com.rsupport.rsperm.APKRSperm;
import com.rsupport.rsperm.IRSPerm;
import com.rsupport.rsperm.UDSperm;
import com.rsupport.util.validator.LG318RSpermPackageValidator;
import com.rsupport.util.MemoryFileEx;
import com.rsupport.util.RspermCheck;
import com.rsupport.util.log.RLog;

/**
 * rsperm bind class.
 * {@link RspermScreenCapture}를 처리하기 전에 Binder를 우선 처리.
 * Binder를 초기화 하고, {@link #bind} 함수를 호출하면, rsperm or root/USB를 통해 초기화.
 * <p>
 * 프로그램이 종료되면, {@link #unbind}을 호출해서 종료해주어야 한다.
 *
 * @author taehwan
 */
public class Binder {
    public static final String TAG = "CaptureAshmem";

    public static final int RSPERM_BIND_SUCCESS = 0;
    public static final int RSPERM_BINDED = 1;
    public static final int RSPERM_BIND_FAIL = -1;

    /**
     * Rooting과 rsperm 사용을 체크하기 위한 변수.
     * {@link MemoryFileEx#readBytes}와 {@link MemoryFileEx#writeBytes}에서 root/rsperm 사용시 예외처리를 위하여 flag 설정.
     */
    public static boolean IS_ROOTED = false;

    private Context mContext = null;
    private Process mProcess;

    private IRSPerm mPerm = null;

    private final IBinderListener mBinderListener = new IBinderListener() {
        @Override
        public void onBindService(boolean ret) {
        }

        @Override
        public void onUnBindService(boolean ret) {

        }

        @Override
        public void onServiceConnected() {

        }

        @Override
        public void onServiceDisconnected() {

        }
    };

    private static Binder mBinder;

    private Binder() {
    }

    public synchronized static Binder getInstance() {
        if (mBinder == null) {
            mBinder = new Binder();
        }
        return mBinder;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    /**
     * bind된 rsperm을 return
     * {@link RspermScreenCapture}에 load 하여 사용.
     *
     * @return
     */
    public synchronized IRSPerm getBinder() {
        return mPerm;
    }

    /**
     * binder가 정상적으로 binder 되었는지 체크하기 위한 메소드.
     * binder 체크 후 {@link RspermScreenCapture} 사용.
     *
     * @return
     */
    public synchronized boolean isBinderAlive() {
        return mPerm != null && mPerm.isBinded();
    }

    /**
     * rsperm bind
     *
     * @return {RSPERM_BINDED, RSPERM_BIND_SUCCESS, RSPERM_BIND_FAIL}이 return 되며, 실패시에는 사용 불과.
     */
    public synchronized int bind() {
        if (isBinderAlive())
            return RSPERM_BINDED;

        int ret = RSPERM_BIND_SUCCESS;

        final int timeout = 1000;
        String rsperm;
        // find installed rsperm.apk
        try {
            rsperm = new RspermCheck(mContext.getPackageManager(), new LG318RSpermPackageValidator()).installed("com.rsupport.rsperm");

        } catch (Exception e) {
            return RSPERM_BIND_FAIL;
        }
        RLog.d("rsperm packageName : " + rsperm);

        if (rsperm == null) {
            return RSPERM_BIND_FAIL;
        }

        if (isExcludeRSPermPkg(rsperm)) {
            RLog.d("rsperm packageName : " + rsperm);
            return RSPERM_BIND_FAIL;
        }

        RLog.v("------------------------------ Rsperm bind() 1");

        if (!bindRSperm(rsperm, timeout)) { // rsperm 사용.
            Log.e(TAG, "fail bindRSperm(" + rsperm + ")");

            RLog.v("------------------------------ Rsperm bind() 2");

            if (!bindRSperm(null, timeout)) { // Rooting or USB 사용.
                RLog.e("fail bindRSperm(null)");
                unbind();
                return RSPERM_BIND_FAIL;
            }
        }

        return ret;
    }

    private boolean isExcludeRSPermPkg(String rsperm) {
        return "com.rsupport.rsperm.ntt".equals(rsperm) ||
                "com.rsupport.skt.engine.tservice".equals(rsperm) ||
                "com.rsupport.rsperm.aa.second".equals(rsperm) ||
                "com.rsupport.rsperm.aa.first".equals(rsperm);
    }

    private boolean bindRSperm(String pkgName, int timeout) {
        RLog.i("try binding with " + pkgName);
        if (mPerm != null)
            return true;

        IRSPerm binder;
        boolean ret = false;
        if (pkgName == null) {
            RLog.v("------------------------------ Rsperm bind() 3");
            binder = new UDSperm(mContext, true); // try with a process which executed by adb shell.
            ret = binder.bind(null);
            LLog.w("binder.bind(null) ret : " + ret);

            if (ret == false) {
                // try again with su
                mProcess = UDSperm.exec(mContext.getApplicationInfo().nativeLibraryDir + "/liblauncher.so", true);
                if (mProcess != null) {
                    ret = binder.bind(null);
                    IS_ROOTED = true;
                }
            }

        } else {
            RLog.v("------------------------------ Rsperm bind() 4");
            IS_ROOTED = false;
//			binder = new APKRSperm(mContext);
            binder = new APKRSperm(mContext, mBinderListener);
            ret = binder.bind(pkgName);
        }

        if (!ret) {
            RLog.e("binding with perm failed. " + ret);
            return false;
        }
        RLog.e("binding with perm success. " + ret);

        RLog.v("------------------------------ Rsperm bind() 5");
        for (int i = 0; i < timeout; i += 100) {
            if (binder.isBinded())
                break;
            SystemClock.sleep(100);
        }

        RLog.v("------------------------------ Rsperm bind() 6");


        Log.d(TAG, "isBinded : " + binder.isBinded());
        if (!binder.isBinded()) {
            RLog.v("------------------------------ Rsperm bind() 6.5");
            binder.unbind();
            return false;
        }

        int sdkver = android.os.Build.VERSION.SDK_INT;

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) { // ICS MR1일 경우(SDK 15) SDK 14로 처리한다.
            sdkver = android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        }

        if (Build.VERSION.SDK_INT > 21) { // Lollipop 이상일 경우 SDK 21로 처리한다.
            sdkver = Build.VERSION_CODES.LOLLIPOP;
        }
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT <= 15) {
            sdkver = 14;
        }
        if (Build.VERSION.SDK_INT == 9) {
            sdkver = 9;
        }

        String soPath;
        // Android N 버전에서 pdk21 로드시 ssl.so 가 없다며 오류발생  롤리팝 이후로 캡쳐시 미디어프로젝션을 이용하기 때문에
        // so 내 캡쳐 기능 사용 안함 제어권한을 위해 so 로드만 함
        // 2019.11.11(kwcho) - Android 24부터 rspdk 에서 화면 캡쳐 api native 에서 사용할 수 없어 제어 권한만 사용한다.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            soPath = String.format(mContext.getApplicationInfo().nativeLibraryDir + "/librspdk%d.so", 24);

        } else {
            soPath = String.format(mContext.getApplicationInfo().nativeLibraryDir + "/librspdk%d.so", sdkver);

        }

        if (!binder.loadJni(soPath)) {
            RLog.e("loading so failed. " + soPath);
            return false;
        }
        RLog.i("binded with perm.");
        RLog.v("------------------------------ Rsperm bind() 7");


        mPerm = binder;

        return true;
    }

    /**
     * 프로그램 종료시에 사용한 rsperm을 unbind.
     */
    public synchronized void unbind() {
        if (mPerm != null) {
            mPerm.unbind();
            mPerm = null;
        }
        if (!mBinder.isBinderAlive()) {
            mPerm = null;
        }
    }

}