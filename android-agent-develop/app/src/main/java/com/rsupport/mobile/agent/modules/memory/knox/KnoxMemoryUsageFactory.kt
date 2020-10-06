package com.rsupport.mobile.agent.modules.memory.knox

import android.content.Context
import com.rsupport.knox.KnoxManagerCompat
import com.rsupport.mobile.agent.modules.memory.MemoryUsage
import com.rsupport.mobile.agent.modules.memory.UsageContainer
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppFactory
import java.util.concurrent.atomic.AtomicBoolean

class KnoxMemoryUsageFactory(private val context: Context, private val runningAppFactory: RunningAppFactory, private val knoxManagerCompat: KnoxManagerCompat) : MemoryUsage.Factory {
    private val isClosed = AtomicBoolean()

    override fun get(): UsageContainer<MemoryUsage> {
        val memoryUsageList = mutableListOf<MemoryUsage>()
        val runningAppList = runningAppFactory.create().getRunningAppInfos()
        runningAppList.forEach { runningAppInfo ->
            if (isClosed.get()) return@forEach

            memoryUsageList.add(
                    knoxManagerCompat.getMemoryUsage(context, runningAppInfo.pkgName).let {
                        MemoryUsageKnox(runningAppInfo.pkgName, if (it != KnoxManagerCompat.INVALID_MEMORY) it else 0)
                    }
            )
        }
        return UsageContainer(memoryUsageList)
    }

    override fun close() {
        isClosed.set(true)
    }
}