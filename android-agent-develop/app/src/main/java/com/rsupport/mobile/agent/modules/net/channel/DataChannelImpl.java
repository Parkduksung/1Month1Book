package com.rsupport.mobile.agent.modules.net.channel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.WindowManager;

import com.rsupport.commons.net.socket.SocketCompat;
import com.rsupport.jni.RC45SocketCompat;
import com.rsupport.knox.KnoxManagerCompat;
import com.rsupport.media.mediaprojection.utils.DisplayUtils;
import com.rsupport.mobile.agent.BuildConfig;
import com.rsupport.mobile.agent.constant.Global;
import com.rsupport.mobile.agent.constant.GlobalStatic;
import com.rsupport.mobile.agent.modules.channel.screen.StreamController;
import com.rsupport.mobile.agent.modules.device.inject.EventDispatcher;
import com.rsupport.mobile.agent.modules.device.inject.KeyPadEvent;
import com.rsupport.mobile.agent.modules.device.power.PowerKeyController;
import com.rsupport.mobile.agent.modules.engine.EngineType;
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck;
import com.rsupport.mobile.agent.modules.ftp.RsFTPTrans;
import com.rsupport.mobile.agent.modules.function.Clipboard;
import com.rsupport.mobile.agent.modules.function.ScreenDraw;
import com.rsupport.mobile.agent.modules.function.ScreenNameSender;
import com.rsupport.mobile.agent.modules.function.SpeakerPhone;
import com.rsupport.mobile.agent.modules.function.TopmostText;
import com.rsupport.mobile.agent.modules.net.OnConfigChangeListener;
import com.rsupport.mobile.agent.modules.net.PacketHandler;
import com.rsupport.mobile.agent.modules.net.model.HeaderPacket;
import com.rsupport.mobile.agent.modules.net.model.MsgPacket;
import com.rsupport.mobile.agent.modules.net.model.Packet;
import com.rsupport.mobile.agent.modules.net.model.RcpClipboardBeginMsg;
import com.rsupport.mobile.agent.modules.net.protocol.MessageID;
import com.rsupport.mobile.agent.modules.sysinfo.ApplicationInfo;
import com.rsupport.mobile.agent.modules.sysinfo.ApplicationInfoHelper;
import com.rsupport.mobile.agent.modules.sysinfo.CPUUsageInfo;
import com.rsupport.mobile.agent.modules.sysinfo.ProcessInfo;
import com.rsupport.mobile.agent.modules.sysinfo.SystemInfo;
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppFactory;
import com.rsupport.mobile.agent.modules.sysinfo.appinfo.AppInfoCache;
import com.rsupport.mobile.agent.modules.sysinfo.phone.PhoneMemory;
import com.rsupport.mobile.agent.modules.sysinfo.process.ProcessItem;
import com.rsupport.mobile.agent.service.HxdecThread;
import com.rsupport.mobile.agent.service.RSPermService;
import com.rsupport.mobile.agent.service.command.AgentCommandFunction;
import com.rsupport.mobile.agent.utils.ComputeCoordinate;
import com.rsupport.mobile.agent.utils.Converter;
import com.rsupport.mobile.agent.utils.DispUtil;
import com.rsupport.mobile.agent.utils.SdkVersion;
import com.rsupport.mobile.agent.utils.Utility;
import com.rsupport.mobile.agent.utils.compress.Compress;
import com.rsupport.rsperm.IRSPerm;
import com.rsupport.sony.SonyManager;
import com.rsupport.util.log.RLog;

import org.jetbrains.annotations.NotNull;
import org.koin.java.KoinJavaComponent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import config.EngineConfigSetting;
import kotlin.Lazy;

public class DataChannelImpl implements DataChannel, PacketHandler<MsgPacket>, OnConfigChangeListener {
    public byte[][] processInfoPacket;
    public byte[][] appInfoPacket;
    private byte[][] logInfoPacket;
    private byte[] basicInfoPacket;
    private byte[] settingInfoPacket;
    private byte[] memoryInfoPacket;
    private byte[] chartInfoPacket;
    private byte[] debugInfoPacket;
    private byte[] sendBuffer;
    private byte[] recvClipboardData;
    private byte[] unzipData;
    private byte[] clipPartByte;
    private byte[] zipBuffer;
    private byte[] compressedBytes;
    private byte[] clipBytes;
    private byte[] clipNoneNullBytes;
    private SpeakerPhone speakerPhone;
    private Context mainContext;
    private HeaderPacket headerPacket;
    private ProcessInfo processInfo;
    private ApplicationInfo applicationInfo;
    private ScreenNameSender screenNameSender = null;
    private Compress compress;
    private String oldClipText = "";
    private String newClipText;
    private boolean isDataChannelStart;
    private boolean isOldStateScreenlock;
    private boolean isNewStateScreenlock;
    private boolean isOldStateSpeakerPhoneOn;
    private boolean isNewStateSpeakerPhoneOn;
    private boolean isClose;
    private boolean isDefaultLock;
    private int recvClipboardDataPos;
    private OnMouseEventListener mouseEventListener;

    public static boolean iscontrolRequest;

    public static String sSavedSRNum = null;

    public ScreenDraw screenDraw;
    private SocketCompat socketCompat;
    private StreamController screenStreamController;

    private AgentCommandFunction agentCommFun;
    private ComputeCoordinate computeCoordinate = new ComputeCoordinate();
    private Lazy<EngineTypeCheck> engineTypeCheckLazy = KoinJavaComponent.inject(EngineTypeCheck.class);
    private Lazy<EventDispatcher.Factory> eventDispatcherFactory = KoinJavaComponent.inject(EventDispatcher.Factory.class);
    private Lazy<PowerKeyController> powerKeyControllerLazy = KoinJavaComponent.inject(PowerKeyController.class);
    private Lazy<RSPermService> rspermServiceLazy = KoinJavaComponent.inject(RSPermService.class);
    private Lazy<KnoxManagerCompat> knoxManagerCompatLazy = KoinJavaComponent.inject(KnoxManagerCompat.class);
    private Lazy<RunningAppFactory> runningAppFactoryLazy = KoinJavaComponent.inject(RunningAppFactory.class);
    private Lazy<SystemInfo> systemInfoLazy = KoinJavaComponent.inject(SystemInfo.class);
    private Lazy<SdkVersion> sdkVersionLazy = KoinJavaComponent.inject(SdkVersion.class);

    public DataChannelImpl(Context context, StreamController screenStreamController) {
        this.screenStreamController = screenStreamController;
        mainContext = context;
        headerPacket = new HeaderPacket();
        sendBuffer = new byte[512 * 10];
        zipBuffer = new byte[4096 * 3];
        isClose = false;
        isDefaultLock = true;
        compress = new Compress();
        agentCommFun = new AgentCommandFunction();
        computeCoordinate.setBaseResolution(systemInfoLazy.getValue().getDisplayWidth(), systemInfoLazy.getValue().getDisplayHeight());
        knoxCheckTouchPoint();
        processInfo = new ProcessInfo(mainContext, this, runningAppFactoryLazy.getValue(), new PhoneMemory(mainContext, new PhoneMemory.ProcessMemInfoFileFactory()));
        applicationInfo = new ApplicationInfo(mainContext, new ApplicationInfoHelper(mainContext), new AppInfoCache());
    }

    @Override
    public void setSocketCompat(SocketCompat socketCompat) {
        this.socketCompat = socketCompat;
    }

    @Override
    public void setMouseEventListener(OnMouseEventListener mouseEventListener) {
        this.mouseEventListener = mouseEventListener;
    }

    public void setContext(Context context) {
        this.mainContext = context;
    }

    private void knoxCheckTouchPoint() {
        EngineTypeCheck engineTypeCheck = engineTypeCheckLazy.getValue();
        SdkVersion sdkVersion = sdkVersionLazy.getValue();
        if (engineTypeCheck.getEngineType() == EngineType.ENGINE_TYPE_KNOX && !(sdkVersion.greaterThan21())) {
            engineTypeCheck.checkEngineType();
        }
    }

    /*
     * private String readString(byte[] data, int index) { int length =
     * Converter.readIntLittleEndian(data, index); index += 4;
     *
     * byte[] bytes = new byte[length]; System.arraycopy(data, index, bytes, 0,
     * length); index += length;
     *
     * String ret = ""; try { ret = new String(bytes, "UTF-16LE"); } catch
     * (Exception e) {}
     *
     * return ret; }
     */

    private String getFullFilePath(String requestPath) {

        if (requestPath.toLowerCase().equals("%sd%")) {
            requestPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
            if (requestPath.length() <= 0) {
                requestPath = "/";
            }
        }
        return requestPath;
    }

