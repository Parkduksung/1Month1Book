package com.rsupport.mobile.agent.modules.sysinfo.cpu.knox

import com.rsupport.mobile.agent.modules.memory.KeyObject
import com.rsupport.mobile.agent.modules.sysinfo.cpu.CpuUsage

class CpuUsageKnox(private val packageName: String, private val usagePercent: Int) : CpuUsage() {
    override fun usagePercent(): Int {
        return usagePercent
    }

    override fun available(): Boolean {
        return true
    }

    override fun getKey(): KeyObject {
        return KeyObject(packageName)
    }
}