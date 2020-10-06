package com.rsupport.mobile.agent.modules.sysinfo.cpu.rsperm

import com.rsupport.mobile.agent.modules.memory.KeyObject
import com.rsupport.mobile.agent.modules.sysinfo.cpu.CpuUsage
import java.util.regex.Pattern
import kotlin.math.roundToInt

class CpuUsageDumpsys(private val textLine: String) : CpuUsage() {

    private val pkgName: String by lazy {
        findPackageName(textLine.trim())
    }

    override fun usagePercent(): Int {
        return parseCpuUsagePercent(textLine)
    }

    override fun available(): Boolean {
        return usagePercent() != INVALID_PERCENT && pkgName.isNotEmpty()
    }

    override fun getKey(): KeyObject {
        return KeyObject(pkgName)
    }

    private fun parseCpuUsagePercent(textLine: String): Int {
        return try {
            val index = textLine.indexOf("%")
            if (index <= 0) return INVALID_PERCENT
            parseFirstPercentText(textLine)?.replace("%".toRegex(), "")?.toFloat()?.roundToInt()
                    ?: INVALID_PERCENT
        } catch (e: Exception) {
            return INVALID_PERCENT
        }
    }

    private fun parseFirstPercentText(text: String): String? {
        val pattern = Pattern.compile("(([\\d]+([\\.](\\d)*)?)|([\\.](\\d)+))%")
        val matcher = pattern.matcher(text)
        return if (matcher.find()) {
            matcher.group()
        } else null
    }

    private fun findPackageName(textLine: String): String {
        if (textLine.indexOf('/') < 0) return ""
        return textLine.substring(textLine.indexOf('/') + 1).let {
            it.split("[/,:]".toRegex()).let {
                if (it.isNotEmpty()) it[0] else ""
            }
        }
    }
}
