package com.rsupport.mobile.agent.service.command;

import android.graphics.Point;
import android.os.Build;

import com.rsupport.knox.KnoxScreenCapture;
import com.rsupport.litecam.RspermScreenCapture;
import com.rsupport.litecam.util.RecordScreen;
import com.rsupport.litecam.util.RecordScreenHead;
import com.rsupport.media.stream.RsVDScreenCapture;
import com.rsupport.mobile.agent.constant.AgentBasicInfo;
import com.rsupport.mobile.agent.constant.Global;
import com.rsupport.mobile.agent.modules.engine.EngineType;
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck;
import com.rsupport.mobile.agent.modules.push.RSPushMessaging;
import com.rsupport.mobile.agent.service.HxdecThread;
import com.rsupport.mobile.agent.service.RSPermService;
import com.rsupport.mobile.agent.utils.SdkVersion;
import com.rsupport.rsperm.IRSPerm;
import com.rsupport.util.log.RLog;

import org.apache.http.util.ByteArrayBuffer;
import org.koin.java.KoinJavaComponent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import control.Converter;
import kotlin.Lazy;

public class AgentCommand5010 extends AgentCommandBasic {
    private int errorCode = 0;
    private int mRotation;

    private Lazy<EngineTypeCheck> engineTypeCheckLazy = KoinJavaComponent.inject(EngineTypeCheck.class);
    private Lazy<RSPermService> rspermServiceLazy = KoinJavaComponent.inject(RSPermService.class);

    public AgentCommand5010(int rotation) {
        mRotation = rotation;
    }

    @Override
    public int agentCommandexe(byte[] data, int index) {

        AgentCMDDefaultStruct defultStruct = new AgentCMDDefaultStruct();
        defultStruct.readRemoteControlData(data, index);

        if (AgentCommandFunction.ipMacBlockCheck(Global.getInstance().getAppContext(), defultStruct.remoteIP, defultStruct.remoteMAC, defultStruct.LoginID)) {
            errorCode = 90509;
        }

        returnCommand();
        return 0;
    }

    private boolean isSonyLollipop() {
        if (engineTypeCheckLazy.getValue().getEngineType() == EngineType.ENGINE_TYPE_SONY) {
            return sdkVersionLazy.getValue().greaterThan21();
        }
        return false;
    }

    ByteArrayBuffer buf = new ByteArrayBuffer(4096);
    ByteArrayBuffer encbuf = new ByteArrayBuffer(128);
    byte[] encBytes = null;
    String path = Global.getInstance().getAppContext().getFilesDir() + "/screenshot.jpg";
    byte[] sendByte = null;

    private int returnCommand() {
        int sessionPacket = 30301;
        int commandKey = 5010;
        byte[] frameData;
        if (isConnectedScreen()) {
            return 0;
        }

        buf.append(Converter.getBytesFromIntLE(sessionPacket), 0, 4);


        encbuf.append(Converter.getBytesFromIntLE(commandKey), 0, 4);
        encbuf.append(Converter.getBytesFromIntLE(0), 0, 4);
//		String path = AgentMainService.service.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/screenShot";
        if (errorCode == 0) {
            EngineTypeCheck engineTypeCheck = engineTypeCheckLazy.getValue();
            SdkVersion sdkVersion = sdkVersionLazy.getValue();
            engineTypeCheck.checkEngineType();

            if (engineTypeCheck.getEngineType() == EngineType.ENGINE_TYPE_RSPERM || isSonyLollipop()) {
                if (!sdkVersion.greaterThan21()) {
                    IRSPerm rsperm = rspermServiceLazy.getValue().getRsperm();
                    if (rsperm != null) {
                        RspermScreenCapture screenCapture = new RspermScreenCapture(rsperm);
                        Point realCaptureSize = RecordScreen.getResolution720P(Global.getInstance().getAppContext(), "1.0f").screenSize;
                        screenCapture.create(realCaptureSize, RecordScreenHead.RGB);
                        screenCapture.setBitmapSave(true);
                        frameData = new byte[screenCapture.getBufferSize()];
                        if (Build.MODEL.equals("LG-F300K")) {
                            screenCapture.screenshot(Global.getInstance().getAppContext(), frameData, frameData.length, mRotation);
                        } else {
                            screenCapture.screenshot(Global.getInstance().getAppContext(), frameData, frameData.length);
                        }
                        sendFile();
                    }
                } else {
                    vdCapture();
                }
            } else if (engineTypeCheck.getEngineType() == EngineType.ENGINE_TYPE_KNOX) {
                if (sdkVersion.greaterThan21()) {
                    vdCapture();
                } else {
                    KnoxScreenCapture capture = KnoxScreenCapture.getInstance();
                    Point realCaptureSize = new Point(480, 800);
                    capture.finalize();
                    capture.initialize(realCaptureSize.x, realCaptureSize.y);
                    if (capture.getScreenSize() == -1) return 0;
                    frameData = new byte[capture.getScreenSize()];
                    boolean ret = capture.screenShotFile(frameData, capture.getScreenSize(), path, realCaptureSize.x, realCaptureSize.y, mRotation);
                    if (ret) {
                        sendFile();
                    }
                }
            }
        }
        return 0;
    }

    private boolean isConnectedScreen() {
        if (getAgentThread() == null) return false;
        return getAgentThread().isConnectedScreen();
    }

    private HxdecThread getAgentThread() {
        return Global.getInstance().getAgentThread();
    }

    private void vdCapture() {
        Point realCaptureSize = RecordScreen.getResolution(Global.getInstance().getAppContext(), "1.0f").screenSize;

        RsVDScreenCapture vrvdScreenCaptur = new RsVDScreenCapture(Global.getInstance().getAppContext());
        vrvdScreenCaptur.startScreenCapture(new RsVDScreenCapture.CaptureCallback() {
            @Override
            public void onSuccess() {
                new Thread(() -> sendFile()).start();
            }

            @Override
            public void onFailure() {
                RLog.e("onFailure capture");
            }
        }, realCaptureSize.x, realCaptureSize.y);
    }

    private void sendFile() {
        File screenShotFile = new File(path);
        FileInputStream fis = null;
        RLog.i("SendFile");
        if (screenShotFile.exists()) {

            RLog.i("screenShotFile.exists() : " + screenShotFile.exists());
            byte[] buffer = new byte[4096 * 2];
            int readByte = 0;
            int fileSize = 0;
            try {
                fis = new FileInputStream(screenShotFile);

                while ((readByte = fis.read(buffer)) != -1) {
                    encbuf.append(buffer, 0, readByte);
                    fileSize += readByte;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            encBytes = encbuf.toByteArray();

            System.arraycopy(Converter.getBytesFromIntLE(fileSize), 0, encBytes, 4, 4);
            screenShotFile.delete();
        } else {
            errorCode = -1;
        }

        if (encBytes == null) {
            encBytes = encbuf.toByteArray();
        }

        buf.append(Converter.getBytesFromIntLE(errorCode), 0, 4);

        sendByte = AgentCommandFunction.pushBasicInfo(buf, encBytes);

        RLog.d("sendByte !!! " + sendByte.length);

        AgentCommand.dec_bitcrosswise(sendByte, 0);

        RSPushMessaging messaging = RSPushMessaging.getInstance();
        messaging.send(AgentBasicInfo.getAgentGuid(Global.getInstance().getAppContext()) + "/console", sendByte);
    }

}
