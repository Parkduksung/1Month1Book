package com.rsupport.media.stream;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.rsupport.media.ICodecListener;
import com.rsupport.media.IMediaCodecListener;
import com.rsupport.media.header.XVIDEOHEADER;
import com.rsupport.media.header.XVIDEOHEADERREC64;
import com.rsupport.media.mediaprojection.Configuration;
import com.rsupport.media.mediaprojection.utils.DisplayUtils;
import com.rsupport.sony.EncoderSony;
import com.rsupport.util.Screen;
import com.rsupport.util.log.RLog;

import java.nio.ByteBuffer;

import config.EngineConfigSetting;

/**
 * Created by hyun on 2016. 12. 2..
 */

@TargetApi(Build.VERSION_CODES.KITKAT)
public class RsSonyStream implements ScreenStream {

    private Context context;
    private EncoderSony encoderSony;
    private ICodecListener dataSendListener;
    private Display display;

    //    private ProjectionPermission projectionPermission;

    private boolean isCloseStream = false;
    private boolean isRotationStart = false;
    private Point screenSize;
    private boolean isLandscape = false;
    private int nowRotation = 0;

    public RsSonyStream(Context context) {
        this.context = context;
        isCloseStream = false;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            display = windowManager.getDefaultDisplay();
            nowRotation = DisplayUtils.convertSurfaceRotationToDegree(display.getRotation());
            if (EngineConfigSetting.isPC_Viewer) {
                if (nowRotation == 180) nowRotation = 0;
            }
            screenSize = DisplayUtils.getScreenSize(display);
            isLandscape = Screen.isLandscape(screenSize, display);
            screenSize = sizeSwap(screenSize.x, screenSize.y);

        } else {
            throw new NullPointerException("Not found WindowManager");
        }
    }


    @Override
    public void setEncoderListener(ICodecListener dataSendListener) {
        this.dataSendListener = dataSendListener;
    }

    @Override
    public int start() {
        isCloseStream = false;
        try {
            initEncoder();
            return startEncoder();

        } catch (Exception e) {
        }
        return 0;
    }

    @Override
    public void setFps(int fps) {
        if (encoderSony != null) {
            encoderSony.setFps(fps);
        }
    }

    @Override
    public void startStreamReload() {
        RLog.i("Start stream reload : " + isRotationStart);
        if (isRotationStart) {
            startEncoder();
        }
    }

    private RsSonyStream.MediaCodecCallback mediaCodecCallback;

    /**
     * Encoder 초기화를 진행한다.
     */
    private int initEncoder() {
        Point point = DisplayUtils.detectVirtualDisplaySize(display, 480);
        point = sizeSwap(point.x, point.y);
        int width = point.x;
        int height = point.y;
        int frameRate = getFrameRate();
        RLog.i("initEncoder :  x : " + width + "y : " + height);
        Configuration configuration = new Configuration();
        configuration.mediaFormat = createMediaFormat(width, height);
        configuration.codecValue = getCodecValue();
        configuration.screenWidth = width;
        configuration.screenHeight = height;

        mediaCodecCallback = new RsSonyStream.MediaCodecCallback();

        encoderSony = new EncoderSony(context);
//            if (Global.getInstance().agentThread.isPC_Viewer) {
//                Log.i("PCView rotation : " + nowRotation);
//                encoderKnox.setRotation(nowRotation);
//            }
        encoderSony.setMediaCodecListener(mediaCodecCallback);
        encoderSony.setConfiguration(configuration);

        // Client에 셋팅 정보 전송.
        mediaCodecCallback.onSendEncoderInfo(width, height, frameRate);
        mediaCodecCallback.onSendScreenInfo(width, height, frameRate);
        return 0;
    }

    /**
     * 초기화된 Encoder를 실행한다.
     */
    private int startEncoder() {
        if (encoderSony != null) {
            encoderSony.initialized();
        }
        return 0;
    }

    @Override
    public void stop() {
        isCloseStream = true;
        isRotationStart = !isCloseStream;
        stop(true);
    }

    private void stop(boolean isCloseStream) {
        this.isCloseStream = isCloseStream;
        if (encoderSony != null) {
            encoderSony.stop();
        }

    }

    @Override
    public int resume() {
        return 0;
    }

    @Override
    public void pause() {
        // Do noting
    }

    @Override
    public void close() {
        isCloseStream = true;
//        stopScreenSharing();
    }

    @Override
    public void changeRotation(Context context, int savedRotation, int rotation) {
        if (!isCloseStream && savedRotation != rotation) {
            nowRotation = DisplayUtils.convertSurfaceRotationToDegree(rotation);
            if (EngineConfigSetting.isPC_Viewer) {
                if (nowRotation == 180) nowRotation = 0;
            }
            RLog.i("Rotation " + rotation);
            stop(false);
        }
    }

    Point size;

    private MediaFormat createMediaFormat(int width, int height) {
        size = new Point(width, height);

        RLog.i("Encoder size : " + size.x + ", " + size.y);

        MediaFormat mediaFormat = new MediaFormat();
        mediaFormat.setInteger(MediaFormat.KEY_WIDTH, size.x);
        mediaFormat.setInteger(MediaFormat.KEY_HEIGHT, size.y);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, getBitRate());
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, getFrameRate());
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, getIFrameInterval());
        return mediaFormat;
    }

    /**
     * Bitrate 화질을 결정하는 Codec_value를 정의한다.
     * 아래 CODEC_VALUE를 return 한다. float이며, 값이 낮을 수록 화질이 저화됨. 최대 0.16f까지 하는게 좋다.
     */
    private float getCodecValue() {
        return 0.10f;
    }

    private int getBitRate() {
        return 1024 * 1024 * 2;
    }

    private int getFrameRate() {
        return 10;
    }

    private int getIFrameInterval() {
        return 2;
    }

    private class MediaCodecCallback implements IMediaCodecListener {

        @Override
        public void onSendEncoderInfo(int width, int height, int frameRate) {
            XVIDEOHEADERREC64 vHeader = new XVIDEOHEADERREC64();
            vHeader.videoWidth = size.x;
            vHeader.videoHeight = size.y;
            vHeader.framepersecond = frameRate;

            byte[] bytes = new byte[vHeader.size()];
            vHeader.push(bytes, 0);

            dataSendListener.onVideoHeaderRec(bytes);
        }

        @Override
        public void onSendScreenInfo(int width, int height, int frameRate) {
            XVIDEOHEADER vHeader = getScreenInfo();

            vHeader.framepersecond = frameRate;
            vHeader.videoWidth = size.x;
            vHeader.videoHeight = size.y;
            vHeader.sourceType = XVIDEOHEADER.ENCODER_TYPE_OMX_FOR_VD;
            vHeader.isLandscape = isLandscape ? 1 : 0;

            byte[] bytes = new byte[vHeader.size()];
            vHeader.push(bytes, 0);

            dataSendListener.onVideoHeader(bytes);
        }

        @Override
        public void onSendFormatChanged(MediaFormat mediaFormat) {
            dataSendListener.onVDeqeueFormatChanged(mediaFormat);
        }

        @Override
        public void onSendDequeueEvent(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                bufferInfo.size = 0; // TODO size 정보를 0으로 변경 (네트워크 전송시에는 0으로 변경하지 않아야 한다... 현재는 이렇게)
            }

            if (bufferInfo.size > 0) {
                dataSendListener.onVDequeueOutput(byteBuffer, bufferInfo);
            }
        }

        @Override
        public void onSendStatus(@StatusType int status) {
            switch (status) {
                case IMediaCodecListener.STATUS_STOP:
                    if (!isCloseStream) {
                        isRotationStart = !isCloseStream;
                        /*
                         * 외부 close가 아닌 화면 Rotation close 라면 initEncoder 호출 */
                        initEncoder();

//                        if (Global.getInstance().agentThread.isPC_Viewer) {
                        startEncoder();
//                        }
                    }
                    break;

                case IMediaCodecListener.EVENT_ERROR_INITIALIZATION_FAILED:
                case IMediaCodecListener.EVENT_ERROR_CAPTURE:
                    RLog.e("Capture fail " + status);
                    break;
            }
        }
    }

    private XVIDEOHEADER getScreenInfo() {


        XVIDEOHEADER xvideoheader = new XVIDEOHEADER();
        xvideoheader.frameWidth = screenSize.x;
        xvideoheader.frameHeight = screenSize.y;
        xvideoheader.rotation = DisplayUtils.convertSurfaceRotationToDegree(display.getRotation());

        if (EngineConfigSetting.isPC_Viewer) {
            if (xvideoheader.rotation == 180) xvideoheader.rotation = 0;
        }

        return xvideoheader;
    }

    private Point sizeSwap(int width, int height) {
        int rotation = display.getRotation();

        RLog.i("Rotation : " + rotation + " width " + width + ", " + height);

        switch (rotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                if (width > height) { // Rotation이 0, 180 인데 실제 가로가 더 넓은 경우
                    return new Point(height, width);
                }
                break;

            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                if (width > height) { // Rotation이 90, 270 인데 실제 가로가 더 넓은 경우
                    return new Point(height, width);
                }
                break;
        }

        return new Point(width, height);
    }
}
