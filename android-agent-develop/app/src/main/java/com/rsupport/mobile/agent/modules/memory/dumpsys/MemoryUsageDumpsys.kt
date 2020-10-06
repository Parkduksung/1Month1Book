package com.rsupport.mobile.agent.modules.memory.dumpsys

import android.text.TextUtils
import com.rsupport.mobile.agent.modules.memory.MemoryUsage
import com.rsupport.mobile.agent.modules.memory.KeyObject
import java.util.*
import java.util.regex.Pattern

/**
 * 메모리 사용량을 [dumpsys meminfo] 을 이용해 추출한 데이터 기준으로 생성한다.
 */
class MemoryUsageDumpsys(private val textLine: String) : MemoryUsage() {

    private val PID_INDEX = 1

    /**
     * process id
     */
    val pid: String by lazy {
        getProcessID()
    }

    val pkgName: String by lazy {
        getPackageName()
    }

    override fun available(): Boolean {
        return !TextUtils.isEmpty(pid) && !TextUtils.isEmpty(pkgName)
    }

    override fun getUsageMemory(): Long {
        return findMemoryUsageByte(textLine)
    }

    private fun getProcessID(): String {
        return findMemoryUsagePid(textLine)
    }

    private fun getPackageName(): String {
        return findPackageName(textLine)
    }

    private fun findPackageName(textLine: String): String {
        if (!textLine.toLowerCase(Locale.ENGLISH).contains("pid")) {
            return ""
        }
        return textLine.trim().split(" ").let {
            val pkgIndex = 1
            if (it.size <= pkgIndex) {
                return@let ""
            } else it[pkgIndex]
        }
    }

    private fun findMemoryUsagePid(textLine: String): String {
        if (!textLine.toLowerCase(Locale.ENGLISH).contains("pid")) {
            return ""
        }
        return textLine.substring(textLine.indexOf("pid")).split(" ").let {
            if (it.size <= PID_INDEX) {
                return@let ""
            } else {
                it[PID_INDEX].replace(Regex("[^0-9]"), "")
            }
        }
    }

    private fun findMemoryUsageByte(textLine: String): Long {
        getMemoryString(textLine)?.let {
            val convertLongMemoryUsage = convertStringToLong(it)
            if (convertLongMemoryUsage != INVALID_USAGE_INFO) {
                return convertLongMemoryUsage * 1024
            }
        }
        return INVALID_USAGE_INFO
    }

    private fun getMemoryString(textLine: String): String? {
        val index = textLine.indexOf(':')
        if (index <= 0) return null
        return textLine.substring(0, index)
    }

    private fun convertStringToLong(memoryString: String): Long {
        val pattern = Pattern.compile("[0-9]+([,][0-9]{3})*")
        val matcher = pattern.matcher(memoryString)
        if (matcher.find()) {
            try {
                return replaceOnlyNumber(matcher.group()).toLong()
            } catch (e: Exception) {
            }
        }
        return INVALID_USAGE_INFO
    }

    private fun replaceOnlyNumber(text: String): String {
        return text.replace(Regex("[^0-9]"), "")
    }

    override fun memoryKey(): KeyObject {
        return KeyObject(getPackageName())
    }
}