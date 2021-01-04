package com.rsupport.rsperm;

import android.util.Log;

import com.rsupport.util.rslog.MLog;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class RCCommander {
    static final int INFO_REQ_RCCOMMAND = 12;
    static final int JNI_RCCMD_SHELL = 30;
    private static final byte TRANS_MAIN = 0;

	private static int int2byte4(int n, byte[] dst, int offset) { // litte-endian byte4 order.
		dst[offset+0] = (byte) (n);
		dst[offset+1] = (byte) (n >> 8);
		dst[offset+2] = (byte) (n >> 16);
		dst[offset+3] = (byte) (n >> 24);
		return offset+4;
	}

	public static int procRCCommand(com.rsupport.rsperm.i service) {

		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd hh:mm:ss", Locale.getDefault());
		String date = sdf.format(new Date());

 		try {
			String failMessage   = date + " W/RemoteCall(0): <The latest version of RemoteCall is required.>\n" +
			                       date + " W/RemoteCall(0): <The latest version of RemoteCall is required.>";
            byte[] failBytes     = failMessage.getBytes(Charset.defaultCharset());
			int    failBytesSize = failBytes.length;
            byte[] resultBytes   = new byte[4 + 1 + 4 + failBytesSize]; // type(4) + transtype(1) + totallen(4) + message(totallen)

			int offset          = int2byte4(INFO_REQ_RCCOMMAND, resultBytes, 0);
			resultBytes[offset] = TRANS_MAIN;
			offset              = int2byte4(failBytes.length, resultBytes, ++offset);

			System.arraycopy(failBytes, 0, resultBytes, offset, failBytes.length);

            service.je01(resultBytes);

		} catch (Exception e) {
            MLog.e("getFailMessage exception : " + Log.getStackTraceString(e));
		}

 		return 0;
	}

}
