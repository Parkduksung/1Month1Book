package com.rsupport.mobile.agent.modules.memory

import java.io.ByteArrayInputStream
import java.io.InputStreamReader

abstract class MemoryUsage : KeyContainer {

    companion object {
        const val INVALID_USAGE_INFO = -1L

        @JvmStatic
        fun from(factory: Factory): UsageContainer<MemoryUsage> {
            return factory.get()
        }
    }

    interface Factory {
        fun get(): UsageContainer<MemoryUsage>
        fun close()
    }

    protected abstract fun getUsageMemory(): Long
    protected abstract fun available(): Boolean
    protected abstract fun memoryKey(): KeyObject

    /**
     * 사용하고 있는 메모라양 byte
     */
    val usageByte: Long by lazy {
        getUsageMemory()
    }

    /**
     * 정상적으로 메모리 사용량과 pid 를 이용할 수 있는지 확인한다.
     */
    val isAvailable: Boolean by lazy {
        if (usageByte == INVALID_USAGE_INFO) return@lazy false
        available()
    }

    override val keyObject: KeyObject by lazy {
        memoryKey()
    }
}

interface KeyContainer {
    val keyObject: KeyObject
}

/**
 * String 을 line 별로 list 로 읽는다.
 */
class LineSplitter(private val text: String) {
    fun getLines(): List<String> {
        return InputStreamReader(ByteArrayInputStream(text.toByteArray())).use {
            it.readLines()
        }
    }
}
