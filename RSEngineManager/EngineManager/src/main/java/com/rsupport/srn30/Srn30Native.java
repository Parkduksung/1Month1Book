package com.rsupport.srn30;

import android.os.Build;
import android.util.Log;

import com.rsupport.util.Utils;

import java.nio.ByteBuffer;


public class Srn30Native {

	public static native boolean initEncoder(long address, String encOpts);
	public static native boolean sendAFrame(long address, int channelId);
	public static native boolean sendVDFrame(ByteBuffer imageBuffer, int channelId);

	public static native boolean sendADRMFrame(long address, int channelId);
	public static native boolean destoryEncoder();

    static {
        boolean loaded = false;
        try {
            System.loadLibrary("net10");
            loaded = true;
        }
        catch(Exception e) {
            Log.e("Net10", "loading failed: " + e.toString());
        }
    }
}
