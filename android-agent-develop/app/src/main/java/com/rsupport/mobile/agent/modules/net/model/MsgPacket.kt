package com.rsupport.mobile.agent.modules.net.model

import com.rsupport.util.log.RLog
import control.Converter

class MsgPacket : Packet {

    var msgID = 0
    var dataSize = 0
    var data: ByteArray? = null

    // 패킷 만들기
    override fun push(szBuffer: ByteArray, nStart: Int) {
        if (szBuffer.size < 5) {
            RLog.w("szBuffer size error. (" + szBuffer.size + ")")
            return
        }
        var nIndex = nStart
        szBuffer[nIndex] = msgID.toByte()
        nIndex++
        System.arraycopy(Converter.getBytesFromIntLE(dataSize), 0, szBuffer, nIndex, 4)
        nIndex += 4
        data?.let {
            System.arraycopy(it, 0, szBuffer, nIndex, it.size)
        }
    }

    override fun save(szBuffer: ByteArray, nStart: Int) {}

    override fun save(szBuffer: ByteArray, nStart: Int, dstOffset: Int, dstLen: Int) {
        var nIndex = nStart
        var st = 0
        // id
        msgID = (szBuffer[nIndex].toInt() and 0xff)
        nIndex++
        st++
        if (nIndex - nStart >= dstLen) {
            return
        }
        // datasize
        if (st >= dstOffset) {
            dataSize = Converter.readIntLittleEndian(szBuffer, nIndex)
            nIndex += 4
        }
        st++
        if (nIndex - nStart >= dstLen) {
            return
        }
        // data
        if (st >= dstOffset) {
            if (szBuffer.size >= nIndex + dataSize) {
                data = ByteArray(dataSize)
                System.arraycopy(szBuffer, nIndex, data, 0, dataSize)
            }
        }
    }

    override fun size(): Int {
        return 5 + (data?.size ?: 0)
    }

    override fun clear() {
        msgID = 0
        dataSize = 0
        data = null
    }
}