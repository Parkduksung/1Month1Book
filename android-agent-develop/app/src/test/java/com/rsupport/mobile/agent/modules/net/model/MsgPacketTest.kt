package com.rsupport.mobile.agent.modules.net.model

import base.BaseTest
import com.rsupport.mobile.agent.utils.Converter
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import org.koin.core.module.Module
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MsgPacketTest : BaseTest() {

    override fun createModules(): List<Module> {
        return emptyList()
    }

    // msgid 가 같은지를 확인한다.
    @Test
    fun msgidSameTest() {
        val msgID = 10

        val msgPacket = MsgPacket().apply {
            this.msgID = msgID
        }
        val byteArray = ByteArray(5)
        msgPacket.push(byteArray, 0)

        MatcherAssert.assertThat("메세지 아이디가 달라서 실패", byteArray[0], Matchers.`is`(msgID.toByte()))
    }

    // 125 이상의 msgid 가 같은지를 확인한다.
    @Test
    fun msgidUByteTest() {
        val msgID = 239

        val msgPacket = MsgPacket().apply {
            this.msgID = msgID
        }
        val byteArray = ByteArray(5)
        msgPacket.push(byteArray, 0)

        MatcherAssert.assertThat("메세지 아이디가 달라서 실패", byteArray[0], Matchers.`is`(msgID.toByte()))
    }

    // 데이터가 null 일때 데이터 사이즈가 0인것을 확인한다.
    @Test
    fun dataNullTest() {
        val msgID = 10

        val msgPacket = MsgPacket().apply {
            this.msgID = msgID
            this.data = null
        }
        val byteArray = ByteArray(5)
        msgPacket.push(byteArray, 0)

        MatcherAssert.assertThat("데이터가 null 인데 사이즈가 있어서 실패", Converter.readIntLittleEndian(byteArray, 1), Matchers.`is`(0))
    }


    // 데이터가 비어있을때 데이터사이즈가 0인것을 확인한다.
    @Test
    fun dataEmptyTest() {
        val msgID = 10

        val msgPacket = MsgPacket().apply {
            this.msgID = msgID
            this.data = ByteArray(0)
            this.dataSize = 0
        }
        val byteArray = ByteArray(5)
        msgPacket.push(byteArray, 0)

        MatcherAssert.assertThat("데이터가 empty 인데 사이즈가 있어서 실패", Converter.readIntLittleEndian(byteArray, 1), Matchers.`is`(0))
    }

    // 데이터가 비어있을때 데이터사이즈가 0인것을 확인한다.
    @Test
    fun dataSizeTest() {
        val msgID = 10

        val msgPacket = MsgPacket().apply {
            this.msgID = msgID
            this.data = ByteArray(250)
            this.dataSize = 250
        }
        val byteArray = ByteArray(1024)
        msgPacket.push(byteArray, 0)

        MatcherAssert.assertThat("데이터가 250 인데 사이즈가 달라서 실패", Converter.readIntLittleEndian(byteArray, 1), Matchers.`is`(250))
    }

    // 잘못된 사이즈를 입력했을때
    @Test
    fun wrongSizeTest() {
        val msgID = 10
        val msgPacket = MsgPacket().apply {
            this.msgID = msgID
            this.data = ByteArray(0)
        }
        val byteArray = ByteArray(1)
        msgPacket.push(byteArray, 0)
        MatcherAssert.assertThat("데이터가 empty 인데 사이즈가 있어서 실패", byteArray[0], Matchers.`is`(0.toByte()))
    }

    // 메세지 아이디를 확인한다.
    @Test
    fun saveMsgIdTest() {
        val msgPacket = MsgPacket()
        val msgId: Byte = 10

        val byteBuffer = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN).apply {
            put(msgId)
        }
        msgPacket.save(byteBuffer.array(), 0, 0, byteBuffer.capacity())
        MatcherAssert.assertThat("메세지 아이디가 달라서 실패", msgPacket.msgID, Matchers.`is`(msgId.toInt()))
    }


    // 125 이상의 메세지 아이디를 확인한다.
    @Test
    fun saveMsgIdSingedTest() {
        val msgPacket = MsgPacket()

        val msgId: Byte = 239.toByte()

        val byteBuffer = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN).apply {
            put(msgId)
        }
        msgPacket.save(byteBuffer.array(), 0, 0, byteBuffer.capacity())
        MatcherAssert.assertThat("메세지 아이디가 달라서 실패", msgPacket.msgID, Matchers.`is`(msgId.toInt() and 0xff))
    }


    // 메세지 사이즈를 확인한다.
    @Test
    fun saveDataTest() {
        val msgPacket = MsgPacket()
        val msgId: Byte = 10
        val msgSize = 250

        val byteBuffer = ByteBuffer.allocate(5 + msgSize).order(ByteOrder.LITTLE_ENDIAN).apply {
            put(msgId)
            putInt(msgSize)
            put(ByteArray(msgSize))
        }
        msgPacket.save(byteBuffer.array(), 0, 0, byteBuffer.capacity())
        MatcherAssert.assertThat("메세지 사이즈가 달라서 실패", msgPacket.dataSize, Matchers.`is`(msgSize))
    }

    @Test
    fun clearTest() {
        val msgPacket = MsgPacket()
        val msgId: Byte = 10
        val msgSize = 250

        val byteBuffer = ByteBuffer.allocate(5 + msgSize).order(ByteOrder.LITTLE_ENDIAN).apply {
            put(msgId)
            putInt(msgSize)
            put(ByteArray(msgSize))
        }
        msgPacket.save(byteBuffer.array(), 0, 0, byteBuffer.capacity())
        msgPacket.clear()


        MatcherAssert.assertThat("메세지 아이디가 달라서 실패", msgPacket.msgID, Matchers.`is`(0))
        MatcherAssert.assertThat("메세지 사이즈가 달라서 실패", msgPacket.dataSize, Matchers.`is`(0))
    }
}