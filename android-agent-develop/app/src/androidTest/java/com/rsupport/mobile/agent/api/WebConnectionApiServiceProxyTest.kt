package com.rsupport.mobile.agent.api

import androidx.test.platform.app.InstrumentationRegistry
import com.rsupport.mobile.agent.constant.Global
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.repo.config.ProxyInfo
import com.rsupport.mobile.agent.repo.device.DeviceRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.java.KoinJavaComponent

class WebConnectionApiServiceProxyTest{


    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val deviceRepository by KoinJavaComponent.inject(DeviceRepository::class.java)
    private val configRepository by KoinJavaComponent.inject(ConfigRepository::class.java)

    private val groupID = "kwcho"
    private val userID = "kwcho"
    private val userPwd = "111111"

    @Before
    fun setup() {
        configRepository.setCustomServerURL("https://stap.rview.com")
        configRepository.setUseProxy(true)
        configRepository.setProxyInfo(ProxyInfo("172.25.231.34", "8080", "rsup", "test"))
    }

    @Test
    fun resultAgentList() = runBlocking<Unit>{

        val apiService = WebConnectionApiService(context, Global.getInstance().webConnection, deviceRepository, configRepository)

        val consoleLoginResult = apiService.consoleLogin(groupID, userID, userPwd)
        assertTrue(consoleLoginResult.isSuccess)
    }


}