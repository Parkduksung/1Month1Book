package com.rsupport.mobile.agent.modules.memory.shell

import android.app.ActivityManager
import com.rsupport.mobile.agent.modules.memory.MemoryUsage
import com.rsupport.mobile.agent.modules.memory.UsageContainer
import com.rsupport.util.log.RLog
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Android 19(Kitkat)이하 에서 사용할 수 있는 Process 의 메모리 사용량을 조사하는 factory
 */
class ProcStatMemoryUsageFactory(private val activityManager: ActivityManager) : MemoryUsage.Factory {
    private val isClosed = AtomicBoolean()

    override fun get(): UsageContainer<MemoryUsage> {
        val memoryUsageList = mutableListOf<MemoryUsage>()
        activityManager.runningAppProcesses.forEach {
            try {
                if (isClosed.get()) return@forEach
                MemoryUsageProcStat(it.processName, readProc(it.pid.toString())).apply {
                    if (isAvailable) {
                        memoryUsageList.add(this)
                    }
                }
            } catch (e: Exception) {
                RLog.e(e)
            }
        }
        return UsageContainer(memoryUsageList)
    }

    override fun close() {
        isClosed.set(true)
    }

    private fun readProc(pid: String): String {
        val buffer = ByteArray(4096)
        return FileInputStream("/proc/$pid/stat").use { inputStream ->
            ByteArrayOutputStream().use { baos ->
                var len: Int
                while (inputStream.read(buffer).also { len = it } != -1) {
                    baos.write(buffer, 0, len)
                }
                baos.toString("UTF-8")
            }
        }
    }
}