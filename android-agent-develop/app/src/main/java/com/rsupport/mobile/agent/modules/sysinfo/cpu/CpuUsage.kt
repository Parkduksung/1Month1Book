package com.rsupport.mobile.agent.modules.sysinfo.cpu

import com.rsupport.mobile.agent.modules.memory.KeyContainer
import com.rsupport.mobile.agent.modules.memory.KeyObject
import com.rsupport.mobile.agent.modules.memory.UsageContainer

abstract class CpuUsage : KeyContainer {

    protected abstract fun usagePercent(): Int
    protected abstract fun available(): Boolean
    protected abstract fun getKey(): KeyObject

    val isAvailable: Boolean by lazy {
        if (percent == INVALID_PERCENT) return@lazy false
        available()
    }

    val percent: Int by lazy {
        usagePercent()
    }

    override val keyObject: KeyObject by lazy {
        getKey()
    }

    companion object {
        const val INVALID_PERCENT = -1

        @JvmStatic
        fun from(factory: Factory): UsageContainer<CpuUsage> {
            return factory.get()
        }
    }

    interface Factory {
        fun get(): UsageContainer<CpuUsage>
        fun close()
    }
}

