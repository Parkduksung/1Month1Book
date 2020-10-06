package com.rsupport.mobile.agent.modules.device.inject

import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class KeyPadEventTest {

    // empty byte data 로부터 KeyPadEvent 를 생성했을때 유효하지 않은 데이터가 생성되는지를 확인한다.
    @Test
    fun emptyByteTest() = runBlocking {
        val emptyBytes = ByteArray(0)

        val keypadEvent = KeyPadEvent.from(emptyBytes)

        MatcherAssert.assertThat("입력이 없는데 InvalidData 가 아니아서 실패", keypadEvent, Matchers.instanceOf(KeyPadEvent.InvalidData::class.java))
    }


    // KeyEvent byteData 가 KeyEvent 를 추출할 수 없이 적은데이터가 전달 되었을때를 확인한다.
    @Test
    fun checkShortDataValidationTest() = runBlocking {
        val shortDataBytes = ByteArray(2).apply {
            // event count
            set(0, 1)
        }

        val keypadEvent = KeyPadEvent.from(shortDataBytes)

        MatcherAssert.assertThat("입력데이터가 적은데 InvalidData이 아니어서 실패", keypadEvent, Matchers.instanceOf(KeyPadEvent.InvalidData::class.java))
    }

    // KeyEvent byteData 가 KeyEvent 를 추출할 수 있는 데이터보다 많이 전달 되었을때를 확인한다.
    @Test
    fun checkBigDataValidationTest() = runBlocking {
        val bigDataBytes = ByteArray(10).apply {
            // event count
            set(0, 1)
        }

        val keypadEvent = KeyPadEvent.from(bigDataBytes)

        MatcherAssert.assertThat("충분한 데이터가 입력되었는데 Events 가 아니어서 실패", keypadEvent, Matchers.instanceOf(KeyPadEvent.Events::class.java))
    }

    // KeyEvent byteData 가 KeyEvent 를 추출할 수 있는 정확한 데이터가 전달되었을때를 확인한다.
    @Test
    fun checkDataValidationTest() = runBlocking {
        val correctDataBytes = ByteArray(5).apply {
            // event count
            set(0, 1)
        }

        val keypadEvent = KeyPadEvent.from(correctDataBytes)

        MatcherAssert.assertThat("정확한 데이터가 입력되었는데 Events 가 아니어서 실패", keypadEvent, Matchers.instanceOf(KeyPadEvent.Events::class.java))
    }

    // KeyEvent byteData 가 1개 일때를 확인한다.
    @Test
    fun checkKeyEventTest() = runBlocking {
        val eventCount = 1
        val oneKeyEventDataBytes = createEventDataBytes(eventCount)

        when (val keypadEvent = KeyPadEvent.from(oneKeyEventDataBytes)) {
            is KeyPadEvent.Events -> {
                MatcherAssert.assertThat("1 개의 event 가 입력되었는데 갯수가 달라서 실패", keypadEvent.events.size, Matchers.`is`(eventCount))
            }
            KeyPadEvent.InvalidData -> MatcherAssert.assertThat("정확한 데이터가 입력되었는데 Events 가 아니어서 실패", false)
        }
    }

    // KeyEvent byteData 가 10개 일때를 확인한다.
    @Test
    fun checkKeyEvent10Test() = runBlocking {
        val eventCount = 10
        val oneKeyEventDataBytes = createEventDataBytes(eventCount)

        when (val keypadEvent = KeyPadEvent.from(oneKeyEventDataBytes)) {
            is KeyPadEvent.Events -> {
                MatcherAssert.assertThat("1 개의 event 가 입력되었는데 갯수가 달라서 실패", keypadEvent.events.size, Matchers.`is`(eventCount))
            }
            KeyPadEvent.InvalidData -> MatcherAssert.assertThat("정확한 데이터가 입력되었는데 Events 가 아니어서 실패", false)
        }
    }

    private fun createEventDataBytes(eventCount: Int): ByteArray {
        val countBufferByte = 1
        val eventBufferSize = 4
        return ByteArray(countBufferByte + eventBufferSize * eventCount).apply {
            set(0, eventCount.toByte())
        }
    }
}