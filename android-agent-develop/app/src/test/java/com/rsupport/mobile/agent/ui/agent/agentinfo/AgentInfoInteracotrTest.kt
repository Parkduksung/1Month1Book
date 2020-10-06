package com.rsupport.mobile.agent.ui.agent.agentinfo

import android.content.Context
import android.os.PowerManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import base.mock
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import com.rsupport.mobile.agent.api.ApiService
import com.rsupport.mobile.agent.api.model.AgentInfo
import com.rsupport.mobile.agent.repo.agent.AgentRepository
import com.rsupport.mobile.agent.status.AgentStatus
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.SdkVersion
import com.rsupport.rscommon.exception.RSException
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class AgentInfoInteracotrTest {

    private val sdkVersion: SdkVersion = mock()
    private val agentRepository: AgentRepository = mock()
    private val powerManager: PowerManager = mock()
    private val agentStatus: AgentStatus = mock()
    private val observer: Observer<String> = mock()
    private val apiService: ApiService = mock()

    private val context = spy(ApplicationProvider.getApplicationContext<Context>())

    private lateinit var agentInfoInteractor: AgentInfoInteractor
    private val packageName = "com.package.name"

    @Before
    fun setup() {
        loadKoinModules(module(override = true) {
            single { sdkVersion }
            single { agentRepository }
            single { agentStatus }
            single { apiService }
            single { context }
        })
        agentInfoInteractor = AgentInfoInteractor().apply {
            initialized()
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    // 1. 베터리 최적화 무시를 해야한다.
    // - sdk 23 이상,
    @Test
    fun greaterThan23Test() {
        Mockito.`when`(context.getSystemService(Context.POWER_SERVICE)).thenReturn(powerManager)
        Mockito.`when`(sdkVersion.greaterThan23()).thenReturn(true)
        Mockito.`when`(powerManager.isIgnoringBatteryOptimizations(packageName)).thenReturn(false)
        Mockito.`when`(context.packageName).thenReturn(packageName)
        agentInfoInteractor.ignoreBatteryOptimized.observeForever(observer)

        Mockito.verify(observer).onChanged(packageName)
    }


    // 2. 베터리 최적화 무시를 하지 않아도된다.
    // - sdk 23 미만
    @Test
    fun greaterThan23FaileTest() {
        Mockito.`when`(sdkVersion.greaterThan23()).thenReturn(false)
        agentInfoInteractor.ignoreBatteryOptimized.observeForever(observer)
        Mockito.verify(observer).onChanged(null)
    }

    // 3. 베터리 최적화 무시를 하지 않아도된다.
    // - 이미 베터리최적화를 무시한경우
    @Test
    fun greaterThan23AndIgnoredTest() {
        Mockito.`when`(context.getSystemService(Context.POWER_SERVICE)).thenReturn(powerManager)
        Mockito.`when`(sdkVersion.greaterThan23()).thenReturn(true)
        Mockito.`when`(powerManager.isIgnoringBatteryOptimizations(packageName)).thenReturn(true)
        Mockito.`when`(context.packageName).thenReturn(packageName)
        agentInfoInteractor.ignoreBatteryOptimized.observeForever(observer)
        Mockito.verify(observer).onChanged(null)
    }


    // AgentInfo 가 변경되었을때 적용되는지를 확인한다.
    @Test
    fun agentInfoChangedTest() = runBlocking<Unit> {

        val liveData = MutableLiveData<AgentInfo>()
        val observer: Observer<AgentInfo> = mock()
        val emptyAgentInfo = AgentInfo()
        val agentInfo = AgentInfo().apply { guid = "1234" }

        Mockito.`when`(agentRepository.getAgentInfo()).thenReturn(liveData)
        agentInfoInteractor.agentInfo.observeForever(observer)
        liveData.value = emptyAgentInfo
        liveData.value = agentInfo

        Mockito.verify(observer).onChanged(emptyAgentInfo)
        Mockito.verify(observer).onChanged(agentInfo)
    }

    @Test
    fun `로그인 실패시 오류코드를 확인한다`() = runBlocking<Unit> {
        whenever(apiService.agentLogin(any())).thenReturn(Result.failure(RSException(999)))
        val errorCode = agentInfoInteractor.loginProcess()
        assertEquals(999, errorCode)
    }
}
