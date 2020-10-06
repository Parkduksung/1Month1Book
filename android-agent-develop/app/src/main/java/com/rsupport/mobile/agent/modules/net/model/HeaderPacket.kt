package com.rsupport.mobile.agent.modules.net.model

import com.rsupport.mobile.agent.utils.Converter
import com.rsupport.util.log.RLog

data class HeaderPacket(var payloadtype: Int = 0,
                        var msgsize: Int = 0) : Packet {

    override fun save(szBuffer: ByteArray, nStart: Int) {
        if (szBuffer.size < nStart + 4) {
            RLog.w("save error. szBuffer.${szBuffer.size}, start.${nStart}")
            return
        }

        payloadtype = (szBuffer[nStart].toInt() and 0xFF)
        msgsize = Converter.readIntLittleEndian(szBuffer, nStart + 1)
    }

    override fun save(szBuffer: ByteArray, nStart: Int, dstOffset: Int, dstLen: Int) {
        RLog.w("not support save.")
    }

    override fun push(szBuffer: ByteArray, nStart: Int) {
        szBuffer[nStart] = payloadtype.toByte()
        System.arraycopy(Converter.getBytesFromIntLE(msgsize), 0, szBuffer, nStart + 1, 4)
    }

    override fun size(): Int {
        return 5
    }

    override fun clear() {
        payloadtype = 0
        msgsize = 0
    }
}