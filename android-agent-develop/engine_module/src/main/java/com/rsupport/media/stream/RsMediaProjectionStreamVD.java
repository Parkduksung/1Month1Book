package com.rsupport.media.stream;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.rsupport.media.ICodecListener;
import com.rsupport.media.IMediaCodecListener;
import com.rsupport.media.header.XVIDEOHEADER;
import com.rsupport.media.header.XVIDEOHEADERREC64;
import com.rsupport.media.mediaprojection.Configuration;
import com.rsupport.media.mediaprojection.IScreenCapturable;
import com.rsupport.media.mediaprojection.IVirtualDisplay;
import com.rsupport.media.mediaprojection.ProjectionPermission;
import com.rsupport.media.mediaprojection.record.EncoderVirtualDisplay;
import com.rsupport.media.mediaprojection.utils.DisplayUtils;
import com.rsupport.util.log.RLog;

import java.nio.ByteBuffer;

import config.EngineConfigSetting;

/**
 * Created by taehwan on 5/13/15.
 * <p>
 * 4.4 VirtualDisplay or 5.0 upper MediaProjection
 * Encoder
 */
@TargetApi(21)
public class RsMediaProjectionStreamVD implements ScreenStream {

    private Context context;
    private EncoderVirtualDisplay encoderVirtualDisplay;
    private ICodecListener dataSendListener;

    private ProjectionPermission projectionPermission;
    private IVirtualDisplay virtualDisplay = null;

    private boolean isCloseStream = false;
    private boolean isRotationStart = false;
    private Display display;


