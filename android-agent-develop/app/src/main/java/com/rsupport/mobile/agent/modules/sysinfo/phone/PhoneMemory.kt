package com.rsupport.mobile.agent.modules.sysinfo.phone

import android.app.ActivityManager
import android.content.Context
import com.rsupport.util.log.RLog
import config.EngineConfigSetting
import java.io.*
import java.util.*

class PhoneMemory(private val context: Context, private val memInfoStreamFactory: MemInfoStreamFactory = ProcessMemInfoFileFactory()) {

    private val activityManger: ActivityManager by lazy {
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    data class MemoryInfo(
            val available: Long,
            val total: Long,
            val threshold: Long = EMPTY,
            val buffers: Long = EMPTY,
            val cached: Long = EMPTY
    ) {
        companion object {
            const val EMPTY = -1L
        }

        val empty: Boolean
            get() {
                return available == EMPTY || total == EMPTY
            }

        val free: Long
            get() = total - available

        val percent: Double
            get() {
                return free / total.toDouble() * 100
            }
    }

    fun getMemoryInfo(): MemoryInfo {
        return try {
            extractMemInfo().let {
                val totalMsg = getMemoryInfo(it, "memtotal")
                        ?: throw IllegalArgumentException("totalMem is null.")
                MemoryInfo(
                        available = getAvailableMemory(),
                        total = extractMemoryToByte(totalMsg),
                        threshold = getThresholdMemory(),
                        buffers = extractMemoryToByte(getMemoryInfo(it, "buffers")),
                        cached = extractMemoryToByte(getMemoryInfo(it, "cached"))
                )
            }
        } catch (e: Exception) {
            RLog.e(e)
            MemoryInfo(MemoryInfo.EMPTY, MemoryInfo.EMPTY)
        }
    }

    private fun extractMemInfo(): List<String> {
        return memInfoStreamFactory.create().let {
            BufferedReader(InputStreamReader(it, EngineConfigSetting.UTF_8), 1024).use { reader ->
                var line: String?
                val memInfoList = mutableListOf<String>()
                while (reader.readLine().also { line = it } != null) {
                    line?.apply {
                        memInfoList.add(this)
                    }
                }
                memInfoList
            }
        }
    }

    private fun getMemoryInfo(memInfoList: List<String>, prefix: String): String? {
        return memInfoList.firstOrNull { it.toLowerCase(Locale.getDefault()).startsWith(prefix) }
    }

    private fun getAvailableMemory(): Long {
        val mi = ActivityManager.MemoryInfo()
        activityManger.getMemoryInfo(mi)
        return mi.availMem
    }

    private fun getThresholdMemory(): Long {
        val mi = ActivityManager.MemoryInfo()
        activityManger.getMemoryInfo(mi)
        return mi.threshold
    }

    private fun extractMemoryToByte(line: String?): Long {
        var inputLine = line
        if (inputLine != null) {
            var idx = inputLine.indexOf(':')
            if (idx != -1) {
                inputLine = inputLine.substring(idx + 1).trim()
                idx = inputLine.lastIndexOf(' ')
                if (idx != -1) {
                    val unit = inputLine.substring(idx + 1)
                    try {
                        var size = inputLine.substring(0, idx).trim().toLong()
                        if ("kb".equals(unit, ignoreCase = true)) {
                            size *= 1024
                        } else if ("mb".equals(unit, ignoreCase = true)) {
                            size *= 1024 * 1024.toLong()
                        } else if ("gb".equals(unit, ignoreCase = true)) {
                            size *= 1024 * 1024 * 1024.toLong()
                        } else {
                            RLog.i("Unexpected mem unit format: $inputLine")
                        }
                        return size
                    } catch (e: Exception) {
                        RLog.e(e.localizedMessage)
                    }
                } else {
                    RLog.e("Unexpected mem value format: $inputLine")
                }
            } else {
                RLog.e("Unexpected mem format: $inputLine")
            }
        }
        return -1
    }

    interface MemInfoStreamFactory {
        fun create(): InputStream
    }

    class ProcessMemInfoFileFactory : MemInfoStreamFactory {
        override fun create(): InputStream {
            return FileInputStream(File("/proc/meminfo"))
        }
    }
}

