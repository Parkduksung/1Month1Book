package com.rsupport.litecam;

import android.graphics.Point;


public class ScreenInfo {
    /**
     * Encoder data information
     *
     * @param bufferIndex, time
     */
    public static class CaptureFrame {
        public int inputBufferIndex;
        public long time;

        public CaptureFrame(int bufferIndex, long time) {
            inputBufferIndex = bufferIndex;
            this.time = time;
        }
    }


    /**
     * Encoder size info
     *
     * @param mScreenSize       is real screen Size
     * @param mScreenRotateSize is rotate screen Size
     * @param mRotate           is rotate
     */
    public static class ResolutionInfo {
        public Point screenSize;
        public Point dstScreenSize;
        public Point alignedScreenSize;
        public int bytePerLine;
        public int rotate;
    }

}
