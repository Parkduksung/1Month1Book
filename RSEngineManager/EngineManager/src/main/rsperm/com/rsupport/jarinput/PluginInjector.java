package com.rsupport.jarinput;

import java.lang.reflect.Method;

import android.content.Context;
import android.util.Log;

import com.rsupport.util.RsupApplication;
import com.rsupport.util.rslog.MLog;

import dalvik.system.PathClassLoader;

// currently inject agent only.
public class PluginInjector implements IMonkeyHandler
{
	static  Method injectWithBytes;
	static  Method injectWithPrimitive;
//	static  Method jd2n;
	static  Object obj;
	
	// "pluginApk|inject|/data/app/com.rsupport.jarinput-1.apk|com.rsupport.jarinput.ex.MonkeyAgentEx";
	public PluginInjector(String apkInfo) throws Exception
	{
		String args[] = apkInfo.split("\\|");
		Object aObj = null;
		Class<?> libClss = null;
        if ("inject".equals(args[1]))
        {	// "pluginApk|inject|/data/app/aa.bb.cc-1.apk|com.test.aa.bb.Loader";
        		
//        		if ("using reflection" != null) {
//	        		Class<?> pcl = Class.forName("dalvik.system.PathClassLoader");
//	    			
//	    			Object cl = pcl
//	    					//.getConstructor(String.class, ClassLoader.class)
//	    					.getConstructor(String.class, Class.forName("java.lang.ClassLoader"))
//	    					.newInstance( new Object[]{args[2], RsupApplication.context.getClassLoader()} );
//	    			libClss = (Class<?>) pcl.getMethod("loadClass", String.class).invoke(cl, args[3]);
//        		}
//        		
//        		else 
        		{
					ClassLoader cl = new PathClassLoader(args[2], RsupApplication.context.getClassLoader());
					libClss = cl.loadClass(args[3]);
        		}
				aObj = libClss.newInstance();
				Method init = libClss.getMethod("init", new Class[] { Context.class, Method.class } );
				init.invoke(aObj,  RsupApplication.context);
				PluginInjector.obj = aObj;
        }
       
		try {
			PluginInjector.injectWithBytes = libClss.getMethod("handle", new Class[] { byte[].class, int.class, int.class });
		} catch (Exception e) {
		}
		try {
			PluginInjector.injectWithPrimitive = libClss.getMethod("handle", new Class[] { int.class, int.class, int.class, int.class, int.class,
					int.class });
		} catch (Exception e) {
		}
//        try {
//        	PluginInjector.jd2n = libClss.getMethod("handle", new Class[] { byte[].class, int.class});
//        }
//        catch(Exception e) {}
        
		// ---------------------------- defeat dex2jar
		Context context = RsupApplication.context;
		String szPackage = null;
		try {
			szPackage = context.getPackageName();
			if (szPackage == null)
				throw new android.util.AndroidException();
		} catch (android.util.AndroidException e) {
		} catch (Exception e) {
			MLog.e(szPackage + ", " + e.toString());
		}

		try {
			szPackage = context.getPackageName();
			if (szPackage == null)
				throw new android.util.AndroidException();
		} catch (android.util.AndroidException e) {
			MLog.e(szPackage + ", " + e.toString());
		}
		// ---------------------------- defeat dex2jar
	}
	

	@Override
	public void handle(int action, int i1, int i2, int i3, int i4, int i5) {
		try {
			injectWithPrimitive.invoke(PluginInjector.obj, action, i1, i2, i3, i4, i5);
		} catch (Exception e) {
			MLog.e(e.toString());
		}
	}

	@Override
	public void handle(byte[] bb, int offset, int len) {
		try {
			injectWithBytes.invoke(PluginInjector.obj, bb, offset, len);
		} catch (Exception e) {
			MLog.e(e.toString());
		}
	}
//	static void injectWithBytes(byte[] data, int offset, int len) {
//		try {
//			injectWithBytes.invoke(PluginInjector.obj, data, offset, len);
//		} catch (Exception e) {
//			log.e(e.toString());
//		}
//	}
//
//	static void injectWithPrimitive(int action, int i1, int i2, int i3, int i4, int i5) {
//		try {
//			injectWithPrimitive.invoke(PluginInjector.obj, action, i1, i2, i3, i4, i5);
//		} catch (Exception e) {
//			log.e(e.toString());
//		}
//	}
//
//	static int jd2n(byte[] bulk, int len) {
//		try {
//			return (Integer) jd2n.invoke(PluginInjector.obj, bulk, len);
//		} catch (Exception e) {
//			log.e(e.toString());
//		}
//		return 0;
//	}
	
	static boolean isInjectable() {
		return PluginInjector.injectWithBytes != null || PluginInjector.injectWithPrimitive != null;
	}
}

