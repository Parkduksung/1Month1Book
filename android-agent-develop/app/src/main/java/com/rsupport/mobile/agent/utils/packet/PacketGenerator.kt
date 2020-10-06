package com.rsupport.mobile.agent.utils.packet

interface PacketGenerator<T> {
    fun create(source: T): ByteArray
}