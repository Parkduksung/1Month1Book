package com.rsupport.mobile.agent.modules.sysinfo.process

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rsupport.mobile.agent.TestApplication
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppInfo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.StringContains.containsString
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(
        application = TestApplication::class
)
@RunWith(RobolectricTestRunner::class)
class ProcessItemTest {

    @Test
    fun process_info() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val processItem = ProcessItem(context, 1024 * 1024 * 100, true, RunningAppInfo("test.pkg")).apply {
            setCpuPercent(10)
            setUsageMemory(1000 * 1000 * 15)
        }

        val processInfoString = processItem.processInfoString
        assertThat(processInfoString, containsString("test.pkg&/"))
        assertThat(processInfoString, containsString("10 %&/"))
        assertThat(processInfoString, containsString("15.00 MB&/"))
        assertThat(processInfoString, containsString("0&/"))
        assertThat(processInfoString, containsString("1"))
    }
}