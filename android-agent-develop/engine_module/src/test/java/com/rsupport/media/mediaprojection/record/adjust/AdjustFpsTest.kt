package com.rsupport.media.mediaprojection.record.adjust

import kotlinx.coroutines.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Test

class AdjustFpsTest {

    private val adjustFps = AdjustFps()
    private val testTime: Long = 1000

    @Test
    fun should_called_30fps() = runBlocking<Unit> {
        val fps = 30
        adjustFps.setFps(fps)
        assertThat(loopTest(testTime), rangeMatcher(fps))
    }

    @Test
    fun should_called_25fps() = runBlocking<Unit> {
        val fps = 25
        adjustFps.setFps(fps)
        assertThat(loopTest(testTime), rangeMatcher(fps))
    }

    @Test
    fun should_called_20fps() = runBlocking<Unit> {
        val fps = 20
        adjustFps.setFps(fps)
        assertThat(loopTest(testTime), rangeMatcher(fps))
    }

    @Test
    fun should_called_15fps() = runBlocking<Unit> {
        val fps = 15
        adjustFps.setFps(fps)
        assertThat(loopTest(testTime), rangeMatcher(fps))
    }

    @Test
    fun should_called_10fps() = runBlocking<Unit> {
        val fps = 10
        adjustFps.setFps(fps)
        assertThat(loopTest(testTime), rangeMatcher(fps))
    }

    @Test
    fun should_called_5fps() = runBlocking<Unit> {
        val fps = 5
        adjustFps.setFps(fps)
        assertThat(loopTest(testTime), rangeMatcher(fps))
    }

    @Test
    fun should_called_change_down_fps() = runBlocking<Unit> {
        assertFpsRange(30, 25)
        assertFpsRange(30, 20)
        assertFpsRange(30, 15)
        assertFpsRange(30, 10)
        assertFpsRange(30, 5)
    }

    @Test
    fun should_called_change_up_fps() = runBlocking<Unit> {
        assertFpsRange(10, 30)
        assertFpsRange(10, 25)
        assertFpsRange(10, 20)
        assertFpsRange(10, 15)
    }

    private fun assertFpsRange(fps: Int, changeFps: Int) {
        adjustFps.setFps(fps)

        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            adjustFps.setFps(changeFps)
        }
        val callCount = loopTest(2000)
        assertThat(callCount, rangeMatcher(fps + changeFps))
    }


    private fun rangeMatcher(fps: Int) = allOf(greaterThan(fps - 2), lessThan(fps + 3))

    private fun loopTest(testTime: Long): Int {
        val startTime = System.currentTimeMillis()
        var callCount = 0
        while (System.currentTimeMillis() - startTime < testTime) {
            if (adjustFps.isDelayed) {
                continue
            }
            callCount++
        }
        return callCount
    }
}