package com.rsupport.mobile.agent.modules.channel

import base.BaseTest
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.timeout
import com.rsupport.commons.net.socket.DataStream
import com.rsupport.commons.net.socket.SocketCompat
import com.rsupport.mobile.agent.modules.net.PacketHandler
import com.rsupport.mobile.agent.modules.net.SocketCompatFactory
import com.rsupport.mobile.agent.modules.net.channel.DataChannel
import com.rsupport.mobile.agent.modules.net.model.HeaderPacket
import com.rsupport.mobile.agent.modules.net.model.MsgPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.module.Module
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@RunWith(MockitoJUnitRunner::class)
class CRCAgentDataChannelTest : BaseTest() {

    override fun createModules(): List<Module> {
        return emptyList()
    }

    @Mock
    lateinit var onChannelListener: OnChannelListener

    @Mock
    lateinit var socketCompatFactory: SocketCompatFactory

    @Before
    override fun setup() {
        super.setup()
    }

    // 접속 시도시 성공했을때 결과를 확인한다.
    @Test
    fun agentConnectedTest() = runBlocking<Unit> {
        Mockito.`when`(socketCompatFactory.create(context)).thenReturn(ConnectedSocketCompatStub())

        val channel: CRCChannel = CRCAgentDataChannel(context).apply {
            setSocketCompatFactory(socketCompatFactory)
            setOnChannelListener(onChannelListener)
        }
        channel.startThread()

        Mockito.verify(onChannelListener, timeout(1000)).onConnected()
        MatcherAssert.assertThat("접속 상태가 아니라서 실패", channel.isConnected(), Matchers.`is`(true))
    }

    // 접속을 실패 했을때 결과를 확인한다.
    @Test
    fun agentPrepareFailTest() = runBlocking {
        Mockito.`when`(socketCompatFactory.create(context)).thenReturn(SocketCompatFailStub())

        val channel: CRCChannel = CRCAgentDataChannel(context).apply {
            setSocketCompatFactory(socketCompatFactory)
            setOnChannelListener(onChannelListener)
        }
        channel.startThread()
        Mockito.verify(onChannelListener, timeout(1000)).onConnectFail()
        Mockito.verify(onChannelListener, timeout(1000)).onDisconnected()
        MatcherAssert.assertThat("접속 상태 여서 실패", channel.isConnected(), Matchers.`is`(false))
    }

    // 데이터 스트림에서 읽은값이 0보다 작거나 같을때 접속이 종료되는지 확인한다.
    @Test
    fun readStreamDataEmptyTest() = runBlocking<Unit> {
        Mockito.`when`(socketCompatFactory.create(context)).thenReturn(ConnectedSocketCompatStub(DataStreamReadZeroStub()))

        val channel: CRCChannel = CRCAgentDataChannel(context).apply {
            setSocketCompatFactory(socketCompatFactory)
            setOnChannelListener(onChannelListener)
        }
        channel.startThread()

        Mockito.verify(onChannelListener, timeout(1000)).onConnected()
        Mockito.verify(onChannelListener, timeout(1000)).onDisconnected()
        MatcherAssert.assertThat("읽기가 성공해서 실패", channel.isConnected(), Matchers.`is`(false))
    }

    // msgPacket 이 정상적으로 수신되는지를 확인한다.
    @Test
    fun packetReceiveTest() = runBlocking<Unit> {
        val payload = 15
        val msgID = 1
        val message = "hello"

        val byteArrayInputStream = createPacketStream(payload, msgID, message)
        val mockDataChannel = Mockito.mock(DataChannel::class.java)
        val mockPacketHandler = Mockito.mock(PacketHandler::class.java) as PacketHandler<MsgPacket>

        Mockito.`when`(mockDataChannel.getPacketHandler()).thenReturn(mockPacketHandler)
        Mockito.`when`(socketCompatFactory.create(context)).thenReturn(ConnectedSocketCompatStub(DataStreamPacket(byteArrayInputStream)))

        val channel: CRCAgentDataChannel = CRCAgentDataChannel(context).apply {
            setSocketCompatFactory(socketCompatFactory)
            setOnChannelListener(onChannelListener)
            setChannelFactory(DataChannelMockFactory(mockDataChannel))
        }
        channel.startThread()

        val packetCaptor = argumentCaptor<MsgPacket>()
        val payloadInt = argumentCaptor<Int>()

        Mockito.verify(mockPacketHandler, timeout(1000)).onReceive(payloadInt.capture(), packetCaptor.capture())

        MatcherAssert.assertThat("payload 가 달라서 실패", payloadInt.firstValue, Matchers.`is`(payload))
        MatcherAssert.assertThat("msgid 가 달라서 실패", packetCaptor.firstValue.msgID, Matchers.`is`(msgID))
        MatcherAssert.assertThat("message data 가 달라서 실패", String(packetCaptor.firstValue.data!!), Matchers.`is`(message))
    }


