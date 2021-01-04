package com.rsupport.srn30;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.rsupport.util.MemoryFileEx;


//	#pragma pack(push,4)
//	typedef struct {
//		// syncronization info :
//		int	consumer;
//		int producer;
//		// screenshot info
//		int success;
//		int width, height, bytesPerLine, rgbFormat;
//		int bitType;
//		union {
//			char bits[0];
//			struct {
//				int rectCount;
//				I16Rect	rcs[100];
//				char bits[0];
//			} diff;
//		};
//		enum { RGB, NV12, I420, NV21, YUY2, RGBwithDIRTY };
//	} ASHM_SCREEN;

public class ASHM_SCREEN {
	public final static int RGB = 0;
    @Deprecated
    public final static int NV12 = 1;
    @Deprecated
	public final static int I420 = 2;
	public final static int NV21 = 3;
	public final static int YUY2 = 4;
	public final static int RGBwithDIRTY = 5;

	public final static int HEADER_SIZE = 4*8;
	public final static int RGBwithDIRTY_HEADER_SIZE = HEADER_SIZE + 4 + 100*8;

	public static class Header {

		// success flag 성공.
		public static final int FLAG_SUCCESS = 1;

		public int success; // captured ptr: 0 means failed.
		public int width, height, bytesPerLine, rgbFormat;
		public int bitType;
		
		@Override
		public String toString() {
			return String.format("ASHM HEADER [success.0x%x, %d x %d, bytesPerLine: %d, rgbFormat: %d, bytType: %d]",
					success, width, height, bytesPerLine, rgbFormat, bitType
					);
		}
	}
	public static Header get(MemoryFileEx mf) {
		ByteBuffer bb = ByteBuffer.allocate(512).order(ByteOrder.LITTLE_ENDIAN);
		try {
			mf.readBytes(bb.array(), 0, 0, HEADER_SIZE);
			bb.limit(HEADER_SIZE);
			bb.position(4*2);

			Header h = new Header();
			h.success      = bb.getInt();
			h.width        = bb.getInt();
			h.height       = bb.getInt();
			h.bytesPerLine = bb.getInt();
			h.rgbFormat    = bb.getInt();
			h.bitType      = bb.getInt();
			return h;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}