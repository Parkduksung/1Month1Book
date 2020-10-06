package com.rsupport.mobile.agent.modules.sysinfo.cpu

import com.rsupport.mobile.agent.extension.guard
import com.rsupport.mobile.agent.modules.engine.EngineType
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import com.rsupport.mobile.agent.modules.sysinfo.cpu.kitkat.CpuUsageKitkatFactory
import com.rsupport.mobile.agent.modules.sysinfo.cpu.knox.CpuUsageKnoxFactory
import com.rsupport.mobile.agent.modules.sysinfo.cpu.rsperm.CpuUsageRspermFactory
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.koin.java.KoinJavaComponent.inject
import utils.bindRperm
import utils.checkSamsungDevice
import utils.isKitKat

class CpuUsageTest {

    private val engineChecker by inject(EngineTypeCheck::class.java)

    @Before
    fun setup() {
        engineChecker.checkEngineType()
    }

    @Test
    fun cpuUsageRspermTest() = runBlocking<Unit> {
        bindRperm().guard {
            return@runBlocking
        }
        val cpuUsageFactory = CpuUsageProvider().create()
        MatcherAssert.assertThat("cpu 정보를 가져오지 못해서 실패", cpuUsageFactory.get().empty, Matchers.`is`(false))
    }

    @Test
    fun cpuUsageKnoxTest() = runBlocking<Unit> {
        checkSamsungDevice().guard {
            return@runBlocking
        }
        val cpuUsageFactory = CpuUsageProvider().create()
        MatcherAssert.assertThat("cpu 정보를 가져오지 못해서 실패", cpuUsageFactory.get().empty, Matchers.`is`(false))
    }

    @Test
    fun cpuUsageKitKatTest() = runBlocking<Unit> {
        isKitKat().guard {
            return@runBlocking
        }
        val cpuUsageFactory = CpuUsageProvider().create()
        MatcherAssert.assertThat("cpu 정보를 가져오지 못해서 실패", cpuUsageFactory.get().empty, Matchers.`is`(false))
    }
}


