package com.rsupport.mobile.agent.modules.sysinfo

import android.content.Context
import com.nhaarman.mockitokotlin2.whenever
import com.rsupport.mobile.agent.modules.net.channel.DataChannelImpl
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppFactory
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppInfo
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningApplication
import com.rsupport.mobile.agent.modules.sysinfo.phone.MemInfoStreamFactoryStub
import com.rsupport.mobile.agent.modules.sysinfo.phone.PhoneMemory
import com.rsupport.mobile.agent.modules.sysinfo.phone.givenActivityMemoryInfo
import junit.framework.Assert.assertFalse
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.StringContains.containsString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProcessInfoTest {
    private lateinit var context: Context
    private val mockDataChannelImpl = mock(DataChannelImpl::class.java)
    private val mockRunningAppFactory = mock(RunningAppFactory::class.java)
    private lateinit var processInfo: ProcessInfo

    @Before
    fun setup() {
        whenever(mockRunningAppFactory.create()).thenReturn(RunningApplicationStub())
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun memory_info_color_0() {
        context = givenActivityMemoryInfo(1024 * 1024 * 40, 1024 * 1024 * 60)
        processInfo = ProcessInfo(context, mockDataChannelImpl, mockRunningAppFactory, PhoneMemory(context, MemInfoStreamFactoryStub()))
        val memoryInfo = processInfo.memoryInfo
        assertThat(memoryInfo, containsString("0&/3654 MB"))
        assertThat(memoryInfo, containsString("1&/3614 MB"))
        assertThat(memoryInfo, containsString("2&/40 MB&/0"))
    }

    @Test
    fun memory_info_color_1() {
        context = givenActivityMemoryInfo(1024 * 1024 * 1024, 1024 * 1024 * 60)
        processInfo = ProcessInfo(context, mockDataChannelImpl, mockRunningAppFactory, PhoneMemory(context, MemInfoStreamFactoryStub()))
        val memoryInfo = processInfo.memoryInfo
        assertThat(memoryInfo, containsString("0&/3654 MB"))
        assertThat(memoryInfo, containsString("1&/2630 MB"))
        assertThat(memoryInfo, containsString("2&/1024 MB&/1"))
    }


    @Test
    fun check_memory_percent_when_get_chartinfo() {
        val total = 3741928 * 1024L
        val free = (total) - 1024 * 1024 * 1024L

        val percent = (free.toDouble() / total.toDouble()) * 100

        context = givenActivityMemoryInfo(1024 * 1024 * 1024, 1024 * 1024 * 60)
        processInfo = ProcessInfo(context, mockDataChannelImpl, mockRunningAppFactory, PhoneMemory(context, MemInfoStreamFactoryStub()))
        val chartInfo = processInfo.chartInfo

        assertThat(chartInfo, containsString(percent.toInt().toString().plus("&&")))

    }

    @Test
    fun get_process_items() {
        context = givenActivityMemoryInfo(1024 * 1024 * 1024, 1024 * 1024 * 60)
        processInfo = ProcessInfo(context, mockDataChannelImpl, mockRunningAppFactory, PhoneMemory(context, MemInfoStreamFactoryStub()))

        val processInfoItems = processInfo.loadProcessItems()

        processInfo.close()
        assertThat(processInfoItems.size, `is`(2))
    }


    @Test
    fun kill_process() {
        context = givenActivityMemoryInfo(1024 * 1024 * 1024, 1024 * 1024 * 60)
        processInfo = ProcessInfo(context, mockDataChannelImpl, mockRunningAppFactory, PhoneMemory(context, MemInfoStreamFactoryStub()))

        processInfo.loadProcessItems()

        processInfo.killProcess("com.test.pkg1")

        whenever(mockRunningAppFactory.create()).thenReturn(RunningApplicationKilledStub())
        processInfo.loadProcessItems()

        val isAlive = processInfo.checkProcessLive("com.test.pkg1")
        assertFalse(isAlive)
    }


}

class RunningApplicationKilledStub : RunningApplication {
    override fun getRunningAppInfos(): List<RunningAppInfo> {
        return listOf(
                RunningAppInfo("com.android.phone")
        )
    }
}

class RunningApplicationStub : RunningApplication {
    override fun getRunningAppInfos(): List<RunningAppInfo> {
        return listOf(
                RunningAppInfo("com.test.pkg1"),
                RunningAppInfo("com.android.phone")
        )
    }
}