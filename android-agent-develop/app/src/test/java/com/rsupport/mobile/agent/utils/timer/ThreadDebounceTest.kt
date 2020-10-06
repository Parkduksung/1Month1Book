package com.rsupport.mobile.agent.utils.timer

import com.nhaarman.mockitokotlin2.timeout
import kotlinx.coroutines.*
import org.junit.Test
import org.mockito.Mockito
import java.lang.Runnable

class ThreadDebounceTest {


    // 1 초동안 10번이 호추되는지 확인한다.
    @Test
    fun debounceTest() = runBlocking<Unit> {
        val runnable = Mockito.mock(Runnable::class.java)

        val timerDebounce = ThreadDebounceTimer()
        timerDebounce.schedule(100, runnable)

        Mockito.verify(runnable, timeout(1000).atLeast(9)).run()
    }

    // 실중중 취소가 되는지 확인한다.
    @Test
    fun cancelWhenRunningTest() = runBlocking {

        val runnable = Mockito.mock(Runnable::class.java)

        val timerDebounce = ThreadDebounceTimer()
        timerDebounce.schedule(100, runnable)

        launch(Dispatchers.IO) {
            delay(550)
            timerDebounce.cancel()
        }

        Mockito.verify(runnable, timeout(1050).times(5)).run()
    }
}


