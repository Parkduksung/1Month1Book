package com.rsupport.mobile.agent.modules.sysinfo.app

import android.app.ActivityManager
import com.rsupport.rsperm.IRSPerm
import com.rsupport.util.log.RLog

class RunningApplicationRsperm(private val rsperm: IRSPerm) : RunningApplication {
    override fun getRunningAppInfos(): List<RunningAppInfo> {
        val runningAppList = mutableListOf<RunningAppInfo>()
        try {
            updateRunningApp(rsperm.runningProcesses, runningAppList)
        } catch (e: Exception) {
            RLog.e(e)
            return RunningApplicationKitkat().getRunningAppInfos()
        }
        return runningAppList
    }

    private fun updateRunningApp(runningAppProcesses: List<ActivityManager.RunningAppProcessInfo>, runningPackagesName: MutableList<RunningAppInfo>) {
        runningAppProcesses.forEach {
            runningPackagesName.add(RunningAppInfo(it.processName))
        }
    }
}