    public RsMediaProjectionStreamVD(Context context) {
        this.context = context;
        isCloseStream = false;
        projectionPermission = new ProjectionPermission(context);

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            display = windowManager.getDefaultDisplay();
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
            RLog.i("ProjectionPermission.isBound " + projectionPermission.isBound());
            if (!projectionPermission.isBound()) {
                projectionPermission.bind();
            }
            IScreenCapturable capturable = projectionPermission.createScreenCapturable();
            virtualDisplay = (IVirtualDisplay) capturable.initialized();
            initEncoder();

            return startEncoder();

        } catch (Exception e) {
            RLog.w(e);
        }
        return 0;
    }

    @Override
    public void setFps(int fps) {
        if (encoderVirtualDisplay != null) {
            encoderVirtualDisplay.setFps(fps);
        }
    }

    @Override
    public void startStreamReload() {
        RLog.i("Start stream reload : " + isRotationStart);
        if (isRotationStart) {
            startEncoder();
        }
    }

    private MediaCodecCallback mediaCodecCallback;

    /**
     * Encoder 초기화를 진행한다.
     */

    private int initEncoder() {
        if (virtualDisplay != null) {
            Point point = DisplayUtils.detectVirtualDisplaySize(display, 480);
            int width = point.x;
            int height = point.y;
            int frameRate = getFrameRate(point);

            Configuration configuration = new Configuration();
            configuration.mediaFormat = createMediaFormat(width, height);
            configuration.codecValue = getCodecValue();
            configuration.screenWidth = width;
            configuration.screenHeight = height;

            mediaCodecCallback = new RsMediaProjectionStreamVD.MediaCodecCallback();

            encoderVirtualDisplay = new EncoderVirtualDisplay(context);
            if (EngineConfigSetting.isPC_Viewer) {
                encoderVirtualDisplay.setRotation(DisplayUtils.convertSurfaceRotationToDegree(display.getRotation()));
            }
            encoderVirtualDisplay.setMediaCodecListener(mediaCodecCallback);
            encoderVirtualDisplay.setConfiguration(configuration);
            encoderVirtualDisplay.setVirtualDisplay(virtualDisplay);

            // Client에 셋팅 정보 전송.
            mediaCodecCallback.onSendEncoderInfo(width, height, frameRate);
            mediaCodecCallback.onSendScreenInfo(width, height, frameRate);
        }
        return 0;
    }

    /**
     * 초기화된 Encoder를 실행한다.
     */
    private int startEncoder() {
        if (encoderVirtualDisplay != null) {
            encoderVirtualDisplay.initialized();
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
        if (encoderVirtualDisplay != null) {
            encoderVirtualDisplay.stop();
        }
        if (projectionPermission.isBound()) {
            projectionPermission.unbind();
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
        if (projectionPermission != null) {
            projectionPermission.onDestory();
        }
    }

    @Override
    public void changeRotation(Context context, int savedRotation, int rotation) {
        if (!isCloseStream && savedRotation != rotation) {
            RLog.i("Rotation " + rotation);
            stop(false);
        }
    }

    /**
     * PC View의 경우 코드 인코딩 화면을 ASHM 방식을 사용하도록 한다.(항상 세로)
     */
    private Point sizeSwap(int width, int height) {
        int rotation = display.getRotation();

        if (EngineConfigSetting.isHCIBuild() || EngineConfigSetting.isZidoo()) {
            return new Point(width, height);
        }

        RLog.i("Rotation : " + rotation + " width " + width + ", " + height);

        if (EngineConfigSetting.isPC_Viewer) {
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
        }

        return new Point(width, height);
    }

    private MediaFormat createMediaFormat(int width, int height) {
        Point size = sizeSwap(width, height);
        RLog.i("Encoder size : " + size.x + ", " + size.y);

        MediaFormat mediaFormat = new MediaFormat();
        mediaFormat.setInteger(MediaFormat.KEY_WIDTH, size.x);
        mediaFormat.setInteger(MediaFormat.KEY_HEIGHT, size.y);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, getBitRate(size));
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, getFrameRate(size));
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, getIFrameInterval(size));
        return mediaFormat;
    }

    /**
     * Bitrate 화질을 결정하는 Codec_value를 정의한다.
     * 아래 CODEC_VALUE를 return 한다. float이며, 값이 낮을 수록 화질이 저화됨. 최대 0.16f까지 하는게 좋다.
     */
    private float getCodecValue() {
        return 0.12f;
    }

    private int getBitRate(Point size) {
        return (int) (size.x * size.y * getFrameRate(size) * getCodecValue());
    }

    private int getFrameRate(Point size) {
        if (EngineConfigSetting.isHCIBuild() || EngineConfigSetting.isZidoo() || DisplayUtils.isHorizontalDevice(display, size.x, size.y)) {
            return 20;
        }
        return 30;
    }

    private int getIFrameInterval(Point size) {
        if (EngineConfigSetting.isHCIBuild() || EngineConfigSetting.isZidoo() || DisplayUtils.isHorizontalDevice(display, size.x, size.y)) {

            return 1;
        }
        return 2;
    }

    private class MediaCodecCallback implements IMediaCodecListener {

        @Override
        public void onSendEncoderInfo(int width, int height, int frameRate) {
            XVIDEOHEADERREC64 vHeader = new XVIDEOHEADERREC64();
            vHeader.videoWidth = width;
            vHeader.videoHeight = height;
            vHeader.framepersecond = frameRate;

            byte[] bytes = new byte[vHeader.size()];
            vHeader.push(bytes, 0);

            dataSendListener.onVideoHeaderRec(bytes);
        }

        @Override
        public void onSendScreenInfo(int width, int height, int frameRate) {
            XVIDEOHEADER vHeader = getScreenInfo();

            vHeader.framepersecond = frameRate;
            vHeader.videoWidth = width;
            vHeader.videoHeight = height;
            vHeader.sourceType = XVIDEOHEADER.ENCODER_TYPE_OMX_FOR_VD;

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

                        if (EngineConfigSetting.isPC_Viewer) {
                            startEncoder();
                        }
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
        Point screenSize = DisplayUtils.getScreenSize(display);

        XVIDEOHEADER xvideoheader = new XVIDEOHEADER();
        Point size = sizeSwap(screenSize.x, screenSize.y);
        xvideoheader.frameWidth = size.x;
        xvideoheader.frameHeight = size.y;
        xvideoheader.rotation = DisplayUtils.convertSurfaceRotationToDegree(display.getRotation());

        return xvideoheader;
    }
}
