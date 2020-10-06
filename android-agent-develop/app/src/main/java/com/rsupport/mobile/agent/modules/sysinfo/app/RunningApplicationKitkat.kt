package com.rsupport.mobile.agent.modules.sysinfo.app

import android.app.ActivityManager
import org.koin.java.KoinJavaComponent

class RunningApplicationKitkat : RunningApplication {
    private val activityManager by KoinJavaComponent.inject(ActivityManager::class.java)

    override fun getRunningAppInfos(): List<RunningAppInfo> {
        val runningAppList = mutableListOf<RunningAppInfo>()
        updateRunningApp(activityManager.runningAppProcesses, runningAppList)
        return runningAppList
    }

    private fun updateRunningApp(runningAppProcesses: List<ActivityManager.RunningAppProcessInfo>, runningPackagesName: MutableList<RunningAppInfo>) {
        runningAppProcesses.forEach {
            runningPackagesName.add(RunningAppInfo(it.processName))
        }
    }
}