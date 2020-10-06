package com.rsupport.phone2phone;

import java.nio.ByteBuffer;

/**
 * Created by objects on 4/7/15.
 */
public class P2PNative {

    //
    // common
    //
    native public static boolean native_init(boolean asEncoder);

    //
    // for encoder
    //
    native public static int native_getOutputBufferSize(int width, int height, int viewerBitsPerPixel);

    native public static boolean native_shutdown();

    native public static int native_zencode(ByteBuffer jBits, int width, int height, int format, int stride,
                                            ByteBuffer jBuf, int offset, int viewBpp, int cacheSize);

    //
    // decoder
    //
    native public static int native_getPalette(int bpp, byte[] paletteBuf);

    native public static int native_zdecode(ByteBuffer jData, int dataOffset, int dataSize, ByteBuffer jBuf, int bufOffset, int bufSize);

    native public static void native_decodeTest(String filePath);

    static {
        System.loadLibrary("encoder");
    }
}

