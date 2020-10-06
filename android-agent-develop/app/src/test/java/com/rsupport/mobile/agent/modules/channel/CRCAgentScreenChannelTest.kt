package com.rsupport.mobile.agent.modules.channel

import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.rsupport.commons.net.socket.DataStream
import com.rsupport.commons.net.socket.SocketCompat
import com.rsupport.media.ICodecAdapter
import com.rsupport.media.ICodecListener
import com.rsupport.media.stream.ScreenStream
import com.rsupport.mobile.agent.TestApplication
import com.rsupport.mobile.agent.modules.channel.screen.ChannelConstants
import com.rsupport.mobile.agent.modules.channel.screen.RC45CodecAdapter
import com.rsupport.mobile.agent.modules.channel.screen.ScreenStreamFactory
import com.rsupport.mobile.agent.modules.net.SocketCompatFactory
import com.rsupport.mobile.agent.modules.net.model.HeaderPacket
import com.rsupport.mobile.agent.modules.net.model.MsgPacket
import com.rsupport.mobile.agent.modules.net.protocol.MessageID
import com.rsupport.mobile.agent.utils.timer.ThreadDebounceTimer
import junit.framework.Assert.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(
        application = TestApplication::class
)
@RunWith(RobolectricTestRunner::class)
class CRCAgentScreenChannelTest {

    private lateinit var context: Context
    private val sendPacketListener = mock(CRCAgentScreenChannel.OnSendPacketListener::class.java)
    private val packetWriteListener = mock(OnPacketWriteListener::class.java)
    private val screenStreamFactory = mock(ScreenStreamFactory::class.java)
    private lateinit var screenChannel: CRCAgentScreenChannel
    private lateinit var socketCompatFactory: SocketCompatFactory

    @Before
    fun setup() = runBlocking<Unit> {
        context = ApplicationProvider.getApplicationContext()
        socketCompatFactory = mock(SocketCompatFactory::class.java)
        screenChannel = CRCAgentScreenChannel(context, screenStreamFactory, ThreadDebounceTimer())
        screenChannel.setSendPacketListener(sendPacketListener)
        screenChannel.setSocketCompatFactory(socketCompatFactory)
    }

    @Test
    fun can_create() {
        val screenChannel = CRCAgentScreenChannel(context, screenStreamFactory, ThreadDebounceTimer())
        assertNotNull(screenChannel)
    }

    @Test
    fun receive_packet_when_send_boost_packet() = runBlocking<Unit> {
        setupScreenChannel(ConnectedSocketCompatStub(packetWriteListener))
        val dataBuffer = "hello".toByteArray()
        val sendBuffer = createSendBuffer(dataBuffer)

        val sendResult = screenChannel.sendBoostPacket(1, 2, sendBuffer, dataBuffer.count())
        verify(sendPacketListener).onSend(any())
        verify(packetWriteListener).onReceive(eq(1), eq(2), eq(dataBuffer), eq(dataBuffer.count()))
        assertTrue(sendResult)
    }


    @Test
    fun write_packet_size() = runBlocking<Unit> {
        setupScreenChannel(ConnectedSocketCompatStub(packetWriteListener))
        val dataBuffer = "hello".toByteArray()
        val sendBuffer = createSendBuffer(dataBuffer)

        val sendResult = screenChannel.sendBoostPacket(1, 2, sendBuffer, dataBuffer.count())
        verify(sendPacketListener).onSend(eq(H264OutPutData(dataBuffer.count())))
        assertTrue(sendResult)
    }

    @Test
    fun receive_packet_when_send_boost_empty_packet() = runBlocking<Unit> {
        setupScreenChannel(ConnectedSocketCompatStub(packetWriteListener))

        val sendResult = screenChannel.sendBoostPacket(3, 4, null, 0)
        verify(sendPacketListener).onSend(any())
        verify(packetWriteListener).onReceive(eq(3), eq(4), eq(null), eq(0))
        assertTrue(sendResult)
    }

    @Test
    fun write_fail_boost_packet() = runBlocking<Unit> {
        setupScreenChannel(WriteFailConnectedSocketCompatStub())

        val sendResult = screenChannel.sendBoostPacket(5, 6, null, 0)
        assertFalse(sendResult)
    }

