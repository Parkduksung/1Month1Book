package com.rsupport.mobile.agent.ui.login

import androidx.lifecycle.Observer
import base.BaseTest
import com.nhaarman.mockitokotlin2.any
import com.rsupport.mobile.agent.api.model.ConsoleLoginResult
import com.rsupport.mobile.agent.modules.engine.EngineType
import com.rsupport.mobile.agent.ui.agent.agentinfo.AgentInfoInteractor
import com.rsupport.mobile.agent.ui.base.ViewState
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.rscommon.exception.RSException
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LoginViewModelTest : BaseTest() {

    override fun createModules(): List<Module> {
        return listOf(
                module {
                    factory { agentInfoInteractor }
                    factory { loginInteractor }
                }
        )
    }

    @Mock
    private lateinit var agentInfoInteractor: AgentInfoInteractor

    @Mock
    private lateinit var loginInteractor: LoginInteractor

    @Mock
    private lateinit var observer: Observer<ViewState>

    private lateinit var loginViewModel: LoginViewModel


    @Before
    override fun setup() {
        super.setup()
        loginViewModel = LoginViewModel(application)
        loginViewModel.viewState.observeForever(observer)
    }


    // Agent 가 Install 되어 있는지 확인한다.
    @Test
    fun checkAgentInstallStatusTest() = runBlocking {
        Mockito.`when`(loginInteractor.isShowTutorial()).thenReturn(true)
        Mockito.`when`(agentInfoInteractor.isAgentInstalled()).thenReturn(true)
        loginViewModel.updateViewState()
        Mockito.verify(observer).onChanged(MainViewState.StartAgentInfoViewState)
    }

    // Tutorial 을 봤는지 확인한다.
    @Test
    fun checkShowTutorialTest() = runBlocking {
        Mockito.`when`(loginInteractor.isShowTutorial()).thenReturn(false)
        loginViewModel.updateViewState()
        Mockito.verify(observer).onChanged(MainViewState.ShowTutorialViewState)
    }

    // 로그인시 서버 정보 설정을 잘못했을때 오류를 확인한다.
    @Test
    fun invalidServerTest() = runBlocking<Unit> {
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(false)

        loginViewModel.performLogin()
        Mockito.verify(observer).onChanged(MainViewState.InvalidServerURL)
    }

    // User Id 를 입력하지 않았을때 오류를 확인한다.
    @Test
    fun emptyUserIdTest() = runBlocking<Unit> {
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        loginViewModel.userIdObservable.set("")

        loginViewModel.performLogin()
        Mockito.verify(observer).onChanged(MainViewState.EmptyUserId)
    }

    // User Pwd 를 입력하지 않았을때 오류를 확인한다.
    @Test
    fun emptyUserPwdTest() = runBlocking<Unit> {
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        loginViewModel.userIdObservable.set("1234")
        loginViewModel.userPwdObservable.set("")

        loginViewModel.performLogin()
        Mockito.verify(observer).onChanged(MainViewState.EmptyUserPwd)
    }

    // 지원하지 않는 단말일경우 정의하지 않은 오류 발생했을테 테스트
    @Test
    fun notSupportEngineTest() = runBlocking<Unit> {
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.failure(RuntimeException("not defined exception")))
        loginViewModel.userIdObservable.set("1234")
        loginViewModel.userPwdObservable.set("1234")

        loginViewModel.performLogin()

        Mockito.verify(observer).onChanged(MainViewState.NotSupportDevice)
    }


    // 지원하지 않는 단말일경우 RSException 발생시 테스트
    @Test
    fun notSupportEngineRSExceptionTest() = runBlocking<Unit> {
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.failure(RSException(1)))
        loginViewModel.userIdObservable.set("1234")
        loginViewModel.userPwdObservable.set("1234")

        loginViewModel.performLogin()

        Mockito.verify(observer).onChanged(MainViewState.NotSupportDevice)
    }


    // Console 로그인 실패시 정의하지 않은 오류 테스트
    @Test
    fun consoleLoginFailTest() = runBlocking<Unit> {
        val exception = RuntimeException("not defiled exception")
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.success(SupportEngine.from(EngineType.ENGINE_TYPE_RSPERM)))
        Mockito.`when`(loginInteractor.consoleLogin(any(), any(), any())).thenReturn(Result.failure(exception))

        loginViewModel.userIdObservable.set("1234")
        loginViewModel.userPwdObservable.set("1234")

        loginViewModel.performLogin()

        Mockito.verify(observer).onChanged(MainViewState.LoginFailure(exception))

    }

    // Console 로그인 실패시 RSException 오류 테스트
    @Test
    fun consoleLoginRSExceptionFailTest() = runBlocking<Unit> {
        val exception = RSException(1)
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.success(SupportEngine.from(EngineType.ENGINE_TYPE_RSPERM)))
        Mockito.`when`(loginInteractor.consoleLogin(any(), any(), any())).thenReturn(Result.failure(exception))

        loginViewModel.userIdObservable.set("1234")
        loginViewModel.userPwdObservable.set("1234")

        loginViewModel.performLogin()

        Mockito.verify(observer).onChanged(MainViewState.LoginFailure(exception))

    }


    // Console 로그인 성공 테스트
    @Test
    fun consoleLoginSuccessTest() = runBlocking<Unit> {
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.success(SupportEngine.from(EngineType.ENGINE_TYPE_RSPERM)))
        Mockito.`when`(loginInteractor.consoleLogin(any(), any(), any())).thenReturn(Result.Success(ConsoleLoginState.from(ConsoleLoginResult())))

        loginViewModel.userIdObservable.set("1234")
        loginViewModel.userPwdObservable.set("1234")

        loginViewModel.performLogin()

        Mockito.verify(observer).onChanged(MainViewState.LoginSuccess)

    }


    // Console 로그인 성공시 업데이트 가능 테스트
    @Test
    fun consoleLoginAvailableUpdateTest() = runBlocking<Unit> {
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.success(SupportEngine.from(EngineType.ENGINE_TYPE_RSPERM)))
        Mockito.`when`(loginInteractor.consoleLogin(any(), any(), any())).thenReturn(Result.Success(ConsoleLoginState.from(ConsoleLoginResult().apply { newVersion = "1" })))

        loginViewModel.userIdObservable.set("1234")
        loginViewModel.userPwdObservable.set("1234")

        loginViewModel.performLogin()

        Mockito.verify(observer).onChanged(MainViewState.UpdataAvailable)
    }


    // Console 로그인 성공시 비밀번호 유효기간 남음
    @Test
    fun consoleLoginPwdExpiredDayTest() = runBlocking<Unit> {
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.success(SupportEngine.from(EngineType.ENGINE_TYPE_RSPERM)))
        Mockito.`when`(loginInteractor.consoleLogin(any(), any(), any())).thenReturn(Result.Success(ConsoleLoginState.from(ConsoleLoginResult().apply { passwordLimitDays = "2" })))

        loginViewModel.userIdObservable.set("1234")
        loginViewModel.userPwdObservable.set("1234")

        loginViewModel.performLogin()

        Mockito.verify(observer).onChanged(MainViewState.PassExpireDay("2", ""))
    }
}