package com.rsupport.litecam.media;

import com.rsupport.litecam.ScreenInfo.ResolutionInfo;


public class ImageProcessing {

    /**
     * src 위에 Watermark를 Overlay 하기 위한 메소드로 dstColorType의 색상값으로 변환 된다.
     *
     * @param src       RGBA byte
     * @param dst       해당 단말기의 colorType의 byte
     * @param info
     * @param colorType
     */
    public static void imageOverlay(byte[] src, ResolutionInfo info, byte[] dst, int dstColorType) {
        nativeImageOverlay(src, info.screenSize.x, info.screenSize.y, info.bytePerLine, dst, info.dstScreenSize.x, info.dstScreenSize.y, dstColorType, info.rotate);
    }

//	public static void imageOverlay(byte[] src, int screenX, int screenY, byte[] dst, int dstColorType) {
//		nativeImageOverlay(src, info.screenSize.x, info.screenSize.y, info.bytePerLine, dst, info.dstScreenSize.x, info.dstScreenSize.y, dstColorType, info.rotate);
//	}

    /**
     * Convert color.
     * 색상값 변환 메소드로, src의 colorType을 dst의 colorType으로 변경하는 메소드.
     *
     * @param src
     * @param srcColorType
     * @param info
     * @param dst
     * @param dstColorType
     */
    public static void convertColor(byte[] src, int srcColorType, ResolutionInfo info, byte[] dst, int dstColorType) {
        nativeConvertColor(src, info.screenSize.x, info.screenSize.y, info.bytePerLine, srcColorType, dst, info.dstScreenSize.x, info.dstScreenSize.y, dstColorType);
    }

    /**
     * assets 폴더의 resource를 NDK에서 불러와 미리 rotate 처리
     *
     * @param apkPath
     * @param filename assets 폴더의 파일명
     * @param rotate   화면 회전.
     */
    public static native void nativeOverlayImageLoad(String apkPath, String filename, int angle);

    /**
     * Watermark overlay src data는 RGBA, dst는 해당 단말기의 colorType
     *
     * @param src       rgba
     * @param dst       해당 단말기의 colorType
     * @param width
     * @param height
     * @param colorType 해당 단말기의 색상값 (NV12 OR I420)
     */
    private static native void nativeImageOverlay(byte[] src, int srcWidth, int srcHeight, int bytePerLine, byte[] dst, int dstWidth, int dstHeight, int dstColorType, int angle);

    /**
     * convert color는 예외처리에 사용. (NV12 to NV12, I420 to I420)
     * 색상값 변경을 위한 메소드로, src는 해당 단말기의 colorType이고, dst 역시 colorType으로 출력.
     *
     * @param src
     * @param dst
     * @param width
     * @param height
     * @param colorType
     */
    private static native void nativeConvertColor(byte[] src, int srcWidth, int srcHeight, int bytePerLine, int srcColorType, byte[] dst, int dstWidth, int dstHeight, int dstColorType);

    static {
        System.loadLibrary("watermark");
    }
}
