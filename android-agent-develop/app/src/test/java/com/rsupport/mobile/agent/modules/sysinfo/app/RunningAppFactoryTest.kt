package com.rsupport.mobile.agent.modules.sysinfo.app

import base.BaseTest
import base.mock
import com.rsupport.mobile.agent.modules.engine.EngineType
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import com.rsupport.mobile.agent.service.RSPermService
import com.rsupport.mobile.agent.utils.SdkVersion
import com.rsupport.rsperm.IRSPerm
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.Mockito

class RunningAppFactoryTest : BaseTest() {
    @Mock
    private lateinit var mockEngineTypeChecker: EngineTypeCheck

    @Mock
    private lateinit var mockSdkVersion: SdkVersion

    @Mock
    private lateinit var mockRspermService: RSPermService

    override fun createModules(): List<Module> {
        return listOf(
                module {
                    single { mockSdkVersion }
                    single { mockEngineTypeChecker }
                    single { mockRspermService }
                }
        )
    }

    // SDK Version 이 Kitkat 이하일때 생성된 객체를 확인한다.
    @Test
    fun runningApplicationCreateWhenKitkat() {
        Mockito.`when`(mockSdkVersion.lessThanOrEqual19()).thenReturn(true)
        val runningAppFactory = RunningAppFactory(mockSdkVersion, mockEngineTypeChecker, mockRspermService)
        val runningApplication = runningAppFactory.create()
        MatcherAssert.assertThat("SDK 가 Kitkat 이하인데 KitKat 용이 아니어서 실패", runningApplication, Matchers.instanceOf(RunningApplicationKitkat::class.java))
    }

    // SDK Version 이 롤리팝 이상이고 Knox 일때 생성된 객체를 확인한다.
    @Test
    fun runningApplicationCreateWhenKnox() {
        Mockito.`when`(mockSdkVersion.lessThanOrEqual19()).thenReturn(false)
        Mockito.`when`(mockEngineTypeChecker.getEngineType()).thenReturn(EngineType.ENGINE_TYPE_KNOX)

        val runningAppFactory = RunningAppFactory(mockSdkVersion, mockEngineTypeChecker, mockRspermService)
        val runningApplication = runningAppFactory.create()
        MatcherAssert.assertThat("SDK 가 롤리팝 이상이고 Knox 용이 아니어서 실패", runningApplication, Matchers.instanceOf(RunningApplicationKnox::class.java))
    }

    // SDK Version 이 롤리팝 이상이고 Rsperm 이고 binding 되어있을때 생성된 객체를 확인한다.
    @Test
    fun runningApplicationCreateWhenRsperm() {

        val mockRsperm = mock<IRSPerm>()
        Mockito.`when`(mockSdkVersion.lessThanOrEqual19()).thenReturn(false)
        Mockito.`when`(mockEngineTypeChecker.getEngineType()).thenReturn(EngineType.ENGINE_TYPE_RSPERM)
        Mockito.`when`(mockRspermService.isBind()).thenReturn(true)
        Mockito.`when`(mockRspermService.getRsperm()).thenReturn(mockRsperm)

        val runningAppFactory = RunningAppFactory(mockSdkVersion, mockEngineTypeChecker, mockRspermService)
        val runningApplication = runningAppFactory.create()
        MatcherAssert.assertThat("SDK 가 롤리팝 이상이고 Rsperm 용이 아니어서 실패", runningApplication, Matchers.instanceOf(RunningApplicationRsperm::class.java))
    }

    // SDK Version 이 롤리팝 이상이고 Rsperm EngineType 인데 binding 이 안되어 있을때 객체를 확인한다.
    @Test
    fun runningApplicationCreateWhenRspermNotBind() {
        val mockRsperm = mock<IRSPerm>()
        Mockito.`when`(mockSdkVersion.lessThanOrEqual19()).thenReturn(false)
        Mockito.`when`(mockEngineTypeChecker.getEngineType()).thenReturn(EngineType.ENGINE_TYPE_RSPERM)
        Mockito.`when`(mockRspermService.isBind()).thenReturn(false)

        val runningAppFactory = RunningAppFactory(mockSdkVersion, mockEngineTypeChecker, mockRspermService)
        val runningApplication = runningAppFactory.create()
        MatcherAssert.assertThat("SDK 가 롤리팝 이상이고 Rsperm 인데 binding 안되어있을때 KitKat 용이 호출되지 않아서 실패", runningApplication, Matchers.instanceOf(RunningApplicationKitkat::class.java))
    }

    // SDK Version 이 롤리팝 이상이고 Rsperm EngineType 인데 Rsperm을 가져오지 못했을때 객체를 확인한다.
    @Test
    fun runningApplicationCreateWhenRspermIsNull() {
        Mockito.`when`(mockSdkVersion.lessThanOrEqual19()).thenReturn(false)
        Mockito.`when`(mockEngineTypeChecker.getEngineType()).thenReturn(EngineType.ENGINE_TYPE_RSPERM)
        Mockito.`when`(mockRspermService.isBind()).thenReturn(true)
        Mockito.`when`(mockRspermService.getRsperm()).thenReturn(null)

        val runningAppFactory = RunningAppFactory(mockSdkVersion, mockEngineTypeChecker, mockRspermService)
        val runningApplication = runningAppFactory.create()
        MatcherAssert.assertThat("SDK 가 롤리팝 이상이고 Rsperm 인데 Rsperm 이 null 일때 Kitkat 이 호출되지 않아서 실패", runningApplication, Matchers.instanceOf(RunningApplicationKitkat::class.java))
    }

}