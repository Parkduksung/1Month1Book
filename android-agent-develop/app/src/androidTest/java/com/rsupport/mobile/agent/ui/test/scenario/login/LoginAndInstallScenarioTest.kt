package com.rsupport.mobile.agent.ui.test.scenario.login

import androidx.test.filters.LargeTest
import com.rsupport.mobile.agent.ui.test.utils.InstallUtils
import com.rsupport.mobile.agent.ui.test.utils.LoginUtils
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@LargeTest
class LoginAndInstallScenarioTest {
    private val loginUtils = LoginUtils()
    private val installUtils = InstallUtils()

    @Before
    fun setup() = runBlocking<Unit> {
        loginUtils.setup()
        installUtils.setup()
        loginUtils.launch()
    }

    @After
    fun tearDown() = runBlocking {
        loginUtils.tearDown()
        installUtils.tearDown()
    }

    // 로그인 > Agent 설치 > Agent 대기 상태를 확인한다.
    @Test
    fun loginAndInstallTest() = runBlocking<Unit> {
        loginUtils.performLogin()
        installUtils.performInstall()
        installUtils.verifyAgentInfoActivity()
    }

}