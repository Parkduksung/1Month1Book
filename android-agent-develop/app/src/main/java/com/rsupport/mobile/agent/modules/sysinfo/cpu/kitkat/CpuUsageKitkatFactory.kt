package com.rsupport.mobile.agent.modules.sysinfo.cpu.kitkat

import android.app.ActivityManager
import com.rsupport.mobile.agent.modules.memory.UsageContainer
import com.rsupport.mobile.agent.modules.sysinfo.CPUUsageInfo
import com.rsupport.mobile.agent.modules.sysinfo.cpu.CpuUsage
import org.koin.java.KoinJavaComponent
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Kitkat 이하 버전에서 cpu 사용량을 조회 하기 위한 class 로 사용된다.
 */
class CpuUsageKitkatFactory : CpuUsage.Factory {
    private val activityManager by KoinJavaComponent.inject(ActivityManager::class.java)
    private val isClosed = AtomicBoolean()

    override fun get(): UsageContainer<CpuUsage> {
        CPUUsageInfo.getInstance().exec()
        val cpuUsageList = mutableListOf<CpuUsage>()
        activityManager.runningAppProcesses.forEach {
            if (isClosed.get()) return@forEach
            cpuUsageList.add(CpuUsageKitKat(it.processName, it.pid))
        }
        CPUUsageInfo.getInstance().clear()
        return UsageContainer(cpuUsageList)
    }

    override fun close() {
        isClosed.set(true)
    }
}