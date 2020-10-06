package com.rsupport.mobile.agent.modules.sysinfo

import android.content.Context
import com.rsupport.mobile.agent.modules.channel.screen.StreamController
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import com.rsupport.mobile.agent.modules.net.channel.DataChannelImpl
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppFactory
import com.rsupport.mobile.agent.modules.sysinfo.phone.PhoneMemory
import com.rsupport.mobile.agent.modules.sysinfo.phone.PhoneMemory.ProcessMemInfoFileFactory
import com.rsupport.mobile.agent.utils.SdkVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.java.KoinJavaComponent.inject
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class ProcessInfoConnectedTest {
    private val context by inject(Context::class.java)
    private val runningAppFactory by inject(RunningAppFactory::class.java)
    private val engineTypeCheck by inject(EngineTypeCheck::class.java)
    private val sdkVersion by inject(SdkVersion::class.java)

    @Before
    fun setup() {
        engineTypeCheck.checkEngineType()
    }

    // TODO ProcessInfo 리팩토링 필요
    @Test
    fun loadProcessCallTest() = runBlocking {
        val screenStreamController = Mockito.mock(StreamController::class.java)
        val dataChannel = withContext(Dispatchers.Main) {
            DataChannelImpl(context, screenStreamController)
        }

        val processInfo = ProcessInfo(context, dataChannel, runningAppFactory, PhoneMemory(context, ProcessMemInfoFileFactory()))
        processInfo.loadProcessItems()
        MatcherAssert.assertThat("데이터를 읽지 못해서 실패", processInfo.loadProcessItems(), Matchers.notNullValue())
    }

}