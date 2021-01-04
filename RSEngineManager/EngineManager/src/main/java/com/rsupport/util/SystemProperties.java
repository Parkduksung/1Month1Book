package com.rsupport.util;

import java.lang.reflect.Method;

public class SystemProperties {

	static public String get(String key, String defVal) {
		try {
			return (String)_get.invoke(null, key, defVal);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defVal;
	}
	static Method _get;
	static {
		try {
			_get = Class.forName("android.os.SystemProperties")
					.getMethod("get", new Class[] {String.class, String.class});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
