package com.rsupport.litecam.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.rsupport.litecam.ScreenInfo.ResolutionInfo;

import android.annotation.SuppressLint;
import android.media.MediaCodecInfo;

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

public class RecordScreenHead {
    /**
     * Color 정의 색상값 <b>RGBA</b>
     */
    public final static int RGB = 0; // RGBA

    /**
     * Color 정의 색상값 <b>NV12</b>
     */
    public final static int NV12 = 1; // NV12

    /**
     * Color 정의 색상값 <b>I420</b>
     */
    public final static int I420 = 2; // I420

    /**
     * Color 정의 색상값 <b>NV21</b>
     */
    public final static int NV21 = 3; // NV12

    /**
     * Color 정의 색상값 <b>YUY2</b>
     */
    public final static int YUY2 = 4; // YUV2

    public final static int HEADER_SIZE = 4 * 8;

    public int success; // captured ptr: 0 means failed.
    public int width, height, bytesPerLine, rgbFormat;
    public int bitType;

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("ASHM HEADER [success.0x%x, %d x %d, bytesPerLine: %d, rgbFormat: %d, bytType: %d]", success, width, height, bytesPerLine, rgbFormat, bitType);
    }

    public static RecordScreenHead get(byte[] buffer) {
        ByteBuffer bb = ByteBuffer.allocate(512).order(ByteOrder.LITTLE_ENDIAN);
        bb.put(buffer, 0, HEADER_SIZE);
        bb.position(4 * 2);

        RecordScreenHead h = new RecordScreenHead();
        h.success = bb.getInt();
        h.width = bb.getInt();
        h.height = bb.getInt();
        h.bytesPerLine = bb.getInt();
        h.rgbFormat = bb.getInt();
        h.bitType = bb.getInt();
        return h;
    }

    public static int getRSColorFormat(int colorFormat) {
        int ashmType = RecordScreenHead.RGB;

        switch (colorFormat) {
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                LLog.d("ASHM_SCREEN.I420");
                ashmType = RecordScreenHead.I420;
                break;

            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                LLog.d("ASHM_SCREEN.NV12");
                ashmType = RecordScreenHead.NV12;
                break;
        }

        return ashmType;
    }

    /**
     * rsperm에서 캡쳐한 데이터를 기반으로 실제 screenSize를 체크하고, 정의한다. rsperm으로 캡쳐한 데이터의 색상값이 {@link RecordScreenHead#RGB}인 경우에만
     * 다음 로직을 따르게 된다. 해당 로직은 GPU에서만 재 정의된다.
     *
     * @param head
     * @param info
     * @param isGPU
     * @return
     */
    public static ResolutionInfo setScreenSize(RecordScreenHead head, ResolutionInfo info, boolean isGPU) {
        info.bytePerLine = (info.dstScreenSize.x * 4);

        if (head.bitType == RecordScreenHead.RGB) {
            LLog.d("info.dstScreenSize.x * 4 : " + (info.dstScreenSize.x * 4) + " info.bytePerLine : " + info.bytePerLine);
            info.bytePerLine = head.bytesPerLine;

            if (isGPU) { // GPU 처리시에만 rsperm 의 capture bytePerLine으로 값을 조정.
                if ((info.dstScreenSize.x * 4) < info.bytePerLine) {
                    info.dstScreenSize.x = info.bytePerLine / 4;
                    info.alignedScreenSize.x = info.dstScreenSize.x;
                }
            }
        }

        return info;
    }
}