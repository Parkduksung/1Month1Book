package com.rsupport.mobile.agent.modules.sysinfo.phone

import android.app.ActivityManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.io.InputStream

@RunWith(RobolectricTestRunner::class)
class PhoneMemoryTest {

    @Test
    fun memory_info() {
        val context = givenActivityMemoryInfo(1024 * 3, 1024 * 4)

        val phoneMemory = PhoneMemory(context, MemInfoStreamFactoryStub())
        val memoryInfo = phoneMemory.getMemoryInfo()

        assertThat(memoryInfo.total, `is`(3741928 * 1024L))
        assertThat(memoryInfo.buffers, `is`(122780 * 1024L))
        assertThat(memoryInfo.cached, `is`(995880 * 1024L))
        assertThat(memoryInfo.available, `is`(3 * 1024L))
        assertThat(memoryInfo.threshold, `is`(4 * 1024L))
    }


}

fun givenActivityMemoryInfo(availableMem: Long, threadhold: Long): Context {
    val context = spy(ApplicationProvider.getApplicationContext<Context>())
    val activityManager = mock(ActivityManager::class.java)

    whenever(activityManager.getMemoryInfo(any())).doAnswer {
        val memoryInfo = it.arguments[0] as ActivityManager.MemoryInfo
        memoryInfo.availMem = availableMem
        memoryInfo.threshold = threadhold
        Unit
    }

    whenever(context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).thenReturn(activityManager)
    return context
}


class MemInfoStreamFactoryStub : PhoneMemory.MemInfoStreamFactory {
    override fun create(): InputStream {
        return ByteArrayInputStream(memInfo.toByteArray())
    }

    private val memInfo = """
                        MemTotal:        3741928 kB
                        MemFree:          154144 kB
                        MemAvailable:    1069048 kB
                        Buffers:          122780 kB
                        Cached:           995880 kB
                        SwapCached:         9528 kB
                        Active:          1643288 kB
                        Inactive:         554632 kB
                        Active(anon):     804504 kB
                        Inactive(anon):   279568 kB
                        Active(file):     838784 kB
                        Inactive(file):   275064 kB
                        Unevictable:        3112 kB
                        Mlocked:            3112 kB
                        SwapTotal:       1852528 kB
                        SwapFree:        1288104 kB
                        Dirty:                 0 kB
                        Writeback:             0 kB
                        AnonPages:       1074720 kB
                        Mapped:           658420 kB
                        Shmem:              2396 kB
                        Slab:             305408 kB
                        SReclaimable:      79504 kB
                        SUnreclaim:       225904 kB
                        KernelStack:       54576 kB
                        PageTables:       101704 kB
                        NFS_Unstable:          0 kB
                        Bounce:                0 kB
                        WritebackTmp:          0 kB
                        CommitLimit:     3723492 kB
                        Committed_AS:   121141360 kB
                        VmallocTotal:   263061440 kB
                        VmallocUsed:           0 kB
                        VmallocChunk:          0 kB
                        CmaTotal:         299008 kB
                        CmaFree:           15012 kB
                        """.trimIndent()
}