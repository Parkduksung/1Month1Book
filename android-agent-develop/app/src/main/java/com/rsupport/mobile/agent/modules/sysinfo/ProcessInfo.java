package com.rsupport.mobile.agent.modules.sysinfo;


import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;

import com.rsupport.litecam.binder.Binder;
import com.rsupport.mobile.agent.modules.memory.KeyObject;
import com.rsupport.mobile.agent.modules.memory.MemoryUsage;
import com.rsupport.mobile.agent.modules.memory.UsageContainer;
import com.rsupport.mobile.agent.modules.net.channel.DataChannelImpl;
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppFactory;
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppInfo;
import com.rsupport.mobile.agent.modules.sysinfo.cpu.CpuUsage;
import com.rsupport.mobile.agent.modules.sysinfo.cpu.CpuUsageProvider;
import com.rsupport.mobile.agent.modules.sysinfo.phone.PhoneMemory;
import com.rsupport.mobile.agent.modules.sysinfo.phone.PhoneStorage;
import com.rsupport.mobile.agent.modules.sysinfo.process.ProcessCache;
import com.rsupport.mobile.agent.modules.sysinfo.process.ProcessItem;
import com.rsupport.mobile.agent.modules.sysinfo.process.ResourceUpdater;
import com.rsupport.mobile.agent.utils.SdkVersion;
import com.rsupport.mobile.agent.utils.Utility;
import com.rsupport.rsperm.IRSPerm;
import com.rsupport.util.log.RLog;

import org.jetbrains.annotations.NotNull;
import org.koin.java.KoinJavaComponent;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import kotlin.Lazy;


public class ProcessInfo {
    private Context context;
    private RunningAppFactory runningAppFactory;
    private ProcessCache processCache;

    private DataChannelImpl mDataChannel;
    private PhoneMemory phoneMemory;
    private PhoneStorage phoneStorage;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final ResourceUpdater resourceUpdater;
    private final MemoryUsage.Factory memoryUsageFactory = MemoryUsageFactoryProvider.get();
    private final CpuUsage.Factory cpuUsageFactory = new CpuUsageProvider().create();

    private Lazy<SdkVersion> sdkVersionLazy = KoinJavaComponent.inject(SdkVersion.class);

    public ProcessInfo(
            @NotNull Context context,
            @NotNull DataChannelImpl dataChannel,
            @NotNull RunningAppFactory runningAppFactory,
            @NotNull PhoneMemory phoneMemory
    ) {
        this.processCache = new ProcessCache();
        this.phoneStorage = new PhoneStorage(context);
        this.context = context;
        this.runningAppFactory = runningAppFactory;
        this.phoneMemory = phoneMemory;
        this.mDataChannel = dataChannel;
        resourceUpdater = new ResourceUpdater(context, processCache);
    }

    public void close() {
        isClosed.set(true);
        processCache.close();
        resourceUpdater.close();
        memoryUsageFactory.close();
        cpuUsageFactory.close();
    }

    public List<ProcessItem> loadProcessItems() {
        List<RunningAppInfo> runningAppInfos = runningAppFactory.create().getRunningAppInfos();

        updateProcess(runningAppInfos);

        updateUsage(MemoryUsage.from(memoryUsageFactory), CpuUsage.from(cpuUsageFactory));
        return resourceUpdater.update();
    }

    private void updateUsage(UsageContainer<MemoryUsage> memoryUsageContainer, UsageContainer<CpuUsage> cpuUsageContainer) {
        for (ProcessItem processItem : processCache.getProcessItems()) {
            if (isClosed.get()) break;

            String pkgName = processItem.getRunningAppInfo().getPkgName();
            MemoryUsage memoryUsage = memoryUsageContainer.find(new KeyObject(pkgName));
            if (memoryUsage != null) {
                processItem.setUsageMemory(memoryUsage.getUsageByte());
            }

            CpuUsage cpuUsage = cpuUsageContainer.find(new KeyObject(pkgName));
            if (cpuUsage != null) {
                processItem.setCpuPercent(cpuUsage.getPercent());
            }
        }
    }

    public String getMemoryInfo() {
        String[] memInfo = getMemInfo();
        return "0" + "&/" + memInfo[0] + "&/" + "&&" +
                "1" + "&/" + (memInfo[1]) + "&/" + "&&" +
                "2" + "&/" + memInfo[2] + "&/" + getFreeMemColor(memInfo);
    }

    public String getChartInfo() {
        return getMemoryPercent() + "&&" + getDiskPercent() + "&&" + getCpuUsagePercent();
    }

    private long getCpuUsagePercent() {
        UsageContainer<CpuUsage> cpuUsageContainer = CpuUsage.from(cpuUsageFactory);
        List<RunningAppInfo> runningAppInfoList = runningAppFactory.create().getRunningAppInfos();
        long cpuUsagePercent = 0L;
        for (RunningAppInfo runningAppInfo : runningAppInfoList) {
            if (isClosed.get()) break;
            CpuUsage cpuUsage = cpuUsageContainer.find(new KeyObject(runningAppInfo.getPkgName()));
            if (cpuUsage != null) {
                cpuUsagePercent += cpuUsage.getPercent();
            }
        }
        return cpuUsagePercent;
    }

    private int getMemoryPercent() {
        PhoneMemory.MemoryInfo memoryInfo = phoneMemory.getMemoryInfo();
        if (memoryInfo.getEmpty()) return 0;
        return (int) memoryInfo.getPercent();
    }

