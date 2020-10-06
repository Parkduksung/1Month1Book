package com.rsupport.mobile.agent.status

import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.koin.java.KoinJavaComponent.inject

class AgentStatusAndroidTest {

    private val agentStatus: AgentStatus by inject(AgentStatus::class.java)

    @Before
    fun setup() {
        agentStatus.clear()
    }

    // 최초 실생값을 확인한다.
    @Test
    fun agentStatusIdleTest() = runBlocking {
        val status = agentStatus.get()
        MatcherAssert.assertThat("초기값이 잘못되서 실패", status, Matchers.`is`(AgentStatus.AGENT_STATUS_NOLOGIN))
    }

    // Login Status로 변경후 값을 확인한다.
    @Test
    fun loginStatusTest() = runBlocking {
        agentStatus.setLoggedIn()
        val status = agentStatus.get()
        MatcherAssert.assertThat("로그인 값이 아니라서 실패", status, Matchers.`is`(AgentStatus.AGENT_STATUS_LOGIN))
    }

    // Logout Status 로 변경후 값을 확인한다.
    @Test
    fun logoutStatusTest() = runBlocking {
        agentStatus.setLogOut()
        val status = agentStatus.get()
        MatcherAssert.assertThat("로그아웃 상태가 아니라서 실패", status, Matchers.`is`(AgentStatus.AGENT_STATUS_LOGOUT))
    }

    // 원격제어 중으로 상태를 변경후 값을 확인한다.
    @Test
    fun remoteStatusTest() = runBlocking {
        agentStatus.setRemoting()
        val status = agentStatus.get()
        MatcherAssert.assertThat("원격제어 상태가 아니라서 실패", status, Matchers.`is`(AgentStatus.AGENT_STATUS_REMOTING))
    }
}