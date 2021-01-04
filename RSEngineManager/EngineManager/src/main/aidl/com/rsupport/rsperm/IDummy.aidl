
package com.rsupport.rsperm;

import com.rsupport.rsperm.IDummyCallback;

interface IDummy
{
	void 	registerCallback(IDummyCallback cb);
	int		testDummy1(in String jniPath); 			// loadLibrary
	int		testDummy2(in byte[] bulk, int len); 	// data2Native
	int		setFlags(int flags);
	
	byte[] 	query(in byte[] bulk, int len); // transact id: 5, 103(rsperm v1)
	
	// for separated libsrn.so : rsperm v2+
	ParcelFileDescriptor	getFile(String ashName, int length, int reserved1, int reserved2);
	int						capture(int w, int h, int flags);
	
	void injectWithPrimitive(int action, int i1, int i2, int i3, int i4, int i5);
	void injectWithBytes(in byte[] datas, int offset, int len);
	
	// 2014.02.14 Interface related SecureSetting: SKT isn't support(Permission is not included).
	boolean putSFloat(String name, float value);
	boolean putSInt(String name, int value);
	boolean putSLong(String name, long value);
	boolean putSString(String name, String value);
	
	boolean putGFloat(String name, float value);
	boolean putGInt(String name, int value);
	boolean putGLong(String name, long value);
	boolean putGString(String name, String value);

	// 2014.07.18: rsperm v3: SKT isn't support: SKT isn't support(Permission is not included).
	boolean createVirtualDisplay(String name, int w, int h, int dpi, in Surface surf, int flag);
	int		capture2(in Surface surface, int w, int h, int dpi, int flags);
	boolean initInjector(boolean create);
}
