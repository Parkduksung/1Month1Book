package com.rsupport.mobile.agent.modules.channel.screen

import android.media.MediaCodec
import android.media.MediaFormat
import com.rsupport.media.ICodecAdapter
import com.rsupport.mobile.agent.modules.channel.CRCAgentScreenChannel
import com.rsupport.util.log.RLog
import java.nio.ByteBuffer

class RC45CodecAdapter(private val screenChannel: CRCAgentScreenChannel) : ICodecAdapter() {
    private var mIsSendVHeaderRec = false

    override fun onVDeqeueFormatChanged(data: ByteArray) {
        RLog.i("RC45CodecAdapter onVDeqeueFormatChanged len : " + data.size)
        screenChannel.sendPacket(ChannelConstants.rcpX264Stream, ChannelConstants.rcpX264StreamSPPS, data, data.size)
    }

    override fun onVDeqeueFormatChanged(data: ByteArray?, offset: Int, size: Int) {
        RLog.i("RC45CodecAdapter onVDeqeueFormatChanged len : $size")
        screenChannel.sendPacket(ChannelConstants.rcpX264Stream, ChannelConstants.rcpX264StreamSPPS, data, size)
    }

    override fun onVDeqeueFormatChanged(format: MediaFormat) {
        val spsb = format.getByteBuffer("csd-0")
        val ppsb = format.getByteBuffer("csd-1")
        val spps = ByteArray(spsb.capacity() + ppsb.capacity())
        spsb[spps, 0, spsb.capacity()]
        ppsb[spps, spsb.capacity(), ppsb.capacity()]
        RLog.i("RC45CodecAdapter onVDeqeueFormatChanged len : " + spps.size)
        screenChannel.sendPacket(ChannelConstants.rcpX264Stream, ChannelConstants.rcpX264StreamSPPS, spps, spps.size)
    }

    override fun onVDequeueOutput(data: ByteArray) {
//            RLog.i("VideoStreamTrace CRCAgentScreenChannel EncoderObserver onDequeueOutput len : " + data.length);
        screenChannel.sendBoostPacket(ChannelConstants.rcpX264Stream, ChannelConstants.rcpX264StreamData, data, data.size)
    }

    override fun onVDequeueOutput(data: ByteArray?, offset: Int, size: Int) {
//            RLog.i("VideoStreamTrace CRCAgentScreenChannel EncoderObserver onDequeueOutput len : " + size);
        val sendBuffer = getSendBuffer(size + CRCAgentScreenChannel.PACKET_HEADERSIZE)
        data?.let { System.arraycopy(it, offset, sendBuffer, CRCAgentScreenChannel.PACKET_HEADERSIZE, size) }
        screenChannel.sendBoostPacket(ChannelConstants.rcpX264Stream, ChannelConstants.rcpX264StreamData, sendBuffer, size)
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
        //            RLog.i("VideoStreamTrace CRCAgentScreenChannel EncoderObserver onDequeueOutput len : " + bufferInfo.size + ", " + size + ", " + bufferInfo.offset);
        val sendBuffer = getSendBuffer(size + CRCAgentScreenChannel.PACKET_HEADERSIZE)
        byteBuffer.clear()
        byteBuffer[sendBuffer, CRCAgentScreenChannel.PACKET_HEADERSIZE, size]
        byteBuffer.flip()
        screenChannel.sendBoostPacket(ChannelConstants.rcpX264Stream, ChannelConstants.rcpX264StreamData, sendBuffer, size)
    }

    override fun onVideoHeader(data: ByteArray) {
        RLog.i("RC45CodecAdapter onVideoHeader len : " + data.size)
        screenChannel.sendPacket(ChannelConstants.rcpX264Stream, ChannelConstants.rcpX264StreamHeader, data, data.size)
    }

    override fun onVideoHeaderRec(data: ByteArray) {
        if (mIsSendVHeaderRec) return
        RLog.i("RC45CodecAdapter onVideoHeaderRec len : " + data.size)
        mIsSendVHeaderRec = true
        screenChannel.sendPacket(ChannelConstants.rcpX264Stream, ChannelConstants.rcpX264StreamHeaderRec, data, data.size)
    }

    override fun onVStart() {
        RLog.i("RC45CodecAdapter EncoderObserver onStart")
    }

    override fun onVStop() {
        RLog.i("RC45CodecAdapter EncoderObserver onVStop")
    }

    override fun onVError() {
        RLog.i("RC45CodecAdapter EncoderObserver onVError")
    }
}