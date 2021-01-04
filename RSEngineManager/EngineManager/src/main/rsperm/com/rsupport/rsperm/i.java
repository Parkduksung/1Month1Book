package com.rsupport.rsperm;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.Surface;

import com.rsupport.jarinput.IMonkeyHandler;
import com.rsupport.jarinput.MonkeyAgent;
import com.rsupport.util.Config;
import com.rsupport.util.Plugin;
import com.rsupport.util.rslog.MLog;

import java.io.FileDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;


public class i extends android.app.Service {
	
	// 0x0100 : native.v2 : ashmem, capture
	// 0x0300 : native.v3 : v2 features + create virtual display, capture to surface.
	private final static int versionFlags = 0x0300;

	// 0x0001 : kill myself.
	// 0x0002 : stopSelf
	// 0x0004 : setToDefault
	public static int flags = 0 
			| 0x0001 // kill myself
			| 0x0002 // stopSelf
			| 0x0004 // setToDefault
			| versionFlags
			;


	public void onCreate() {
		super.onCreate();
		MLog.v("%s created: flags=%08x", getPackageName(), flags);
	}

	public void onDestroy() {
		MLog.v(getPackageName() + " destroyed");
		if ((flags & 0x2) != 0)
			stopSelf();
		super.onDestroy();
		mMonkey = null;
		if ((flags & 0x1) != 0)
			android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	public IBinder onBind(Intent intent) {
        Config.clearPkgSignature();
		MLog.v(getPackageName() + " binded.");
		if (Config.DBG) MLog.d("Intent.onBind="+intent.toString());
		return mBinder;
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		Config.clearPkgSignature();
		if (Config.DBG) MLog.d("Intent.onRebind=" + intent.toString());
	}

	@Override
	public boolean onUnbind(Intent intent) {
		MLog.v(getPackageName() + " unbinded.");
		if (Config.DBG) MLog.d("Intent.onUnbind=" + intent.toString());
		mBinderCB = null;

		// clear natives
		try { native_getASM(null, 0,0,0); } // clear ASM
		catch (Throwable e) { MLog.v("getASM is not linked."); }

		try {
			byte[] cmd = { 3 }; // 3 = JNI_AgentShutdown
			MLog.i("JNI_AgentShutdown called");
			int ret = jd2n(cmd, cmd.length);
			MLog.i("JNI_AgentShutdown end : " + ret);
		} catch (Throwable e) { MLog.v(e.toString()); }

		try {
			this.createVirtualDisplay(null, 0, 0, 0, null, 0);
		} catch (Throwable e) {
			MLog.w(e.toString());
		}

        Config.clearPkgSignature();
		return super.onUnbind(intent);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		MLog.v("config changed");
		if ((flags & 0x4) != 0)
			newConfig.setToDefaults();
		super.onConfigurationChanged(newConfig);
	}

	// --------------- service related
	// callback(called from jni module)
	public void je01(byte[] data) { // post data to RSUI.
		if (mBinderCB == null) {
			MLog.w("null binderCB: len." + data.length);
			return;
		}
		try {
			mBinderCB.onEvent(data);
		} catch (RemoteException e) {
			mBinderCB = null;
			MLog.e(e.toString());
		}
	}

	public int je02(byte[] data) { // post data to RSUI & get return value.
		if (mBinderCB == null) {
			MLog.w("null binderCB: len." + data.length);
			return -1;
		}
		try {
			return mBinderCB.getInt(data);
		} catch (RemoteException e) {
			mBinderCB = null;
			MLog.e(e.toString());
		}
		return -1;
	}
	
	//
	// ---------------- service binder: called from RSUI ----------------
	//
	private VirtualDisplay mVDisp = null;
	private IDummyCallback mBinderCB = null;
	private IDummy.Stub mBinder = new IDummy.Stub() {

		@Override
		public void registerCallback(IDummyCallback cb) {
			
			Config.checkPkgSignatureEx();
			mBinderCB = cb;
		}

		// return -1: error.
		// return  0: libpsrn loading ok.
		// return  0: submodule loading ok(postfix with ":s")
		@Override
		public int testDummy1(String jniPath) { // loadNative

			Config.checkPkgSignatureEx();
			return i.this.loadJni(jniPath) ? 0 : -1;
		}

		@Override
		public int testDummy2(byte[] bulk, int len) { // data2Native

			Config.checkPkgSignatureEx();
			if (Config.SKT == false && (int) bulk[0] == RCCommander.JNI_RCCMD_SHELL)
				return RCCommander.procRCCommand(i.this);

			return jd2n(bulk, len); // jni-call
		}
		
		@Override
		public int setFlags(int newFlags) {
			
			Config.checkPkgSignatureEx();
			int old = flags;
			flags = newFlags | versionFlags;
			MLog.v("flags: %08x->%08x", old, newFlags);
			return old;
		}

		@Override
		public byte[] query(byte[] bulk, int len) throws RemoteException {
			
			Config.checkPkgSignatureEx();
			return i.this.query(bulk, len);
		}

		
		//-----------------------------[ FOR Litecam & external encoder style]
		@Override
		public ParcelFileDescriptor getFile(String ashName, int length, int reserved1, int reserved2) throws RemoteException {
			
			Config.checkPkgSignatureEx();
			try {
				// create ashmemory with given name.
				FileDescriptor fd = native_getASM(ashName, length, reserved1, reserved2);
				if (fd.valid()) 
					return getPFD(fd);
			} catch (Exception e) {
				throw new RuntimeException("getFile: " + e.toString());
			}
			return null;
		}

		@Override
		public int capture(int w, int h, int flags) throws RemoteException {
			
			Config.checkPkgSignatureEx();
			return  native_capture(w,h,flags);
		}

		@Override
		public void injectWithPrimitive(int action, int i1, int i2, int i3, int i4, int i5) throws RemoteException {
			
			Config.checkPkgSignatureEx();
			i.injectWithPrimitive(action, i1, i2, i3, i4, i5);
		}

		@Override
		public void injectWithBytes(byte[] datas, int offset, int len) throws RemoteException {
			
			Config.checkPkgSignatureEx();
			i.injectWithBytes(datas, offset, len);			
		}

		// convert fd to ParcelFileDescriptor.
		private ParcelFileDescriptor getPFD(FileDescriptor fd) throws NoSuchMethodException, Exception {
			
			Config.checkPkgSignatureEx();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
				Method dup = ParcelFileDescriptor.class.getMethod("dup", FileDescriptor.class);
				return (ParcelFileDescriptor)dup.invoke(null, fd);
			}
			else 
			{
				Constructor<ParcelFileDescriptor> cont = ParcelFileDescriptor.class.getConstructor(FileDescriptor.class);
				return cont.newInstance(fd);
			}
		}


		//Secure system settings
		@Override
		public boolean putSFloat(String name, float value) {
			
			Config.checkPkgSignatureEx();
			if (Config.SKT) return false;
			return Settings.Secure.putFloat(getContentResolver(), name, value);
		}
		
		@Override
		public boolean putSInt(String name, int value) {
			
			Config.checkPkgSignatureEx();
			if (Config.SKT) return false;
			return Settings.Secure.putInt(getContentResolver(), name, value);
		}
		
		@Override
		public boolean putSLong(String name, long value) {
			
			Config.checkPkgSignatureEx();
			if (Config.SKT) return false;
			return Settings.Secure.putLong(getContentResolver(), name, value);
		}
		
		@Override
		public boolean putSString(String name, String value) {
			
			Config.checkPkgSignatureEx();
			if (Config.SKT) return false;
			return Settings.Secure.putString(getContentResolver(), name, value);
		}
		
		//Global system settings
		@Override
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		public boolean putGFloat(String name, float value) {
			
			Config.checkPkgSignatureEx();
			if (Config.SKT) return false;
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) return false;
			return Settings.Global.putFloat(getContentResolver(), name, value);
		}
		
