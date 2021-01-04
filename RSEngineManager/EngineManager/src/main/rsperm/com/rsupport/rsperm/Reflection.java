package com.rsupport.rsperm;

import android.content.Context;
import android.os.IBinder;

import com.rsupport.util.rslog.MLog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Reflection {

	static public Class<?> getClass(String name) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			MLog.e(e.toString());
		}
		return null;
	}

	static public void dumpClass(String name) {
		dumpClass( getClass(name) );
	}

	static public void dumpClass(Class<?> c) {
		if (c == null) return ;
		for (Method method : c.getMethods()) {
			boolean isStatic = Modifier.isStatic(method.getModifiers());
			MLog.i((isStatic ? "[S]" : "[M]") + method.getName());
			MLog.i("  * " + method.getReturnType().getName());
			for (Class<?> p : method.getParameterTypes())
				MLog.i("  - " + p.getName());

		}

		for (Field f : c.getFields()) {
			MLog.i("[F] " + f.getName() + ": " + f.getType().getName());
		}
	}


	 static public Method getMethod(Class<?> c, String name, Class<?>... clses) {
		 try {
			 return c.getMethod(name, clses);
		 }
		 catch(NoSuchMethodException e) {
			 MLog.w("There is no %s", name);
		 }
		 return null;
	 }


	 static public Object getPowerManager() {

		 Object powerManager = null;
		 try {
			Object pmBinder = Class.forName("android.os.ServiceManager")
					.getMethod("getService", String.class)
					.invoke(null, new Object[] { Context.POWER_SERVICE });
			powerManager = Class.forName("android.os.IPowerManager$Stub")
					.getMethod("asInterface", IBinder.class)
					.invoke(null, new Object[] { pmBinder });
		 }
		 catch(Exception e) {
			 MLog.e(e.toString());
		 }
		 if (powerManager == null)
			 MLog.e("null powerManager!");

		 return powerManager;
	 }

}
