package com.rsupport.engine

import android.content.Context
import com.rsupport.knox.KnoxManagerCompat
import com.rsupport.mobile.agent.modules.engine.EngineFactory
import com.rsupport.mobile.agent.modules.engine.EngineProviders
import com.rsupport.mobile.agent.modules.engine.EngineType
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import com.rsupport.sony.SonyManager
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class EngineTypeCheckerTest {


    @Mock
    lateinit var context: Context

    @Mock
    lateinit var knoxManager: KnoxManagerCompat

    @Mock
    lateinit var sonyManager: SonyManager

    lateinit var factory: EngineFactory

    lateinit var engineTypeCheck: EngineTypeCheck

    @Before
    fun setup() {
        factory = object : EngineFactory {
            override fun create(): EngineTypeCheck {
                return EngineTypeCheck(context, knoxManager, sonyManager)
            }
        }
        engineTypeCheck = EngineProviders.of(context, factory).get()
    }

    // Knox를 지원할때 engine type 을 확인한다.
    @Test
    fun engineKnoxTest() {
        Mockito.`when`(knoxManager.isKnoxAvailable(context)).thenReturn(KnoxManagerCompat.KNOX_SUPPORTED_DEVICE)
        engineTypeCheck.checkEngineType()
        MatcherAssert.assertThat("knox 가 아니라서 실패", engineTypeCheck.getEngineType(), Matchers.`is`(EngineType.ENGINE_TYPE_KNOX))
    }

    // Knox 버전이 낮을때 engine type 을 확인한다.
    @Test
    fun engineKnoxLowerTest() {
        Mockito.`when`(sonyManager.isServiceAvailable(context)).thenReturn(false)
        Mockito.`when`(knoxManager.isKnoxAvailable(context)).thenReturn(KnoxManagerCompat.KNOX_LOW_VERSION_DEVICE)
        engineTypeCheck.checkEngineType()
        MatcherAssert.assertThat("knox라서 실패", engineTypeCheck.getEngineType(), Matchers.not(EngineType.ENGINE_TYPE_KNOX))
    }

    // Knox 를 지원하지 않을때 engine type 을 확인한다.
    @Test
    fun engineKnoxNotSupportTest() {
        Mockito.`when`(sonyManager.isServiceAvailable(context)).thenReturn(false)
        Mockito.`when`(knoxManager.isKnoxAvailable(context)).thenReturn(KnoxManagerCompat.KNOX_NOT_SUPPORT)
        engineTypeCheck.checkEngineType()
        MatcherAssert.assertThat("knox라서 실패", engineTypeCheck.getEngineType(), Matchers.not(EngineType.ENGINE_TYPE_KNOX))
    }

    // Rsperm 인지를 확인한다.
    @Test
    fun engineRspermTest() {
        Mockito.`when`(knoxManager.isKnoxAvailable(context)).thenReturn(KnoxManagerCompat.KNOX_NOT_SUPPORT)
        Mockito.`when`(sonyManager.isServiceAvailable(context)).thenReturn(false)

        engineTypeCheck.checkEngineType()
        MatcherAssert.assertThat("Rsperm이 아니라서 실패", engineTypeCheck.getEngineType(), Matchers.`is`(EngineType.ENGINE_TYPE_RSPERM))
    }

    // knox 버전이 낮을때 Rsperm 인지를 확인한다.
    @Test
    fun engineRspermWhenKnoxLowerTest() {
        Mockito.`when`(knoxManager.isKnoxAvailable(context)).thenReturn(KnoxManagerCompat.KNOX_LOW_VERSION_DEVICE)
        Mockito.`when`(sonyManager.isServiceAvailable(context)).thenReturn(false)

        engineTypeCheck.checkEngineType()
        MatcherAssert.assertThat("Rsperm이 아니라서 실패", engineTypeCheck.getEngineType(), Matchers.`is`(EngineType.ENGINE_TYPE_RSPERM))
    }


    // Sony 인지를 확인한다.
    @Test
    fun engineSonyTest() {
        Mockito.`when`(knoxManager.isKnoxAvailable(context)).thenReturn(KnoxManagerCompat.KNOX_NOT_SUPPORT)
        Mockito.`when`(sonyManager.isServiceAvailable(context)).thenReturn(true)

        engineTypeCheck.checkEngineType()
        MatcherAssert.assertThat("Sony perm이 아니라서 실패", engineTypeCheck.getEngineType(), Matchers.`is`(EngineType.ENGINE_TYPE_SONY))
    }


    // Knox 버전이 낮을때 Sony 인지를 확인한다.
    @Test
    fun engineSonyWhenKnoxLowerTest() {
        Mockito.`when`(knoxManager.isKnoxAvailable(context)).thenReturn(KnoxManagerCompat.KNOX_LOW_VERSION_DEVICE)
        Mockito.`when`(sonyManager.isServiceAvailable(context)).thenReturn(true)

        engineTypeCheck.checkEngineType()
        MatcherAssert.assertThat("Sony perm이 아니라서 실패", engineTypeCheck.getEngineType(), Matchers.`is`(EngineType.ENGINE_TYPE_SONY))
    }
}