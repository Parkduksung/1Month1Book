package com.rsupport.mobile.agent.modules.channel.screen

import android.media.MediaCodec
import android.media.MediaFormat
import com.rsupport.media.ICodecAdapter
import com.rsupport.mobile.agent.modules.channel.CRCAgentScreenChannel
import com.rsupport.mobile.agent.modules.channel.CRCAgentScreenChannel.PACKET_HEADERSIZE
import com.rsupport.mobile.agent.modules.net.protocol.MessageID
import com.rsupport.util.log.RLog
import java.nio.ByteBuffer

class RSNetCodecAdapter(private val screenChannel: CRCAgentScreenChannel) : ICodecAdapter() {

    override fun onVDeqeueFormatChanged(data: ByteArray) {
        RLog.i("RSNetCodecAdapter onVDeqeueFormatChanged len : " + data.size)
        screenChannel.sendPacket(MessageID.rcpXENC264Stream, MessageID.rcpXENC264StreamData, data, data.size)
    }

    override fun onVDeqeueFormatChanged(data: ByteArray?, offset: Int, size: Int) {
        RLog.i("RSNetCodecAdapter onVDeqeueFormatChanged len : $size")
        screenChannel.sendPacket(MessageID.rcpXENC264Stream, MessageID.rcpXENC264StreamData, data, size)
    }

    override fun onVDeqeueFormatChanged(format: MediaFormat) {
        val spsBuffer = format.getByteBuffer("csd-0")
        val ppsBuffer = format.getByteBuffer("csd-1")
        val sendBuffer = ByteArray(spsBuffer.capacity() + ppsBuffer.capacity())
        spsBuffer[sendBuffer, 0, spsBuffer.capacity()]
        ppsBuffer[sendBuffer, spsBuffer.capacity(), ppsBuffer.capacity()]
        RLog.i("RSNetCodecAdapter onVDeqeueFormatChanged len : " + sendBuffer.size)
        screenChannel.sendPacket(MessageID.rcpXENC264Stream, MessageID.rcpXENC264StreamData, sendBuffer, sendBuffer.size)
    }

    override fun onVDequeueOutput(data: ByteArray) {
        screenChannel.sendBoostPacket(MessageID.rcpXENC264Stream, MessageID.rcpXENC264StreamData, data, data.size)
    }

    override fun onVDequeueOutput(data: ByteArray?, offset: Int, size: Int) {
        val sendBuffer = getSendBuffer(size + PACKET_HEADERSIZE)
        data?.let { System.arraycopy(it, offset, sendBuffer, PACKET_HEADERSIZE, size) }
        screenChannel.sendBoostPacket(MessageID.rcpXENC264Stream, MessageID.rcpXENC264StreamData, sendBuffer, size)
    }

    private var bytesTempBuffer = ByteArray(1024 * 1024)
    private fun getSendBuffer(size: Int): ByteArray {
        if (bytesTempBuffer.size < size) {
            bytesTempBuffer = ByteArray(size)
        }
        return bytesTempBuffer
    }

    override fun onVDequeueOutput(byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        val size = bufferInfo.size - bufferInfo.offset
        val sendBuffer = getSendBuffer(size + PACKET_HEADERSIZE)
        byteBuffer.clear()
        byteBuffer[sendBuffer, PACKET_HEADERSIZE, size]
        byteBuffer.flip()
        screenChannel.sendBoostPacket(MessageID.rcpXENC264Stream, MessageID.rcpXENC264StreamData, sendBuffer, size)
    }

    override fun onVideoHeader(data: ByteArray) {
        screenChannel.sendPacket(MessageID.rcpXENC264Stream, MessageID.rcpXENC264StreamHeader, data, data.size)
    }

    override fun onVideoHeaderRec(data: ByteArray) {
    }

    override fun onVStart() {
    }

    override fun onVStop() {
    }

    override fun onVError() {
    }
}