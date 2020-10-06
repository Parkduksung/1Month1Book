package com.rsupport.mobile.agent.modules.channel

import android.content.Context
import com.rsupport.commons.net.socket.DataStream
import com.rsupport.commons.net.socket.SocketCompat
import com.rsupport.mobile.agent.modules.channel.screen.ChannelConstants
import com.rsupport.mobile.agent.modules.net.OnConfigChangeListener
import com.rsupport.mobile.agent.modules.net.SocketCompatFactory
import com.rsupport.mobile.agent.modules.net.model.HeaderPacket
import com.rsupport.mobile.agent.modules.net.model.MsgPacket
import com.rsupport.mobile.agent.modules.net.model.Packet
import com.rsupport.mobile.agent.utils.timer.DebounceTimer
import com.rsupport.util.log.RLog
import control.Converter
import java.util.concurrent.atomic.AtomicBoolean

abstract class CRCChannel(protected val context: Context, private val nopTimer: DebounceTimer) : OnConfigChangeListener {

    companion object {
        private const val NOP_INTERVAL: Long = 25000
    }

    private var receivedData = ByteArray(1024 * 10)

    private var onChannelListener: OnChannelListener? = null
    private var socketCompatFactory: SocketCompatFactory? = null
    private var socketCompat: SocketCompat? = null
    private var dataStream: DataStream? = null

    private var isThreadRun = AtomicBoolean(false)

    private var readPacketThread: Thread? = null

    /**
     * [startThread] 실행시 가장먼저 불린다.
     */
    protected abstract fun onPrepare()

    /**
     * [startThread] 실행시 접속이 성공하면 호출된다.
     * @param socketCompat 네트워크 객체
     */
    protected abstract fun onConnected(socketCompat: SocketCompat)

    /**
     * [startThread] 실행시 접속이 실패하면 호출된다.
     */
    protected abstract fun onConnectFail()

    /**
     * [startThread] 접속 성공이후 접속이 종료될때 호출된다.
     */
    protected abstract fun onDisconnected()

    /**
     * 채널 자원이 해지될때 호출된다.
     */
    protected abstract fun onReleased()

    /**
     * [startThread] 을 호출후 접속 성공후 데이터를 전송 받으면 호출된다.
     */
    protected abstract fun onReceivePacket(payloadType: Int, msg: MsgPacket)

    /**
     * 채널을 접속하고 시작할때 호출한다.
     * 호출하기 전에 [setSocketCompatFactory]에 [SocketCompatFactory] 를 설정해야한다.
     *
     * @see [setSocketCompatFactory]
     * @see [SocketCompatFactory]
     * @see [onPrepare]
     * @see [onConnected]
     * @see [onDisconnected]
     * @see [onReceivePacket]
     * @see [onReceivePacket]
     */
    @Synchronized
    fun startThread() {
        isThreadRun.set(true)
        onChannelListener?.onConnecting()
        onPrepare()
        readPacketThread = Thread {
            try {
                val socketCompat: SocketCompat
                synchronized(this@CRCChannel) {
                    socketCompat = createSocket(context)
                }
                if (!isThreadRun.get() || !socketCompat.connect()) {
                    onConnectFail()
                    onChannelListener?.onConnectFail()
                    release()
                    return@Thread
                }
                keepSessionThread()

                onConnected(socketCompat)
                onChannelListener?.onConnected()

                readPacketLoop()
            } catch (e: Exception) {
                RLog.e(e)
                onConnectFail()
                onChannelListener?.onConnectFail()
            } finally {
                socketCompat?.disconnect()
                onDisconnected()
                onChannelListener?.onDisconnected()
            }
        }.apply { start() }
    }

    fun setOnChannelListener(channelListener: OnChannelListener) {
        this.onChannelListener = channelListener;
    }

    fun setSocketCompatFactory(socketCompatFactory: SocketCompatFactory) {
        this.socketCompatFactory = socketCompatFactory
    }

    /**
     * [startThread] 호출후 접속이 성공했는지를 확인한다.
     * @return 접속상태명 true, 그렇지 않으면 false
     */
    @Synchronized
    fun isConnected(): Boolean {
        return socketCompat?.isConnected() ?: false
    }

    @Synchronized
    fun release(): Boolean {
        try {
            stopThread()
            return closeSocket()
        } finally {
            onReleased()
        }
    }

    protected fun writeExact(sendBuf: ByteArray, offset: Int, nSize: Int): Boolean {
        if (socketCompat == null) {
            RLog.e("socketCompat is null")
            return false
        }
        val writeResult = dataStream?.write(sendBuf, offset, nSize) ?: false
        if (!writeResult) {
            RLog.e("Failed to Decoder.WriteExact")
        }
        nopTimer.update()
        return writeResult
    }

