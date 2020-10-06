package com.rsupport.mobile.agent.modules.sysinfo.app

import android.content.Context
import android.content.pm.PackageManager
import com.rsupport.knox.KnoxManagerCompat
import org.koin.java.KoinJavaComponent

class RunningApplicationKnox : RunningApplication {
    private val context by KoinJavaComponent.inject(Context::class.java)
    private val packageManager by KoinJavaComponent.inject(PackageManager::class.java)
    private val knoxManagerCompat by KoinJavaComponent.inject(KnoxManagerCompat::class.java)

    override fun getRunningAppInfos(): List<RunningAppInfo> {
        val runningAppList = mutableListOf<RunningAppInfo>()
        val installedPackages = packageManager.getInstalledApplications(0)

        installedPackages.forEach {
            if (knoxManagerCompat.isRunningPkg(context, it.packageName)) {
                runningAppList.add(RunningAppInfo(it.packageName))
            }
        }
        return runningAppList
    }
}