package com.rsupport.media;

import android.graphics.Bitmap;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;

import com.rsupport.litecam.util.LLog;

public class RsMediaUtils {

    public static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {
                continue;
            }

            for (String type : codecInfo.getSupportedTypes()) {
                if (type.equalsIgnoreCase(mimeType)) {
                    LLog.i(true, "SelectCodec : " + codecInfo.getName());
                    return codecInfo;
                }
            }
        }
        return null;
    }


    public static boolean isRecognizedColorFormat(int colorFormat) {
        switch (colorFormat) {
            // these are the formats we know how to handle for this test
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                LLog.d("COLOR_FormatYUV420Planar");
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
                LLog.d("COLOR_FormatYUV420PackedPlanar");
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                LLog.d("COLOR_FormatYUV420SemiPlanar");
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
                LLog.d("COLOR_FormatYUV420PackedSemiPlanar");
            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                LLog.d("COLOR_TI_FormatYUV420PackedSemiPlanar");
                return true;
            default:
                return false;
        }
    }

    // from OMX_COLOR_FORMATTYPE
    public final static int COLOR_FormatMonochrome = 1;
    public final static int COLOR_Format8bitRGB332 = 2;
    public final static int COLOR_Format12bitRGB444 = 3;
    public final static int COLOR_Format16bitARGB4444 = 4;
    public final static int COLOR_Format16bitARGB1555 = 5;
    public final static int COLOR_Format16bitRGB565 = 6;
    public final static int COLOR_Format16bitBGR565 = 7;
    public final static int COLOR_Format18bitRGB666 = 8;
    public final static int COLOR_Format18bitARGB1665 = 9;
    public final static int COLOR_Format19bitARGB1666 = 10;
    public final static int COLOR_Format24bitRGB888 = 11;
    public final static int COLOR_Format24bitBGR888 = 12;
    public final static int COLOR_Format24bitARGB1887 = 13;
    public final static int COLOR_Format25bitARGB1888 = 14;
    public final static int COLOR_Format32bitBGRA8888 = 15;
    public final static int COLOR_Format32bitARGB8888 = 16;
    public final static int COLOR_FormatYUV411Planar = 17;
    public final static int COLOR_FormatYUV411PackedPlanar = 18;
    public final static int COLOR_FormatYUV420Planar = 19;
    public final static int COLOR_FormatYUV420PackedPlanar = 20;
    public final static int COLOR_FormatYUV420SemiPlanar = 21;
    public final static int COLOR_FormatYUV422Planar = 22;
    public final static int COLOR_FormatYUV422PackedPlanar = 23;
    public final static int COLOR_FormatYUV422SemiPlanar = 24;
    public final static int COLOR_FormatYCbYCr = 25;
    public final static int COLOR_FormatYCrYCb = 26;
    public final static int COLOR_FormatCbYCrY = 27;
    public final static int COLOR_FormatCrYCbY = 28;
    public final static int COLOR_FormatYUV444Interleaved = 29;
    public final static int COLOR_FormatRawBayer8bit = 30;
    public final static int COLOR_FormatRawBayer10bit = 31;
    public final static int COLOR_FormatRawBayer8bitcompressed = 32;
    public final static int COLOR_FormatL2 = 33;
    public final static int COLOR_FormatL4 = 34;
    public final static int COLOR_FormatL8 = 35;
    public final static int COLOR_FormatL16 = 36;
    public final static int COLOR_FormatL24 = 37;
    public final static int COLOR_FormatL32 = 38;
    public final static int COLOR_FormatYUV420PackedSemiPlanar = 39;
    public final static int COLOR_FormatYUV422PackedSemiPlanar = 40;
    public final static int COLOR_Format18BitBGR666 = 41;
    public final static int COLOR_Format24BitARGB6666 = 42;
    public final static int COLOR_Format24BitABGR6666 = 43;

    public final static int COLOR_TI_FormatYUV420PackedSemiPlanar = 0x7f000100;
    // COLOR_FormatSurface indicates that the data will be a GraphicBuffer metadata reference.
    // In OMX this is called OMX_COLOR_FormatAndroidOpaque.
    public final static int COLOR_FormatSurface = 0x7F000789;
    public final static int COLOR_QCOM_FormatYUV420SemiPlanar = 0x7fa30c00;

    public static int selectColorFormat(String mimeType) {
        int selColorFormat = -1;

        try {
            MediaCodecInfo codecInfo = selectCodec(mimeType);
            if (codecInfo == null) {
                throw new RuntimeException("Unable to find an appropriate codec for " + mimeType);
            }

            boolean isSupportYUVSemiPlanar = false, isSupportYUVPlanar = false;

            MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
            for (int i = 0; i < capabilities.colorFormats.length; i++) {
                int colorFormat = capabilities.colorFormats[i];
                if (isRecognizedColorFormat(colorFormat)) {
                    if (colorFormat == COLOR_FormatYUV420SemiPlanar) isSupportYUVSemiPlanar = true;
                    if (colorFormat == COLOR_FormatYUV420Planar) isSupportYUVPlanar = true;
                    LLog.d(true, "Find a good color format for " + codecInfo.getName() + " / " + mimeType + " / " + colorFormat);
                    selColorFormat = colorFormat;
//					return colorFormat;
                }
            }

            if (isSupportYUVSemiPlanar) {
                selColorFormat = COLOR_FormatYUV420SemiPlanar;
            } else if (isSupportYUVPlanar) {
                selColorFormat = COLOR_FormatYUV420Planar;
            }

        } catch (IllegalArgumentException e) {
            LLog.e(true, "IllegalArgumentException : " + e);
        }

        return selColorFormat;
    }

//	Bitmap bitmap = BitmapFactory.decodeFile(path);
//	byte[] data = getNV21(bitmap.getWidth(), bitmap.getHeight(), bitmap);
//	encoder.offerEncoder(data);

    public static byte[] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {

        int[] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

        scaled.recycle();

        return yuv;
    }

    public static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }

                index++;
            }
        }
    }

//	public static void encodeYUV420SP(byte[] yuv420sp, byte[] argb, int width, int height) {
//	    final int frameSize = width * height;
//
//	    int yIndex = 0;
//	    int uvIndex = frameSize;
//
//	    int a, R, G, B, Y, U, V;
//	    int index = 0;
//	    for (int j = 0; j < height; j++) {
//	        for (int i = 0; i < width; i++) {
//
//	            a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
//	            R = (argb[index] & 0xff0000) >> 16;
//	            G = (argb[index] & 0xff00) >> 8;
//	            B = (argb[index] & 0xff) >> 0;
//
//	            // well known RGB to YUV algorithm
//	            Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
//	            U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
//	            V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;
//
//	            // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
//	            //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
//	            //    pixel AND every other scanline.
//	            yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
//	            if (j % 2 == 0 && index % 2 == 0) {
//	                yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
//	                yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
//	            }
//
//	            index++;
//	        }
//	    }
//	}

}
