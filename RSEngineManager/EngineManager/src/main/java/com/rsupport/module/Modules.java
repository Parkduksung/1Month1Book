package com.rsupport.module;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.preference.PreferenceManager;
import android.util.Log;

// lib/librspdk?.so
// assets/librspdk?.so

// dump res/raw/librspdk.so to /data/data/package-name/files/librspdk.so
public class Modules {
	private final static String TAG = "Srn30Modules";
	
	public static boolean prepare(Context cxt) {
		if (!isFirstTimeAfterUpgrade(cxt)) return true;
//		final boolean isX86 = android.os.Build.CPU_ABI.startsWith("x86");
		int rawId = 0;//isX86 ? R.raw.librspdk_x86:R.raw.librspdk;
		return rawToFile(cxt, "librspdk.so", rawId);
	}
	
	private static boolean isFirstTimeAfterUpgrade(Context cxt)
	{
		int currentVersion = -1;
		try {
			PackageInfo info = cxt.getPackageManager().getPackageInfo(cxt.getPackageName(), 0);
			currentVersion = info.versionCode;
		}
		catch(Exception e) {
			Log.e(TAG, e.toString());
			return true; // force to create so module.
		}
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(cxt);
		int lastVersion = prefs.getInt("versionCode", 0);
		if (currentVersion != lastVersion) {
			prefs.edit().putInt("versionCode", currentVersion).commit();
			return true;
		}
		return false;
	}
	
	private static boolean rawToFile(Context cxt, String fileName, int rawId) {
		//String soPath = cxt.getApplicationInfo().dataDir + "/librspdk.so";
		String soPath = cxt.getFilesDir().getAbsolutePath()+ "/" + fileName;
		InputStream is = cxt.getResources().openRawResource(rawId);

		File so = new File(soPath);
		so.delete();

		boolean ret = false;
		Log.v(TAG, "new "+ so.toString());
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(soPath);
			byte[] buf = new byte[1024*8];
			
			while (true) {
				int len = is.read(buf);
				if (len == -1) break;
				fos.write(buf, 0, len);
			}
			ret = true;
			so.setExecutable(true, false);
			so.setReadable(true, false);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}

		try {
			if (fos != null) fos.close();
			if (is != null) is.close();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
		if (!ret) so.delete();
		return ret;
	}
}