    private void procFileListRequest(MsgPacket msg) {
        try {
            String decPath = "";
            String filePath = new String(msg.getData(), "UTF-16LE");
            // long elapsedtime = 0;
            decPath = getFullFilePath(filePath);
            RsFTPTrans.setFilelist(decPath);
            byte[] data = RsFTPTrans.getFlielistData(decPath);
            if (filePath.equals("%sd%") && data == null) {
                procFileListRequest("/");
                return;
            }
            sendPacket(MessageID.rcpSFTP, MessageID.rcpExpPathList, data,
                    data.length);
            sendPacket(MessageID.rcpSFTP, MessageID.rcpExpPathListEnd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void procFileListRequest(String filepath) {
        try {
            RsFTPTrans.setFilelist(filepath);
            byte[] data = RsFTPTrans.getFlielistData(filepath);
            sendPacket(MessageID.rcpSFTP, MessageID.rcpExpPathList, data,
                    data.length);
            sendPacket(MessageID.rcpSFTP, MessageID.rcpExpPathListEnd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean procFileDelete(MsgPacket msg) {
        boolean ret = false;
        try {
            m_delMsg = msg;
            String filePath;
            filePath = new String(m_delMsg.getData(), "UTF-16LE");
            ret = RsFTPTrans.procDeleteFiles(filePath);
            sendPacket(MessageID.rcpSFTP, MessageID.rcpExpFileDeleteEnd);
            // if (!ret) return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private boolean procFileExecute(MsgPacket msg) {
        boolean ret = false;
        try {
            String filePath;
            filePath = new String(msg.getData(), "UTF-16LE");
            ret = RsFTPTrans.fileExecute(filePath);
            if (!ret)
                return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private void callConfirmActivity() {
//		Intent agentIntent = new Intent(mainContext, AgentService.class);
//		agentIntent.putExtra("type", "userconfirm");
//		agentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		mainContext.startService(agentIntent);
        sendPacket(MessageID.rcpSFTP, MessageID.rcpExpDownConfirmOK);
    }

    private MsgPacket m_delMsg;

    private boolean procFTPDeleteConfirm(MsgPacket msg) {
        boolean ret = false;
        try {
            m_delMsg = msg;
            // ApplicationLockActivity.requestConfirmPopup(ApplicationLockActivity.LOCKCONFIRM_FTPDEL);
//			AgentService.strFTPDeleteFileName = new String(m_delMsg.data, "UTF-16LE");
            callConfirmActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private boolean isFileOnConfirm(String strBody) {
        boolean ret = false;
        int token = strBody.indexOf(";");
        int fileFlag = Integer.valueOf(strBody.substring(0, token));
        if (fileFlag == 0)
            ret = true;
        return ret;
    }

    private String getFilenameOnConfirm(String strBody) {
        String ret = "";
        int token = strBody.indexOf(";");
        int token2 = strBody.indexOf(";", token + 1);
        if (token2 > 0) {
            ret = strBody.substring(token + 1, token2);
        } else {
            ret = strBody.substring(token + 1, strBody.length());
        }
        return ret;
    }

    private String[] getFilenameOnConfirm(String strBody, int count) {
        RLog.d("getFilenameOnConfirm");
        String filePath[] = new String[count];
        int start = 0;
        int end = strBody.indexOf(";");
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                if (i == count - 1) {
                    filePath[i] = strBody.substring(start, strBody.length());
                } else {
                    filePath[i] = strBody.substring(start, end);
                }
                start = end + 1;
                end = strBody.indexOf(";", start);
                RLog.d("procFTPFileDiskCheck_getFilenameOnConfirm : "
                        + filePath[i]);
            }
        } else {
            filePath[0] = strBody;
        }
        return filePath;
    }

    private Long getFileSizeOnConfirm(String strBody) {
        RLog.d("getFileSizeOnConfirm");
        File check = new File(strBody);
        long sumSize = 0;

        if (check.isFile()) {
            RLog.d("File Size  ::" + check.length());
            fileTotalCount++;
            return check.length();
        } else if (check.isDirectory()) {
            // 폴더도 갯수 카운트
            folderTotalCount++;
            File[] fileList = check.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                File childFile = fileList[i];
                sumSize += childFile.length();
                if (childFile.isDirectory()) {
                    sumSize += getFileSizeOnConfirm(childFile.getAbsolutePath());
                } else {
                    fileTotalCount++;
                }
            }
            return sumSize;
        } else
            return 0L;
    }

    private int getRestFileCount(String strBody) {
        int ret = 0;
        try {
            int token = strBody.indexOf(";");
            token = strBody.indexOf(";", token + 1);
            if (token < 0) {
                ret = 0;
            } else {
                String str = strBody.substring(token + 1, strBody.length());
                str.trim();
                if (str.length() <= 0) {
                    ret = 0;
                } else {
                    ret = Integer.valueOf(str);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ret = 0;
        }
        return ret;
    }

    private int fileDiskCheckFileCount(String strBody) {
        int ret = 1;
        Pattern p = Pattern.compile(";");

        Matcher m = p.matcher(strBody);

        for (int i = 0; m.find(i); i = m.end())
            ret++;

        return ret;
    }

    int fileTotalCount;
    int folderTotalCount;

    private void sendDownLoadInformation(MsgPacket msg) {
        RLog.d("sendDownLoadInformation");

        try {
            String strBody = new String(msg.getData(), "UTF-16LE");

            int fileCount = fileDiskCheckFileCount(strBody);

            RLog.d("procFTPFileDiskCheck_fileCount" + fileCount);
            String filePath[] = getFilenameOnConfirm(strBody, fileCount);

            Long fileSize = 0L;
            fileTotalCount = 0;
            folderTotalCount = 0;
            for (int i = 0; i < fileCount; i++) {
                fileSize += getFileSizeOnConfirm(filePath[i]);
            }

            RLog.d("sendDownLoadInformation_fileSize  : " + fileSize);
            byte[] noneNullBytes = null;
            byte[] addNullBytes = null;
            String message = fileSize.toString() + ";" + fileTotalCount + ";"
                    + folderTotalCount;

            RLog.d("sendDownLoadInformation_message  : " + message);
            if (message != null) {
                try {
                    noneNullBytes = message.getBytes("UTF-16LE");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                addNullBytes = new byte[noneNullBytes.length + 2];
                System.arraycopy(noneNullBytes, 0, addNullBytes, 0,
                        noneNullBytes.length);
            }
            RLog.d("rcpExpFolderInfomation");
            sendPacket(MessageID.rcpSFTP, MessageID.rcpExpFolderInfomation,
                    addNullBytes, addNullBytes.length);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private boolean procFTPDownConfirm(MsgPacket msg) {
        RLog.d("procFTPDownConfirm");
        boolean ret = false;
        try {
            String strBody = new String(msg.getData(), "UTF-16LE");
            RLog.d("procFTPDownConfirm strBody  :::   " + strBody);
//			AgentService.strFTPConfirmFileName = getFilenameOnConfirm(strBody);
//			if (CoreVariables.isAgreeFtpDownConfirm
//					|| CoreVariables.fileTransferAgreeOption == false) {
//				RLog.d("CoreVariables.fileTransferAgreeOption : "
//						+ CoreVariables.fileTransferAgreeOption);
//
            ret = true;
            sendPacket(MessageID.rcpSFTP, MessageID.rcpExpDownConfirmOK);
            return ret;
//			} else if (CoreVariables.isCancleFtpDownConfirm) {
//				ret = true;
//				sendPacket(MessageID.rcpSFTP, MessageID.rcpExpDownConfirmCancel);
//				// OperationRecord.getInstance().writeLog(OperationRecord.FLE_REJ,
//				// AgentService.strFTPConfirmFileName);
//				return ret;
//			}
            // if (isFileOnConfirm(strBody)) {
            // ApplicationLockActivity.requestConfirmPopup(ApplicationLockActivity.LOCKCONFIRM_FTPDOWN_FILE);
            // } else {
            // ApplicationLockActivity.requestConfirmPopup(ApplicationLockActivity.LOCKCONFIRM_FTPDOWN_FOLDER);
            // }
//			callConfirmActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private boolean procFTPUpConfirm(MsgPacket msg) {
        boolean ret = false;
        try {
//			if (CoreVariables.isAgreeFtpUpConfirm
//					|| CoreVariables.fileTransferAgreeOption == false) {
//				RLog.d("CoreVariables.fileTransferAgreeOption : "
//						+ CoreVariables.fileTransferAgreeOption);
            ret = true;
            sendPacket(MessageID.rcpSFTP, MessageID.rcpExpUpConfirmOK);
            return ret;
//			} else if (CoreVariables.isCancleFtpDownConfirm) {
//				ret = true;
//				RLog.d("CoreVariables.isCancleFtpDownConfirm : "
//						+ CoreVariables.isCancleFtpDownConfirm);
//				sendPacket(MessageID.rcpSFTP, MessageID.rcpExpDownConfirmCancel);
            // OperationRecord.getInstance().writeLog(OperationRecord.FLE_REJ,
            // AgentService.strFTPConfirmFileName);
//				return ret;
//			}
//			String strBody = new String(msg.data, "UTF-16LE");
//			AgentService.strFTPConfirmFileName = getFilenameOnConfirm(strBody);
            // ApplicationLockActivity.restFileCount =
            // getRestFileCount(strBody);
            // if (isFileOnConfirm(strBody)) {
            // ApplicationLockActivity.requestConfirmPopup(ApplicationLockActivity.LOCKCONFIRM_FTPUP_FILE);
            // } else {
            // ApplicationLockActivity.requestConfirmPopup(ApplicationLockActivity.LOCKCONFIRM_FTPUP_FOLDER);
            // }
//			callConfirmActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private void procRcpKeyMouseCtrl(MsgPacket msg) {
        RLog.i("procRcpKeyMouseCtrl ::: " + (int) msg.getMsgID());
        switch ((int) msg.getMsgID()) {
            case MessageID.rcpKeyMouseCtrlRequest:
                RLog.d("MessageID.rcpKeyMouseCtrl");
                //rcpKeyMouseCtrlConfirm
                //rcpKeyMouseCtrlReject
                DispUtil.getScreenPixel(mainContext);
                byte sendpacket[] = new byte[1];
                sendpacket[0] = 3;
                sendPacket(MessageID.rcpKeyMouseCtrl, MessageID.rcpKeyMouseCtrlConfirm, sendpacket, 1);
                break;
            case MessageID.rcpKeyEvent:
                RLog.d("MessageID.rcpKeyEvent");
                RLog.d("rcpdeviceKey  [0]: " + msg.getData()[0] + ", code : " + msg.getData()[3]);

                break;
            case MessageID.rcpMouseEvent:
                RLog.d("MessageID.rcpMouseEvent");
                break;
            case MessageID.rcpTouchEvent:
                RLog.d("MessageID.rcpTouchEvent");
                break;
            case MessageID.rcpKeypadEvent:
                RLog.d("MessageID.rcpKeypadEvent");
                break;
            case MessageID.rcpMonkeyTouch:
                deviceScreenOn();

                RLog.d("MessageID.rcpMonkeyTouch");
                byte keyByte[] = msg.getData();
                int count = (int) keyByte[0];
                final byte action = keyByte[1];
                int index = 2;
                byte id;
                short x;
                short y;
                short x2;
                short y2;

                RLog.d("rcpMonkeyTouch : total count : " + count + ", action : " + action);

                final Point computedPoint = new Point();
                final Point computedPoint2 = new Point();

                for (int i = 0; i < count; i++) {
                    id = keyByte[index]; //default = 0
                    index++;
                    x = Converter.readShortLittleEndian(keyByte, index);
                    index += 2;
                    y = Converter.readShortLittleEndian(keyByte, index);
                    index += 2;
                    x2 = Converter.readShortLittleEndian(keyByte, index);
                    index += 2;
                    y2 = Converter.readShortLittleEndian(keyByte, index);
                    index += 2;

                    Point p = computeCoordinate.compute(x, y);
                    computedPoint.x = p.x;
                    computedPoint.y = p.y;

                    // x2 의 값이 -32768일때는 single touch로 x2, y2 값을 수정하지 않는다.
                    if (x2 == -32768) {
                        computedPoint2.x = x2;
                        computedPoint2.y = y2;
                    } else {
                        Point p2 = computeCoordinate.compute(x, y);
                        computedPoint2.x = p2.x;
                        computedPoint2.y = p2.y;
                    }

                    RLog.d("rcpMonkeyTouch2 : index = " + i + ", id : " + id + ", x : " + x + ", y : " + y + ", x2 : " + x2 + ", y2 : " + y2 + ", computedX : " + computedPoint.x + ", computedY: " + computedPoint.y);
                    startTouchEvent(action, computedPoint.x, computedPoint.y, computedPoint2.x, computedPoint2.y);

                    // drawing
                    if (x2 == -32768) {
                        screenDownEvent(action, computedPoint.x, computedPoint.y);
                    } else if (action == MotionEvent.ACTION_DOWN) {
                        screenDownEvent((byte) MotionEvent.ACTION_UP, computedPoint.x, computedPoint.y);
                    }
                }
                break;
            case MessageID.rcpMonkeyWheel:
                RLog.d("keymouse", "MessageID.rcpMonkeyWheel");
                break;
            case MessageID.rcpMonkeyKeypad:
                RLog.d("keymouse", "MessageID.rcpMonkeyKeypad");
                deviceScreenOn();

                KeyPadEvent keyPadEvent = KeyPadEvent.from(msg.getData());
                if (keyPadEvent instanceof KeyPadEvent.Events) {
                    EventDispatcher<KeyPadEvent.Events> eventDispatcher = eventDispatcherFactory.getValue().create();
                    boolean dispatchResult = eventDispatcher.dispatch((KeyPadEvent.Events) keyPadEvent);
                    RLog.v("dispatchResult." + dispatchResult);
                }
                break;
            case MessageID.rcpMonkeyString:
                RLog.d("keymouse", "MessageID.rcpMonkeyString");
                break;
            case MessageID.rcpMonkeyKeylayout:
                RLog.d("keymouse", "MessageID.rcpMonkeyKeylayout");
                break;
            default:
                RLog.d("keymouse", "msgId : " + (int) msg.getMsgID());
                break;

        }

    }

    private void procRcpProcess(MsgPacket msg) {
        switch (msg.getMsgID()) {

            case MessageID.rcpProcessListRequest:
                byte[] data = agentCommFun.getRVSystemInfo(mainContext);
                sendPacket(MessageID.rcpProcess, MessageID.rcpProcessList, data, data.length);
                break;

            case MessageID.rcpProcessKillRequest:
                String pakageName = getString(msg);
                pakageName = pakageName.substring(0, pakageName.indexOf("\\"));

                RLog.d("kill pakageName : " + pakageName);
                agentCommFun.killRVSystemInfo(mainContext, pakageName);
                break;
        }
    }

    private void procRcpSFTP(MsgPacket msg) {
        switch ((int) msg.getMsgID()) {
            case MessageID.rcpExpPath:
                RLog.d("rcpExpPath");
                procFileListRequest(msg);
                break;
            case MessageID.rcpExpFileDelete:
                RLog.d("rcpExpFileDelete");
                procFileDelete(msg);
                break;
            case MessageID.rcpExpFileExecute:
                RLog.d("rcpExpFileExecute");
                procFileExecute(msg);
                break;
            case MessageID.rcpMobileFTPFileList:
                RLog.d("rcpMobileFTPFileList");
                break;
            case MessageID.rcpMobileFTPEnd:
                RLog.d("rcpMobileFTPEnd");
                break;
            case MessageID.rcpExpDownloadStart:
                RLog.d("rcpExpDownloadStart");
                RsFTPTrans.reset();
                procFTPDownConfirm(msg);
                break;
            case MessageID.rcpExpUploadStart:
                RLog.d("rcpExpUploadStart");
                RsFTPTrans.reset();
                procFTPUpConfirm(msg);
                break;
            case MessageID.rcpExpSeclectedFileDiskCheck:
                RLog.d("rcpExpSeclectedFileDiskCheck");
                procFTPFileDiskCheck(msg);
                break;
            case MessageID.rcpExpFolderInfomationRequest:
                RLog.d("rcpExpFolderInfomationRequest");
                sendDownLoadInformation(msg);
                break;
        }
    }

    private boolean procFTPFileDiskCheck(MsgPacket msg) {
        boolean ret = false;
        try {
            String filePath[];
            RLog.d("procFTPFileDiskCheck  :" + msg.getData());
//			RLog.d("procFTPFileDiskCheck _CoreVariables.sendMaxFileSize  :"
//					+ CoreVariables.sendMaxFileSize);
            //Todo sendMaxFileSize 웹에서 값 받는 부분 없음

            Long maxFileSize = 200l; /*Long.parseLong(CoreVariables.sendMaxFileSize);*/
            maxFileSize = maxFileSize * 1048576L;
            String strBody = new String(msg.getData(), "UTF-16LE");

            RLog.d("procFTPFileDiskCheck_strBody :" + strBody);
            int fileCount = fileDiskCheckFileCount(strBody);

            RLog.d("procFTPFileDiskCheck_fileCount" + fileCount);
            filePath = getFilenameOnConfirm(strBody, fileCount);
            Long fileSize = 0L;
            for (int i = 0; i < fileCount; i++) {

                fileSize += getFileSizeOnConfirm(filePath[i]);
            }
            RLog.d("procFTPFileDiskCheck_fileSize  : " + fileSize);
            RLog.d("procFTPFileDiskCheck_maxFileSize  : " + maxFileSize);
            if (maxFileSize > fileSize) {
                RLog.d("procFTPFileDiskCheck_rcpExpSeclectedFileDiskCheck_OK");
                sendPacket(MessageID.rcpSFTP,
                        MessageID.rcpExpSeclectedFileDiskCheck_OK);
            } else {
                RLog.d("procFTPFileDiskCheck_rcpExpSeclectedFileDiskCheck_FAIL");
                sendPacket(MessageID.rcpSFTP,
                        MessageID.rcpExpSeclectedFileDiskCheck_FAIL);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private void getInfoFromURL(String url) {
        int index1 = url.indexOf("?");
        int index2 = 0;

        String connUrl = url.substring(0, index1);

        index1 = url.indexOf("room_id=");
        index2 = url.indexOf("&", index1);
        String roomid = url.substring(index1 + 8, index2);

        index1 = url.indexOf("agentid=");
        index2 = url.indexOf("&", index1);
        String agentid = url.substring(index1 + 8, index2);

        index1 = url.indexOf("guid=");
        index2 = url.indexOf("}", index1);
        String guid = url.substring(index1 + 5, index2 + 1);

        SharedPreferences pref = mainContext.getSharedPreferences("reconnect.info", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("connurl", connUrl);
        editor.putString("roomid", roomid);
        editor.putString("agentid", agentid);
        editor.putString("guid", guid);
        editor.putLong("timelife", System.currentTimeMillis());
        editor.commit();
    }

    private void procRcpReboot(MsgPacket msg) {
        try {
            switch ((int) msg.getMsgID()) {
                case MessageID.rcpReConnectRequest:
//				ApplicationLockActivity.requestConfirmPopup(ApplicationLockActivity.LOCKCONFIRM_REBOOT);
//				callConfirmActivity();
                    break;
                case MessageID.rcpReConnectInfo:
                    String reconnInfoStr = new String(msg.getData(), "UTF-16LE");
                    reconnInfoStr = reconnInfoStr.trim();
                    getInfoFromURL(reconnInfoStr);
                    break;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void getInfoFromSessionInfo(String url) {
        int index1 = url.indexOf("?");
        int index2 = 0;

        index1 = url.indexOf("agentid=");
        index2 = url.indexOf("&", index1);
        String agentid = url.substring(index1 + 8, index2);

        index1 = url.indexOf("guid=");
        index2 = url.indexOf("}", index1);
        String guid = url.substring(index1 + 5, index2 + 1);

        index1 = url.indexOf("room_id=");
        index2 = url.indexOf("&", index1);
//		String roomid = url.substring(index1+8, index1+8+9);
        String roomid = url.substring(index1 + 8, index2 != -1 ? index2 : url.length());

        RLog.d("***********************************************************************");
        RLog.d("******* session transfer information from another viewe r**************");
        RLog.d("***********************************************************************");
        RLog.d("SessionTransInfo agentid : " + agentid);
        RLog.d("SessionTransInfo guid    : " + guid);
        RLog.d("SessionTransInfo room_id : " + roomid);
        RLog.d("***********************************************************************");

        SharedPreferences pref = mainContext.getSharedPreferences("reconnect.info", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("agentid", agentid);
        editor.putString("guid", guid);
        editor.putString("roomid", roomid);
        editor.commit();
    }

    private void procSessionTransfer(MsgPacket msg) {
        try {
            switch ((int) msg.getMsgID()) {
                case MessageID.rcpSessionTransferRequest:
//				ApplicationLockActivity.requestConfirmPopup(ApplicationLockActivity.LOCKCONFIRM_SESSIONTRANS);
//				callConfirmActivity();
                    break;
                case MessageID.rcpSessionTransferInfo:
//    			String sessionInfo = new String(msg.data, "UTF-16LE");
                    String sessionInfo = getString(msg);
                    getInfoFromSessionInfo(sessionInfo);
//    			callNewSession();
                    break;
                case MessageID.rcpSessionTransferConnected:
                    break;
                case MessageID.rcpSessionTransferDisconnected:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void procMobile(MsgPacket msg) {
        RLog.d("Payload_Type : rcpMobile : " + (int) msg.getMsgID());
        switch ((int) msg.getMsgID()) {
            case MessageID.rcpMobileNotiBarDownRequest:
                RLog.d("MessageID.rcpMobile : rcpMobileNotiBarDownRequest");
//			downNotifications();
                break;
            case MessageID.rcpMobileNotiBarUpRequest:
                RLog.d("MessageID.rcpMobile : rcpMobileNotiBarUpRequest");
//			upNotifications();
                break;
            case MessageID.rcpMobileShowSupport:
                RLog.d("MessageID.rcpMobile : rcpMobileShowSupport");
                visiblOverlayView(true);
                break;
            case MessageID.rcpMobileHideSupport:
                RLog.d("MessageID.rcpMobile : rcpMobileHideSupport");
                visiblOverlayView(false);
                break;
            case MessageID.rcpMobileOpenScreenRequest:
                RLog.d("MessageID.rcpMobile : rcpMobileOpenScreenRequest");
                controlRequest();
                break;
            case MessageID.rcpMobileDualSimChange:
                RLog.d("MessageID.rcpMobile : rcpDualSimChange");
                setDualCast();
                break;
            case MessageID.rcpMobileRecentApp:
                RLog.d("rcpMobileResentApp");
                recentApp();
                break;
            case MessageID.rcpMobileFTPCancle:
                RLog.d("MessageID.rcpMobile : rcpMobileFTPCancle");
                Global.getInstance().getAgentThread().getChannelAgentFTP().getRcmpFTPChannel().procCancelFile(msg);
                break;
            case MessageID.rcpMobileHome:
//			GlobalStatic.home();
                break;
            case MessageID.rcpMobileQuickSettingHome:
                RLog.d("MessageID.rcpMobile : rcpMobileQuickSettingHome");
                callSettingPage();
                break;
            case MessageID.rcpMobileSystemInfoRequest:
                RLog.d("rcpMobileSystemInfoRequest");
                sendSystemTabInfo(msg);
                break;
            //quicksetting 사용안함
            case MessageID.rcpMobileSystemQuickSettings:
                RLog.d("rcpMobileSystemQuickSettings");
//			callSettingPages((int)(msg.data[0]));
                break;

            case MessageID.rcpMobileProcessInfoRequest:
                RLog.d("rcpMobileProcessInfoRequest");
                sendProcessTabInfo(msg);
                break;

            case MessageID.rcpMobileProcessChartInfo:
                RLog.d("rcpMobileProcessChartInfo");
                CPUUsageInfo.getInstance().exec();
                sendChartInfo();
                break;

            case MessageID.rcpMobileProcessKill:
                RLog.d("rcpMobileProcessKill");
                callProcessKillThread(msg);
                break;

            case MessageID.rcpMobileApplicationInfoRequest:
                RLog.d("rcpMobileApplicationInfoRequest");
                sendApplicationInfo(msg);
                break;

            case MessageID.rcpMobileApplicationRemove:
                RLog.d("rcpMobileApplicationRemove");
//			ApplicationLockActivity.requestConfirmPopup(ApplicationLockActivity.LOCKCONFIRM_APPREMOVE);
                String strRemoveAppName = getString(msg);
                Uri uri = applicationInfo.getPackageUriFrom(strRemoveAppName);
                if (uri != null && strRemoveAppName != null) {
                    mainContext.startActivity(new Intent(Intent.ACTION_DELETE, uri).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
                break;

            case MessageID.rcpMobileApplicationRun:
//			if(!ApplicationLockActivity.mIsFinish) return;  //appLockDisplaying
                RLog.d("rcpMobileApplicationRun");
                runApplication(getString(msg));
                checkAppRunning(getString(msg));
                break;

            case MessageID.rcpMobileApplicationDetailInfo:
                RLog.d("rcpMobileApplicationDetailInfo");
                sendApplicationDetailInfo(msg);
                break;

            case MessageID.rcpMobileLogInfoRequest:
                RLog.d("rcpMobileLogInfoRequest");
                setLogcommand(getLogCommandString(msg));
                sendLogTabInfo(msg);
                break;

            case MessageID.rcpMobileMessageRequest:
                RLog.d("rcpMobileMessageRequest");
//			addChatItemMessage(getString(msg));	
                showMessage(getString(msg));
                break;

            case MessageID.rcpMobileMessageChatRequest:
                RLog.d("rcpMobileMessageChatRequest");
//			callChatting(getString(msg));
                break;

            case MessageID.rcpMobileCameraRequest:
                RLog.d("rcpMobileCameraRequest");
                callCamera();
                break;

            case MessageID.rcpMobileCameraKill:
                RLog.d("rcpMobileCameraKill");
                killCamera();
                break;

            case MessageID.rcpMobileSpeekerPhoneOnRequest:
                RLog.d("rcpMobileSpeekerPhoneOnRequest");
                startSpeakerPhone();
                break;

            case MessageID.rcpMobileSpeekerPhoneOffRequest:
                RLog.d("rcpMobileSpeekerPhoneOffRequest");
                stopSpeakerPhone();
                break;

            case MessageID.rcpMobileConfig:
                RLog.d("rcpMobileConfig");
                callSettingPage();
                break;

            case MessageID.rcpMobileRotateLockRequest:
                RLog.d("rcpMobileRotateLockRequest");
                lockScreenRotation();
                break;

            case MessageID.rcpMobileRotateReleaseRequest:
                RLog.d("rcpMobileRotateReleaseRequest");
                unlockScreenRotation();
                break;

            case MessageID.rcpMobileAppOpt:
                RLog.d("rcpMobileAppOpt");
                break;

            case MessageID.rcpMobileINIT:
                RLog.d("rcpMobileINIT");
                break;

            case MessageID.rcpKT_INIT:
                RLog.d("rcpKT_INIT");
                sSavedSRNum = getString(msg);
                break;

            case MessageID.rcpMobilePauseRequest:
                RLog.d("rcpMobilePauseRequest");
                break;

            case MessageID.rcpMobileScreenChannelStart:
                RLog.d("rcpMobileScreenChannelStart");
                break;

            case MessageID.rcpMobileUnPauseRequest:
                RLog.d("rcpMobileUnPauseRequest");
                break;
            case MessageID.rcpMobileActivityAppRequestOn:
                RLog.d("rcpMobileActivityAppRequestOn");
                setScreenNameSender(true);
                break;

            case MessageID.rcpMobileActivityAppRequestOFF:
                RLog.d("rcpMobileActivityAppRequestOFF");
                setScreenNameSender(false);
                break;
            // 0 - capture, 1 - record start, 2 - record stop
            case MessageID.rcpMobileMessageHelper:
                RLog.d("rcpMobileMessageHelper");
                break;

            case MessageID.rcpMobile_SamsungPrinterServiceMode:
                RLog.d("rcpMobile_SamsungPrinterServiceMode");
                callSamsungPrinterServiceMode();
                break;


        }
    }

    private void procRcpLaserPointer(final MsgPacket msg) {
        try {
            switch ((int) msg.getMsgID()) {
                case MessageID.rcpLaserPointerStart:
                    Point pointerStart = computeCoordinate.compute(Converter.byte2Toshort(msg.getData(), 1), Converter.byte2Toshort(msg.getData(), 3));
                    if (mouseEventListener != null) {
                        mouseEventListener.onEvent(new MouseEvent(TopmostText.REMOTESTATE_LASER, pointerStart.x, pointerStart.y));
                    }
                    break;
                case MessageID.rcpLaserPointerPos:
                    Point pointerPos = computeCoordinate.compute(Converter.byte2Toshort(msg.getData(), 0), Converter.byte2Toshort(msg.getData(), 2));
                    if (mouseEventListener != null) {
                        mouseEventListener.onEvent(new MouseEvent(TopmostText.REMOTESTATE_LASER, pointerPos.x, pointerPos.y));
                    }
                    break;
                case MessageID.rcpLaserPointerEnd:
                    if (mouseEventListener != null) {
                        mouseEventListener.onEvent(new MouseEvent(TopmostText.REMOTESTATE_RELEASED));
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HxdecThread getHxdecThread() {
        return Global.getInstance().getAgentThread();
    }

    public PacketHandler<MsgPacket> getPacketHandler() {
        return this;
    }

    @Override
    public void onReceive(int payload, @NotNull MsgPacket msgPacket) {
        RLog.d("receiveData");
        try {
            RLog.d("DataChannel receiveData payload : " + payload);
            switch (payload) {
                case MessageID.rcpXENC264Stream: {
                    if (msgPacket.getMsgID() == MessageID.rcpXENC264StreamStart) {
                        if (screenStreamController != null) {
                            screenStreamController.start();
                        }
                    } else if (msgPacket.getMsgID() == MessageID.rcpXENC264StreamStop) {
                        if (screenStreamController != null) {
                            screenStreamController.stop();
                        }
                    }
                }
                break;
                case MessageID.rcpX264Stream:
                    if (msgPacket.getMsgID() == MessageID.rcpX264StreamStart) {
                        if (screenStreamController != null) {
                            screenStreamController.start();
                        }
                    } else if (msgPacket.getMsgID() == MessageID.rcpX264StreamReload) {
                        if (screenStreamController != null) {
                            screenStreamController.restart();
                        }
                    } else if (msgPacket.getMsgID() == MessageID.rcpX264StreamStop) {
                        if (screenStreamController != null) {
                            screenStreamController.stop();
                        }
                    } else if (msgPacket.getMsgID() == MessageID.rcpX264StreamResume) {
                        if (screenStreamController != null) {
                            screenStreamController.resume();
                        }
                    } else if (msgPacket.getMsgID() == MessageID.rcpX264StreamPause) {
                        if (screenStreamController != null) {
                            screenStreamController.pause();
                        }
                    }
                    break;
                case MessageID.rcpChannel:
                    if (msgPacket.getMsgID() == MessageID.rcpChannelConnectRequest) {
                        RLog.d("rcpChannel rcpChannelConnectRequest");
                        byte[] data = msgPacket.getData();
                        int channelID = data[0];
                        int port = Converter.readIntLittleEndian(data, 1);
                        String connectGuid = "";
                        if (msgPacket.getData().length > 7) {
                            byte[] guid = new byte[msgPacket.getData().length - 7];
                            System.arraycopy(msgPacket.getData(), 5, guid, 0, msgPacket.getData().length - 7);
                            connectGuid = new String(guid, "UTF-16LE");
                        }
                        deviceScreenOn();
                        Global.getInstance().getAgentThread().connectChannel(channelID, port, connectGuid);
                    }
                    break;
                case MessageID.rcpKeyMouseCtrl:
                    RLog.d("payloadtype : rcpKeyMouseCtrl");
                    procRcpKeyMouseCtrl(msgPacket);
                    break;
                case MessageID.rcpSysInfo:
                    RLog.d("payloadtype : rcpSysInfo");
                    if (msgPacket.getMsgID() == MessageID.rcpSystemInfoRequest) {
                        String systemInfo = systemInfoLazy.getValue().getSystemInfo();
                        byte[] sysInfo = getBytesFromString(systemInfo);
                        sendPacket(MessageID.rcpMobile, MessageID.rcpMobileSystemInfo, sysInfo, sysInfo.length);
                    }
                    break;
                case MessageID.rcpProcess:
                    RLog.d("payloadtype : rcpProcess");
                    procRcpProcess(msgPacket);
                    break;
                case MessageID.rcpSFTP:
                    RLog.d("MessageID.rcpSFTP");
                    procRcpSFTP(msgPacket);
                    break;
                case MessageID.rcpRebootConnect:
                    procRcpReboot(msgPacket);
                    break;
                case MessageID.rcpLaserPointer:
                    procRcpLaserPointer(msgPacket);
                    break;
                case MessageID.rcpSessionTransfer:
                    procSessionTransfer(msgPacket);
                    break;
                case MessageID.rcpMobile:
                    procMobile(msgPacket);
                    break;
                case MessageID.rcpDraw:
                    RLog.d("Payload_Type : rcpDraw");
                    switch ((int) msgPacket.getMsgID()) {
                        case MessageID.rcpDrawStart:
                            RLog.d("rcpDrawStart");
                            startDrawScreen();
                            break;

                        case MessageID.rcpDrawEnd:
                            RLog.d("rcpDrawEnd");
                            stopDrawScreen();
                            break;

                        case MessageID.rcpDrawInfo:
                            RLog.d("rcpDrawInfo");
                            //ignore (default_rcpDrawInfoMsg : free, red, 1, 0)
                            setDrawInfo(msgPacket.getData());
                            break;

                        case MessageID.rcpDrawData:
                            RLog.d("rcpDrawData");
                            drawFreeLine(msgPacket.getData());
                            break;

                        case MessageID.rcpDrawClear:
                            RLog.d("rcpDrawClear");
                            clearDrawScreen();
                            break;
                    }
                    break;

                case MessageID.rcpFavorite:
                    RLog.d("Payload_Type : rcpFavorite");
                    switch (msgPacket.getMsgID()) {
                        case MessageID.rcpFavoriteURL:
                            callBrowser(msgPacket);
                            break;
                    }
                    break;

                case MessageID.rcpClipboard:
                    RLog.d("Payload_Type : rcpClipboard");

                    switch (msgPacket.getMsgID()) {
                        case MessageID.rcpClipboardDataRequest:
                            RLog.d("rcpClipboardDataRequest");
                            sendClipboardData();
                            break;

                        case MessageID.rcpClipboardDataBegin:
                            RLog.d("rcpClipboardDataBegin");
                            setClipboardDataBegin(msgPacket);
                            break;

                        case MessageID.rcpClipboardData:
                            RLog.d("rcpClipboardData");
                            setClipboardData(msgPacket);
                            break;

                        case MessageID.rcpClipboardDataEnd:
                            RLog.d("rcpClipboardDataEnd");
                            setClipboardDataEnd();
                            break;
                    }
                    break;
                case MessageID.rcpOption:
                    RLog.d("Payload_Type : rcpOption");

                    break;
                case MessageID.rcpScreenCaptur:
                    RLog.d("Payload_Type : rcpScreenCaptur");
                    switch (msgPacket.getMsgID()) {
                        case MessageID.rcpScreenCaptureRequest:
                            break;
                    }
                    break;
                case MessageID.rcpRecord:
                    RLog.d("Payload_Type : rcpRecord");
                    switch (msgPacket.getMsgID()) {
                        case MessageID.rcpScreenRecordRequest:
                            break;
                    }
                    break;

                case MessageID.rcpTerminalInfo:
                    RLog.d("rcpTerminalInfo");
                    switch (msgPacket.getMsgID()) {
                        case MessageID.rcpTerminalInfoRequest:
                            RLog.d("rcpTerminalInfoRequest");
                            EngineConfigSetting.isPC_Viewer = true;
                            sendTerminalInfo();
                            sendAndroidInfo();
                            break;
                    }
                    break;
                ///////////////////////////////////////////////////////////////////////////////////RV ADD
                case MessageID.rcpResolution:
                    RLog.d("rcpResolution");
                    switch (msgPacket.getMsgID()) {
                        case MessageID.rcpResolutionCurrentMode:
                            RLog.d("rcpResolutionCurrentMode ");
                            short width = (short) systemInfoLazy.getValue().getDisplayWidth();
                            short height = (short) systemInfoLazy.getValue().getDisplayHeight();
                            RLog.d("rcpResolutionCurrentMode : " + width + " , " + height);
                            byte sendpacket[] = new byte[5];
                            System.arraycopy(Converter.getBytesFromShortLE(width), 0, sendpacket, 0, 2);
                            System.arraycopy(Converter.getBytesFromShortLE(height), 0, sendpacket, 2, 2);
                            sendpacket[4] = 0;
                            sendPacket(MessageID.rcpResolution, MessageID.rcpResolutionCurrentMode, sendpacket, sendpacket.length);

                            break;
                    }
                    break;
                default:
                    RLog.d("PayloadType : (" + payload + ") is not define.");
                    checkClipboardText();
//			sendAndroidInfo();
            }
        } catch (Exception e) {
            RLog.e(e);
        }
    }

    private void setScreenNameSender(boolean isSend) {
        if (screenNameSender == null) {
            screenNameSender = new ScreenNameSender(mainContext, this);
        }
        if (isSend == true) {
            screenNameSender.start();
        } else {
            screenNameSender.stop();
        }
    }

    private void sendAndroidInfo() {
        if (!isDataChannelStart) {
            sendAndroidEngineVersion();
            sendAndroidProtocolVersion();

            sendMobileInfo();
            sendScreenConnInfo();
            sendKeyboardMouseConnInfo();
            initProcess();
            isDataChannelStart = true;
        }
    }

    private void sendAndroidEngineVersion() {
        try {
            byte[] noneNullBytes = String.valueOf(GlobalStatic.androidEngineVersion).getBytes("UTF-16LE");
            byte[] addNullBytes = new byte[noneNullBytes.length + 2];
            System.arraycopy(noneNullBytes, 0, addNullBytes, 0, noneNullBytes.length);

            RLog.d("androidEngineVersion : " + GlobalStatic.androidEngineVersion);
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileEngineVersion, addNullBytes, addNullBytes.length);

        } catch (Exception e) {

        }
    }

    private void sendAndroidProtocolVersion() {
        try {
            byte[] noneNullBytes = String.valueOf(GlobalStatic.androidProtocolVersion).getBytes("UTF-16LE");
            byte[] addNullBytes = new byte[noneNullBytes.length + 2];
            System.arraycopy(noneNullBytes, 0, addNullBytes, 0, noneNullBytes.length);

            RLog.d("androidProtocolVersion : " + GlobalStatic.androidProtocolVersion);
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileVersion, addNullBytes, addNullBytes.length);

        } catch (Exception e) {

        }
    }

    public void initProcess() {
        basicInfoPacket = getBasicInfo();
        checkBasicInfoByteValue();
        if (basicInfoPacket != null && basicInfoPacket.length > 0) {
            sendSystemInfo();
        }
        if (settingInfoPacket != null && GlobalStatic.androidProtocolVersion == 1) {
            RLog.d("rcpMobileSystemInfo : " + settingInfoPacket.length);
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileSystemInfo, settingInfoPacket, settingInfoPacket.length);
            settingInfoPacket = null;
        }
        checkSystemProperties();
        checkNetworkState();
    }


    private void sendSystemTabInfo(MsgPacket msg) {
        if ((int) (msg.getData()[0]) == 0) {
            basicInfoPacket = getBasicInfo();
            checkBasicInfoByteValue();
            if (basicInfoPacket != null && basicInfoPacket.length > 0) {
                sendSystemInfo();
            }
        } else if ((int) (msg.getData()[0]) == 1) {
            sendSettingInfo();
        }
    }

    private void sendSystemInfo() {
        if (basicInfoPacket == null) return;
        RLog.d("sendSystemInfo");
        compressedBytes = compress.compressData(basicInfoPacket);
        sendPacket(MessageID.rcpMobile, MessageID.rcpMobileSystemInfo, compressedBytes, compressedBytes.length);
        compressedBytes = null;
        basicInfoPacket = null;
    }

    /*
     * Only protocol version 1 (No compress)
     */
    private void sendSettingInfo() {
        settingInfoPacket = null;
        getSettingInfo();
        checkSettingInfoByteValue();
        if (settingInfoPacket != null && settingInfoPacket.length > 0) {
            RLog.d("sendSettingInfo");
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileSystemInfo, settingInfoPacket, settingInfoPacket.length);
            settingInfoPacket = null;
        }
    }

    private void sendProcessTabInfo(MsgPacket msg) {
        if ((int) (msg.getData()[0]) == 0) {
            sendMemoryInfo();
        } else if ((int) (msg.getData()[0]) == 1) {
            sendProcessAppInfo(msg);
        }
    }

    /*
     * Only protocol version 1 (No compress)
     */
    private void sendMemoryInfo() {
        memoryInfoPacket = getMemoryInfoPacket();
        if (memoryInfoPacket.length > 0) {
            RLog.d("sendMemoryInfo");
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileProcessInfo, memoryInfoPacket, memoryInfoPacket.length);
            memoryInfoPacket = null;
        }
    }

    /*
     * No compress (If proceeeInfoPacket is compressed, compression's size will be increase.)
     */
    private void sendProcessAppInfo(MsgPacket msg) {
        List<ProcessItem> processItems = processInfo.loadProcessItems();
        processInfoPacket = getProcessItemsBytes(processItems);
        if (processInfoPacket.length > 0) {
            for (byte[] bytes : processInfoPacket) {
                RLog.d("sendProcessAppInfo : + " + bytes.length);
                sendPacket(MessageID.rcpMobile, MessageID.rcpMobileProcessInfo, bytes, bytes.length);
                if (!socketCompat.isConnected()) return;
            }
            processInfoPacket = null;
        } else {
            RLog.d("ProInfoPacket Null");
        }
        RLog.d("rcpMobileProcessInfoEnd");
        if (msg.getData() != null && msg.getData().length > 0) {
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileProcessInfoEnd, new byte[]{msg.getData()[0]}, 1);
        }
    }

    private byte[][] getProcessItemsBytes(List<ProcessItem> processItems) {
        boolean isAddImage = false;
        if (Utility.isWifiReady(Utility.mainContext)) {
            isAddImage = true;
        }
        ByteArrayOutputStream oStream = new ByteArrayOutputStream();
        byte[][] processItemsBytes = null;
        byte[] bmpBytes = null;
        byte[] textBytes = null;
        byte[] itemInfoBytes = null;
        byte[] noneNullBytes = null;

        int size = processItems.size();
        processItemsBytes = new byte[size][];
        for (int index = 0; index < size; index++) {
            ProcessItem pi = processItems.get(index);

            if (pi.getIcon() != null && isAddImage && pi.getIcon().getClass().equals(BitmapDrawable.class)) {
                BitmapDrawable bd = (BitmapDrawable) pi.getIcon();
                bd.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, oStream);
                bmpBytes = oStream.toByteArray();
            }
            noneNullBytes = pi.getProcessInfoString().getBytes(StandardCharsets.UTF_16LE);
            textBytes = new byte[noneNullBytes.length + 2];
            System.arraycopy(noneNullBytes, 0, textBytes, 0, noneNullBytes.length);

            if (bmpBytes != null) {
                itemInfoBytes = new byte[1 +                        //type
                        4 +                        //text byte length
                        4 +                        //bmp byte length
                        textBytes.length +        //text byte
                        bmpBytes.length];            //bmp byte
            } else {
                itemInfoBytes = new byte[1 +                        //type
                        4 +                        //text byte length
                        4 +                        //bmp byte length
                        textBytes.length            //text byte
                        ];                        //bmp byte
            }
            int startPos = 0;
            System.arraycopy(new byte[]{Converter.getBytesFromIntLE(1)[0]}, 0, itemInfoBytes, startPos, 1);
            startPos += 1;
            System.arraycopy(Converter.getBytesFromIntLE(textBytes.length), 0, itemInfoBytes, startPos, 4);
            startPos += 4;
            if (bmpBytes != null) {
                System.arraycopy(Converter.getBytesFromIntLE(bmpBytes.length), 0, itemInfoBytes, startPos, 4);
            } else {
                System.arraycopy(Converter.getBytesFromIntLE(0), 0, itemInfoBytes, startPos, 4);
            }
            startPos += 4;
            System.arraycopy(textBytes, 0, itemInfoBytes, startPos, textBytes.length);
            startPos += textBytes.length;
            if (bmpBytes != null) {
                System.arraycopy(bmpBytes, 0, itemInfoBytes, startPos, bmpBytes.length);
                startPos += bmpBytes.length;
            }
            processItemsBytes[index] = itemInfoBytes;
            bmpBytes = null;
            textBytes = null;
            itemInfoBytes = null;
            oStream.reset();
        }
        try {
            oStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        RLog.i("return processItemsBytes");
        return processItemsBytes;
    }


    /*
     * No compress (If charInfoPacket is compressed, compression's size will be increase.)
     */
    private void sendChartInfo() {
        chartInfoPacket = getChartInfoPacket();
        if (chartInfoPacket.length > 0) {
            RLog.d("sendChartInfo");
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileProcessChartInfo, chartInfoPacket, chartInfoPacket.length);
            chartInfoPacket = null;
        }
    }

    /*
     * No compress (If appInfoPacket is compressed, compression's size will be increase.)
     */
    private MsgPacket msgPacket;

    private void sendApplicationInfo(MsgPacket msg) {
        msgPacket = msg;
        sendAppRequestInfo(msgPacket);
    }

    private synchronized void sendAppRequestInfo(MsgPacket msg) {
        if (msg != null) {
            msgPacket = null;
            appInfoPacket = null;
            getAppInfo((int) (msg.getData()[1]));
            if (appInfoPacket != null && appInfoPacket.length > 0) {
                RLog.d("sendAppRequestInfo");
                for (int i = 0; i < appInfoPacket.length; i++) {
                    if (msgPacket != null) break;
                    sendPacket(MessageID.rcpMobile, MessageID.rcpMobileApplicationInfo, appInfoPacket[i], appInfoPacket[i].length);
                    appInfoPacket[i] = null;
                }
                appInfoPacket = null;
            }
        }
        RLog.d("rcpMobileApplicationInfoEnd");
        sendPacket(MessageID.rcpMobile, MessageID.rcpMobileApplicationInfoEnd, new byte[]{msg.getData()[0]}, 1);
    }

    private void sendApplicationDetailInfo(MsgPacket msg) {
        appDetailInfoPacket = null;
        getAppDetailInfoPacket(getString(msg));
        if (appDetailInfoPacket != null && appDetailInfoPacket.length > 0) {
            RLog.d("sendApplicationDetailInfo");
            compressedBytes = compress.compressData(appDetailInfoPacket);
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileApplicationDetailInfo, compressedBytes, compressedBytes.length);
            compressedBytes = null;
            appDetailInfoPacket = null;
        }
    }

    private void sendLogTabInfo(MsgPacket msg) {
        try {
            logInfoPacket = null;
            checkLogInfoByteValue();
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileLogInfoEnd);
//				OperationRecord.getInstance().writeLog(OperationRecord.LOG_INF);
        } catch (Exception e) {
            RLog.e("rcpMobileLogInfoFail : " + Log.getStackTraceString(e));
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileLogInfoFail);
        }
    }

    private void sendMobileInfoForKeyMap() {
        WindowManager wm = (WindowManager) mainContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int width = systemInfoLazy.getValue().getDisplayWidth();
        int height = systemInfoLazy.getValue().getDisplayHeight();
        Point size = new Point();
        if (Build.VERSION.SDK_INT > 13) {
            display.getRealSize(size);
        } else {
            size.x = display.getWidth();
            size.y = display.getHeight();

        }
        RLog.e("ScreenSize : X :: " + size.x + " Y:: " + size.y);
        RLog.e("Rotatation : " + display.getRotation());
        if ((display.getRotation() != Surface.ROTATION_0) && display.getRotation() != Surface.ROTATION_180) {
            if (!Utility.isLandscape(size, display) && !isDefaultLandscapeDevice()) {
                width = systemInfoLazy.getValue().getDisplayHeight();
                height = systemInfoLazy.getValue().getDisplayWidth();
            }

            if (EngineConfigSetting.isPC_Viewer) {
                if (DisplayUtils.isHorizontalDevice(display, width, height)) {
                    width = systemInfoLazy.getValue().getDisplayHeight();
                    height = systemInfoLazy.getValue().getDisplayWidth();
                }
            }
        }

        String mobileInfo = systemInfoLazy.getValue().getFirmwareVersion() + "&/" + systemInfoLazy.getValue().getManufacturer() + "&/" + width + "&/" + height;
        try {
            byte[] noneNullBytes = mobileInfo.toLowerCase().getBytes("UTF-16LE");
            byte[] addNullBytes = new byte[noneNullBytes.length + 2];
            System.arraycopy(noneNullBytes, 0, addNullBytes, 0, noneNullBytes.length);

            RLog.d("rcpMobileInformation : " + mobileInfo);
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileKeyMap, addNullBytes, addNullBytes.length);

            noneNullBytes = null;
            addNullBytes = null;
        } catch (Exception e) {

        }
    }

    private boolean isDefaultLandscapeDevice() {
        return (Build.MODEL.equals("QUAD-CORE A33 y3") || BuildConfig.FLAVOR.equals("vuzix"));
    }

    private void sendInputMethodInfo() {
        String inputMethod = Utility.getKeyboardEx(mainContext);
        try {
            byte[] noneNullBytes = inputMethod.getBytes("UTF-16LE");
            byte[] addNullBytes = new byte[noneNullBytes.length + 2];
            System.arraycopy(noneNullBytes, 0, addNullBytes, 0, noneNullBytes.length);

            RLog.d("rcpMobileInputMethod : " + inputMethod);
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileInputMethod, addNullBytes,
                    addNullBytes.length);

            noneNullBytes = null;
            addNullBytes = null;
        } catch (Exception e) {
        }
    }

    private void sendIpAddress() {
        String ipAddress = Utility.getIPAddress(mainContext);
        try {
            byte[] noneNullBytes = ipAddress.getBytes("UTF-16LE");
            byte[] addNullBytes = new byte[noneNullBytes.length + 2];
            System.arraycopy(noneNullBytes, 0, addNullBytes, 0, noneNullBytes.length);

            RLog.d("rcpMobileIPAddress : " + ipAddress);
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileIPAddress, addNullBytes, addNullBytes.length);

            noneNullBytes = null;
            addNullBytes = null;
        } catch (Exception e) {
        }
    }

    private void sendMobileInfo() {

        sendMobileInfoForKeyMap(); // 모바일 인포 EX
        sendInputMethodInfo();
        sendIpAddress();
        String mobileInfo = systemInfoLazy.getValue().getOSName() + "&/" + systemInfoLazy.getValue().getModelName();
        try {
            byte[] noneNullBytes = mobileInfo.toLowerCase().getBytes("UTF-16LE");
            byte[] addNullBytes = new byte[noneNullBytes.length + 2];
            System.arraycopy(noneNullBytes, 0, addNullBytes, 0, noneNullBytes.length);

            RLog.d("rcpMobileInformation : " + mobileInfo);
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileInformation, addNullBytes, addNullBytes.length);

            noneNullBytes = null;
            addNullBytes = null;
        } catch (Exception e) {

        }
    }

    private void sendScreenConnInfo() {
        EngineTypeCheck engineTypeCheck = engineTypeCheckLazy.getValue();
        SdkVersion sdkVersion = sdkVersionLazy.getValue();
        engineTypeCheck.checkEngineType();
        if (engineTypeCheck.getEngineType() == EngineType.ENGINE_TYPE_RSPERM) {
            IRSPerm rsperm = getRsPerm();
            RLog.i("Rsperm :::::::::::::::::::::::::: " + rsperm);
            if (rsperm != null) {
                if (rsperm.isBinded()) {
                    RLog.d("rcpMobileScreenSupport");
                    sendPacket(MessageID.rcpMobile, MessageID.rcpMobileScreenSupport);
                    return;
                } else {
                    RLog.d("!rsperm.isBinded()");
                    if (sdkVersion.greaterThan21()) {
                        sendPacket(MessageID.rcpMobile, MessageID.rcpMobileScreenSupport);
                        return;
                    }
                }
            } else {
                if (sdkVersion.greaterThan21()) {
                    RLog.d("hasLollipop");
                    sendPacket(MessageID.rcpMobile, MessageID.rcpMobileScreenSupport);
                    return;
                }
            }
        } else if (engineTypeCheck.getEngineType() == EngineType.ENGINE_TYPE_KNOX
                || engineTypeCheck.getEngineType() == EngineType.ENGINE_TYPE_SONY) {
            RLog.d("rcpMobile_ScreenSupport_KNOX");
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileScreenSupport);
            return;
        }

        RLog.d("rcpMobileScreenNotSupport");
        sendPacket(MessageID.rcpMobile, MessageID.rcpMobileScreenNotSupport);
    }

    private void sendKeyboardMouseConnInfo() {
        EngineTypeCheck engineTypeCheck = engineTypeCheckLazy.getValue();
        int engineType = engineTypeCheck.getEngineType();
        if (engineType == EngineType.ENGINE_TYPE_RSPERM) {
            IRSPerm rsperm = getRsPerm();
            if (rsperm != null) {
                if (rsperm.isBinded()) {
                    RLog.d("rcpMobileKeyMouseSupport");
                    sendPacket(MessageID.rcpMobile, MessageID.rcpMobileKeyMouseSupport);
                    return;
                } else {
                    RLog.d("!rsperm.isBinded()");
                }
            } else {
                RLog.d("rsperm != nul");

            }
        } else if (engineType == EngineType.ENGINE_TYPE_KNOX) {
            RLog.d("rcpMobile_KNOX_KeyMouseSupport");
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileKeyMouseSupport);
            return;
        }

        RLog.d("rcpMobileKeyMouseNotSupport");
        sendPacket(MessageID.rcpMobile, MessageID.rcpMobileKeyMouseNotSupport);

    }

    public void checkNetworkState() {
        if (Utility.isWifiReady(mainContext)) {
            RLog.d("rcpMobileWIFIResponse");
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileWIFIResponse);
        } else {
            RLog.d("rcpMobile3GResponse");
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobile3GResponse);
        }
    }

    private void checkAppInfoByteValue() {
        int count = 0;
        while (true) {
            if (appInfoPacket != null || count > 100) {
                RLog.e("appInfoPacket_not_null!!!!");
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
    }

    private byte[] getBytesFromString(@NotNull String text) {
        byte[] noneNullBytes = text.getBytes(StandardCharsets.UTF_16LE);
        byte[] addNullBytes = new byte[noneNullBytes.length + 2];
        System.arraycopy(noneNullBytes, 0, addNullBytes, 0, noneNullBytes.length);
        return addNullBytes;
    }


    private byte[] getBasicInfo() {
        String basicInfo = systemInfoLazy.getValue().getBasicInfo();

        byte[] basicInfoBytes = getBytesFromString(basicInfo);

        byte[] basicInfoPacket = new byte[1 +                        //type
                4 +                        //basicInfo length
                basicInfoBytes.length];    //basicInfo

        int startPos = 0;
        System.arraycopy(new byte[]{Converter.getBytesFromIntLE(0)[0]}, 0, basicInfoPacket, startPos, 1);
        startPos += 1;
        System.arraycopy(Converter.getBytesFromIntLE(basicInfoBytes.length), 0, basicInfoPacket, startPos, 4);
        startPos += 4;
        System.arraycopy(basicInfoBytes, 0, basicInfoPacket, startPos, basicInfoBytes.length);
        return basicInfoPacket;
    }

    private void getSettingInfo() {
        ((Activity) mainContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String settingInfo = systemInfoLazy.getValue().getSettingInfo();

                byte[] settingInfoBytes = getBytesFromString(settingInfo);

                byte[] settingInfoPacket = new byte[1 +                        //type
                        4 +                        //settingInfo length
                        settingInfoBytes.length];    //settingInfo

                int startPos = 0;

                System.arraycopy(new byte[]{Converter.getBytesFromIntLE(1)[0]}, 0, settingInfoPacket, startPos, 1);
                startPos += 1;
                System.arraycopy(Converter.getBytesFromIntLE(settingInfoBytes.length), 0, settingInfoPacket, startPos, 4);
                startPos += 4;
                System.arraycopy(settingInfoBytes, 0, settingInfoPacket, startPos, settingInfoBytes.length);

                DataChannelImpl.this.settingInfoPacket = settingInfoPacket;
                RLog.d("Get_SettingInfoPacket : " + settingInfoPacket.length);
            }
        });
    }

    /*
     * Process Info
     */
    private byte[] getMemoryInfoPacket() {
        String memoryInfo = processInfo.getMemoryInfo();
        byte[] memoryInfoData = getBytesFromString(memoryInfo);

        byte[] memInfoPacket = new byte[1 +                        //type
                4 +                        //mem byte length
                memoryInfoData.length];        //mem byte
        int startPos = 0;
        System.arraycopy(new byte[]{Converter.getBytesFromIntLE(0)[0]}, 0, memInfoPacket, startPos, 1);
        startPos += 1;
        System.arraycopy(Converter.getBytesFromIntLE(memoryInfoData.length), 0, memInfoPacket, startPos, 4);
        startPos += 4;
        System.arraycopy(memoryInfoData, 0, memInfoPacket, startPos, memoryInfoData.length);

        return memInfoPacket;
    }

    private byte[] getChartInfoPacket() {
        String chartInfo = processInfo.getChartInfo();

        byte[] chartBytes = getBytesFromString(chartInfo);

        byte[] chartInfoPacket = new byte[4 +                        //mem byte length
                chartBytes.length];            //mem byte

        int startPos = 0;
        System.arraycopy(Converter.getBytesFromIntLE(chartBytes.length), 0, chartInfoPacket, startPos, 4);
        startPos += 4;
        System.arraycopy(chartBytes, 0, chartInfoPacket, startPos, chartBytes.length);
        return chartInfoPacket;
    }


    private void callProcessKillThread(final MsgPacket msg) {
        new Thread(new Runnable() {
            public void run() {
                String processName = getString(msg);
                killProcess(processName);
                checkProcessKill(processName);
            }
        }).start();
    }

    public void killProcess(String processName) {
        if (processInfo != null) {
            processInfo.killProcess(processName);
        }
    }

    private void checkProcessKill(final String processName) {
        if (processName == null) return;

        byte[] addNullBytes = getBytesFromString(processName);
        ;

        processInfo.loadProcessItems();

        if (processInfo.checkProcessLive(processName)) {
            RLog.d("rcpMobileProcessKillFail");
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileProcessKillFail, addNullBytes, addNullBytes.length);
        } else {
            RLog.d("rcpMobileProcessKillSuccess");
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileProcessKillSuccess, addNullBytes, addNullBytes.length);
        }
    }

    /*
     * Application Info
     */
    private void getAppInfo(final int appType) {
        Message msg = Message.obtain();
        msg.obj = appType;
//		appInfoHandler.sendMessage(msg);
        appInfoPacket = applicationInfo.loadApps((Integer) msg.obj);
    }


    private boolean runApplication(String appName) {
        if (appName != null) {
            return applicationInfo.runApp(appName);
        } else {
            return false;
        }
    }

    private void checkAppRunning(final String appName) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

                byte[] noneNullBytes = null;
                byte[] addNullBytes = null;

                if (appName != null) {
                    try {
                        noneNullBytes = appName.getBytes("UTF-16LE");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    addNullBytes = new byte[noneNullBytes.length + 2];
                    System.arraycopy(noneNullBytes, 0, addNullBytes, 0, noneNullBytes.length);

                    if (applicationInfo.isAppRunning(appName)) {
                        sendPacket(MessageID.rcpMobile, MessageID.rcpMobileApplicationRunSuccess, addNullBytes, addNullBytes.length);
                    } else {
                        sendPacket(MessageID.rcpMobile, MessageID.rcpMobileApplicationRunFail, addNullBytes, addNullBytes.length);
                    }
                }
            }
        }).start();
    }

    private byte[] appDetailInfoPacket;

    public void getAppDetailInfoPacket(final String packageName) {
        appDetailInfoPacket = applicationInfo.getAppDetailInfoPacket(packageName);
    }

    /*
     * Log Info
     */


    private static String logCommand;

    public static void setLogcommand(String msg) {
        logCommand = msg;
    }

    private void callCamera() {
        RLog.d("callCamera");
        new Handler(Looper.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                try {
                    mainContext.startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (Exception e) {
                    RLog.e("off_CAMERA");
                }
            }
        });
    }

    private void showMessage(final String textMessage) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Utility.showBigFontMessage(textMessage);
            }
        });
    }

    private void killCamera() {
        if (processInfo != null) {
            processInfo.killCamera();
        }
    }

    private void startSpeakerPhone() {
        if (speakerPhone == null) {
            speakerPhone = new SpeakerPhone(mainContext);
        }
        speakerPhone.startSpeakerPhone();
    }

    private void stopSpeakerPhone() {
        if (speakerPhone == null) {
            speakerPhone = new SpeakerPhone(mainContext);
        }
        if (isClose) {
            speakerPhone.stopSpeakerPhoneWithoutToast();
        } else {
            speakerPhone.stopSpeakerPhone();
        }
    }

    public boolean isSpeakerPhoneOn() {
        if (speakerPhone == null) {
            speakerPhone = new SpeakerPhone(mainContext);
        }
        return speakerPhone.checkSpeakerPhoneStatus();
    }


    private void writeSpeakerPhone() {
        if (isNewStateSpeakerPhoneOn && !isClose) {
            RLog.d("rcpMobileSpeekerPhoneOnResponse");
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileSpeekerPhoneOnResponse);
        } else if (!isClose) {
            RLog.d("rcpMobileSpeekerPhoneOffResponse");
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileSpeekerPhoneOffResponse);
        }
    }

    public void callBrowser(MsgPacket msg) {
        Utility.callBrowser(mainContext, getString(msg));
    }

    public void callSettingPage() {
        new Handler(Looper.getMainLooper()).post(() -> mainContext.startActivity(new Intent(Settings.ACTION_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
    }

    public void callSamsungPrinterServiceMode() {
        if (!BuildConfig.FLAVOR.equals("samsungprinter")) {
            return;
        }
        new Handler(Looper.getMainLooper()).post(() -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.MODEL.equals("sec_smdkc210")) {
                intent.setComponent(new ComponentName("ui.service", "com.samsung.xoa.ui.local.android.service.main.ServiceModeLogInActivity"));
            } else if (Build.MODEL.toLowerCase().equals("samsung-printer-tablet")) {
                intent.setComponent(new ComponentName("com.sec.android.ngen.app.servicemode", "com.sec.android.ngen.common.alib.servicemode.main.ServiceModeLogInActivity"));
            }
            mainContext.startActivity(intent);

        });
    }


    public void lockScreenRotation() {
        Settings.System.putInt(mainContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
    }

    public void unlockScreenRotation() {
        Settings.System.putInt(mainContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
    }

    public boolean isScreenRotationLock() {
        int state = 0;
        try {
            state = Settings.System.getInt(mainContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (state == 0) {
            return true;
        } else {
            return false;
        }
    }

    private void checkClipboardText() {
        if (!Clipboard.getInstance(mainContext).isHasText()) return;

        newClipText = Clipboard.getInstance(mainContext).getText();
        if (newClipText == null) return;

        if (!oldClipText.equals(newClipText)) {

            sendClipboardFormat();

            oldClipText = newClipText;
        }
    }

    public void checkSystemProperties() {

        isOldStateScreenlock = isScreenRotationLock();
        isNewStateScreenlock = isOldStateScreenlock;
        writeRotation();

        isOldStateSpeakerPhoneOn = isSpeakerPhoneOn();
        isNewStateSpeakerPhoneOn = isOldStateSpeakerPhoneOn;
        writeSpeakerPhone();

        if (Clipboard.getInstance(mainContext).isHasText()) {
            oldClipText = Clipboard.getInstance(mainContext).getText();
            newClipText = oldClipText;
        } else {
            newClipText = oldClipText = "";
        }
        prepareLocale();
    }


    private String oldLocale;
    private String newLocale;

    private void prepareLocale() {
        oldLocale = Utility.getLocale();
        newLocale = oldLocale;
    }


    private void writeRotation() {
        if (isNewStateScreenlock && !isClose) {
            RLog.d("rcpMobileRotateLocked");
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileRotateLocked);
        } else if (!isClose) {
            RLog.d("rcpMobileRotateReleased");
            sendPacket(MessageID.rcpMobile, MessageID.rcpMobileRotateReleased);
        }
    }

    private void startDrawScreen() {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (screenDraw == null) {
                screenDraw = new ScreenDraw(mainContext);
                screenDraw.onComputeCoordinateChanged(computeCoordinate);
            }
            screenDraw.startDrawScreen();
            screenDraw.returnStatusBarSize();
        });
    }

    private void stopDrawScreen() {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (screenDraw != null) {
                screenDraw.erasePoints();
                screenDraw.stopDrawScreen();
                screenDraw = null;
            }
        });
    }

    public void clearDrawScreen() {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (screenDraw != null) {
                screenDraw.erasePoints();
            }
        });
    }

