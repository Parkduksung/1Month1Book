package com.rsupport.util;

//
public class conv {
//	public static int intToByte4(int n, byte[] dst, int offset) {
//		dst[offset+0] = (byte) (n);
//		dst[offset+1] = (byte) (n >> 8);
//		dst[offset+2] = (byte) (n >> 16);
//		dst[offset+3] = (byte) (n >> 24);
//		return offset+4;
//	}
//	public static int intToByte2(int n, byte[] dst, int offset) {
//		dst[offset+0] = (byte) (n);
//		dst[offset+1] = (byte) (n >> 8);
//		return offset+2;
//	}
//
// 	public static int shortToByte2(short n, byte[] dst, int offset) {
//		dst[offset+0] = (byte) (n);
//		dst[offset+1] = (byte) (n >> 8);
//		return offset+2;
//	}
// 	
//
 	public static int byte4Toint(byte[]src, int offset) {
 		int ret =          (src[offset+3] & 0xff);
 		ret = (ret << 8) | (src[offset+2] & 0xff);
 		ret = (ret << 8) | (src[offset+1] & 0xff);
 		ret = (ret << 8) | (src[offset+0] & 0xff);
 		return ret;
 	}
 	public static int byte2Toint(byte[]src, int offset) {
 		int ret =          (src[offset+1] & 0xff);
 		ret = (ret << 8) | (src[offset+0] & 0xff);
 		return ret;
 	}
// 	public static short byte2Toshort(byte[]src, int offset) {
// 		int ret =          (src[offset+1] & 0xff);
// 		ret = (ret << 8) | (src[offset+0] & 0xff);
// 		return (short)ret;
// 	}
//
//	public static int fillString(byte[] dst, int idst, String str) {
//
//		if (str != null) {
//			int len = str.length();
//			System.arraycopy(str.getBytes(), 0, dst, idst, len);
//			idst += len;
//		}
//		dst[idst] = 0; // null-terminated.
//		return idst+1;
//	}

}
