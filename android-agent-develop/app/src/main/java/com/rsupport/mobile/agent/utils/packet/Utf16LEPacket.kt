package com.rsupport.mobile.agent.utils.packet

import java.nio.charset.StandardCharsets

class Utf16LEPacket : PacketGenerator<String> {
    override fun create(source: String): ByteArray {
        val noneNullBytes = source.toByteArray(StandardCharsets.UTF_16LE)
        val addNullBytes = ByteArray(noneNullBytes.size + 2)
        System.arraycopy(noneNullBytes, 0, addNullBytes, 0, noneNullBytes.size)
        return addNullBytes
    }
}