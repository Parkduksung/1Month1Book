package com.rsupport.mobile.agent.ui.agent.agentinfo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import base.ViewModelBaseTest
import com.rsupport.mobile.agent.constant.ComConstant
import com.rsupport.mobile.agent.utils.SdkVersion
import com.rsupport.mobile.agent.utils.NetworkUtils
import com.rsupport.mobile.agent.api.model.AgentInfo
import com.rsupport.mobile.agent.status.AgentStatus
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AgentInfoViewModelTest : ViewModelBaseTest() {
    private val LOGIN_FAIL = -1
    private val LOGIN_SUCCESS = 0

    @Mock
    lateinit var ignoreBatteryObserver: Observer<String>

    @Mock
    lateinit var booleanObserver: Observer<Boolean>

    @Mock
    lateinit var agentInfoInteractor: AgentInfoInteractor

    @Mock
    lateinit var sdkVersion: SdkVersion

    @Mock
    lateinit var networkUtils: NetworkUtils

    override fun createModules(): List<Module> {
        return listOf(
                module {
                    factory { agentInfoInteractor }
                },
                module {
                    single { sdkVersion }
                    single { networkUtils }
                }
        )
    }

    private val packageName = "com.package.name"

    // 1. 베터리 최적화 무시가 설정 안되어 있을때 확인한다.
    @Test
    fun ignoreBatteryOptimizedTest() = runBlocking<Unit> {
        Mockito.`when`(agentInfoInteractor.ignoreBatteryOptimized).thenReturn(MutableLiveData(packageName))
        val agentInfoViewModel = AgentInfoViewModel(application)

        agentInfoViewModel.batteryOptimized.observeForever(ignoreBatteryObserver)

        Mockito.verify(ignoreBatteryObserver).onChanged(packageName)
    }

    // 2. 1. 베터리 최적화 무시가 설정 되어 있을때 확인한다.
    @Test
    fun ignoreBatteryOptimizedFalseTest() = runBlocking<Unit> {

        Mockito.`when`(agentInfoInteractor.ignoreBatteryOptimized).thenReturn(MutableLiveData(null))

        val agentInfoViewModel = AgentInfoViewModel(application)
        agentInfoViewModel.batteryOptimized.observeForever(ignoreBatteryObserver)

        Mockito.verify(ignoreBatteryObserver).onChanged(null)
    }


    // 3. Agent 등록을 했을때 로그인 상태인 것을 확인한다.
    @Test
    fun agentLoggedInWhenRegisterAgentTest() = runBlocking {
        Mockito.`when`(agentInfoInteractor.agentInfo).thenReturn(MutableLiveData<AgentInfo>(AgentInfo().apply { status = AgentStatus.AGENT_STATUS_LOGIN.toInt() }))

        val agentInfoViewModel = AgentInfoViewModel(application)
        agentInfoViewModel.isLoggedIn.observeForever(booleanObserver)

        Mockito.verify(booleanObserver).onChanged(true)
    }


    // 3. Agent 등록하지 않았을때 로그아웃 상태인 것을 확인한다.
    @Test
    fun agentLoggedOutWhenNotRegisterAgentTest() = runBlocking {
        Mockito.`when`(agentInfoInteractor.agentInfo).thenReturn(MutableLiveData<AgentInfo>(AgentInfo().apply { status = AgentStatus.AGENT_STATUS_LOGOUT.toInt() }))

        val agentInfoViewModel = AgentInfoViewModel(application)
        agentInfoViewModel.isLoggedIn.observeForever(booleanObserver)

        Mockito.verify(booleanObserver).onChanged(false)
    }

    // 4. Agent 를 로그인 한다. (Guid가 없는 상태이다. Agent 를 등록하지 않은 상태)
    @Test
    fun agentLoginEmptyGuidTest() = runBlocking<Unit> {
        Mockito.`when`(agentInfoInteractor.agentInfo).thenReturn(MutableLiveData<AgentInfo>().apply {
            value = AgentInfo().also { agentInfo ->
                agentInfo.guid = null
            }
        })
        val agentInfoViewModel = AgentInfoViewModel(application)
        agentInfoViewModel.viewState.observeForever(viewStateObserver)
        agentInfoViewModel.toggleLogin()
        Mockito.verify(viewStateObserver).onChanged(AgentInfoViewState.EmptyGuid)
    }

    // 5. Agent 를 로그인한다. (Agent 등록 상태, 로그아웃 상태, OFF line 상태 )
    @Test
    fun agentLoginOfflineTest() = runBlocking<Unit> {
        Mockito.`when`(networkUtils.isAvailableNetwork()).thenReturn(false)
        Mockito.`when`(agentInfoInteractor.agentInfo).thenReturn(MutableLiveData<AgentInfo>().apply {
            value = AgentInfo().also { agentInfo ->
                agentInfo.guid = "1234"
            }
        })
        val agentInfoViewModel = AgentInfoViewModel(application)
        agentInfoViewModel.viewState.observeForever(viewStateObserver)
        agentInfoViewModel.toggleLogin()
        Mockito.verify(viewStateObserver).onChanged(AgentInfoViewState.OffLineState)
    }


    // 6. Agent 를 로그인한다. (Agent 등록 상태, 로그아웃 상태, On line 상태, 로그인 실패 )
    @Test
    fun agentLoginFailTest() = runBlocking<Unit> {
        Mockito.`when`(networkUtils.isAvailableNetwork()).thenReturn(true)
        Mockito.`when`(agentInfoInteractor.agentInfo).thenReturn(MutableLiveData<AgentInfo>().apply {
            value = AgentInfo().also { agentInfo ->
                agentInfo.guid = "1234"
            }
        })
        val agentInfoViewModel = AgentInfoViewModel(application)
        Mockito.`when`(agentInfoInteractor.loginProcess()).thenReturn(LOGIN_FAIL)
        agentInfoViewModel.viewState.observeForever(viewStateObserver)
        agentInfoViewModel.toggleLogin()
        Mockito.verify(viewStateObserver).onChanged(Mockito.any(AgentInfoViewState.LoginFail::class.java))
    }

    // 7. Agent 를 로그인한다. (Agent 등록 상태, 로그아웃 상태, On line 상태, 로그인 성공 )
    @Test
    fun agentLoginCompletedTest() = runBlocking<Unit> {
        Mockito.`when`(networkUtils.isAvailableNetwork()).thenReturn(true)
        Mockito.`when`(agentInfoInteractor.agentInfo).thenReturn(MutableLiveData<AgentInfo>().apply {
            value = AgentInfo().also { agentInfo ->
                agentInfo.guid = "1234"
            }
        })
        val agentInfoViewModel = AgentInfoViewModel(application)
        Mockito.`when`(agentInfoInteractor.loginProcess()).thenReturn(LOGIN_SUCCESS)
        agentInfoViewModel.viewState.observeForever(viewStateObserver)
        agentInfoViewModel.toggleLogin()
        Mockito.verify(viewStateObserver).onChanged(AgentInfoViewState.LoginCompleted)
    }

}