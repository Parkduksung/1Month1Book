package com.rsupport.mobile.agent.modules.memory.shell

import com.rsupport.mobile.agent.modules.memory.MemoryUsage
import com.rsupport.mobile.agent.modules.memory.KeyObject
import java.util.*

/**
 * https://linux.die.net/man/5/proc
 * (24) Resident Set Size: number of pages the process has in real memory.
 * This is just the pages which count toward text, data, or stack space.
 * This does not include pages which have not been demand-loaded in, or which are swapped out.
 */
class MemoryUsageProcStat(private val pkgName: String, private val statString: String) : MemoryUsage() {
    override fun getUsageMemory(): Long {
        return readMemoryStat()
    }

    override fun available(): Boolean {
        return true
    }

    override fun memoryKey(): KeyObject {
        return KeyObject(pkgName)
    }

    private fun readMemoryStat(): Long {
        try {
            var line = statString.trim { it <= ' ' }
            val idx = line.lastIndexOf(')')
            if (idx != -1) {
                line = line.substring(idx + 1).trim { it <= ' ' }
                val tokens = StringTokenizer(line)
                var rss: String? = null
                var i = 0
                var tk: String?
                // [21] for [rss]
                while (tokens.hasMoreTokens()) {
                    tk = tokens.nextToken()
                    if (i == 21) {
                        rss = tk
                        break
                    }
                    i++
                }
                return (rss!!.toLong() * 4 * 1024)
            } else return INVALID_USAGE_INFO
        } catch (e: Exception) {
            return INVALID_USAGE_INFO
        }
    }
}