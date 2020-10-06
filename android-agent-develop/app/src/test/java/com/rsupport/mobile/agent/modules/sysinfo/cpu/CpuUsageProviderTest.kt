package com.rsupport.mobile.agent.modules.sysinfo.cpu

import base.BaseTest
import com.rsupport.mobile.agent.modules.engine.EngineType
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import com.rsupport.mobile.agent.modules.sysinfo.cpu.kitkat.CpuUsageKitkatFactory
import com.rsupport.mobile.agent.modules.sysinfo.cpu.knox.CpuUsageKnoxFactory
import com.rsupport.mobile.agent.modules.sysinfo.cpu.rsperm.CpuUsageRspermFactory
import com.rsupport.mobile.agent.service.RSPermService
import com.rsupport.mobile.agent.utils.SdkVersion
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.Mockito

class CpuUsageProviderTest : BaseTest() {

    @Mock
    private lateinit var sdkVersion: SdkVersion

    @Mock
    private lateinit var engineTypeCheck: EngineTypeCheck

    @Mock
    private lateinit var rspermService: RSPermService

    override fun createModules(): List<Module> {
        return listOf(
                module {
                    single { sdkVersion }
                    single { engineTypeCheck }
                    single { rspermService }
                }
        )
    }

    // kitkat 이하 일대 해당 class 가 로딩 되는지 확인하다.
    @Test
    fun kitkatCpuUsageTest() {
        Mockito.`when`(sdkVersion.lessThanOrEqual19()).thenReturn(true)
        val cpuUsageFactory = CpuUsageProvider().create()
        MatcherAssert.assertThat("Kitkat 용 factory 가 생성되지 않아서 실패", cpuUsageFactory, Matchers.instanceOf(CpuUsageKitkatFactory::class.java))
    }

    // EngineType 이 Knox 일때 해당 class 가 로딩되는지 확인한다.
    @Test
    fun knoxCpuUsageTest() {
        Mockito.`when`(sdkVersion.lessThanOrEqual19()).thenReturn(false)
        Mockito.`when`(engineTypeCheck.getEngineType()).thenReturn(EngineType.ENGINE_TYPE_KNOX)
        val cpuUsageFactory = CpuUsageProvider().create()
        MatcherAssert.assertThat("knox 용 factory 가 생성되지 않아서 실패", cpuUsageFactory, Matchers.instanceOf(CpuUsageKnoxFactory::class.java))
    }

    // EngineType 이 Rsperm 일때 해당 class 가 로딩되는지 확인한다.
    @Test
    fun rspermCpuUsageTest() {
        Mockito.`when`(sdkVersion.lessThanOrEqual19()).thenReturn(false)
        Mockito.`when`(engineTypeCheck.getEngineType()).thenReturn(EngineType.ENGINE_TYPE_RSPERM)
        val cpuUsageFactory = CpuUsageProvider().create()
        MatcherAssert.assertThat("rsperm 용 factory 가 생성되지 않아서 실패", cpuUsageFactory, Matchers.instanceOf(CpuUsageRspermFactory::class.java))
    }

}