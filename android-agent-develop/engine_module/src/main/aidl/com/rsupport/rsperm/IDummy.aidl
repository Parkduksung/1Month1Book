
package com.rsupport.rsperm;

import com.rsupport.rsperm.IDummyCallback;

interface IDummy
{
	void 	registerCallback(IDummyCallback cb);
	int		testDummy1(in String jniPath);
	int		testDummy2(in byte[] bulk, int len);
	int		setFlags(int flags);
	byte[] 	query(in byte[] bulk, int len);
	ParcelFileDescriptor	getFile(String ashName, int length, int reserved1, int reserved2);
	int						capture(int w, int h, int flags);
	
	void injectWithPrimitive(int action, int i1, int i2, int i3, int i4, int i5);
	void injectWithBytes(in byte[] datas, int offset, int len);
	
	boolean putSFloat(String name, float value);
	boolean putSInt(String name, int value);
	boolean putSLong(String name, long value);
	boolean putSString(String name, String value);
	
	boolean putGFloat(String name, float value);
	boolean putGInt(String name, int value);
	boolean putGLong(String name, long value);
	boolean putGString(String name, String value);

    boolean createVirtualDisplay(String name, int w, int h, int dpi, in Surface surf, int flag);
    int		capture2(in Surface surface, int w, int h, int dpi, int flags);
    boolean initInjector(boolean create);
    List getRunningProcesses();

    boolean putFloat(String name, float value);
    boolean putInt(String name, int value);
    boolean putLong(String name, long value);
    boolean putString(String name, String value);
    int update(in Bitmap screenshot, int stretchRate, int rotation);
}
