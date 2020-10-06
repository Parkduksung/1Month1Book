package com.rsupport.mobile.agent.modules.channel.screen.factory

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.rsupport.media.stream.*
import com.rsupport.mobile.agent.TestApplication
import com.rsupport.mobile.agent.modules.channel.screen.ScreenStreamFactory
import com.rsupport.mobile.agent.modules.engine.EngineType
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import com.rsupport.mobile.agent.service.RSPermService
import com.rsupport.mobile.agent.utils.SdkVersion
import com.rsupport.rsperm.IRSPerm
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.hamcrest.Matchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(
        application = TestApplication::class
)
@RunWith(RobolectricTestRunner::class)
class ScreenStreamFactoryImplTest {
    private val context: Context = ApplicationProvider.getApplicationContext<Context>()
    private val engineTypeCheck: EngineTypeCheck = mock()
    private val rspermService: RSPermService = mock()
    private val rsperm: IRSPerm = mock()
    private val sdkVersion: SdkVersion = mock()

    @Test
    fun can_create_stream_when_rsperm_null_over_lollipop() = runBlocking<Unit> {
        given(engineTypeCheck.getEngineType()).willAnswer { EngineType.ENGINE_TYPE_RSPERM }
        given(rspermService.getRsperm()).willAnswer { null }
        given(sdkVersion.greaterThan21()).willAnswer { true }

        val screenStreamFactory: ScreenStreamFactory = ScreenStreamFactoryImpl(context, engineTypeCheck, rspermService, sdkVersion)
        val screenStream = screenStreamFactory.create()

        assertThat(screenStream, Matchers.instanceOf(RsMediaProjectionStreamVD::class.java))
    }

    @Test
    fun can_create_stream_when_rsperm_unbound_over_lollipop() = runBlocking<Unit> {
        given(engineTypeCheck.getEngineType()).willAnswer { EngineType.ENGINE_TYPE_RSPERM }
        given(rsperm.isBinded).willAnswer { false }
        given(rspermService.getRsperm()).willAnswer { rsperm }
        given(sdkVersion.greaterThan21()).willAnswer { true }

        val screenStreamFactory: ScreenStreamFactory = ScreenStreamFactoryImpl(context, engineTypeCheck, rspermService, sdkVersion)
        val screenStream = screenStreamFactory.create()

        assertThat(screenStream, Matchers.instanceOf(RsMediaProjectionStreamVD::class.java))
    }

    @Test
    fun can_create_stream_when_rsperm_bound_over_lollipop() = runBlocking<Unit> {
        given(engineTypeCheck.getEngineType()).willAnswer { EngineType.ENGINE_TYPE_RSPERM }
        given(rsperm.isBinded).willAnswer { true }
        given(rspermService.getRsperm()).willAnswer { rsperm }
        given(sdkVersion.greaterThan21()).willAnswer { true }

        val screenStreamFactory: ScreenStreamFactory = ScreenStreamFactoryImpl(context, engineTypeCheck, rspermService, sdkVersion)
        val screenStream = screenStreamFactory.create()

        assertThat(screenStream, Matchers.instanceOf(RsMediaProjectionStream::class.java))
    }

    @Test
    fun can_create_stream_when_rsperm_bound_under_lollipop() = runBlocking<Unit> {
        given(engineTypeCheck.getEngineType()).willAnswer { EngineType.ENGINE_TYPE_RSPERM }
        given(rsperm.isBinded).willAnswer { true }
        given(rspermService.getRsperm()).willAnswer { rsperm }
        given(sdkVersion.greaterThan21()).willAnswer { false }

        val screenStreamFactory: ScreenStreamFactory = ScreenStreamFactoryImpl(context, engineTypeCheck, rspermService, sdkVersion)
        val screenStream = screenStreamFactory.create()

        assertThat(screenStream, Matchers.instanceOf(RsScreenCaptureStream::class.java))
    }

