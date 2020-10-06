package com.rsupport.mobile.agent.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rsupport.mobile.agent.constant.AgentBasicInfo
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.lang.RuntimeException
import java.security.GeneralSecurityException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException

@RunWith(RobolectricTestRunner::class)
class WebConnectionConsoleLoginTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setup() {
        AgentBasicInfo.setApiVersion(context, "")
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun resultFalseWhenStreamIsNull() {
        val apiService = createApiService(context)
        val consoleLoginResult = apiService.consoleLogin("webID", "webPass", "bizID")
        assertFalse(consoleLoginResult.isSuccess)
    }

    @Test
    fun `Network stream 생성시 IOException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IOException("IOException"))
        val consoleLoginResult = apiService.consoleLogin("webID", "webPass", "bizID")
        assertFalse(consoleLoginResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 InvalidKeyException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidKeyException("InvalidKeyException"))
        val consoleLoginResult = apiService.consoleLogin("webID", "webPass", "bizID")
        assertFalse(consoleLoginResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 BadPaddingException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, BadPaddingException("BadPaddingException"))
        val consoleLoginResult = apiService.consoleLogin("webID", "webPass", "bizID")
        assertFalse(consoleLoginResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 InvalidAlgorithmParameterException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidAlgorithmParameterException("InvalidAlgorithmParameterException"))
        val consoleLoginResult = apiService.consoleLogin("webID", "webPass", "bizID")
        assertFalse(consoleLoginResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 IllegalBlockSizeException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IllegalBlockSizeException("IllegalBlockSizeException"))
        val consoleLoginResult = apiService.consoleLogin("webID", "webPass", "bizID")
        assertFalse(consoleLoginResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 알수 없는 오류가 발생하면 실패한다`() {
        val apiService = createApiService(context, RuntimeException("RuntimeException"))
        val consoleLoginResult = apiService.consoleLogin("webID", "webPass", "bizID")
        assertFalse(consoleLoginResult.isSuccess)
        assertEquals(RSErrorCode.UNKNOWN, GlobalStatic.g_errNumber)
    }

    @Test
    fun resultFalseWhenEmptyData() {
        val apiService = createApiService(context, "empty_data.xml")
        val consoleLoginResult = apiService.consoleLogin("webID", "webPass", "bizID")
        assertFalse(consoleLoginResult.isSuccess)
    }

    @Test
    fun resultFalseWhenRetCode900() {
        val apiService = createApiService(context, "retcode_900.xml")
        val consoleLoginResult = apiService.consoleLogin("webID", "webPass", "bizID")
        assertFalse(consoleLoginResult.isSuccess)

        assertEquals(900, GlobalStatic.g_errNumber)
    }

    @Test
    fun resultFalseWhenResule900() {
        val apiService = createApiService(context, "result_900.xml")
        val consoleLoginResult = apiService.consoleLogin("webID", "webPass", "bizID")
        assertFalse(consoleLoginResult.isSuccess)

        assertEquals(900, GlobalStatic.g_errNumber)
    }

    @Test
    fun throwRSExceptionIO_ERRORWhenStreamIOError() {
        val apiService = createApiService(context, IOException("io exception"))

        val errorCode = apiService.consoleLogin("webID", "webPass", "bizID").let {
            ((it as? Result.Failure)?.throwable as? RSException)?.errorCode
        }

        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun resultTrueWhenRetCode100() {
        val apiService = createApiService(context, "retcode_success100_empty.xml")

        val consoleLoginResult = apiService.consoleLogin("webID", "webPass", "bizID")
        assertTrue(consoleLoginResult.isSuccess)
    }

    @Test
    fun resultTrueWhenResult100() {
        val apiService = createApiService(context, "result_success100_empty.xml")

        val consoleLoginResult = apiService.consoleLogin("webID", "webPass", "bizID")
        assertTrue(consoleLoginResult.isSuccess)
    }

    @Test
    fun protocolHttpWhenServerHttpURL() {
        val apiService = createApiService(context, "console/console_login_success_http.xml")
        val consoleLoginResult = apiService.consoleLogin("webID", "webPass", "bizID")
        assertTrue(consoleLoginResult.isSuccess)

        assertEquals("http://www.rview.com", GlobalStatic.connectionInfo.webServer)
        assertEquals("http", GlobalStatic.connectionInfo.webProtocol)
        assertEquals("80", GlobalStatic.connectionInfo.webServerPort)
    }

    @Test
    fun protocolHttpsWhenServerHttpsURL() {
        val apiService = createApiService(context, "console/console_login_success_http.xml")
        val consoleLoginResult = apiService.consoleLogin("webID", "webPass", "bizID")
        assertTrue(consoleLoginResult.isSuccess)

        assertEquals("http://www.rview.com", GlobalStatic.connectionInfo.webServer)
        assertEquals("http", GlobalStatic.connectionInfo.webProtocol)
        assertEquals("80", GlobalStatic.connectionInfo.webServerPort)
    }


    @Test
    fun resultTrueWhenResult100Data() {
        val apiService = createApiService(context, "console/console_login_success_data.xml")
        val consoleLoginResult = apiService.consoleLogin("webID", "webPass", "bizID")
        assertTrue(consoleLoginResult.isSuccess)

        assertEquals("https://www.rview.com", GlobalStatic.connectionInfo.webServer)
        assertEquals("https", GlobalStatic.connectionInfo.webProtocol)
        assertEquals("443", GlobalStatic.connectionInfo.webServerPort)
        assertEquals("/services/api/console/agent_group_list", GlobalStatic.connectionInfo.agentlisturl)
        assertEquals("/services/api/console/agent_update", GlobalStatic.connectionInfo.agentupdateurl)
        assertEquals("usekey-1234", GlobalStatic.connectionInfo.userKey)
        assertEquals("0", GlobalStatic.connectionInfo.newVersion)
        assertEquals("0", GlobalStatic.connectionInfo.accountLock)
        assertEquals("00:00", GlobalStatic.connectionInfo.waitLockTime)
        assertEquals("7", GlobalStatic.connectionInfo.rvoemtype)
        assertEquals("0", GlobalStatic.connectionInfo.loginFailCount)
        assertEquals("0", GlobalStatic.connectionInfo.newnoticeseq)
        assertEquals("0", GlobalStatic.connectionInfo.passwordlimitdays)
        assertEquals("/services/api/agent_install/check_validate_access_account", AgentBasicInfo.RV_AGENT_URL_VALIDATE)
        assertEquals("/services/api/agent/login", AgentBasicInfo.RV_AGENT_URL_LOGIN)
        assertEquals("/services/api/agent_install/install_ok", AgentBasicInfo.RV_AGENT_URL_INSTALL)
        assertEquals("847", AgentBasicInfo.getAgentBizID(context))
        assertEquals("accesstoken_123", AgentBasicInfo.getAccessToken(context))
        assertEquals("refreshtoken_123", AgentBasicInfo.getRefreshToken(context))
        assertEquals("refreshtoken_url_123", AgentBasicInfo.getRefreshTokenURL(context))
        assertEquals("v2", AgentBasicInfo.getApiVersion(context))
    }

    @Test
    fun `api version 이 다를경우 다시 호출되어야한다`() {
        val spyWebConnection = spy(createWebConnection(context, "console/console_login_success_data.xml"))
        val apiService = createApiService(context, spyWebConnection)
        val consoleLoginResult = apiService.consoleLogin("webID", "webPass", "bizID")
        assertTrue(consoleLoginResult.isSuccess)
        verify(spyWebConnection, times(2)).consoleLogin(any(), any(), any(), any(), any())
    }
}