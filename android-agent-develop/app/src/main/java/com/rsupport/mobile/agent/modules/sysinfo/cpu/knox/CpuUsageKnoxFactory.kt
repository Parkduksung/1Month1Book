package com.rsupport.mobile.agent.modules.sysinfo.cpu.knox

import android.content.Context
import com.rsupport.knox.KnoxManagerCompat
import com.rsupport.mobile.agent.modules.memory.UsageContainer
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppFactory
import com.rsupport.mobile.agent.modules.sysinfo.cpu.CpuUsage
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Knox 를 이용하여 cpu 사용량을 조회할때 사용한다.
 */
class CpuUsageKnoxFactory : CpuUsage.Factory {
    private val context by inject(Context::class.java)
    private val runningAppFactory by inject(RunningAppFactory::class.java)
    private val knoxManagerCompat by inject(KnoxManagerCompat::class.java)
    private val isClosed = AtomicBoolean()

    override fun get(): UsageContainer<CpuUsage> {
        val cpuUsageList = mutableListOf<CpuUsage>()
        runningAppFactory.create().getRunningAppInfos().forEach {
            if (isClosed.get()) return@forEach
            val cpuPercent = knoxManagerCompat.getCpuUsage(context, it.pkgName).toInt()
            cpuUsageList.add(CpuUsageKnox(it.pkgName, cpuPercent))
        }
        return UsageContainer(cpuUsageList)
    }

    override fun close() {
        isClosed.set(true)
    }
}