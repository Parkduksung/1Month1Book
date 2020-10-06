package com.rsupport.mobile.agent.modules.sysinfo.cpu.rsperm

import com.rsupport.mobile.agent.extension.guard
import com.rsupport.mobile.agent.modules.memory.LineSplitter
import com.rsupport.mobile.agent.modules.memory.UsageContainer
import com.rsupport.mobile.agent.modules.memory.dumpsys.DumpsysReader
import com.rsupport.mobile.agent.modules.sysinfo.cpu.CpuUsage
import com.rsupport.mobile.agent.service.RSPermService
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.atomic.AtomicBoolean

class CpuUsageRspermFactory(private val dumpsysReader: DumpsysReader) : CpuUsage.Factory {
    private val isClosed = AtomicBoolean()
    private val rsPermService by inject(RSPermService::class.java)

    override fun get(): UsageContainer<CpuUsage> {
        canDumpsys().guard {
            return UsageContainer(emptyList())
        }

        val cpuUsageList = mutableListOf<CpuUsage>()
        val dumpsysString = dumpsysReader.read()
        LineSplitter(dumpsysString).getLines().forEach {
            if (isClosed.get()) return@forEach
            CpuUsageDumpsys(it).let {
                if (it.isAvailable) {
                    cpuUsageList.add(it)
                }
            }
        }
        return UsageContainer(cpuUsageList)
    }

    override fun close() {
        isClosed.set(true)
    }

    private fun canDumpsys(): Boolean {
        return (rsPermService.isBind() && rsPermService.getRsperm() != null && rsPermService.getRsperm()?.hasDumpsys() == true)
    }
}