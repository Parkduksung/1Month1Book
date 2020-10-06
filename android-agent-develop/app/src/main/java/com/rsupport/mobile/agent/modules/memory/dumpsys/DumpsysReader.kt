package com.rsupport.mobile.agent.modules.memory.dumpsys

import com.rsupport.mobile.agent.service.RSPermService
import com.rsupport.rsperm.IRSPerm
import com.rsupport.util.log.RLog
import org.koin.java.KoinJavaComponent.inject
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.InputStreamReader

interface StringReader {
    fun read(): String
}

abstract class DumpsysReader : StringReader {
    companion object {
        @JvmStatic
        fun createMemInfo(): DumpsysReader {
            val rspermService by inject(RSPermService::class.java)
            return rspermService.getRsperm()?.let {
                return@let if (it.hasDumpsys()) MemoryDumpsysReaderFromRsperm(it) else null
            } ?: object : DumpsysReader() {
                override fun read(): String {
                    return ""
                }
            }
        }

        @JvmStatic
        fun createCpuInfo(): DumpsysReader {
            val rspermService by inject(RSPermService::class.java)
            return rspermService.getRsperm()?.let {
                return@let if (it.hasDumpsys()) CPUDumpsysReaderFromRsperm(it) else null
            } ?: object : DumpsysReader() {
                override fun read(): String {
                    return ""
                }
            }
        }
    }
}

class CPUDumpsysReaderFromRsperm(rsperm: IRSPerm) : DumpsysReaderFromRsperm(rsperm) {
    private val DUMP_SYS_MEMINFO_FILE = "/data/data/${rsperm.address}/dumpsys_cpuinfo.log"
    private val RM_COMMAND = "/system/bin/rm $DUMP_SYS_MEMINFO_FILE"
    private val DUMP_SYS_MEMINFO_COMMAND = "/system/bin/dumpsys cpuinfo > $DUMP_SYS_MEMINFO_FILE"

    override fun getRemoveCommand(): String {
        return RM_COMMAND
    }

    override fun getExecuteCommand(): String {
        return DUMP_SYS_MEMINFO_COMMAND
    }
}

class MemoryDumpsysReaderFromRsperm(rsperm: IRSPerm) : DumpsysReaderFromRsperm(rsperm) {
    private val DUMP_SYS_MEMINFO_FILE = "/data/data/${rsperm.address}/dumpsys_meminfo.log"
    private val RM_COMMAND = "/system/bin/rm $DUMP_SYS_MEMINFO_FILE"
    private val DUMP_SYS_MEMINFO_COMMAND = "/system/bin/dumpsys meminfo > $DUMP_SYS_MEMINFO_FILE"

    override fun getRemoveCommand(): String {
        return RM_COMMAND
    }

    override fun getExecuteCommand(): String {
        return DUMP_SYS_MEMINFO_COMMAND
    }
}


abstract class DumpsysReaderFromRsperm(private val rsperm: IRSPerm) : DumpsysReader() {

    abstract fun getRemoveCommand(): String
    abstract fun getExecuteCommand(): String

    override fun read(): String {
        executeCommand(getRemoveCommand())
        return executeCommand(getExecuteCommand())?.let {
            return@let InputStreamReader(FileInputStream(it)).use { inputStreamReader ->
                inputStreamReader.readText()
            }
        } ?: ""
    }

    private fun executeCommand(command: String): FileDescriptor? {
        try {
            return rsperm.getFile(command)
        } catch (e: Exception) {
            RLog.e(e)
        }
        return null
    }
}