/*
package com.rsupport.rsperm;

import java.util.List;

import com.rsupport.rsperm.IDummy;
import com.rsupport.rsperm.IDummyCB;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class SrnCaller extends Service
{
	private final static String TAG = "RsupS";
	private int mFlags = 0x0001;

	public void onCreate() { Log.v(TAG, "create");}

	public void onDestroy() {
		Log.v(TAG, "destroy");
		if ((mFlags & 0x2) != 0) stopSelf();
		super.onDestroy();
		if ((mFlags & 0x1) != 0) android.os.Process.killProcess(android.os.Process.myPid());
	}
//	public void onConfigurationChanged(Configuration newConfig) {
//		Log.v(TAG, "config changed");
//		if ((mFlags & 0x4) != 0) newConfig.setToDefaults();
//		super.onConfigurationChanged(newConfig);
//	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.v(TAG, "bind");
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.v(TAG, "unbind");
		if (!super.onUnbind(intent)) return false;
		mBinderCB = null;
		return true;
	}

	// --------------- callback: called from native ---------------  
	public void je01(byte[] data) { // send bulk data to service consumer.
		if (mBinderCB == null) { Log.w(TAG, "0cbi." + data.length); return; }
		try {
			mBinderCB.onEvent(data);
		} catch (RemoteException e) {
			mBinderCB = null;
			e.printStackTrace();
		}
	}

	public int je02(byte[] data) { // send bulk data to service consumer & get int from service consumer.
		if (mBinderCB == null) { Log.w(TAG, "0cbi." + data.length); return -2; }
		try {
			return mBinderCB.getInt(data);
		} catch (RemoteException e) {
			mBinderCB = null;
			e.printStackTrace();
		}
		return -1;
	}

	public int je03() { // get current network state.
		ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo newNI = connMgr.getActiveNetworkInfo();
		if (newNI == null) return -1;
		return newNI.getTypeName().equals("WIFI") ? 11 : 3;
	}

	// ---------------- service binder ----------------
	private IDummyCB    mBinderCB = null;
	private IDummy.Stub mBinder = new IDummy.Stub() {

		public int testDummy1(String soPath, int flags) { // load library
			if (flags < 0) {
				return loadLibrary(soPath) ? 0:-1;
			}
			
			int old = mFlags;
			mFlags = flags;
			return old;
		}

		public int testDummy2(byte[] bulk, int len) { return jd2n(bulk, len);} // java 2 native.
		public void register(IDummyCB cb) { mBinderCB = cb;}
	};

	// ---------------- signature check ----------------
	private String getProcessNameFromPid(int givenPid) {
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> list = am.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo info : list) {
			if (info.pid == givenPid) 
				return info.processName;
		}
		return null;
	}
	 
	private final int RSUP_SIGNNATURE = 971023670;
	private void checkSignatureByPid(int callerPid) {
		PackageManager pm = getApplicationContext().getPackageManager();
		String szPackage = null;
		try {
			szPackage = getApplicationContext().getPackageName();
			PackageInfo info = pm.getPackageInfo(szPackage, PackageManager.GET_SIGNATURES);
			int SELF_SIGNITURE = info.signatures[0].hashCode();
			
			szPackage = getProcessNameFromPid(callerPid);
			info = pm.getPackageInfo(szPackage, PackageManager.GET_SIGNATURES);
			android.content.pm.Signature sig[] = info.signatures;
			for (int count = sig.length; --count >= 0;) {
				int callerSigniture = sig[count].hashCode();
				if (callerSigniture == RSUP_SIGNNATURE || callerSigniture == SELF_SIGNITURE)
					return;
			}
		} catch (NameNotFoundException e) {
			Log.e(TAG, szPackage + ": " + e.toString());
		} catch (Exception e) {
			Log.e(TAG, szPackage + ": " + e.toString());
			e.printStackTrace();
		}
		throw new SecurityException();
	}


	// ---------------- native parts ----------------
	native int		jd2n(byte[] bulk, int len);
	native boolean	jscb(Object obj);
	
	private boolean loadLibrary(String soPath) {
		
		checkSignatureByPid(Binder.getCallingPid()); 

		try {
			System.load(soPath);
			return jscb(this);
		} catch (UnsatisfiedLinkError e) {
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}

*/