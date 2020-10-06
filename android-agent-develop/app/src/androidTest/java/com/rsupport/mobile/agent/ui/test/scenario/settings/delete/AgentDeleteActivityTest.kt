package com.rsupport.mobile.agent.ui.test.scenario.settings.delete

import androidx.test.filters.LargeTest
import com.rsupport.mobile.agent.ui.test.utils.DeleteAgentUtils
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@LargeTest
@RunWith(MockitoJUnitRunner::class)
class AgentDeleteActivityTest {

    private val deleteAgentUtils = DeleteAgentUtils()

    @Before
    fun setup() = runBlocking<Unit> {
        deleteAgentUtils.setup()
    }

    @After
    fun tearDown() = runBlocking<Unit> {
        deleteAgentUtils.tearDown()
    }

    // agent 를 삭제하고 로그인 화면으로 이동하는지를 확인한다.
    @Test
    fun agentDeleteTest() = runBlocking<Unit> {
        deleteAgentUtils.performLoginInstall()
        deleteAgentUtils.verifyAgentInfo()

        deleteAgentUtils.performDelete()
        deleteAgentUtils.verifyLoginActivity()
    }


    // agent 설치 > proxy 설정 ON > Agent 삭제시 팝업을 확인한다.
    @Test
    fun agentDeleteWhenProxyNullTest() = runBlocking {
        deleteAgentUtils.performLoginInstall()
        deleteAgentUtils.verifyAgentInfo()

        deleteAgentUtils.performSetting()
        deleteAgentUtils.performConfiguration()
        deleteAgentUtils.performProxyOn()
        deleteAgentUtils.performBack()
        deleteAgentUtils.performBack()

        deleteAgentUtils.performDelete()
        deleteAgentUtils.verifyProxyConfig()
    }
}