package com.rsupport.util;

import java.io.FileDescriptor;

public class FileDescShare {
	static {
		try {
			System.loadLibrary("_ashmem");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	private static String TAG = "FileDescShare";

	public static boolean getStdioFileDescriptor(FileDescriptor sockfd, FileDescriptor fdin, FileDescriptor fdout, FileDescriptor fderr) {
		return native_getstdfds_from_socket(sockfd, fdin, fdout, fderr);
	}
	
	public static FileDescriptor getSharedFileDescriptor(FileDescriptor fduds, String udsaddr) {
		return native_getfd_from_socket(fduds, udsaddr);
	}
	
	public static int getSharedFileDescriptors(FileDescriptor sockfd, FileDescriptor[] outfds) {
		return native_getfds_from_socket(sockfd, outfds);
	}
	
	private static native int     native_getfds_from_socket(FileDescriptor sockfd, FileDescriptor[] outfds);
	private static native boolean native_getstdfds_from_socket(FileDescriptor sockfd, FileDescriptor fdin, FileDescriptor fdout, FileDescriptor fderr);
	private static native FileDescriptor native_getfd_from_socket(FileDescriptor sockfd, String udsaddr);
}
