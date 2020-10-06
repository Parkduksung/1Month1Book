package com.rsupport.mobile.agent.modules.memory.dumpsys

import com.rsupport.mobile.agent.modules.memory.LineSplitter
import com.rsupport.mobile.agent.modules.memory.MemoryUsage
import com.rsupport.mobile.agent.modules.memory.UsageContainer
import java.util.concurrent.atomic.AtomicBoolean

class DumpsysMemoryUsageFactory(private val dumpsysReader: DumpsysReader) : MemoryUsage.Factory {
    private val isClosed = AtomicBoolean()

    override fun get(): UsageContainer<MemoryUsage> {
        val textLines = dumpsysReader.read()
        val memoryUsageList = mutableListOf<MemoryUsage>()
        val stringReader = LineSplitter(textLines)
        stringReader.getLines().forEach {
            if (isClosed.get()) return@forEach

            MemoryUsageDumpsys(it).let { memoryUsage ->
                if (memoryUsage.isAvailable) {
                    memoryUsageList.add(memoryUsage)
                }
            }
        }
        return UsageContainer(memoryUsageList)
    }

    override fun close() {
        isClosed.set(true)
    }
}