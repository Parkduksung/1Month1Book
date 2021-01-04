package com.rsupport.util;

import java.io.UnsupportedEncodingException;

public class Converter {
    public static void test(){
    }
	public static int int2byte4(int n, byte[] dst, int offset) {
		dst[offset+0] = (byte) (n);
		dst[offset+1] = (byte) (n >> 8);
		dst[offset+2] = (byte) (n >> 16);
		dst[offset+3] = (byte) (n >> 24);
		return offset+4;
	}
	public static int int2byte2(int n, byte[] dst, int offset) {
		dst[offset+0] = (byte) (n);
		dst[offset+1] = (byte) (n >> 8);
		return offset+2;
	}

	public static int short2byte2(short n, byte[] dst, int offset) {
		dst[offset+0] = (byte) (n);
		dst[offset+1] = (byte) (n >> 8);
		return offset+2;
	}

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
	public static short byte2Toshort(byte[]src, int offset) {
		int ret =          (src[offset+1] & 0xff);
		ret = (ret << 8) | (src[offset+0] & 0xff);
		return (short)ret;
	}


	//		public static int fillString(byte[] dst, int idst, String str) {
	//
	//			if (str != null) {
	//				int len = str.length();
	//				System.arraycopy(str.getBytes(), 0, dst, idst, len);
	//				idst += len;
	//			}
	//			dst[idst] = 0; // null-terminated.
	//			return idst+1;
	//		}

	public static int fillString(byte[] dst, int idst, String str) {

		if (str != null) {
			byte[] strBytes = null;
			try {
				strBytes = str.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			int len = strBytes.length;
			System.arraycopy(strBytes, 0, dst, idst, len);
			idst += len;
		}
		dst[idst] = 0; // null-terminated.
		return idst+1;
	}
}
