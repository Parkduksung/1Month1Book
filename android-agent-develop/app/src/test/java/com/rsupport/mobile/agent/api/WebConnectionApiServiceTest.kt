package com.rsupport.mobile.agent.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import com.rsupport.mobile.agent.api.model.AgentLoginResult
import com.rsupport.mobile.agent.constant.Global
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.repo.device.DeviceRepository
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.rscommon.exception.RSException
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.java.KoinJavaComponent.inject
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WebConnectionApiServiceTest {

    private lateinit var webConnection: WebConnection
    private lateinit var deviceRepository: DeviceRepository
    private lateinit var configRepository: ConfigRepository
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        webConnection = Mockito.mock(WebConnection::class.java)
        deviceRepository = Mockito.mock(DeviceRepository::class.java)
        configRepository = Mockito.mock(ConfigRepository::class.java)
    }

    @After
    fun tearDown() {
        stopKoin()
    }


    // 로그인이 성공했을때 결과를 확인한다.
    @Test
    fun agentLoginSuccessResultTest() = runBlocking {
        val guid = "test_guid"
        Mockito.`when`(webConnection.agentLogin(any())).thenReturn(Result.success(AgentLoginResult()))
        val apiService = WebConnectionApiService(context, webConnection, deviceRepository, configRepository)
        val loginResult = apiService.agentLogin(guid)
        assertThat("로그인이 성공했지만 Rsult.Success 가 아니라서실패", loginResult, Matchers.instanceOf(Result.Success::class.java))
    }

    // 로그인이 실패했을때 결과를 확인한다.
    @Test
    fun agentLoginFailResultTest() = runBlocking {
        val guid = "test_guid"
        Mockito.`when`(webConnection.agentLogin(any())).thenReturn(Result.failure(java.lang.RuntimeException("test exception")))
        val apiService = WebConnectionApiService(context, webConnection, deviceRepository, configRepository)
        val loginResult = apiService.agentLogin(guid)
        assertThat("로그인이 성공했지만 Rsult.Failure 가 아니라서실패", loginResult, Matchers.instanceOf(Result.Failure::class.java))
    }

    // 로그인시도시 RSException 발생했을때 오류코드가 정상설정되는지 확인한다.
    @Test
    fun agentLoginFailRSExceptionErrorCodeTest() = runBlocking {
        val guid = "test_guid"
        val expectErrorCode = 3000

        Mockito.`when`(webConnection.agentLogin(any())).thenReturn(Result.failure(RSException(expectErrorCode)))

        val apiService = WebConnectionApiService(context, webConnection, deviceRepository, configRepository)
        val loginResult = apiService.agentLogin(guid)

        val errorCode = ((loginResult as? Result.Failure)?.throwable as? RSException)?.errorCode

        assertThat("로그인 시도시 오류코드가 달라서 실패", expectErrorCode, Matchers.`is`(errorCode))
    }

    // 로그인시도시 Exception 발생했을때 오류코드가 정상설정되는지 확인한다.
    @Test
    fun agentLoginFailExceptionErrorCodeTest() = runBlocking {
        val guid = "test_guid"
        val expectErrorCode = ErrorCode.UNKNOWN_ERROR

        Mockito.`when`(webConnection.agentLogin(any())).thenReturn(Result.failure(RuntimeException("not defined exception.")))

        val apiService = WebConnectionApiService(context, webConnection, deviceRepository, configRepository)
        val loginResult = apiService.agentLogin(guid)

        val errorCode = ((loginResult as? Result.Failure)?.throwable as? RSException)?.errorCode

        assertThat("로그인 시도시 오류코드가 달라서 실패", errorCode, Matchers.`is`(expectErrorCode))
    }


    @Ignore
    @Test
    fun v2ServerUpdateWhenConsoleLogin() = runBlocking<Unit> {

        GlobalStatic.loadResource(context)

        val configRepository: ConfigRepository by inject(ConfigRepository::class.java)
        configRepository.setProductType(GlobalStatic.PRODUCT_SERVER)
//        configRepository.setCustomServerURL("https://rview.com")
        configRepository.setCustomServerURL("https://rv-6014.rsup.io")

        whenever(deviceRepository.macAddress).thenReturn("48:60:5F:6C:CA:4D")
        whenever(deviceRepository.localIP).thenReturn("10.2.103.16")

        Global.getInstance().webConnection.setAESEnable(true)
        Global.getInstance().webConnection.setNetworkInfo()

        val apiService = WebConnectionApiService(context, Global.getInstance().webConnection, deviceRepository, configRepository)
        val consoleLoginResult = apiService.consoleLogin("", "kwcho", "111111")

        assertThat(consoleLoginResult, Matchers.instanceOf(Result.Success::class.java))
    }


}