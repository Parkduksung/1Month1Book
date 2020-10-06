package com.rsupport.mobile.agent.modules.sysinfo.cpu.knox

import com.rsupport.mobile.agent.extension.guard
import com.rsupport.mobile.agent.modules.engine.EngineType
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.koin.java.KoinJavaComponent.inject

class CpuUsageKnoxFactoryTest {

    private val engineTypeCheck by inject(EngineTypeCheck::class.java)

    @Before
    fun setup() = runBlocking {
        engineTypeCheck.checkEngineType()
    }

    // Knox 사용중일때 cpu 사용율을 가져오는지 확인한다.
    @Test
    fun knoxCpuUsageTest() = runBlocking<Unit> {
        isKnoxEngine().guard {
            return@runBlocking
        }
        val cpuUsageKnoxFactory = CpuUsageKnoxFactory()
        val container = cpuUsageKnoxFactory.get()
        MatcherAssert.assertThat("cpu 사용율을 조회하지 못해서 실패", container.empty, Matchers.`is`(false))
    }

    private fun isKnoxEngine(): Boolean {
        return (engineTypeCheck.getEngineType() == EngineType.ENGINE_TYPE_KNOX)
    }
}