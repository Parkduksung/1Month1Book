package com.rsupport.mobile.agent.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rsupport.mobile.agent.api.model.AgentLoginResult
import com.rsupport.mobile.agent.constant.AgentBasicInfo
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.constant.legacy.AgentLoginResultUpdater
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.rscommon.define.RSErrorCode
import junit.framework.Assert
import junit.framework.Assert.assertEquals
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.lang.RuntimeException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException

@RunWith(RobolectricTestRunner::class)
class WebConnectionAgentLoginTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val guid = "guid-1234"

    @Before
    fun setup() {
        GlobalStatic.connectionInfo.clear()
        GlobalStatic.connectionInfo.webServer = ""
        GlobalStatic.connectionInfo.webProtocol = ""
        GlobalStatic.connectionInfo.webServerPort = ""
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun returnFalseWhenStreamIsNull() {
        val apiService = createApiService(context)
        val loginResult = apiService.agentLogin(guid)
        assertThat(loginResult, Matchers.instanceOf(Result.Failure::class.java))
    }

    @Test
    fun `Network stream 생성시 IOException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IOException("IOException"))
        val loginResult = apiService.agentLogin(guid)
        assertFalse(loginResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 InvalidKeyException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidKeyException("InvalidKeyException"))
        val loginResult = apiService.agentLogin(guid)
        assertFalse(loginResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 BadPaddingException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, BadPaddingException("BadPaddingException"))
        val loginResult = apiService.agentLogin(guid)
        assertFalse(loginResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 InvalidAlgorithmParameterException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidAlgorithmParameterException("InvalidAlgorithmParameterException"))
        val loginResult = apiService.agentLogin(guid)
        assertFalse(loginResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 IllegalBlockSizeException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IllegalBlockSizeException("IllegalBlockSizeException"))
        val loginResult = apiService.agentLogin(guid)
        assertFalse(loginResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 알수 없는 오류 발생하면 실패한다`() {
        val apiService = createApiService(context, RuntimeException("RuntimeException"))
        val loginResult = apiService.agentLogin(guid)
        assertFalse(loginResult.isSuccess)
        assertEquals(RSErrorCode.UNKNOWN, GlobalStatic.g_errNumber)
    }

    @Test
    fun returnFalseWhenNotFoundRESULT() {
        val apiService = createApiService(context, "empty_data.xml")

        val loginResult = apiService.agentLogin(guid)
        assertThat(loginResult, Matchers.instanceOf(Result.Failure::class.java))
    }

    @Test
    fun returnFalseWhenResultIsError() {
        val apiService = createApiService(context, "result_900.xml")
        val loginResult = apiService.agentLogin(guid)

        assertThat(loginResult, Matchers.instanceOf(Result.Failure::class.java))
        assertEquals(GlobalStatic.g_errNumber, 900)
        assertEquals(GlobalStatic.g_err, "test 900 error")
    }

    @Test
    fun returnTrueWhenResultIsSuccess() {
        val apiService = createApiService(context, "login/agent_login_success.xml")
        val loginResult = apiService.agentLogin(guid)

        (loginResult as? Result.Success<AgentLoginResult>)?.value?.let {
            AgentLoginResultUpdater().update(it)
        }

        assertThat(loginResult, Matchers.instanceOf(Result.Success::class.java))
        assertEquals("ID", AgentBasicInfo.RV_AGENT_ID)
        assertEquals("SESSIONIP_LENGTH", AgentBasicInfo.RV_AGENT_SESSIONIP_LENGTH)
        assertEquals("SESSIONIP", AgentBasicInfo.RV_AGENT_SESSIONIP)
        assertEquals("SESSIONPORT", AgentBasicInfo.RV_AGENT_SESSIONPORT)
        assertEquals("SESSIONIP2", AgentBasicInfo.RV_AGENT_SESSIONIP2)
        assertEquals("SESSIONPORT2", AgentBasicInfo.RV_AGENT_SESSIONPORT2)
        assertEquals("DISPLAYNAME", AgentBasicInfo.RV_AGENT_DISPLAYNAME)
        assertEquals("SSL", AgentBasicInfo.RV_AGENT_SSL)
        assertEquals("SHAREDFOLDER", AgentBasicInfo.RV_AGENT_SHAREDFOLDER)
        assertEquals("LOGOUT_PAGE", AgentBasicInfo.getLogoutURL(context))
        assertEquals("SESSIONRESULT_PAGE", AgentBasicInfo.RV_AGENT_SESSIONRESULT_PAGE)
        assertEquals("ACCOUNTCHANGE_PAGE", AgentBasicInfo.RV_AGENT_ACCOUNTCHANGE_PAGE)
        assertEquals("CONFIGMODIFY_PAGE", AgentBasicInfo.RV_AGENT_CONFIGMODIFY_PAGE)
        assertEquals("CONFIGQUERY_PAGE", AgentBasicInfo.RV_AGENT_CONFIGQUERY_PAGE)
        assertEquals("RMTCALLCONNECT_PAGE", AgentBasicInfo.RV_AGENT_RMTCALLCONNECT_PAGE)
        assertEquals("RMTCALLDISCONNECT_PAGE", AgentBasicInfo.RV_AGENT_RMTCALLDISCONNECT_PAGE)
        assertEquals("RMTFTPCONNECT_PAGE", AgentBasicInfo.RV_AGENT_RMTFTPCONNECT_PAGE)
        assertEquals("RMTFTPDISCONNECT_PAGE", AgentBasicInfo.RV_AGENT_RMTFTPDISCONNECT_PAGE)
        assertEquals("AGENTHELP_PAGE", AgentBasicInfo.RV_AGENT_AGENTHELP_PAGE)
        assertEquals("LOGIN_PAGE", AgentBasicInfo.getLoginURL(context))
        assertEquals("AUTHWEB_SERVER", AgentBasicInfo.RV_AGENT_AUTHWEB_SERVER)
        assertEquals("AUTHWEB_SERVER2", AgentBasicInfo.RV_AGENT_AUTHWEB_SERVER2)
        assertEquals("UPDATECHECK_PAGE", AgentBasicInfo.RV_AGENT_UPDATECHECK_PAGE)
        assertEquals("UPDATEADDR", AgentBasicInfo.RV_AGENT_UPDATEADDR)
        assertEquals("UPDATEPORT", AgentBasicInfo.RV_AGENT_UPDATEPORT)
        assertEquals("UPDATEADDR2", AgentBasicInfo.RV_AGENT_UPDATEADDR2)
        assertEquals("UPDATEDIR", AgentBasicInfo.RV_AGENT_UPDATEDIR)
        assertEquals("AUTOSCREENLOCK", AgentBasicInfo.RV_AGENT_AUTOSCREENLOCK)
        assertEquals("AUTOSYSTEMLOCK", AgentBasicInfo.RV_AGENT_AUTOSYSTEMLOCK)
        assertEquals("ENABLED_EXT", AgentBasicInfo.RV_AGENT_ENABLED_EXT)
        assertEquals("CRASH_SERVER", AgentBasicInfo.RV_AGENT_CRASH_SERVER)
        assertEquals("CRASH_PAGE", AgentBasicInfo.RV_AGENT_CRASH_PAGE)
        assertEquals("RVOEMTYPE", AgentBasicInfo.RV_AGENT_RVOEMTYPE)
        assertEquals("ASSET_TIMER", AgentBasicInfo.RV_AGENT_ASSET_TIMER)
        assertEquals("RANDOMPROCESS", AgentBasicInfo.RV_AGENT_RANDOMPROCESS)
        assertEquals("CONNECT_SSL_TYPE", AgentBasicInfo.RV_AGENT_CONNECT_SSL_TYPE)
        assertEquals("CONNECT_SERVER_TYPE", AgentBasicInfo.RV_AGENT_CONNECT_SERVER_TYPE)
        assertEquals("PUSHSERVER_ADDRESS", AgentBasicInfo.RV_AGENT_PUSHSERVER_ADDRESS)
        assertEquals("PUSHSERVER_PORT", AgentBasicInfo.RV_AGENT_PUSHSERVER_PORT)
        assertEquals("RSPERM_DOWNLOAD_URL", AgentBasicInfo.RV_AGENT_RSPERM_DOWNLOAD_URL)
        assertEquals("LOGOUT_TOPIC", AgentBasicInfo.RV_AGENT_PUSHSERVER_WILLTOPIC)
        assertEquals("NEWVERSION", AgentBasicInfo.RV_AGENT_NEWVERSION)
        assertEquals(true, AgentBasicInfo.RV_AGENT_PUSH_SSL)
    }

    @Test
    fun containHttpWhenServerIsHttp() {
        GlobalStatic.connectionInfo.webServer = "http://rview.com"
        GlobalStatic.connectionInfo.webProtocol = "http"
        val webConnection = createWebConnection(context)
        val serverURL = webConnection.getServerPageUrl("/pass")
        assertEquals("http://rview.com/pass", serverURL)
    }

    @Test
    fun containHttpsWhenProtocolHttps() {
        GlobalStatic.connectionInfo.webServer = "http://rview.com"
        GlobalStatic.connectionInfo.webProtocol = "https"
        val webConnection = createWebConnection(context)
        val serverURL = webConnection.getServerPageUrl("/pass")
        assertEquals("https://rview.com/pass", serverURL)
    }

    @Test
    fun containHttpWithOut80PortWhenServerIsHttpAndPort80() {
        GlobalStatic.connectionInfo.webServer = "http://rview.com"
        GlobalStatic.connectionInfo.webProtocol = "http"
        GlobalStatic.connectionInfo.webServerPort = "80"
        val webConnection = createWebConnection(context)
        val serverURL = webConnection.getServerPageUrl("/pass")
        assertEquals("http://rview.com/pass", serverURL)
    }

    @Test
    fun containHttpWith8080PortWhenServerIsHttpAndPort8080() {
        GlobalStatic.connectionInfo.webServer = "http://rview.com"
        GlobalStatic.connectionInfo.webProtocol = "http"
        GlobalStatic.connectionInfo.webServerPort = "8080"
        val webConnection = createWebConnection(context)
        val serverURL = webConnection.getServerPageUrl("/pass")
        assertEquals("http://rview.com:8080/pass", serverURL)
    }

    @Test
    fun containHttpsWithOut443WhenProtocolHttpsAndPort443() {
        GlobalStatic.connectionInfo.webServer = "https://rview.com"
        GlobalStatic.connectionInfo.webProtocol = "https"
        GlobalStatic.connectionInfo.webServerPort = "443"
        val webConnection = createWebConnection(context)
        val serverURL = webConnection.getServerPageUrl("/pass")
        assertEquals("https://rview.com/pass", serverURL)
    }

    @Test
    fun containHttpsWith4430WhenProtocolHttpsAndPort4430() {
        GlobalStatic.connectionInfo.webServer = "https://rview.com"
        GlobalStatic.connectionInfo.webProtocol = "https"
        GlobalStatic.connectionInfo.webServerPort = "4430"
        val webConnection = createWebConnection(context)
        val serverURL = webConnection.getServerPageUrl("/pass")
        assertEquals("https://rview.com:4430/pass", serverURL)
    }
}

