package com.rsupport.util;


import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.rsupport.util.rslog.MLog;


public class RsupApplication{

	public static Context 			context;

	public void onCreate(Context context) {
		this.context = context.getApplicationContext();

		Config.fixAllowedSignature(context.getPackageName());

		ApplicationInfo ai = context.getApplicationInfo();

		//MLog.i(ai.primaryCpuAbi);
        String nativeLibDir = ai.nativeLibraryDir;
        MLog.i("libdir=[%s]", nativeLibDir);
        if (nativeLibDir.contains("/arm64") )
            MLog.w("This is 64bit process!!!");

		if (Config.DBG) {
			for (int i=0; i<3; ++i)
				MLog.e("*** This is a debug build!!! Do not release this!!! ***");
		}
		
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo piSelf = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
			MLog.i(piSelf.packageName + ".v" + String.valueOf(piSelf.versionCode));
		} catch (Exception e) {
			MLog.w(e.toString());
			return ;
		}

        int sigMatch = pm.checkSignatures(1000, android.os.Process.myUid());
		if (PackageManager.SIGNATURE_MATCH != sigMatch)
        	MLog.e("Signature mismatch with uid.1000: %d", sigMatch);

	}
}
