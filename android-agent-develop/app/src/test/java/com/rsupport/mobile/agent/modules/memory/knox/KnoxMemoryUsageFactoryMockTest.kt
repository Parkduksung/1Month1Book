package com.rsupport.mobile.agent.modules.memory.knox

import android.content.Context
import com.rsupport.knox.KnoxManagerCompat
import com.rsupport.mobile.agent.modules.memory.KeyObject
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppFactory
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppInfo
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningApplication
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class KnoxMemoryUsageFactoryMockTest {

    @Mock
    private lateinit var mockKnoxManagerCompat: KnoxManagerCompat

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var runningAppFactory: RunningAppFactory

    @Mock
    private lateinit var runningApplication: RunningApplication

    // 해당 package 에 해당하는 메모리 크기를 가져오는지 확인한다.
    @Test
    fun findMemoryUsageByFirstTest() {
        Mockito.`when`(runningAppFactory.create()).thenReturn(runningApplication)
        Mockito.`when`(runningApplication.getRunningAppInfos()).thenReturn(listOf(
                RunningAppInfo("system"),
                RunningAppInfo("system1"),
                RunningAppInfo("system2")
        ))

        Mockito.`when`(mockKnoxManagerCompat.getMemoryUsage(context, "system")).thenReturn(1000)
        Mockito.`when`(mockKnoxManagerCompat.getMemoryUsage(context, "system1")).thenReturn(2000)
        Mockito.`when`(mockKnoxManagerCompat.getMemoryUsage(context, "system2")).thenReturn(3000)


        val knoxMemoryUsageFactory = KnoxMemoryUsageFactory(context, runningAppFactory, mockKnoxManagerCompat)

        val memoryUsageContainer = knoxMemoryUsageFactory.get()

        val memoryUsage = memoryUsageContainer.find(KeyObject("system"))

        MatcherAssert.assertThat("memory 사용량을 찾지 못해서 실패", memoryUsage?.usageByte, Matchers.`is`(1000L))
    }

    // 같은 패키지명이 있을때 마지막의 크기를 가져오는지 확인한다.
    @Test
    fun findMemoryUsageByDuplicateLastTest() {
        Mockito.`when`(runningAppFactory.create()).thenReturn(runningApplication)
        Mockito.`when`(runningApplication.getRunningAppInfos()).thenReturn(listOf(
                RunningAppInfo("system"),
                RunningAppInfo("system1"),
                RunningAppInfo("system2"),
                RunningAppInfo("system")
        ))

        Mockito.`when`(mockKnoxManagerCompat.getMemoryUsage(context, "system")).thenReturn(1000)
        Mockito.`when`(mockKnoxManagerCompat.getMemoryUsage(context, "system")).thenReturn(4000)
        Mockito.`when`(mockKnoxManagerCompat.getMemoryUsage(context, "system1")).thenReturn(2000)
        Mockito.`when`(mockKnoxManagerCompat.getMemoryUsage(context, "system2")).thenReturn(3000)


        val knoxMemoryUsageFactory = KnoxMemoryUsageFactory(context, runningAppFactory, mockKnoxManagerCompat)

        val memoryUsageContainer = knoxMemoryUsageFactory.get()

        val memoryUsage = memoryUsageContainer.find(KeyObject("system"))

        MatcherAssert.assertThat("memory 사용량을 찾지 못해서 실패", memoryUsage?.usageByte, Matchers.`is`(4000L))
    }
}