    @Test
    fun can_create_stream_when_rsperm_unbound_under_lollipop() = runBlocking<Unit> {
        given(engineTypeCheck.getEngineType()).willAnswer { EngineType.ENGINE_TYPE_RSPERM }
        given(rsperm.isBinded).willAnswer { false }
        given(rspermService.getRsperm()).willAnswer { rsperm }
        given(sdkVersion.greaterThan21()).willAnswer { false }

        val screenStreamFactory: ScreenStreamFactory = ScreenStreamFactoryImpl(context, engineTypeCheck, rspermService, sdkVersion)
        val screenStream = screenStreamFactory.create()

        assertThat(screenStream, Matchers.nullValue())
    }


    @Test
    fun can_create_stream_when_knox_over_lollipop() = runBlocking<Unit> {
        given(engineTypeCheck.getEngineType()).willAnswer { EngineType.ENGINE_TYPE_KNOX }
        given(sdkVersion.greaterThan21()).willAnswer { true }

        val screenStreamFactory: ScreenStreamFactory = ScreenStreamFactoryImpl(context, engineTypeCheck, rspermService, sdkVersion)
        val screenStream = screenStreamFactory.create()

        assertThat(screenStream, Matchers.instanceOf(RsMediaProjectionStreamVD::class.java))
    }

    @Test
    fun can_create_stream_when_knox_under_lollipop() = runBlocking<Unit> {
        given(engineTypeCheck.getEngineType()).willAnswer { EngineType.ENGINE_TYPE_KNOX }
        given(sdkVersion.greaterThan21()).willAnswer { false }

        val screenStreamFactory: ScreenStreamFactory = ScreenStreamFactoryImpl(context, engineTypeCheck, rspermService, sdkVersion)
        val screenStream = screenStreamFactory.create()

        assertThat(screenStream, Matchers.instanceOf(RsKNOXStream::class.java))
    }

    @Test
    fun can_create_stream_when_sony_over_lollipop() = runBlocking<Unit> {
        given(engineTypeCheck.getEngineType()).willAnswer { EngineType.ENGINE_TYPE_SONY }
        given(sdkVersion.greaterThan21()).willAnswer { true }

        val screenStreamFactory: ScreenStreamFactory = ScreenStreamFactoryImpl(context, engineTypeCheck, rspermService, sdkVersion)
        val screenStream = screenStreamFactory.create()

        assertThat(screenStream, Matchers.instanceOf(RsMediaProjectionStreamVD::class.java))
    }

    @Test
    fun can_create_stream_when_sony_under_lollipop() = runBlocking<Unit> {
        given(engineTypeCheck.getEngineType()).willAnswer { EngineType.ENGINE_TYPE_SONY }
        given(sdkVersion.greaterThan21()).willAnswer { false }

        val screenStreamFactory: ScreenStreamFactory = ScreenStreamFactoryImpl(context, engineTypeCheck, rspermService, sdkVersion)
        val screenStream = screenStreamFactory.create()

        assertThat(screenStream, Matchers.instanceOf(RsSonyStream::class.java))
    }

    @Test
    fun can_create_stream_when_not_support_engine_type_over_lollipop() = runBlocking<Unit> {
        given(engineTypeCheck.getEngineType()).willAnswer { -1 }
        given(sdkVersion.greaterThan21()).willAnswer { true }

        val screenStreamFactory: ScreenStreamFactory = ScreenStreamFactoryImpl(context, engineTypeCheck, rspermService, sdkVersion)
        val screenStream = screenStreamFactory.create()

        assertThat(screenStream, Matchers.instanceOf(RsMediaProjectionStreamVD::class.java))
    }

    @Test
    fun can_create_stream_when_not_support_engine_type_under_lollipop() = runBlocking<Unit> {
        given(engineTypeCheck.getEngineType()).willAnswer { -1 }
        given(sdkVersion.greaterThan21()).willAnswer { false }

        val screenStreamFactory: ScreenStreamFactory = ScreenStreamFactoryImpl(context, engineTypeCheck, rspermService, sdkVersion)
        val screenStream = screenStreamFactory.create()

        assertThat(screenStream, nullValue())
    }
}

