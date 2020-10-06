package com.rsupport.mobile.agent.service.command;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.rsupport.knox.KnoxScreenCapture;
import com.rsupport.litecam.RspermScreenCapture;
import com.rsupport.litecam.util.RecordScreen;
import com.rsupport.litecam.util.RecordScreenHead;
import com.rsupport.media.mediaprojection.utils.DisplayUtils;
import com.rsupport.media.stream.RsVDScreenCapture;
import com.rsupport.mobile.agent.api.model.SendLiveViewResult;
import com.rsupport.mobile.agent.constant.AgentBasicInfo;
import com.rsupport.mobile.agent.constant.ComConstant;
import com.rsupport.mobile.agent.constant.Global;
import com.rsupport.mobile.agent.modules.engine.EngineType;
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck;
import com.rsupport.mobile.agent.service.HxdecThread;
import com.rsupport.mobile.agent.service.RSPermService;
import com.rsupport.mobile.agent.utils.Result;
import com.rsupport.mobile.agent.utils.SdkVersion;
import com.rsupport.rscommon.exception.RSException;
import com.rsupport.rsperm.IRSPerm;
import com.rsupport.util.log.RLog;

import org.koin.java.KoinJavaComponent;

import java.io.File;

import config.EngineConfigSetting;
import control.Converter;
import kotlin.Lazy;

public class AgentCommand3032 extends AgentCommandBasic {

    LiveViewData mLiveViewData;

    public static int captureWidth = 480;
    public static int captureHeight = 800;
    private static AgentCommand3032 instance = null;

    private Lazy<EngineTypeCheck> engineTypeCheckLazy = KoinJavaComponent.inject(EngineTypeCheck.class);
    private Lazy<RSPermService> rspermServiceLazy = KoinJavaComponent.inject(RSPermService.class);

    private AgentCommand3032() {

    }

    class LiveViewData {
        int width;
        int height;
        String imagePath; // Agents & Preview
        String imageServer;
        int imageServerPort;
        String imagePage;
        int imageTimer;

        public LiveViewData() {

        }

        public LiveViewData(LiveViewData data) {
            width = data.width;
            height = data.height;
            imagePath = data.imagePath;
            imageServer = data.imageServer;
            imageServerPort = data.imageServerPort;
            imagePage = data.imagePage;
            imageTimer = data.imageTimer;

        }

        @Override
        public String toString() {
            return "width : " + width + ", height : " + height + ", imagePath : " + imagePath + ", imageServer : " + imageServer + ", imageServerPort : " + imageServerPort + ", imagePage : "
                    + imagePage + ", imageTimer : " + imageTimer;
        }

    }

    public static AgentCommand3032 getInstance() {
        if (instance == null)
            return new AgentCommand3032();
        else
            return instance;
    }

    @Override
    public int agentCommandexe(byte[] data, int index) {
        readLiveViewData(data, index);
        AgentCommandFunction.runLiveViewExecutor(imageTask);
        return 0;
    }


    private void readLiveViewData(byte[] data, int startIndex) {
        int index = startIndex;
        int size = 0;

        mLiveViewData = new LiveViewData();

        // width
        mLiveViewData.width = Converter.readIntLittleEndian(data, index);
        index += 4;

        // height
        mLiveViewData.height = Converter.readIntLittleEndian(data, index);
        index += 4;

        // imagePath
        size = Converter.readIntLittleEndian(data, index);
        index += 4;
        mLiveViewData.imagePath = new String(data, index, size, EngineConfigSetting.UTF_8);
        index += size;

        // imageServer
        size = Converter.readIntLittleEndian(data, index);
        index += 4;
        mLiveViewData.imageServer = new String(data, index, size, EngineConfigSetting.UTF_8);
        index += size;

        // imageServerPort
        mLiveViewData.imageServerPort = Converter.readIntLittleEndian(data, index);
        index += 4;

        // imageServerPage
        size = Converter.readIntLittleEndian(data, index);
        index += 4;
        mLiveViewData.imagePage = new String(data, index, size, EngineConfigSetting.UTF_8);
        index += size;

        // imageTimer
        mLiveViewData.imageTimer = Converter.readIntLittleEndian(data, index);
        index += 4;

        RLog.d("30302 : " + mLiveViewData.toString());

    }