    private int getDiskPercent() {
        return (phoneStorage.getExternalStoragePercent() + phoneStorage.getInternalStoragePercent()) / 2;
    }

    private void updateProcess(List<RunningAppInfo> runningAppProcessInfoList) {
        long totalMemory = phoneMemory.getMemoryInfo().getTotal();
        processCache.clear();
        if (runningAppProcessInfoList != null) {

            for (int i = 0, size = runningAppProcessInfoList.size(); i < size; i++) {
                if (isClosed.get()) break;
                RunningAppInfo runningAppProcessInfo = runningAppProcessInfoList.get(i);
                String name = runningAppProcessInfo.getPkgName();
                boolean isSystemApp = name.startsWith("com.google.process") //$NON-NLS-1$
                        || name.startsWith("com.android.phone") //$NON-NLS-1$
                        || name.startsWith("android.process") //$NON-NLS-1$
                        || name.startsWith("system") //$NON-NLS-1$
                        || name.startsWith("com.android.inputmethod") //$NON-NLS-1$
                        || name.startsWith("com.android.alarmclock"); //$NON-NLS-1$

                ProcessItem pi = processCache.getCached(name);
                if (pi == null) {
                    pi = new ProcessItem(context, totalMemory, isSystemApp, runningAppProcessInfo);
                }
                processCache.add(pi);
            }
        }
        RLog.i("updateProcess_end");
    }

    /**
     * @return [TOTAL, FREE, AVAILABLE] mb
     */
    private @NotNull
    String[] getMemInfo() {
        PhoneMemory.MemoryInfo memoryInfo = phoneMemory.getMemoryInfo();
        if (memoryInfo.getEmpty()) return new String[]{"EMPTY", "EMPTY", "EMPTY"};

        long totalMemory = memoryInfo.getTotal();
        long free = totalMemory - memoryInfo.getAvailable();
        long available = memoryInfo.getAvailable();
        return new String[]{changeFormatToMB(totalMemory), changeFormatToMB(free), changeFormatToMB(available)};
    }

    private String changeFormatToMB(long mem) {
        return (mem / (1024 * 1024)) + " MB";
    }

    private String getFreeMemColor(String[] memInfo) {
        int total = Integer.parseInt(Utility.extractNumber(memInfo[0].trim()));
        int using = Integer.parseInt(Utility.extractNumber(memInfo[1].trim()));

        if (total / 10 * 8 <= using) {
            return "0";
        } else {
            return "1";
        }
    }

    public void killProcess(String targetName) {
        RLog.i("targetName : " + targetName);
        ProcessItem pi = processCache.findProcessItem(targetName);
        if (pi == null) return;

        // Android KitKat 이하, Rsperm 사용할 때만 활성 상태의 Activity 를 홈 으로 이동 후 지울 종료할 수 있다.
        forwardHomeIfForeground(targetName);

        String self = context.getPackageName();
        if (self.equals(pi.getRunningAppInfo().getPkgName())) {
            mDataChannel.close();
        } else {
            endProcess(context, pi.getRunningAppInfo().getPkgName());
        }
    }

    private void forwardHomeIfForeground(String packageName) {
        List<RunningAppProcessInfo> list = getRunningAppProcesses();
        if (list == null) return;
        if (isClosed.get()) return;

        for (int j = 0, k = list.size(); j < k; j++) {
            if (list.get(j).processName.equals(packageName)) {
                if (list.get(j).importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
                        list.get(j).importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    if (context != null) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS |
                                Intent.FLAG_ACTIVITY_FORWARD_RESULT |
                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP |
                                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        context.startActivity(intent);
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private List<RunningAppProcessInfo> getRunningAppProcesses() {
        SdkVersion sdkVersion = sdkVersionLazy.getValue();

        if (sdkVersion.greaterThan21()) {
            try {
                Binder binder = Binder.getInstance();
                IRSPerm rsperm = binder.getBinder();
                return rsperm.getRunningProcesses();
            } catch (Exception e) {
                return getDefaultRunningAppProcesses();
            }
        } else {
            return getDefaultRunningAppProcesses();
        }
    }

    private List<RunningAppProcessInfo> getDefaultRunningAppProcesses() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return am.getRunningAppProcesses();
    }


    public void killCamera() {
        List<RunningAppInfo> runAppProcessList = runningAppFactory.create().getRunningAppInfos();

        for (int i = 0, n = runAppProcessList.size(); i < n; i++) {
            RunningAppInfo runAppProcess = runAppProcessList.get(i);
            if (runAppProcess.getPkgName().toLowerCase().contains("camera")) {
                endProcess(context, runAppProcess.getPkgName());
                loadProcessItems();
            }
        }
    }

    private void endProcess(Context context, String pkg) {
        if (pkg == null) return;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(pkg);
    }

    public boolean checkProcessLive(String processName) {
        RLog.i("kill target name : " + processName);
        for (ProcessItem processItem : processCache.getProcessItems()) {
            if (processItem.getLabel() != null) {
                RLog.i("pi.label : " + processItem.getLabel());
                if (processName.equals(processItem.getLabel())) {
                    RLog.i(processItem.getLabel() + " is live!!!");
                    return true;
                }
            } else {
                if (processName.equals(processItem.getRunningAppInfo().getPkgName())) {
                    RLog.i(processItem.getRunningAppInfo().getPkgName() + " is live!!!");
                    return true;
                }
            }
        }
        return false;
    }
}
