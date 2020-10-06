package com.rsupport.mqtt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.test.platform.app.InstrumentationRegistry
import com.nhaarman.mockitokotlin2.timeout
import com.rsupport.mobile.agent.modules.push.IPushMessaging
import com.rsupport.mobile.agent.modules.push.RSPushMessaging
import com.rsupport.mobile.agent.BuildConfig
import com.rsupport.mobile.agent.constant.AgentBasicInfo
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.repo.config.ProxyInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.java.KoinJavaComponent.inject
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MqttProxyTest {
    private val pushServerAddress = "stpush.rview.com"
    private val pushServerPort = 443

    private val configRepository by inject(ConfigRepository::class.java)

    @Mock
    private lateinit var connectedVerify: Runnable

    private val proxyIp = "172.25.231.34"
    private val proxyPort = "8080"
    private val proxyUserId = "rsup"
    private val proxyUserPwd = "test"

    @Before
    fun setup() {
        AgentBasicInfo.RV_AGENT_PUSH_SSL = true
        configRepository.setUseProxy(true)
        configRepository.setProxyInfo(
                ProxyInfo(
                        address = proxyIp,
                        port = proxyPort,
                        id = proxyUserId,
                        pwd = proxyUserPwd
                )
        )
    }

    @After
    fun tearDown() = runBlocking<Unit> {
        configRepository.delete()
    }

    private val context: Context
        get() {
            return InstrumentationRegistry.getInstrumentation().targetContext
        }

    @Test
    fun connectTest() = runBlocking<Unit> {
        RSPushMessaging.getInstance().apply {
            setServerInfo(pushServerAddress, pushServerPort)
            register("testTopic")
        }
        delay(3000)
    }

    @Ignore
    @Test
    fun proxyTest() = runBlocking<Unit> {
        registerConnectedReceiver()
        RSPushMessaging.getInstance().apply {
            setServerInfo(pushServerAddress, pushServerPort)
            register("testTopic")
        }
        Mockito.verify(connectedVerify, timeout(1000)).run()
    }

    private fun registerConnectedReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(IPushMessaging.ACTION_PUSH_MESSAGING)
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    if (it.action == IPushMessaging.ACTION_PUSH_MESSAGING &&
                            (intent.getIntExtra(IPushMessaging.EXTRA_KEY_TYPE, -1) == IPushMessaging.TYPE_CONNECTED
                                    || intent.getIntExtra(IPushMessaging.EXTRA_KEY_TYPE, -1) == IPushMessaging.TYPE_VERSION_CONNECTED)) {
                        connectedVerify.run()
                    }
                }
            }
        }, intentFilter)
    }
}