    // 접속 시도후 바로 종료될때 상태를 확인한다.
    @Test
    fun releaseWhenConnectingTest() = runBlocking<Unit> {
        for (loopCount in 0 until 100) {
            val payload = 15
            val msgID = 1
            val message = "hello"

            val byteArrayInputStream = createPacketStream(payload, msgID, message)
            val mockDataChannel = Mockito.mock(DataChannel::class.java)
            val mockPacketHandler = Mockito.mock(PacketHandler::class.java) as PacketHandler<MsgPacket>
            val onChannelListener = Mockito.mock(OnChannelListener::class.java)

            Mockito.`when`(mockDataChannel.getPacketHandler()).thenReturn(mockPacketHandler)
            Mockito.`when`(socketCompatFactory.create(context)).thenReturn(ConnectedSocketCompatStub(DataStreamLoopPacket(byteArrayInputStream)))
            val channel: CRCAgentDataChannel = CRCAgentDataChannel(context).apply {
                setSocketCompatFactory(socketCompatFactory)
                setOnChannelListener(onChannelListener)
                setChannelFactory(DataChannelMockFactory(mockDataChannel))
            }
            channel.startThread()

            CoroutineScope(Dispatchers.IO).launch {
                channel.release()
            }

            Mockito.verify(onChannelListener).onConnecting()
            Mockito.verify(onChannelListener, timeout(1000)).onDisconnected()
        }
    }

    private fun createPacketStream(payload: Int, msgid: Int, message: String): ByteArrayInputStream {
        val msgPacketBuffer = ByteArray(102400)

        val msgPacket = MsgPacket().apply {
            this.msgID = msgid
            data = message.toByteArray()
            dataSize = data?.size ?: 0
        }
        msgPacket.push(msgPacketBuffer, 0)

        val headerPacketBuffer = ByteArray(5)
        val headerPacket = HeaderPacket(payload, msgPacket.size())
        headerPacket.push(headerPacketBuffer, 0)

        val byteArrayOutputStream = ByteArrayOutputStream()
        byteArrayOutputStream.write(headerPacketBuffer, 0, headerPacket.size())
        byteArrayOutputStream.write(msgPacketBuffer, 0, msgPacket.size())

        return ByteArrayInputStream(byteArrayOutputStream.toByteArray())
    }

    class DataChannelMockFactory(val dataChannel: DataChannel) : DataChannel.Factory {
        override fun create(): DataChannel {
            return dataChannel
        }
    }

    class SocketCompatFailStub(private val stream: DataStream = DataStreamStub()) : SocketCompat {

        override fun connect(): Boolean {
            return false
        }

        override fun disconnect() {
        }

        override fun enableEncrypt() {
        }

        override fun getDataStream(): DataStream {
            return stream
        }

        override fun isConnected(): Boolean {
            return false
        }
    }


    class ConnectedSocketCompatStub(private val stream: DataStream = DataStreamStub()) : SocketCompat {

        private var isConnected = false

        override fun connect(): Boolean {
            isConnected = true
            return isConnected
        }

        override fun disconnect() {
            isConnected = false
        }

        override fun enableEncrypt() {
        }

        override fun getDataStream(): DataStream {
            return stream
        }

        override fun isConnected(): Boolean {
            return isConnected
        }
    }

    class DataStreamLoopPacket(private val byteArrayInputStream: ByteArrayInputStream? = null) : DataStream {
        override fun close() {
        }

        override fun read(dst: ByteArray, offset: Int, length: Int): Int {
            return byteArrayInputStream?.let {
                if (byteArrayInputStream.available() < length) {
                    byteArrayInputStream.reset()
                }
                for (index in 0 until length) {
                    dst[index] = byteArrayInputStream.read().toByte()
                }
                return length
            } ?: length
        }

        override fun write(src: ByteArray, offset: Int, length: Int): Boolean {
            return true
        }
    }


    class DataStreamPacket(private val byteArrayInputStream: ByteArrayInputStream? = null) : DataStream {
        override fun close() {
        }

        override fun read(dst: ByteArray, offset: Int, length: Int): Int {
            return byteArrayInputStream?.let {
                if (byteArrayInputStream.available() < length) return DataStream.END_OF_STREAM
                for (index in 0 until length) {
                    dst[index] = byteArrayInputStream.read().toByte()
                }
                return length
            } ?: length
        }

        override fun write(src: ByteArray, offset: Int, length: Int): Boolean {
            return true
        }
    }


    class DataStreamStub : DataStream {
        override fun close() {
        }

        override fun read(dst: ByteArray, offset: Int, length: Int): Int {
            return length
        }

        override fun write(src: ByteArray, offset: Int, length: Int): Boolean {
            return true
        }
    }

    class DataStreamReadZeroStub : DataStream {
        override fun close() {

        }

        override fun read(dst: ByteArray, offset: Int, length: Int): Int {
            return 0
        }

        override fun write(src: ByteArray, offset: Int, length: Int): Boolean {
            return false
        }
    }
}