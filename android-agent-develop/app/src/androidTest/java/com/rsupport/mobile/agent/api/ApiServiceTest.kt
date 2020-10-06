package com.rsupport.mobile.agent.api

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rsupport.mobile.agent.ui.agent.agentinfo.AgentInfoInteractor
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.java.KoinJavaComponent.inject

@RunWith(AndroidJUnit4::class)
class ApiServiceTest {

    private val apiService: ApiService by inject(ApiService::class.java)
    private val agentInfoInteractor by inject(AgentInfoInteractor::class.java)

    @Before
    fun setup() = runBlocking<Unit> {
        agentInfoInteractor.removeAgent()
    }

    private val corpID = "kwcho"
    private val userID = "kwcho"
    private val userPwd = "111111"

    // 1. consoleLogin 이 정상 동작하는지 확인한다.
    @Test
    fun consoleLoginTest() = runBlocking<Unit> {
        val loginResult = apiService.consoleLogin(corpID, userID, userPwd, true)
        /**
         * loginResult LOG
        loginResult.ConsoleLoginResult(
        webServer=https://www.rview.com,
        webServerPort=443,
        agentGroupListURL=/console/agent_group_list,
        agentUpdateURL=/services/api/console/agent_update,
        userKey=A84BF86EF714483E9AFF722842D90871,
        newVersion=0,
        accountLock=0,
        waitLockTime=00:00,
        rvOemType=,
        loginFailCount=0,
        newNoticeSeq=,
        passwordLimitDays=,
        agentValidateURL=/services/api/agent_install/check_validate_access_account,
        agentLoginURL=/services/api/agent/login,
        agentInstallURL=/services/api/agent_install/install_ok, bizID=847
        )
         */
        MatcherAssert.assertThat("로그인이 실패해서 오류", loginResult.isSuccess, Matchers.`is`(true))
    }
}