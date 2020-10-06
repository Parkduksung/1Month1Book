package com.rsupport.mobile.agent.utils.packet

import com.rsupport.mobile.agent.utils.Converter

class PrefixLengthPacket : PacketGenerator<ByteArray> {
    override fun create(source: ByteArray): ByteArray {
        val appDetailInfoPacket = ByteArray(4 + source.size) //length + mem byte
        var startPos = 0
        System.arraycopy(Converter.getBytesFromIntLE(source.size), 0, appDetailInfoPacket, startPos, 4)
        startPos += 4
        System.arraycopy(source, 0, appDetailInfoPacket, startPos, source.size)
        return appDetailInfoPacket
    }
}