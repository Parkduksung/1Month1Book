package com.rsupport.mobile.agent.api

import androidx.test.platform.app.InstrumentationRegistry
import com.rsupport.mobile.agent.constant.Global
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.repo.device.DeviceRepository
import com.rsupport.mobile.agent.utils.Result
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.junit.Assert.assertThat
import org.junit.Ignore
import org.junit.Test
import org.koin.java.KoinJavaComponent.inject

class WebConnectionApiServiceConnectedTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val deviceRepository by inject(DeviceRepository::class.java)
    private val configRepository by inject(ConfigRepository::class.java)

    private val userID = "kwcho"
    private val userPwd = "a111111"

    @Ignore
    @Test
    fun resultAgentList() = runBlocking<Unit> {

        configRepository.setCustomServerURL("https://rv-6014.rsup.io")

        val apiService = WebConnectionApiService(context, Global.getInstance().webConnection, deviceRepository, configRepository)

        val consoleLoginResult = apiService.consoleLogin(null, userID, userPwd)
        assertThat(consoleLoginResult, Matchers.instanceOf(Result.Success::class.java))
    }
}