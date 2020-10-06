package com.rsupport.mobile.agent.modules.sysinfo.process

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.format.Formatter
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppInfo

class ProcessItem(
        private val context: Context,
        private val totalMemory: Long,
        private val isSystemApp: Boolean,
        val runningAppInfo: RunningAppInfo) {

    var label: String? = null
    var icon: Drawable? = null
    var memoryBytes: Long = 0

    private var cpuPercent: Long = 0

    val processInfoString: String
        get() {
            var itemString = ""
            itemString = if (label != null) {
                "$label&/"
            } else {
                runningAppInfo.pkgName + "&/"
            }
            itemString += "$cpuPercent %&/"
            itemString += Formatter.formatFileSize(context, memoryBytes) + "&/"
            itemString += if (isSystemApp) {
                "0" + "&/"
            } else {
                "1" + "&/"
            }
            itemString += getProcessColor(cpuPercent, memoryBytes)
            return itemString
        }

    private fun getProcessColor(cpu: Long, memory: Long): String {
        return if (cpu > 50 || (memory / 2) > totalMemory) {
            "0"
        } else {
            "1"
        }
    }

    fun setUsageMemory(memoryBytes: Long) {
        this.memoryBytes = memoryBytes
    }

    fun setCpuPercent(cpuPercent: Long) {
        this.cpuPercent = cpuPercent
    }

}