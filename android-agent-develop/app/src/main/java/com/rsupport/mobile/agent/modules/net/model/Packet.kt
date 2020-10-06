package com.rsupport.mobile.agent.modules.net.model

interface Packet {

    //After read szBuffer, save to Instance.
    fun save(szBuffer: ByteArray, nStart: Int)

    //After read szBuffer, save to Instance. (from offset position to offset+dstLen position)
    fun save(szBuffer: ByteArray, nStart: Int, dstOffset: Int, dstLen: Int)

    //Save the Contents of Instance to szBuffer
    fun push(szBuffer: ByteArray, nStart: Int)

    //return size.
    fun size(): Int

    fun clear()
}