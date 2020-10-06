package com.rsupport.mobile.agent.ui.settings.delete

import androidx.lifecycle.Observer
import base.BaseTest
import com.nhaarman.mockitokotlin2.any
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.ui.base.ViewState
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.rscommon.exception.RSException
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AgentDeleteViewModelTest : BaseTest() {

    @Mock
    private lateinit var agentDeleteInteractor: AgentDeleteInteractor

    @Mock
    private lateinit var observer: Observer<ViewState>

    override fun createModules(): List<Module> {
        return listOf(
                module { factory { agentDeleteInteractor } }
        )
    }

    private val agentViewModel by lazy {
        AgentDeleteViewModel(application)
    }


    private fun setupIdAndPwd() {
        agentViewModel.userId.set("123")
        agentViewModel.userPwd.set("456")
    }

    // id, pwd 둘다 null일때 EmptyIdAndPwd 이벤트를 확인
    @Test
    fun invalidAccountNullTest() = runBlocking {
        agentViewModel.userId.set(null)
        agentViewModel.userPwd.set(null)

        agentViewModel.viewState.observeForever(observer)
        agentViewModel.deleteAgent()

        Mockito.verify(observer).onChanged(AgentDeleteViewState.EmptyIdAndPwd)
    }


    // id, pwd 둘다 입력하지 않았을때 EmptyIdAndPwd 이벤트를 확인
    @Test
    fun invalidAccountEmptyTest() = runBlocking {
        agentViewModel.userId.set("")
        agentViewModel.userPwd.set("")

        agentViewModel.viewState.observeForever(observer)
        agentViewModel.deleteAgent()

        Mockito.verify(observer).onChanged(AgentDeleteViewState.EmptyIdAndPwd)
    }


    // pwd 만 입력하고 id empty 일때 EmptyId 이벤트를 확인한다.
    @Test
    fun EmptyIdTest() = runBlocking {
        agentViewModel.userId.set("")
        agentViewModel.userPwd.set("123")

        agentViewModel.viewState.observeForever(observer)
        agentViewModel.deleteAgent()

        Mockito.verify(observer).onChanged(AgentDeleteViewState.EmptyId)
    }

    // pwd 만 입력하고 id null 일때 EmptyId 이벤트를 확인한다.
    @Test
    fun EmptyIdNullTest() = runBlocking {
        agentViewModel.userId.set(null)
        agentViewModel.userPwd.set("123")

        agentViewModel.viewState.observeForever(observer)
        agentViewModel.deleteAgent()

        Mockito.verify(observer).onChanged(AgentDeleteViewState.EmptyId)
    }


    // id 만 입력하고 pwd empty 일때 EmptyPwd 이벤트를 확인한다.
    @Test
    fun EmptyPwdTest() = runBlocking {
        agentViewModel.userId.set("123")
        agentViewModel.userPwd.set("")

        agentViewModel.viewState.observeForever(observer)
        agentViewModel.deleteAgent()

        Mockito.verify(observer).onChanged(AgentDeleteViewState.EmptyPwd)
    }

    // pwd 만 입력하고 id null 일때 EmptyId 이벤트를 확인한다.
    @Test
    fun EmptyPwdNullTest() = runBlocking {
        agentViewModel.userId.set("123")
        agentViewModel.userPwd.set(null)

        agentViewModel.viewState.observeForever(observer)
        agentViewModel.deleteAgent()

        Mockito.verify(observer).onChanged(AgentDeleteViewState.EmptyPwd)
    }

    // 잘못된 id or pwd 시 오류 발생 이벤트를 확인한다.
    @Test
    fun invalidAccountTest() = runBlocking {
        setupIdAndPwd()
        Mockito.`when`(agentDeleteInteractor.deleteAgent(any(), any())).thenReturn(Result.failure(RSException(ErrorCode.SETTING_INVALID_ACCOUNT_OR_PWD)))

        agentViewModel.viewState.observeForever(observer)
        agentViewModel.deleteAgent()

        Mockito.verify(observer).onChanged(AgentDeleteViewState.InvalidAccount)
    }


    // 이미 삭제된 agent에 대한 삭제요청일때 이벤트를 확인한다.
    @Test
    fun alreadyDeletedAgentTest() = runBlocking {
        setupIdAndPwd()
        Mockito.`when`(agentDeleteInteractor.deleteAgent(any(), any())).thenReturn(Result.failure(RSException(ErrorCode.SETTING_ALREADY_DELETE_AGENT)))

        agentViewModel.viewState.observeForever(observer)
        agentViewModel.deleteAgent()

        Mockito.verify(observer).onChanged(AgentDeleteViewState.AlreadyDeletedAgent)
    }


    // 정의하지 않은 RSException 발생시 이벤트를 확인한다.
    @Test
    fun unknownErrorCodeTest() = runBlocking {
        setupIdAndPwd()
        Mockito.`when`(agentDeleteInteractor.deleteAgent(any(), any())).thenReturn(Result.failure(RSException(-999)))

        agentViewModel.viewState.observeForever(observer)
        agentViewModel.deleteAgent()

        Mockito.verify(observer).onChanged(AgentDeleteViewState.NotDefinedError(-999))
    }

    // 정의하지 않은 Exception 발생시 이벤트를 확인한다.
    @Test
    fun unknownExceptionTest() = runBlocking {
        setupIdAndPwd()
        Mockito.`when`(agentDeleteInteractor.deleteAgent(any(), any())).thenReturn(Result.failure(RuntimeException("not defined exception")))

        agentViewModel.viewState.observeForever(observer)
        agentViewModel.deleteAgent()

        Mockito.verify(observer).onChanged(AgentDeleteViewState.NotDefinedError(ErrorCode.UNKNOWN_ERROR))
    }


    // 삭제 성공시 이벤트를 확인한다.
    @Test
    fun deleteAgentSuccessTest() = runBlocking {
        setupIdAndPwd()
        Mockito.`when`(agentDeleteInteractor.deleteAgent(any(), any())).thenReturn(Result.success(true))

        agentViewModel.viewState.observeForever(observer)
        agentViewModel.deleteAgent()

        Mockito.verify(observer).onChanged(AgentDeleteViewState.DeletedSuccess)
    }
}