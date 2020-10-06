package com.rsupport.mobile.agent.modules.memory.knox

import android.content.Context
import com.rsupport.knox.KnoxManagerCompat
import com.rsupport.mobile.agent.extension.guard
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import com.rsupport.mobile.agent.modules.memory.KeyObject
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppFactory
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Ignore
import org.junit.Test
import org.koin.java.KoinJavaComponent.inject
import utils.checkSamsungDevice


class KnoxMemoryUsageFactoryTest {

    private val context by inject(Context::class.java)
    private val knoxManagerCompat by inject(KnoxManagerCompat::class.java)
    private val runningAppFactory by inject(RunningAppFactory::class.java)
    private val engineTypeCheck by inject(EngineTypeCheck::class.java)

    @Test
    fun memoryUsageTest() = runBlocking<Unit> {
        checkSamsungDevice().guard { return@runBlocking }
        engineTypeCheck.checkEngineType()
        val factory = KnoxMemoryUsageFactory(context, runningAppFactory, knoxManagerCompat)
        val container = factory.get()
        val memoryUsage = container.find(KeyObject("com.rsupport.mobile.agent"))
        MatcherAssert.assertThat("메모리 사용량을 가져오지 못해서 실패", memoryUsage, Matchers.notNullValue())
    }
}