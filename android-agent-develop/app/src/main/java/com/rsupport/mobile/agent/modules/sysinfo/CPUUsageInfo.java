package com.rsupport.mobile.agent.modules.sysinfo;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

import com.rsupport.litecam.binder.Binder;
import com.rsupport.mobile.agent.utils.SdkVersion;
import com.rsupport.mobile.agent.utils.Utility;
import com.rsupport.rsperm.IRSPerm;
import com.rsupport.util.log.RLog;

import org.koin.java.KoinJavaComponent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import kotlin.Lazy;

public class CPUUsageInfo {
    private final String className = "CPUUsageInfo";
    private static CPUUsageInfo instance = null;
    private Hashtable<String, Integer> pidList = null;
    private int totalUsage = 0;
    private boolean isClear = true;
    private Object lockObject = null;

    private Lazy<SdkVersion> sdkVersionLazy = KoinJavaComponent.inject(SdkVersion.class);

    private CPUUsageInfo() {
        pidList = new Hashtable<String, Integer>();
        lockObject = new Object();
    }

    public static CPUUsageInfo getInstance() {
        if (instance == null) {
            instance = new CPUUsageInfo();
        }
        return instance;
    }

    public void destroy() {
        instance = null;
        clear();
    }

    public void clear() {
        synchronized (lockObject) {
            isClear = true;
            totalUsage = 0;
            if (pidList != null) {
                pidList.clear();
            }
        }
    }

    public int getTotalUsage(List<RunningAppProcessInfo> runningApp) {
        synchronized (lockObject) {
            totalUsage = 0;
            Set set = pidList.keySet();
            Iterator<String> iterator = set.iterator();
            while (iterator.hasNext()) {
                String pid = iterator.next();
                for (int i = 0; i < runningApp.size(); i++) {
                    if (isRunningApp(pid, runningApp.get(i)) == true) {
                        totalUsage += pidList.get(pid);
                    }
                }
            }
            return totalUsage;
        }
    }

    private boolean isRunningApp(String pid, RunningAppProcessInfo info) {
        if (pid.equals(String.valueOf(info.pid)) == true) {
            return true;
        }
        return false;
    }

    public int getUsage(int pid) {
        synchronized (lockObject) {
            String key = String.valueOf(pid);
            if (pidList.containsKey(key) == true) {
                return pidList.get(key);
            }
            return 0;
        }
    }

    public void exec() {
        BufferedReader bufferedReader = null;
        try {
            if (isClear == false) return;

            Process process = Runtime.getRuntime().exec("top -n 1");
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuffer lineBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lineBuffer.append(line).append("\n");
            }
            String cpuInfo = lineBuffer.toString();
            StringTokenizer st = new StringTokenizer(cpuInfo, "\n");
            int i = 0;
            int k = 0;
            int user = 0;

            synchronized (lockObject) {
                totalUsage = 0;
                pidList.clear();
                int pidIndex = 0;
                int cpuIndex = 1;
                while (st.hasMoreElements()) {
                    String info = st.nextToken();
                    // find pid, cpu index
                    if (i == 0) {
                        StringTokenizer st1 = new StringTokenizer(info);
                        while (st1.hasMoreElements()) {
                            String str = st1.nextToken();
                            if (str.contains("User") == true) {
                                String temp = st1.nextToken();
                                if (temp.contains("%") == true) {
                                    user = Integer.parseInt(temp.substring(0, temp.indexOf("%")));
                                } else {
                                    user = Integer.parseInt(temp.substring(0, temp.length()));
                                }
                                break;
                            }
                        }
                    } else if (i == 2) {
                        k = 0;
                        StringTokenizer st1 = new StringTokenizer(info);
                        while (st1.hasMoreElements()) {
                            String str = st1.nextToken();
                            if (str.contains("PID") == true) {
                                pidIndex = k;
                            } else if (str.contains("CPU") == true) {
                                cpuIndex = k;
                            }
                            k++;
                        }
                    }
                    // pid 별 사용량
                    else if (i > 2) {
                        k = 0;
                        String tempPid = "";
                        int tempUsage = 0;

                        StringTokenizer st1 = new StringTokenizer(info);
                        while (st1.hasMoreElements()) {
                            String str = st1.nextToken();
                            if (k == pidIndex) {
                                tempPid = str;
                            } else if (k == cpuIndex) {
                                tempUsage = Integer.parseInt(str.substring(0, str.indexOf("%")));
                                if (tempUsage > 0) {
                                    pidList.put(tempPid, tempUsage);
                                }
                                break;
                            }
                            k++;
                        }
                    }
                    i++;
                }

                int sumUsage = 0;
                if (Utility.mainContext != null) {
                    List<ActivityManager.RunningAppProcessInfo> list = getRunningAppProcesses(Utility.mainContext);
                    sumUsage = getTotalUsage(list);
                }
                resetPersent(sumUsage, user);
            }
            isClear = false;
            RLog.i("CPU Pid Usage : " + pidList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            bufferedReader = null;
        }
    }

    private List getRunningAppProcesses(Context context) {
        SdkVersion sdkVersion = sdkVersionLazy.getValue();

        if (sdkVersion.greaterThan21()) {
            try {
                Binder binder = Binder.getInstance();
                IRSPerm rsperm = binder.getBinder();
                return rsperm.getRunningProcesses();
            } catch (Exception e) {
                e.printStackTrace();
                return getDefaultRunningAppProcesses(context);
            }
        } else {
            return getDefaultRunningAppProcesses(context);
        }
    }

    private List getDefaultRunningAppProcesses(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return am.getRunningAppProcesses();
    }

    private void resetPersent(float totalUsage, float user) {
        RLog.i("resetPersent totalUsage(" + totalUsage + "), user(" + user + ")");
        Hashtable<String, Integer> tempTable = new Hashtable<String, Integer>();
        tempTable = (Hashtable<String, Integer>) pidList.clone();

        Set set = tempTable.keySet();
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()) {
            String pid = iterator.next();
            float use = (float) tempTable.get(pid);
            int useCpu = (int) (use / totalUsage * user);
            pidList.put(pid, useCpu);
            RLog.i("pid(" + pid + "), cpu(" + useCpu + ")");
        }
    }
}
