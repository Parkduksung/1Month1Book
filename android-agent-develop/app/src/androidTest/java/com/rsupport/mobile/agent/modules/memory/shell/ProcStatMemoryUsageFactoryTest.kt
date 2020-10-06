package com.rsupport.mobile.agent.modules.memory.shell

import android.app.ActivityManager
import com.rsupport.mobile.agent.extension.guard
import com.rsupport.mobile.agent.utils.SdkVersion
import com.rsupport.util.log.RLog
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import org.koin.java.KoinJavaComponent.inject


class ProcStatMemoryUsageFactoryTest {

    private val activityManager by inject(ActivityManager::class.java)
    private val sdkVersion by inject(SdkVersion::class.java)

    @Test
    fun readMemoryFromProcStatTest() {
        sdkVersion.lessThanOrEqual19().guard {
            RLog.w("sdk 19초과 버전에서는 동작하지 않아서 테스트 하지 않는다.")
            return@guard
        }
        val procStatMemoryUsageFactory = ProcStatMemoryUsageFactory(activityManager)
        val memoryUsageContainer = procStatMemoryUsageFactory.get()
        MatcherAssert.assertThat("메모리 정모를 찾을 수 없어서 실패", memoryUsageContainer.empty, Matchers.`is`(false))
    }
}