package com.rsupport.mobile.agent.modules.net.model

import com.rsupport.mobile.agent.utils.Converter
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class HeaderPacketTest {

    // index 0일때 payload 를 save 했을때 정상적으로 저장되는지 확인한다.
    @Test
    fun payloadIndex0SaveTest() {
        val index = 0
        val payload: Byte = 1
        val msgSize = 256
        val headerPacket = HeaderPacket()
        headerPacket.save(createByteArray(index, payload, msgSize), index)

        MatcherAssert.assertThat("payload 가 달라서 실패", headerPacket.payloadtype, Matchers.`is`(payload.toInt()))
        MatcherAssert.assertThat("msgSize 가 달라서 실패", headerPacket.msgsize, Matchers.`is`(msgSize))
    }

    // index 0일때 payload가 음수일때 uInt 로 출력되는지 확인한다.
    @Test
    fun singedPayloadIndex0SaveTest() {
        val index = 0
        val payload: Byte = -17
        val msgSize = 256
        val headerPacket = HeaderPacket()
        headerPacket.save(createByteArray(index, payload, msgSize), index)

        MatcherAssert.assertThat("payload 가 달라서 실패", headerPacket.payloadtype, Matchers.`is`(payload.toInt() and 0xFF))
        MatcherAssert.assertThat("msgSize 가 달라서 실패", headerPacket.msgsize, Matchers.`is`(msgSize))
    }


    // index 10일때 payload 를 save 했을때 정상적으로 저장되는지 확인한다.
    @Test
    fun payloadIndex10SaveTest() {
        val index = 10
        val payload: Byte = 1
        val msgSize = 256

        val headerPacket = HeaderPacket()
        headerPacket.save(createByteArray(index, payload, msgSize), index)

        MatcherAssert.assertThat("payload 가 달라서 실패", headerPacket.payloadtype, Matchers.`is`(payload.toInt()))
        MatcherAssert.assertThat("msgSize 가 달라서 실패", headerPacket.msgsize, Matchers.`is`(msgSize))
    }


    // index 0일때 payload가 음수일때 uInt 로 출력되는지 확인한다.
    @Test
    fun singedPayloadIndex10SaveTest() {
        val index = 10
        val payload: Byte = -17
        val msgSize = 256
        val headerPacket = HeaderPacket()
        headerPacket.save(createByteArray(index, payload, msgSize), index)

        MatcherAssert.assertThat("payload 가 달라서 실패", headerPacket.payloadtype, Matchers.`is`(payload.toInt() and 0xFF))
        MatcherAssert.assertThat("msgSize 가 달라서 실패", headerPacket.msgsize, Matchers.`is`(msgSize))
    }

    // payload 양수를 테스트한다.
    @Test
    fun uIntPayloadPushTest() {
        val headerPacket = HeaderPacket().apply {
            payloadtype = 10
            msgsize = 239
        }

        val byteArray = ByteArray(5)
        headerPacket.push(byteArray, 0)
        MatcherAssert.assertThat("payload 가 달라서 실패", byteArray[0], Matchers.`is`(10.toByte()))
        MatcherAssert.assertThat("msg 가 달라서 실패", Converter.readIntLittleEndian(byteArray, 1), Matchers.`is`(239))
    }

    @Test
    fun intPayloadPushTest() {
        val headerPacket = HeaderPacket().apply {
            payloadtype = 239
            msgsize = 239
        }

        val byteArray = ByteArray(5)
        headerPacket.push(byteArray, 0)
        MatcherAssert.assertThat("payload 가 달라서 실패", byteArray[0], Matchers.`is`(239.toByte()))
        MatcherAssert.assertThat("msg 가 달라서 실패", Converter.readIntLittleEndian(byteArray, 1), Matchers.`is`(239))
    }

    @Test
    fun clearTest() {
        val headerPacket = HeaderPacket().apply {
            payloadtype = 239
            msgsize = 239
        }

        headerPacket.clear()

        MatcherAssert.assertThat("payload 가 0 이 아니라 실패", headerPacket.payloadtype, Matchers.`is`(0))
        MatcherAssert.assertThat("msgSize 가 0 이 아니라 실패", headerPacket.msgsize, Matchers.`is`(0))
    }


    private fun createByteArray(index: Int, payload: Byte, msgSize: Int): ByteArray {
        val byteBuffer = ByteBuffer.allocate(15)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.position(index)
        byteBuffer.put(payload)
        byteBuffer.putInt(msgSize)
        return byteBuffer.array()
    }
}