		@Override
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		public boolean putGInt(String name, int value) {
			
			Config.checkPkgSignatureEx();
			if (Config.SKT) return false;
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) return false;
			return Settings.Global.putInt(getContentResolver(), name, value);
		}
		
		@Override
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		public boolean putGLong(String name, long value) {
			
			Config.checkPkgSignatureEx();
			if (Config.SKT) return false;
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) return false;
			return Settings.Global.putLong(getContentResolver(), name, value);
		}
		
		@Override
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		public boolean putGString(String name, String value) {
			
			Config.checkPkgSignatureEx();
			if (Config.SKT) return false;
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) return false;
			return Settings.Global.putString(getContentResolver(), name, value);
		}


		@Override
		@TargetApi(Build.VERSION_CODES.KITKAT) // for SKT HoujaHand
		public boolean createVirtualDisplay(String name, int w, int h, int dpi, Surface surf, int flag) throws RemoteException {
			
			Config.checkPkgSignatureEx();
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return false;
			return i.this.createVirtualDisplay(name, w, h, dpi, surf, flag);
		}

		@Override // for SKT HoujaHand
		public int capture2(Surface surface, int w, int h, int dpi, int flags) throws RemoteException {
			
			Config.checkPkgSignatureEx();
			return native_capture2(surface, w,h, dpi, flags);
		}

		@Override
		public boolean initInjector(boolean create) {
			
			Config.checkPkgSignatureEx();
			try {
				if (create && mMonkey == null)
					mMonkey = new MonkeyAgent();
				else
					mMonkey = null;
				return true;
			}
			catch(Exception e) {
				MLog.e("input init(%b) failed: %s", create, e.toString());
				return false;
			}
		}
	};
	// END-OF-INTERFACE-IMPL.






	boolean loadJni(String soPath) {
		int ret = Plugin.loadLibrary(soPath); // ret : 1 on OK.
		if (ret < 0) return false;
	
		if (ret == Plugin.LOAD_MAINMODULE) {
			try {
		        if (mMonkey == null)
		        	mMonkey = new MonkeyAgent();
			}
			catch(Exception e) {
				MLog.e("input init failed: " + e.toString());
			}

			try {
				return jscb(this, getApplicationContext());
			}
			catch(UnsatisfiedLinkError e) {
				MLog.w("jscb is not linked: " + e.toString());
				return false;
			}
		}
		return true;
	}
	//----------------------------- VIRTUAL DISP
	@TargetApi(Build.VERSION_CODES.KITKAT)
	private boolean createVirtualDisplay(String name, int w, int h, int dpi, Surface surf, int vdflag) throws RemoteException {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
			return false;

		try {
			return 1 == i.native_createVirtualDisplay(surf, w, h, dpi, vdflag);
		} catch (Throwable e) {
			MLog.v(e.toString());
		}

		if (w == 0 && h == 0) {
			if (this.mVDisp != null) {
				this.mVDisp.release();
				this.mVDisp = null;
			}
			return true;
		}

		if (this.mVDisp != null) {
//				log.i("resize: %d x %d", w. h);
//				i.this.mVDisp.resize(w, h, dpi);
//				i.this.mVDisp.setSurface(surf);
//				return true;
			MLog.e("already vdisp installed.");
			return false;
		}
		DisplayManager dispMgr = (DisplayManager) this.getSystemService(Context.DISPLAY_SERVICE);
		this.mVDisp = dispMgr.createVirtualDisplay(name, w, h, dpi, surf, vdflag);
		return (this.mVDisp != null);
	}

	// ---------------------------- INJECT (Version2): new input method test.
	private static IMonkeyHandler mMonkey;
	public static void injectWithBytes(byte[] datas, int offset, int len) { // scroll for ICS+
		//if (log.DBG) log.d("DEBUG - array size: " + datas.length);
		mMonkey.handle(datas,  offset, len);
	}

	public static void injectWithPrimitive(int action, int i1, int i2, int i3, int i4, int i5) { // scroll for ICS+
		//if (log.DBG) log.d("DEBUG - for object leak avoidance");
		mMonkey.handle(action, i1, i2, i3, i4, i5);
	}

	// ---------------- native parts ----------------
	// ver1: flags = 0x00xx
	static public native int         	jd2n(byte[] bulk, int len);
	static public native boolean 		jscb(Object obj, Context cxt);
	static public native byte[]    		query(byte[] bulk, int len);

	// ver2: flags = 0x01xx
	static native FileDescriptor native_getASM(String name, int size, int reserved1, int reserved2);
	static native int native_capture(int w, int h, int flags);
	
	// ver 3: flags = 0x03xx
	static native int native_createVirtualDisplay(Surface surface, int w, int h, int dpi, int flags);
	static native int native_capture2(Surface surface, int w, int h, int dpi, int flags);
	
}