    private void setDrawInfo(final byte[] drawInfoBytes) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (screenDraw == null) {
                screenDraw = new ScreenDraw(mainContext);
                screenDraw.onComputeCoordinateChanged(computeCoordinate);
            }
            screenDraw.setDrawInfo(drawInfoBytes);
        });
    }

    private void drawFreeLine(final byte[] drawBytes) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (screenDraw == null) {
                screenDraw = new ScreenDraw(mainContext);
                screenDraw.onComputeCoordinateChanged(computeCoordinate);
            }
            screenDraw.setDrawData(drawBytes);
            screenDraw.show();
        });

    }

    private void checkBasicInfoByteValue() {
        int count = 0;
        while (true) {
            if (basicInfoPacket != null || count > 100) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
    }

    private void checkSettingInfoByteValue() {
        int count = 0;
        while (true) {
            if (settingInfoPacket != null || count > 100) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
    }

    private void checkLogInfoByteValue() {
        int count = 0;
        while (true) {
            if (logInfoPacket != null || count > 100) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
    }

    private void sendClipboardFormat() {
        new Thread(new Runnable() {
            public void run() {
                RcpClipboardBeginMsg rcpClipBeginMsg = new RcpClipboardBeginMsg();
//				rcpClipBeginMsg.format = (char)MessageID.Clipboard_UnicodeText;
                rcpClipBeginMsg.format = (byte) 2;

                sendPacket(MessageID.rcpClipboard, MessageID.rcpClipboardDataInfo, rcpClipBeginMsg);
            }
        }).start();
    }

    private void sendClipboardData() {
        new Thread(new Runnable() {
            public void run() {
                String clipText = Clipboard.getInstance(mainContext).getText();

                if (clipText == null) return;

                if (!clipText.contains("\r")) {
                    clipText = clipText.replaceAll("\n", "\r\n");
                }

                try {
                    clipNoneNullBytes = clipText.getBytes("UTF-16LE");//"UTF-16LE"
                    clipBytes = new byte[clipNoneNullBytes.length + 2];
                    System.arraycopy(clipNoneNullBytes, 0, clipBytes, 0, clipNoneNullBytes.length);
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }

                RcpClipboardBeginMsg rcpClipBeginMsg = new RcpClipboardBeginMsg();
//				rcpClipBeginMsg.format = (char)MessageID.Clipboard_UnicodeText;
                rcpClipBeginMsg.format = (byte) 2;
                int clipBytesLen = clipBytes.length;
                rcpClipBeginMsg.datasize = clipBytes.length + 1;

                sendPacket(MessageID.rcpClipboard, MessageID.rcpClipboardDataBegin, rcpClipBeginMsg);

                int dataPos = 0;
                int partSize = 0;
                final int MAX_DATA = zipBuffer.length;
                boolean isFirst = true;
                int initPartSize = 0;

                while (clipBytesLen > 0) {
                    partSize = Math.min(clipBytesLen, MAX_DATA);

                    if (isFirst || partSize < initPartSize) {
                        clipPartByte = new byte[partSize];
                        initPartSize = partSize;
                        isFirst = false;
                    }

                    System.arraycopy(clipBytes, dataPos, clipPartByte, 0, clipPartByte.length);

                    if (compress == null) {
                        compress = new Compress();
                    }

                    compressedBytes = compress.compressData(clipPartByte);

                    sendPacket(MessageID.rcpClipboard, MessageID.rcpClipboardData, compressedBytes, compressedBytes.length);

                    clipBytesLen -= partSize;
                    dataPos += partSize;

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                clipPartByte = null;
                compressedBytes = null;
                clipBytes = null;
                clipNoneNullBytes = null;

                sendPacket(MessageID.rcpClipboard, MessageID.rcpClipboardDataEnd);

            }
        }).start();
    }

    private void setClipboardDataBegin(MsgPacket msg) {
        if (msg.getDataSize() > 0) {
            RcpClipboardBeginMsg rcpClipBeginMsg = new RcpClipboardBeginMsg();
            rcpClipBeginMsg.save(msg.getData(), 0);

            recvClipboardDataPos = 0;
            if (rcpClipBeginMsg.datasize > 0) {
                recvClipboardData = new byte[rcpClipBeginMsg.datasize];
            }
        }
    }

    private void setClipboardData(MsgPacket msg) {
        if (recvClipboardData != null && recvClipboardData.length > 0) {
            if (compress == null) {
                compress = new Compress();
            }
            unzipData = compress.uncompressData(msg.getData());
            System.arraycopy(unzipData, 0, recvClipboardData, recvClipboardDataPos, unzipData.length);

            recvClipboardDataPos += unzipData.length;
            unzipData = null;
        }
    }

    private void setClipboardDataEnd() {
        try {
            RLog.i("setClipboardDataEnd : " + new String(recvClipboardData, 0, recvClipboardData.length - 2, "UTF-16LE"));
            Clipboard.getInstance(mainContext).setText(new String(recvClipboardData, 0, recvClipboardData.length - 2, "UTF-16LE").replaceAll("\r\n", "\n"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        recvClipboardData = null;
        recvClipboardDataPos = 0;
    }

    @Override
    public void close() {
        RLog.v("close");
        applicationInfo.close();
        processInfo.close();
        powerKeyControllerLazy.getValue().release();

        setScreenNameSender(false);

        ((NotificationManager) mainContext.getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
        socketCompat.disconnect();

        if (screenDraw != null) {
            screenDraw.erasePoints();
            screenDraw.hide();
        }

        systemInfoLazy.getValue().close();

        if (!isDefaultLock) unlockScreenRotation();

        EngineTypeCheck engineTypeCheck = engineTypeCheckLazy.getValue();
        if (engineTypeCheck.getEngineType() == EngineType.ENGINE_TYPE_SONY) {
            SonyManager.getInstance().nullSonymRemote();
        }


    }

    private String getString(MsgPacket msg) {
        String text = new String(msg.getData(), 0, msg.getDataSize() - 2, StandardCharsets.UTF_16LE).replaceAll("\r\n", "\n");
        RLog.d(text);
        return text.trim();
    }

    private String getLogCommandString(MsgPacket msg) {
        String text = null;
        try {
            text = new String(msg.getData(), 0, msg.getDataSize() - 2, "UTF-16LE").replaceAll("\r\n", "\n");
        } catch (UnsupportedEncodingException e) {
            RLog.e(Log.getStackTraceString(e));
        }
        if (text.length() > 0)
            text = text.substring(1);

        RLog.d(text);
        return text.trim();
    }

    public boolean sendPacket(int payloadtype, int msgid) {
        return sendPacket(payloadtype, msgid, null, 0);
    }

    public boolean sendPacket(int payloadType, int msgId, Packet data) {
        data.push(sendBuffer, 0);
        return sendPacket(payloadType, msgId, sendBuffer, data.size());
    }

    public boolean sendPacket(int payloadtype, int msgid, byte[] data, int dataSize) {
        headerPacket.clear();
        RLog.e("sendPacket payload !!!!! : " + payloadtype + ", " + msgid);

        int totalPacketSize = 0;
        if (data != null && dataSize > 0) {
            totalPacketSize = MessageID.sz_rcpPacket + MessageID.sz_rcpDataMessage + dataSize;
        } else {
            totalPacketSize = MessageID.sz_rcpPacket + MessageID.sz_rcpMessage;
        }

//		byte sendPacket[] = new byte[totalPacketSize];
        byte sendPacket[] = new byte[totalPacketSize + 1];

//		int packetPos = ChannelProto.sz_rcpPacket;
        int packetPos = MessageID.sz_rcpPacket;

//		sendPacket[0] = (byte)0;

        headerPacket.setPayloadtype(payloadtype);
        headerPacket.setMsgsize(totalPacketSize - MessageID.sz_rcpPacket);
        headerPacket.push(sendPacket, 0);
//		rcpPacket.push(sendPacket, 1);

        if (dataSize > 0 && (data != null)) {
            sendPacket[packetPos] = (byte) msgid;
            packetPos++;
            System.arraycopy(Converter.getBytesFromIntLE(dataSize), 0, sendPacket, packetPos, 4);
            packetPos += 4;
            System.arraycopy(data, 0, sendPacket, packetPos, dataSize);
        } else {
            sendPacket[packetPos] = (byte) msgid;
        }
        boolean ok = false;

        if (totalPacketSize > RC45SocketCompat.BUFFER_SIZE) {
            int count = (totalPacketSize / RC45SocketCompat.BUFFER_SIZE) + 1;


            byte[] tempByte = new byte[RC45SocketCompat.BUFFER_SIZE];
            int position = 0;

            for (int i = 0; i < count; i++) {
                if (i == count - 1) {
                    tempByte = new byte[totalPacketSize - position];

//					RLog.i("================LAST================");
//					RLog.i("position : " + position);
//					RLog.i("totalPacketSize : " + totalPacketSize);
//					RLog.i("tempByte : " + tempByte.length);
                    System.arraycopy(sendPacket, position, tempByte, 0, tempByte.length);
                    ok = socketCompat.getDataStream().write(tempByte, 0, tempByte.length);
                } else {
//					RLog.i("================ " + i +" ================");
//					RLog.i("position : " + position);
//					RLog.i("tempByte.length : " + tempByte.length);
                    System.arraycopy(sendPacket, position, tempByte, 0, tempByte.length);
                    ok = socketCompat.getDataStream().write(tempByte, 0, tempByte.length);
                    position += tempByte.length;

                }

            }

        } else {
            ok = socketCompat.getDataStream().write(sendPacket, 0, totalPacketSize);
        }

//		} catch (RemoteException e) {
//			RLog.e( Log.getStackTraceString(e));
//		}
        if (ok) {
            return true;
        } else {
            RLog.e("WriteToDataChannel_Fail");
            return false;
        }
    }

    @SuppressLint("MissingPermission")
    private void setDualCast() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.DUAL_SIM_SWITCHING_KEY_PRESSED");
        Utility.mainContext.sendStickyBroadcast(intent);
    }

    public void controlRequest() {
        iscontrolRequest = true;
//		callChatting("");
    }

    private void visiblOverlayView(boolean visible) {
        if (mouseEventListener != null) {
            mouseEventListener.onEvent(new MouseEvent(visible ? TopmostText.REMOTESTATE_VISIBLE : TopmostText.REMOTESTATE_INVISIBLE));
        }
    }

    private void recentApp() {
        try {
            Class serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method getService = serviceManagerClass.getMethod("getService", String.class);
            IBinder retbinder = (IBinder) getService.invoke(serviceManagerClass, "statusbar");
            Class statusBarClass = Class.forName(retbinder.getInterfaceDescriptor());
            Object statusBarObject = statusBarClass.getClasses()[0].getMethod("asInterface", IBinder.class).invoke(null, new Object[]{retbinder});
            Method clearAll = statusBarClass.getMethod("toggleRecentApps");
            clearAll.setAccessible(true);
            clearAll.invoke(statusBarObject);
        } catch (Exception e) {
        }

    }

    /**
     * 6.0 통합버전을 위한 코드로 Viewer에게 Android, Mobile-Pack 정보를 전송함(해당 정보로 Viewer 의 UI가 동적으로 구성됨.)
     */
    private void sendTerminalInfo() {
        byte[] terminalInfo = new byte[4];
        System.arraycopy(Converter.getBytesFromShortLE(MessageID.RcpPlatformType.Platform_Android), 0, terminalInfo, 0, 2);
        System.arraycopy(Converter.getBytesFromShortLE(MessageID.RcpStandbyType.StandBy_RViewMP), 0, terminalInfo, 2, 2);

        boolean ret = sendPacket(MessageID.rcpTerminalInfo, MessageID.rcpTerminalInfoResponse, terminalInfo, terminalInfo.length);
        RLog.d("<send> rcpTerminalInfo-rcpTerminalInfoResponse : " + ret);
    }


    private void startTouchEvent(int action, int x, int y, int x2, int y2) {
        if (x <= 0 || y <= 0) {
            RLog.e("x, y - !!!" + x + ", " + y);
            return;
        }

        EngineTypeCheck engineTypeCheck = engineTypeCheckLazy.getValue();
        IRSPerm rsperm = getRsPerm();
        switch (engineTypeCheck.getEngineType()) {
            case EngineType.ENGINE_TYPE_RSPERM:
                if (rsperm == null) return;
                try {
                    rsperm.injectTouchEvent(action, x, y, x2, y2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case EngineType.ENGINE_TYPE_KNOX:
                knoxManagerCompatLazy.getValue().injectPointerEvent(mainContext, action, x, y, x2, y2);
                break;
            case EngineType.ENGINE_TYPE_SONY:
                try {
                    SonyManager sonyManager = SonyManager.getInstance();
                    sonyManager.bind(mainContext);
                    if (sonyManager.isConnected()) {
                        sonyManager.injectWithPrimitive(action, 0, x, y, x2, y2);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }

    }


    private void deviceScreenOn() {
        powerKeyControllerLazy.getValue().on();
    }

    private IRSPerm getRsPerm() {
        return rspermServiceLazy.getValue().getRsperm();
    }

    boolean isPointMove = false;
    int startX;
    int startY;

    private void screenDownEvent(byte action, int x, int y) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startX = x;
                startY = y;
                if (mouseEventListener != null) {
                    mouseEventListener.onEvent(new MouseEvent(TopmostText.REMOTESTATE_CLICK, x, y));
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isPointMove) {
                    if (mouseEventListener != null) {
                        mouseEventListener.onEvent(new MouseEvent(TopmostText.REMOTESTATE_DRAG, x, y));
                    }
                } else {
                    if (Math.abs(startX - x) > 5 || Math.abs(startY - y) > 5) {
                        isPointMove = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                isPointMove = false;
                if (mouseEventListener != null) {
                    mouseEventListener.onEvent(new MouseEvent(TopmostText.REMOTESTATE_RELEASED, x, y));
                }
                break;
        }

    }

    public void sendScreenStatus(boolean isOn) {
        sendPacket(MessageID.rcpMobile, isOn ? MessageID.rcpMobileScreenOn : MessageID.rcpMobileScreenOff);
    }

    private void onComputeCoordinateChanged(ComputeCoordinate computeCoordinate) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (screenDraw == null) {
                screenDraw = new ScreenDraw(mainContext);
            }
            screenDraw.onComputeCoordinateChanged(computeCoordinate);
        });
    }

    @Override
    public void onConfigChanged(@NotNull Configuration newConfig) {
        int width = systemInfoLazy.getValue().getDisplayWidth();
        int height = systemInfoLazy.getValue().getDisplayHeight();
        if (computeCoordinate.getOrientation() == ComputeCoordinate.ORIENTATION_PORTRAIT) {
            computeCoordinate.setTargetResolution(Math.min(width, height), Math.max(width, height));
        } else {
            computeCoordinate.setTargetResolution(Math.max(width, height), Math.min(width, height));
        }
        onComputeCoordinateChanged(computeCoordinate);
    }

    @NotNull
    public OnConfigChangeListener getConfigChangedListener() {
        return this;
    }

    public interface OnMouseEventListener {
        void onEvent(MouseEvent mouseEvent);
    }

    public class MouseEvent {
        private int type = -1;
        private int x = 0;
        private int y = 0;

        /**
         * @param type {@link TopmostText#REMOTESTATE_CLICK}, {@link TopmostText#REMOTESTATE_DRAG}, {@link TopmostText#REMOTESTATE_LASER}, {@link TopmostText#REMOTESTATE_RELEASED}, {@link TopmostText#REMOTESTATE_VISIBLE}, {@link TopmostText#REMOTESTATE_INVISIBLE}
         * @param x    touch x
         * @param y    touch y
         */
        public MouseEvent(int type, int x, int y) {
            this.type = type;
            this.x = x;
            this.y = y;
        }

        public MouseEvent(int type) {
            this(type, 0, 0);
        }

        /**
         * @return {@link TopmostText#REMOTESTATE_CLICK}, {@link TopmostText#REMOTESTATE_DRAG}, {@link TopmostText#REMOTESTATE_LASER}, {@link TopmostText#REMOTESTATE_RELEASED}, {@link TopmostText#REMOTESTATE_VISIBLE}, {@link TopmostText#REMOTESTATE_INVISIBLE}
         */
        public int getType() {
            return type;
        }

        /**
         * @return touch x
         */
        public int getX() {
            return x;
        }

        /**
         * @return touch y
         */
        public int getY() {
            return y;
        }
    }
}
