package com.rsupport.mobile.agent.modules.status

import base.BaseTest
import com.rsupport.mobile.agent.constant.ComConstant
import com.rsupport.mobile.agent.status.AgentStatus
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.module.Module
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AgentStatusTest : BaseTest() {
    override fun createModules(): List<Module> {
        return emptyList()
    }

    private lateinit var agentStatus: AgentStatus


    @Mock
    lateinit var statusChangedListener: AgentStatus.OnStatusChangedListener

    @Before
    override fun setup() {
        super.setup()
        agentStatus = AgentStatus(MemoryStatusContainer())
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


    // Login Status로 변경후 callback 이 호출되는지 확인한다.
    @Test
    fun loginStatusCallbackTest() = runBlocking {
        agentStatus.addListener(statusChangedListener)

        agentStatus.setLoggedIn()
        Mockito.verify(statusChangedListener).onChanged(AgentStatus.AGENT_STATUS_LOGIN.toInt())
    }

    // Logout Status 로 변경후 callback 이 호출되는지 확인한다.
    @Test
    fun logoutStatusCallbackTest() = runBlocking {
        agentStatus.addListener(statusChangedListener)
        agentStatus.setLogOut()
        Mockito.verify(statusChangedListener).onChanged(AgentStatus.AGENT_STATUS_LOGOUT.toInt())
    }

    // 원격제어 중으로 상태를 변경후 callback 이 호출되는지 확인한다.
    @Test
    fun remoteStatusCallbackTest() = runBlocking {
        agentStatus.addListener(statusChangedListener)
        agentStatus.setRemoting()
        Mockito.verify(statusChangedListener).onChanged(AgentStatus.AGENT_STATUS_REMOTING.toInt())
    }
}

class MemoryStatusContainer : AgentStatus.StatusContainer {
    private var status = AgentStatus.AGENT_STATUS_NOLOGIN

    override fun get(): Short {
        return status
    }

    override fun set(status: Short) {
        this.status = status
    }

    override fun clear() {
        status = AgentStatus.AGENT_STATUS_NOLOGIN
    }
}

