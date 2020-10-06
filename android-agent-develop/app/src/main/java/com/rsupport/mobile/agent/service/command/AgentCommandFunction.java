package com.rsupport.mobile.agent.service.command;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.rsupport.mobile.agent.R;
import com.rsupport.mobile.agent.utils.Converter;

import org.apache.http.util.ByteArrayBuffer;
import org.koin.java.KoinJavaComponent;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import config.EngineConfigSetting;
import control.Util;

import com.rsupport.mobile.agent.utils.AgentLogManager;
import com.rsupport.mobile.agent.utils.AgentSQLiteHelper;
import com.rsupport.mobile.agent.constant.Global;
import com.rsupport.util.log.RLog;

public class AgentCommandFunction {

    ByteArrayBuffer byteBuf = new ByteArrayBuffer(2048);
    public static WakeLock screenWakeLock;

    public byte[] getRVSystemInfo(Context context) {

        String appName;
        boolean isSys;
        int countInfo = 0;
        byte[] appNameBytes = null;
        byte[] pcNameBytes = null;
        byte[] resultBytes = null;

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> raps = am.getRunningAppProcesses();

        byteBuf.clear();
        try {
            pcNameBytes = ("Android" + android.os.Build.VERSION.RELEASE).getBytes("EUC-KR");

            byteBuf.append(Converter.getBytesFromIntLE(pcNameBytes.length), 0, 4);
            byteBuf.append(pcNameBytes, 0, pcNameBytes.length);

            byteBuf.append(Converter.getBytesFromInt(0), 0, 4);

            for (RunningAppProcessInfo rap : raps) {
                isSys = rap.processName.startsWith("com.google.process") //$NON-NLS-1$
                        || rap.processName.startsWith("com.android.phone") //$NON-NLS-1$
                        || rap.processName.startsWith("android.process") //$NON-NLS-1$
                        || rap.processName.startsWith("system") //$NON-NLS-1$
                        || rap.processName.startsWith("com.android.inputmethod") //$NON-NLS-1$
                        || rap.processName.startsWith("com.android.alarmclock"); //$NON-NLS-1$
                if (isSys) {
                    continue;
                }
                try {
                    appName = (String) context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(rap.processName, PackageManager.GET_UNINSTALLED_PACKAGES));
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                    appName = "NO NAME";
                }

                appName = rap.processName + "\\" + appName;
                appNameBytes = appName.getBytes("EUC-KR");

                byteBuf.append(Converter.getBytesFromIntLE(appNameBytes.length), 0, 4);

                byteBuf.append(appNameBytes, 0, appNameBytes.length);

                byteBuf.append(Converter.getBytesFromInt(0), 0, 4);

                countInfo++;
            }

            resultBytes = byteBuf.toByteArray();

            System.arraycopy(Converter.getBytesFromIntLE(countInfo), 0, resultBytes, pcNameBytes.length + 4, 4);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return resultBytes;
    }

    public void killRVSystemInfo(Context context, String pakageName) {

        RunningAppProcessInfo killProcInfo = null;

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> list = am.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo runProcInfo : list) {

            if (runProcInfo.processName.equals(pakageName)) {

                killProcInfo = runProcInfo;
                if (killProcInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND || killProcInfo.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    RLog.i(pakageName + " Importance : FOREGROUND");

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    context.startActivity(intent);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String[] pkgs = killProcInfo.pkgList;
                if (pkgs == null)
                    break;

                for (String pkg : pkgs) {
                    if (pkg == null)
                        continue;
                    am.restartPackage(pkg);
                    RLog.i("restartPackage : " + pkg);
                }

            }

        }
    }

    public static byte[] pushBasicInfo(ByteArrayBuffer buf, byte[] memStream) {

        char[] charArray = new char[memStream.length];
        for (int i = 0; i < memStream.length; i++) {
            charArray[i] = (char) memStream[i];
        }

        char[] encoded = new char[Util.ap_base64encode_len(memStream.length)];
        Util.ap_base64encode_binary(encoded, charArray, memStream.length);

        String base64String = "";

        StringBuilder builder = new StringBuilder();
        builder.append(encoded);
        base64String = builder.toString();


        buf.append(Converter.getBytesFromIntLE(base64String.getBytes(EngineConfigSetting.UTF_8).length), 0, 4);

        buf.append(base64String.getBytes(), 0, base64String.getBytes(EngineConfigSetting.UTF_8).length);

        return buf.toByteArray();
    }