    protected fun sendNop(cmd: Int): Boolean {
        return sendPacket(ChannelConstants.rcpChannelNop, cmd)
    }

    private fun stopThread() {
        isThreadRun.set(false)
        nopTimer.cancel()
        readPacketThread?.interrupt()
        readPacketThread = null
    }

    private fun readPacketLoop() {
        val headerPacket = HeaderPacket()
        while (isThreadRun.get()) {
            if (!readHeaderExact(headerPacket)) {
                break
            }
            readMsgPacket(headerPacket)
        }
    }

    private fun createSocket(context: Context): SocketCompat {
        checkNotNull(socketCompatFactory) { "must set streamFactory" }
        if (socketCompat == null) {
            socketCompat = socketCompatFactory?.create(context)
            dataStream = socketCompat?.getDataStream()
        }
        return socketCompat!!
    }

    private fun readExact(pBuf: ByteArray?, offset: Int, len: Int): Boolean {
        if (socketCompat == null || len <= 0) return false
        val read = dataStream?.read(pBuf!!, offset, len) ?: -1
        if (read <= 0) {
            RLog.e("Failed to ReadExact s")
            return false
        }
        return true
    }

    private fun readHeaderExact(headerPacket: Packet): Boolean {
        if (headerPacket.size() > receivedData.size) {
            RLog.w("Failed to ReadExact 1")
            return false
        }
        if (readExact(receivedData, 0, headerPacket.size())) {
            headerPacket.save(receivedData, 0)
            return true
        }
        return false
    }

    private fun readExact(len: Int): ByteArray? {
        try {
            if (receivedData.size < len) {
                receivedData = ByteArray(len)
            }
        } catch (e: OutOfMemoryError) {
            return null
        }
        return if (readExact(receivedData, 0, len)) receivedData else null
    }

    private fun closeSocket(): Boolean {
        socketCompat?.disconnect()
        socketCompat = null
        return true
    }

    private fun sendPacket(payloadtype: Int, msgid: Int): Boolean {
        return sendPacket(payloadtype, msgid, null, 0)
    }

    private fun sendPacket(payloadtype: Int, msgid: Int, data: ByteArray?, dataSize: Int): Boolean {
        if (payloadtype == -1) return false
        val headerPacket = HeaderPacket()
        var nPacketSize = 0
        nPacketSize = if (data != null && dataSize > 0) {
            ChannelConstants.sz_rcpPacket + ChannelConstants.sz_rcpDataMessage + dataSize
        } else {
            ChannelConstants.sz_rcpPacket + ChannelConstants.sz_rcpMessage
        }
        val pSendPacket = ByteArray(nPacketSize)
        var nPacketPos = ChannelConstants.sz_rcpPacket
        headerPacket.payloadtype = payloadtype
        headerPacket.msgsize = nPacketSize - ChannelConstants.sz_rcpPacket
        headerPacket.push(pSendPacket, 0)
        if (dataSize > 0 && data != null) {
            pSendPacket[nPacketPos] = msgid.toByte()
            nPacketPos++
            System.arraycopy(Converter.getBytesFromIntLE(dataSize), 0, pSendPacket, nPacketPos, 4)
            nPacketPos += 4
            System.arraycopy(data, 0, pSendPacket, nPacketPos, dataSize)
        } else {
            pSendPacket[nPacketPos] = msgid.toByte()
        }
        return writeExact(pSendPacket, 0, nPacketSize)
    }

    private fun readMsgPacket(headerPacket: HeaderPacket): Boolean {
        val rcpMsgPacket = MsgPacket()
        var rcpMsgPacketReadResult = false
        if (headerPacket.msgsize > 0) {
            val messageBuffer = readExact(headerPacket.msgsize)
            if (messageBuffer == null) rcpMsgPacketReadResult = false else {
                rcpMsgPacketReadResult = true
                rcpMsgPacket.save(messageBuffer, 0, 0, headerPacket.msgsize)
            }
        }
        if (rcpMsgPacketReadResult) {
            onReceivePacket(headerPacket.payloadtype, rcpMsgPacket)
        }
        return rcpMsgPacketReadResult
    }

    private fun keepSessionThread() {
        nopTimer.schedule(NOP_INTERVAL, Runnable {
            if (isConnected()) {
                sendNop(ChannelConstants.rcpNopRequest)
            }
        })
    }
}