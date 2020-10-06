package com.rsupport.mobile.agent.utils.packet

import java.nio.ByteBuffer
import java.nio.ByteOrder

class TypedByteArrayPacket : PacketGenerator<TypedByteArraySourcePacket> {
    override fun create(source: TypedByteArraySourcePacket): ByteArray {
        val type = source.value
        val data1Size = getDataSize(source.data1)
        val data2Size = getDataSize(source.data2)

        val bufferSize = 1 + 4 + 4 + data1Size + data2Size

        val byteBuffer = ByteBuffer.allocate(bufferSize)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)

        putValue(byteBuffer, type)
        putLength(byteBuffer, data1Size)
        putLength(byteBuffer, data2Size)
        putData(byteBuffer, source.data1)
        putData(byteBuffer, source.data2)

        return byteBuffer.array()
    }

    private fun putLength(byteBuffer: ByteBuffer, data1Size: Int) {
        byteBuffer.putInt(data1Size)
    }

    private fun putValue(byteBuffer: ByteBuffer, type: Int) {
        byteBuffer.put(type.toByte())
    }

    private fun putData(byteBuffer: ByteBuffer, data1: ByteArray?) {
        data1?.let {
            byteBuffer.put(it)
        }
    }

    private fun getDataSize(data1: ByteArray?) = data1?.size ?: 0
}