    public static void acquireWakeLock(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        screenWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, context.getClass().getName());

        if (screenWakeLock != null) {
            screenWakeLock.acquire();
        }
    }

    public static void releaseWakeLock() {
        if (screenWakeLock != null) {
            screenWakeLock.release();
            screenWakeLock = null;
        }
    }

    public static boolean ipMacBlockCheck(Context context, String inputIP, String inputMAC, String inputID) {
        if (AgentCommandFunction.ipBlockCheck(Global.getInstance().getAppContext(), inputIP)
                || AgentCommandFunction.macBlockCheck(Global.getInstance().getAppContext(), inputMAC)) {
            final AgentLogManager agentLogManager = KoinJavaComponent.get(AgentLogManager.class);
            agentLogManager.addAgentLog(Global.getInstance().getAppContext(), String.format(Global.getInstance().getAppContext().getString(R.string.agent_log_allow_connection), inputIP, inputMAC, inputID));
            return true;
        }

        return false;
    }

    public static boolean ipBlockCheck(Context context, String inputIP) {
        String startIP[] = null;
        String endIP[] = null;
        String getIP[] = inputIP.split("\\.");
        String startIPSum = "";
        String endIPSum = "";
        String targetIPSum = "";

        AgentSQLiteHelper sqlHelper = new AgentSQLiteHelper(context);
        sqlHelper.initDB();
        ArrayList<String> datas = sqlHelper.getDatas(AgentSQLiteHelper.IP_TABLE, 4);
        sqlHelper.close();
        RLog.d("ipdataSize : " + datas.size() + " , " + inputIP);

        if (datas.size() == 0) {
            return false;
        }

        for (int i = 0; i < datas.size(); i = i + 4) {
            if (datas.get(i + 1).equals("end")) { // ip ~ ip
                startIP = datas.get(i + 2).split("\\.");
                endIP = datas.get(i + 3).split("\\.");

                startIPSum = String.format("%d%03d%03d%03d", Integer.parseInt(startIP[0]), Integer.parseInt(startIP[1]), Integer.parseInt(startIP[2]), Integer.parseInt(startIP[3]));
                endIPSum = String.format("%d%03d%03d%03d", Integer.parseInt(endIP[0]), Integer.parseInt(endIP[1]), Integer.parseInt(endIP[2]), Integer.parseInt(endIP[3]));
                targetIPSum = String.format("%d%03d%03d%03d", Integer.parseInt(getIP[0]), Integer.parseInt(getIP[1]), Integer.parseInt(getIP[2]), Integer.parseInt(getIP[3]));

                long start = Long.parseLong(startIPSum);
                long end = Long.parseLong(endIPSum);
                long target = Long.parseLong(targetIPSum);

                if (start <= target && end >= target) {
                    RLog.d("start <= target && end >= target");
                    return false;
                }
            } else { // ip 단일!!!
                if (datas.get(i + 2).equals(inputIP)) {
                    RLog.d("datas.get(i + 2).equals(inputIP)");
                    return false;
                }
            }
        }
        RLog.d("block ip : " + inputIP);

        return true;
    }

    public static boolean macBlockCheck(Context context, String inputMac) {

        AgentSQLiteHelper sqlHelper = new AgentSQLiteHelper(context);
        sqlHelper.initDB();
        ArrayList<String> datas = sqlHelper.getDatas(AgentSQLiteHelper.MAC_TABLE, 2);
        sqlHelper.close();
        if (datas.size() == 0) {
            return false;
        }

        for (int i = 0; i < datas.size(); i = i + 2) {
            if (datas.get(i + 1).equals(inputMac)) {
                return false;
            }
        }

        RLog.d("block mac : " + inputMac);
        return true;
    }


    private static Executor liveViewExecutor;

    public static void runLiveViewExecutor(Runnable task) {
        if (liveViewExecutor == null) {
            liveViewExecutor = Executors.newSingleThreadExecutor();
        }
        liveViewExecutor.execute(task);
    }

}
