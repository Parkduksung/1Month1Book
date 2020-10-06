package com.rsupport.mobile.agent.utils.packet

class TypedByteArraySourcePacket(
        val value: Int,
        val data1: ByteArray?,
        val data2: ByteArray?
) : SourcePacket