    @Test
    fun send_packet_data() = runBlocking<Unit> {
        val dataBuffer = "hello".toByteArray()

        setupScreenChannel(ConnectedSocketCompatStub(packetWriteListener))
        val sendResult = screenChannel.sendPacket(1, 2, dataBuffer, dataBuffer.count())

        verify(packetWriteListener).onReceive(eq(1), eq(2), eq(dataBuffer), eq(dataBuffer.count()))
        assertTrue(sendResult)
    }

    @Test
    fun send_packet_empty_data() = runBlocking<Unit> {
        setupScreenChannel(ConnectedSocketCompatStub(packetWriteListener))
        val sendResult = screenChannel.sendPacket(1, 2, null, 0)

        verify(packetWriteListener).onReceive(eq(1), eq(2), eq(null), eq(0))
        assertTrue(sendResult)
    }

    @Test
    fun write_fail_packet() = runBlocking<Unit> {
        setupScreenChannel(WriteFailConnectedSocketCompatStub())

        val sendResult = screenChannel.sendPacket(1, 2, null, 0)
        assertFalse(sendResult)
    }

    @Test
    fun start_screen_stream_when_controller_start() = runBlocking<Unit> {
        val mockScreenStream = setupMockScreenStream()

        screenChannel.start()

        verify(mockScreenStream).start()
    }


    @Test
    fun not_called_stop_screen_stream_when_not_start_controller() = runBlocking<Unit> {
        val mockScreenStream = setupMockScreenStream()

        screenChannel.stop()

        verify(mockScreenStream, never()).stop()
    }

    @Test
    fun called_stop_screen_stream_when_start_controller() = runBlocking<Unit> {
        val mockScreenStream = setupMockScreenStream()

        screenChannel.start()
        screenChannel.stop()

        verify(mockScreenStream).stop()
    }

    @Test
    fun not_called_resume_screen_stream_when_nost_start_controller() = runBlocking<Unit> {
        val mockScreenStream = setupMockScreenStream()

        screenChannel.resume()

        verify(mockScreenStream, never()).resume()
    }

    @Test
    fun called_resume_screen_stream_when_start_controller() = runBlocking<Unit> {
        val mockScreenStream = setupMockScreenStream()

        screenChannel.start()
        screenChannel.resume()

        verify(mockScreenStream).resume()
    }

    @Test
    fun not_called_pause_screen_stream_when_nost_start_controller() = runBlocking<Unit> {
        val mockScreenStream = setupMockScreenStream()

        screenChannel.pause()

        verify(mockScreenStream, never()).pause()
    }

    @Test
    fun called_pause_screen_stream_when_start_controller() = runBlocking<Unit> {
        val mockScreenStream = setupMockScreenStream()

        screenChannel.start()
        screenChannel.pause()

        verify(mockScreenStream).pause()
    }

    @Test
    fun not_called_restart_screen_stream_when_nost_start_controller() = runBlocking<Unit> {
        val mockScreenStream = setupMockScreenStream()

        screenChannel.resume()

        verify(mockScreenStream, never()).startStreamReload()
    }

    @Test
    fun called_reload_screen_stream_when_start_controller() = runBlocking<Unit> {
        val mockScreenStream = setupMockScreenStream()

        screenChannel.start()
        screenChannel.restart()

        verify(mockScreenStream).startStreamReload()
    }

    @Test
    fun not_called_fps_screen_stream_when_nost_start_controller() = runBlocking<Unit> {
        val mockScreenStream = setupMockScreenStream()

        screenChannel.onChanged(10)

        verify(mockScreenStream, never()).startStreamReload()
    }

    @Test
    fun called_fps_screen_stream_when_start_controller() = runBlocking<Unit> {
        val mockScreenStream = setupMockScreenStream()

        screenChannel.start()
        screenChannel.onChanged(10)

        verify(mockScreenStream).setFps(eq(10))
    }

    @Test
    fun not_called_changeRotation_screen_stream_when_nost_start_controller() = runBlocking<Unit> {
        val mockScreenStream = setupMockScreenStream()

        screenChannel.onConfigChanged(Configuration())

        verify(mockScreenStream, never()).changeRotation(any(), any(), any())
    }

