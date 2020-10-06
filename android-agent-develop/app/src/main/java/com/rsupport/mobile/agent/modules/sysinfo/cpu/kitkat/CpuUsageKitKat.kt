package com.rsupport.mobile.agent.modules.sysinfo.cpu.kitkat

import com.rsupport.mobile.agent.modules.memory.KeyObject
import com.rsupport.mobile.agent.modules.sysinfo.CPUUsageInfo
import com.rsupport.mobile.agent.modules.sysinfo.cpu.CpuUsage

class CpuUsageKitKat(private val packageName: String, private val pid: Int) : CpuUsage() {
    override fun usagePercent(): Int {
        return CPUUsageInfo.getInstance().getUsage(pid)
    }

    override fun available(): Boolean {
        return true
    }

    override fun getKey(): KeyObject {
        return KeyObject(packageName)
    }
}