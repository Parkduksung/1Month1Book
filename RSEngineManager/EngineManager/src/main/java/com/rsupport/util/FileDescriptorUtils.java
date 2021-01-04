package com.rsupport.util;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.Socket;

import android.os.ParcelFileDescriptor;

/*

/libcore/luni/src/main/java/java/net/Socket.java

 */
public final class FileDescriptorUtils {
 
	static public FileDescriptor getFileDescriptor(int fd) {
		FileDescriptor jfd = new FileDescriptor();
	    try {
	    	Field f = FileDescriptor.class.getDeclaredField("descriptor");
	    	f.setAccessible(true);
	    	f.setInt(jfd, fd);
	    	return jfd;
	    }
	    catch(Exception e) {
	    }
	    return null;
	}
	
	// for SDK10
	static public ParcelFileDescriptor getParcelFileDescriptor(FileDescriptor fd) {
	    try {
			Constructor<?> ctor = ParcelFileDescriptor.class.getConstructor(new Class[]{FileDescriptor.class});
			ctor.setAccessible(true);
			ParcelFileDescriptor pfd = (ParcelFileDescriptor)ctor.newInstance(fd);
			return pfd;
	    }
	    catch(Exception e) {
	    }
	    return null;
	}
	

	private static Field __fd;
    static {
        try {
            __fd = FileDescriptor.class.getDeclaredField("fd");
            __fd.setAccessible(true);
        } catch (Exception ex) {
            __fd = null;
        }   
    }   
 
    /** 
     * Get Input Handle from Socket.
     */
    public static int getInputDescriptor(Socket s) {
        try {
            FileInputStream in = (FileInputStream)s.getInputStream();
            FileDescriptor fd = in.getFD();
            return __fd.getInt(fd);
        } catch (Exception e) { } 
        return -1; 
    }   
 
    /** 
     * Get Output Handle from Socket.
     */
    public static int getOutputDescriptor(Socket s) {
        try {
            FileOutputStream in = (FileOutputStream)s.getOutputStream();
            FileDescriptor fd = in.getFD();
            return __fd.getInt(fd);
        } catch (Exception e) { } 
        return -1; 
    }
    
}