    @Test
    fun called_changeRotation_screen_stream_when_start_controller() = runBlocking<Unit> {
        val mockScreenStream = setupMockScreenStream()

        screenChannel.start()
        screenChannel.onConfigChanged(Configuration())

        verify(mockScreenStream).changeRotation(any(), any(), any())
    }


    @Test
    fun send_stream_packet() = runBlocking<Unit> {
        screenChannel.setCodecDataHandler(RC45CodecAdapter(screenChannel))
        setupScreenChannel(ConnectedSocketCompatStub(packetWriteListener))
        val mockScreenStream = ScreenStreamStub()
        `when`(screenStreamFactory.create()).thenReturn(mockScreenStream)

        screenChannel.start()

        verify(packetWriteListener).onReceive(eq(ChannelConstants.rcpX264Stream), eq(ChannelConstants.rcpX264StreamData), eq("format".toByteArray()), eq(6))
    }

    @Test
    fun called_close_screen_stream_when_start_controller() = runBlocking<Unit> {
        val mockScreenStream = setupMockScreenStream()

        screenChannel.start()
        screenChannel.release()

        verify(mockScreenStream).close()
    }


    private suspend fun setupMockScreenStream(): ScreenStream {
        setupScreenChannel(ConnectedSocketCompatStub(packetWriteListener))
        val mockScreenStream = mock(ScreenStream::class.java)
        `when`(screenStreamFactory.create()).thenReturn(mockScreenStream)
        return mockScreenStream
    }

    private suspend fun setupScreenChannel(socketCompat: SocketCompat) {
        `when`(socketCompatFactory.create(any())).thenReturn(socketCompat)
        screenChannel.startThread()
        delay(200)
    }

    private fun createSendBuffer(dataBuffer: ByteArray): ByteArray {
        val headerSize = 10
        val sendBuffer = ByteArray(dataBuffer.count() + headerSize)
        dataBuffer.copyInto(sendBuffer, headerSize)
        return sendBuffer
    }
}

class ScreenStreamStub : ScreenStream {
    private var encoderListener: ICodecListener? = null

    override fun start(): Int {
        println("encoderListener.$encoderListener")
        encoderListener?.onVDeqeueFormatChanged("format".toByteArray())
        encoderListener?.onVDequeueOutput("format".toByteArray(), 0, 6)
        return 0
    }

    override fun setFps(fps: Int) {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun changeRotation(context: Context?, savedRotation: Int, rotation: Int) {
        TODO("Not yet implemented")
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun startStreamReload() {
        TODO("Not yet implemented")
    }

    override fun resume(): Int {
        TODO("Not yet implemented")
    }

    override fun setEncoderListener(encoderListener: ICodecListener?) {
        this.encoderListener = encoderListener
    }

    override fun close() {
        TODO("Not yet implemented")
    }

}

class WriteFailConnectedSocketCompatStub : ConnectedSocketCompatStub() {
    override fun write(src: ByteArray, offset: Int, length: Int): Boolean {
        return false;
    }
}

interface OnPacketWriteListener {
    fun onReceive(payload: Int, msgID: Int, data: ByteArray?, dataSize: Int)
}

open class ConnectedSocketCompatStub(private val packetWriteListener: OnPacketWriteListener? = null) : SocketCompat, DataStream {
    override fun connect(): Boolean {
        return true
    }

    override fun disconnect() {
    }

    override fun enableEncrypt() {
    }

    override fun getDataStream(): DataStream {
        return this
    }

    override fun isConnected(): Boolean {
        return true
    }

    override fun close() {
    }

    override fun read(dst: ByteArray, offset: Int, length: Int): Int {
        return length
    }

    override fun write(src: ByteArray, offset: Int, length: Int): Boolean {
        val headerPacket = HeaderPacket()
        headerPacket.save(src, 0)
        val msgPacket = MsgPacket()
        msgPacket.save(src, MessageID.sz_rcpPacket, 0, headerPacket.msgsize)

        packetWriteListener?.onReceive(headerPacket.payloadtype, msgPacket.msgID, msgPacket.data, msgPacket.dataSize)
        return true
    }

}
