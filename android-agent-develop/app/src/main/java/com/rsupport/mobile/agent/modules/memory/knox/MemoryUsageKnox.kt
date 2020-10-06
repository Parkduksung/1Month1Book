package com.rsupport.mobile.agent.modules.memory.knox

import com.rsupport.mobile.agent.modules.memory.MemoryUsage
import com.rsupport.mobile.agent.modules.memory.KeyObject

class MemoryUsageKnox(private val packageName: String, private val memoryUsage: Long) : MemoryUsage() {
    override fun getUsageMemory(): Long {
        return memoryUsage
    }

    override fun available(): Boolean {
        return memoryUsage != INVALID_USAGE_INFO
    }

    override fun memoryKey(): KeyObject {
        return KeyObject(packageName)
    }
}