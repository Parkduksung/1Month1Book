package com.rsupport.mobile.agent.ui.settings.delete

import androidx.lifecycle.MutableLiveData
import base.BaseTest
import com.rsupport.mobile.agent.constant.ComConstant
import com.rsupport.mobile.agent.api.ApiService
import com.rsupport.mobile.agent.api.model.AgentDeleteResult
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.repo.agent.AgentRepository
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.rscommon.exception.RSException
import com.rsupport.mobile.agent.api.model.AgentInfo
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.lang.RuntimeException

@RunWith(MockitoJUnitRunner::class)
class AgentDeleteInteractorTest : BaseTest() {

    @Mock
    lateinit var apiService: ApiService

    @Mock
    lateinit var agentRepository: AgentRepository

    @Mock
    lateinit var configRepository: ConfigRepository

    private val agentDeleteInteractor by inject(AgentDeleteInteractor::class.java)

    override fun createModules(): List<Module> {
        return listOf(
                module { factory { AgentDeleteInteractor() } },
                module { factory { agentRepository } },
                module { factory { configRepository } },
                module { factory { apiService } }
        )
    }

    private val NOT_DEFINED_ERROR_CODE = -999
    private val TEST_USER_ID = "userId"
    private val TEST_USER_PWD = "userPwd"
    private val TEST_BIZ_ID = "bizId"
    private val TEST_GUID = "guid-1234"

    // agent 삭제 요청을한다. web 오류코드가 111 일때 ErrorCode.LOGIN_INVALID_USER_ACCOUNT_OR_PWD 을 반환하는지 확인한다.
    @Test
    fun deleteAgent111Error() = runBlocking<Unit> {
        setupAgentInfo()

        Mockito.`when`(apiService.deleteAgent(TEST_GUID, TEST_USER_ID, TEST_USER_PWD, TEST_BIZ_ID)).thenReturn(Result.failure(RSException(ComConstant.WEB_ERR_INVALID_PARAMETER.toInt())))

        val result = deleteAgent()

        MatcherAssert.assertThat("오류코드가 ErrorCode.LOGIN_INVALID_USER_ACCOUNT_OR_PWD 아니라서 실패", result, ErrorCode.SETTING_INVALID_ACCOUNT_OR_PWD.toIs())
    }


    // agent 삭제 요청을한다. web 오류코드가 112 일때 ErrorCode.LOGIN_INVALID_USER_ACCOUNT_OR_PWD 을 반환하는지 확인한다.
    @Test
    fun deleteAgent112Error() = runBlocking<Unit> {
        setupAgentInfo()

        Mockito.`when`(apiService.deleteAgent(TEST_GUID, TEST_USER_ID, TEST_USER_PWD, TEST_BIZ_ID)).thenReturn(Result.failure(RSException(ComConstant.WEB_ERR_NOT_FOUND_USERID.toInt())))

        val result = deleteAgent()
        MatcherAssert.assertThat("오류코드가 ErrorCode.LOGIN_INVALID_USER_ACCOUNT_OR_PWD 아니라서 실패", result, ErrorCode.SETTING_INVALID_ACCOUNT_OR_PWD.toIs())
    }

    // agent 삭제 요청을한다. web 오류코드가 114 일때 ErrorCode.LOGIN_INVALID_USER_ACCOUNT_OR_PWD 을 반환하는지 확인한다.
    @Test
    fun deleteAgent114Error() = runBlocking<Unit> {
        setupAgentInfo()

        Mockito.`when`(apiService.deleteAgent(TEST_GUID, TEST_USER_ID, TEST_USER_PWD, TEST_BIZ_ID)).thenReturn(Result.failure(RSException(ComConstant.WEB_ERR_INVALID_USER_ACCOUNT.toInt())))

        val result = deleteAgent()
        MatcherAssert.assertThat("오류코드가 ErrorCode.LOGIN_INVALID_USER_ACCOUNT_OR_PWD 아니라서 실패", result, ErrorCode.SETTING_INVALID_ACCOUNT_OR_PWD.toIs())
    }


    // agent 삭제 요청을한다. web 오류코드가 141 일때 ErrorCode.LOGIN_INVALID_USER_ACCOUNT_OR_PWD 을 반환하는지 확인한다.
    @Test
    fun deleteAgent141Error() = runBlocking<Unit> {
        setupAgentInfo()

        Mockito.`when`(apiService.deleteAgent(TEST_GUID, TEST_USER_ID, TEST_USER_PWD, TEST_BIZ_ID)).thenReturn(Result.failure(RSException(ComConstant.WEB_ERR_AES_INVALID_USER_ACCOUNT.toInt())))

        val result = deleteAgent()
        MatcherAssert.assertThat("오류코드가 ErrorCode.LOGIN_INVALID_USER_ACCOUNT_OR_PWD 아니라서 실패", result, ErrorCode.SETTING_INVALID_ACCOUNT_OR_PWD.toIs())
    }