    Runnable imageTask = new Runnable() {

        @Override
        public void run() {
            final LiveViewData runData = new LiveViewData(mLiveViewData);
            while (true) {
                if (!sendImage(runData)) {
                    break;
                }
                try {
                    Thread.sleep(runData.imageTimer * 1000);
                    RLog.v("image send sleep..." + runData.toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

    };

    private boolean isSonyLollipop() {
        if (engineTypeCheckLazy.getValue().getEngineType() == EngineType.ENGINE_TYPE_SONY) {
            return sdkVersionLazy.getValue().greaterThan21();
        }
        return false;
    }

    private HxdecThread getAgentThread() {
        return Global.getInstance().getAgentThread();
    }

    @SuppressLint("WrongConstant")
    private boolean sendImage(LiveViewData liveViewData) {

        byte[] frameData = null;
        if (getAgentThread().isConnectedScreen()) {
            return false;
        }
        int rotation = -1;
        WindowManager windowManager = (WindowManager) Global.getInstance().getAppContext().getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();

            Point screenSize = DisplayUtils.getScreenSize(display);
            rotation = display.getRotation();
            if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
                if (screenSize.x > screenSize.y) {          //가로 화면.
                    captureWidth = 800;
                    captureHeight = 480;
                } else {
                    captureHeight = 800;
                    captureWidth = 480;
                }
            }
        }

        final Context context = Global.getInstance().getAppContext();
        String path = context.getFilesDir() + "/screenshot.jpg";
        EngineTypeCheck engineTypeCheck = engineTypeCheckLazy.getValue();
        SdkVersion sdkVersion = sdkVersionLazy.getValue();
        engineTypeCheck.checkEngineType();

        if (engineTypeCheck.getEngineType() == EngineType.ENGINE_TYPE_RSPERM || isSonyLollipop()) {
            if (!sdkVersion.greaterThan21()) {
                RspermScreenCapture screenCapture = null;

                IRSPerm rsperm = rspermServiceLazy.getValue().getRsperm();
                if (rsperm != null) {
                    screenCapture = new RspermScreenCapture(rsperm);
                    Point realCaptureSize = RecordScreen.getResolution(Global.getInstance().getAppContext(), "1.0f").screenSize;
                    //캡처 사이즈 고정으로 변경. #46084
//                    realCaptureSize.x = liveViewData.width;
//                    realCaptureSize.y = liveViewData.height;
                    realCaptureSize.x = captureWidth;
                    realCaptureSize.y = captureHeight;
                    screenCapture.create(realCaptureSize, RecordScreenHead.RGB);
                    screenCapture.setBitmapSave(true);
                    frameData = new byte[screenCapture.getBufferSize()];
                    screenCapture.screenshot(Global.getInstance().getAppContext(), frameData, frameData.length);
                }
            } else {
                vdoCapture();
            }

        } else if (engineTypeCheck.getEngineType() == EngineType.ENGINE_TYPE_KNOX) {
            if (sdkVersion.greaterThan21()) {
                vdoCapture();
            } else {
                KnoxScreenCapture capture = KnoxScreenCapture.getInstance();
                capture.finalize();
                capture.initialize(captureWidth, captureHeight);
                if (capture.getScreenSize() == -1) return false;
                frameData = new byte[capture.getScreenSize()];
                boolean ret = capture.screenShotFile(frameData, capture.getScreenSize(), path, captureWidth, captureHeight, rotation);
                if (!ret) return false;
            }

        }

        File screenShotFile = new File(path);
        String guid = AgentBasicInfo.getAgentGuid(Global.getInstance().getAppContext());
        if (screenShotFile.exists()) {
            final Result<SendLiveViewResult> sendLiveViewResult = Global.getInstance().getWebConnection().sendLiveView(liveViewData.imageServer, liveViewData.imagePage,
                    liveViewData.imageServerPort, guid, captureWidth + "", captureHeight + "",
                    liveViewData.imagePath, screenShotFile.getAbsolutePath());
            if (sendLiveViewResult instanceof Result.Failure) {
                final Throwable throwable = ((Result.Failure<SendLiveViewResult>) sendLiveViewResult).getThrowable();
                if (throwable instanceof RSException) {
                    final int errorCode = ((RSException) throwable).getErrorCode();
                    if (errorCode == ComConstant.NET_ERR_TIMEOUT) {
                        return true;
                    }
                }
            }
            return sendLiveViewResult.isSuccess();
        }
        return false;
    }

    private void vdoCapture() {
        RsVDScreenCapture vrvdScreenCaptur = new RsVDScreenCapture(Global.getInstance().getAppContext());
        vrvdScreenCaptur.startScreenCapture(RsVDScreenCapture.CaptureCallback.empty, captureWidth, captureHeight);
    }
}
