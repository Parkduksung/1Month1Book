package com.rsupport.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.rsupport.util.rslog.MLog;

public class Package {

	public static String installed(Context cxt, String[] pkgs, int offset) {
		PackageManager pm = cxt.getPackageManager();
		for (int i = offset; i < pkgs.length; ++i) {
			try {
				PackageInfo info = pm.getPackageInfo(pkgs[i], PackageManager.GET_META_DATA);
				MLog.v("rsperm : " + info.toString());
				return pkgs[i];
			} catch (NameNotFoundException e) {
			}
		}

		return null;
	}

}