    // agent 삭제 요청을한다. web 오류코드가 212 일때 ErrorCode.WEB_ERR_ALREADY_DELETE_AGENTID 을 반환하는지 확인한다.
    @Test
    fun deleteAgent212Error() = runBlocking<Unit> {
        setupAgentInfo()

        Mockito.`when`(apiService.deleteAgent(TEST_GUID, TEST_USER_ID, TEST_USER_PWD, TEST_BIZ_ID)).thenReturn(Result.failure(RSException(ComConstant.WEB_ERR_ALREADY_DELETE_AGENTID.toInt())))

        val result = deleteAgent()
        MatcherAssert.assertThat("오류코드가 ErrorCode.SETTING_ALREADY_DELETE_AGENTID 아니라서 실패", result, ErrorCode.SETTING_ALREADY_DELETE_AGENT.toIs())
    }


    // agent 삭제 요청을한다. web 오류코드가 정의되지 않았을때 ErrorCode.UNKNOWN_ERROR 을 반환하는지 확인한다.
    @Test
    fun deleteAgentUnknownError() = runBlocking<Unit> {
        setupAgentInfo()

        Mockito.`when`(apiService.deleteAgent(TEST_GUID, TEST_USER_ID, TEST_USER_PWD, TEST_BIZ_ID)).thenReturn(Result.failure(RSException(NOT_DEFINED_ERROR_CODE)))

        val result = deleteAgent()
        MatcherAssert.assertThat("오류코드가 ErrorCode.NOT_DEFINED_ERROR_CODE 아니라서 실패", result, NOT_DEFINED_ERROR_CODE.toIs())
    }

    // agent 삭제 요청을한다. RSException 외의 예외가 발생했을때 ErrorCode.UNKNOWN_ERROR 을 반환하는지 확인한다.
    @Test
    fun deleteAgentUndefinedExceptionError() = runBlocking<Unit> {
        setupAgentInfo()

        Mockito.`when`(apiService.deleteAgent(TEST_GUID, TEST_USER_ID, TEST_USER_PWD, TEST_BIZ_ID)).thenReturn(Result.failure(RuntimeException("not defined")))

        val result = deleteAgent()
        MatcherAssert.assertThat("오류코드가 ErrorCode.UNKNOWN_ERROR 아니라서 실패", result, ErrorCode.UNKNOWN_ERROR.toIs())
    }

    // agent 삭제가 정상으로 되었을때 를 확인한다.
    @Test
    fun deleteAgentSuccessTest() = runBlocking<Unit> {
        setupAgentInfo()
        Mockito.`when`(apiService.deleteAgent(TEST_GUID, TEST_USER_ID, TEST_USER_PWD, TEST_BIZ_ID)).thenReturn(Result.success(AgentDeleteResult()))

        val result = deleteAgent()
        MatcherAssert.assertThat("SUCCESS 아니라서 실패", result, SUCCESS.toIs())
    }


    // Push 로 부터 Agent 가 삭제되었을때 오류 코드를 확인한다.
    @Test
    fun already_deleted_agent_when_agent_info_is_empty() = runBlocking<Unit> {
        Mockito.`when`(agentRepository.getAgentInfo()).thenReturn(MutableLiveData<AgentInfo>(AgentInfo()))

        val result = deleteAgent()
        MatcherAssert.assertThat("오류코드가 ErrorCode.SETTING_ALREADY_DELETE_AGENT 아니라서 실패", result, ErrorCode.SETTING_ALREADY_DELETE_AGENT.toIs())
    }


    private fun setupAgentInfo() {
        Mockito.`when`(agentRepository.getAgentInfo()).thenReturn(MutableLiveData<AgentInfo>(AgentInfo().apply {
            guid = TEST_GUID
            bizId = TEST_BIZ_ID
        }))
    }

    private val SUCCESS = 0

    private suspend fun deleteAgent(): Int {
        return agentDeleteInteractor.deleteAgent(TEST_USER_ID, TEST_USER_PWD).let {
            return@let when (it) {
                is Result.Success -> {
                    SUCCESS
                }
                is Result.Failure -> {
                    if (it.throwable is RSException) {
                        (it.throwable as RSException).errorCode
                    } else ErrorCode.UNKNOWN_ERROR
                }
            }
        }
    }

}

fun Int.toIs(): Matcher<Int> {
    return Matchers.`is`(this)
}