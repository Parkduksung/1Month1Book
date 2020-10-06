package com.rsupport.mobile.agent.modules.device.inject

import base.BaseTest
import com.nhaarman.mockitokotlin2.any
import com.rsupport.knox.KnoxManagerCompat
import com.rsupport.mobile.agent.modules.engine.EngineType
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import com.rsupport.mobile.agent.service.RSPermService
import com.rsupport.rsperm.IRSPerm
import com.rsupport.sony.SonyManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class EventDispatcherTest : BaseTest() {

    @Mock
    lateinit var engineTypeCheck: EngineTypeCheck

    @Mock
    lateinit var rspermService: RSPermService

    @Mock
    lateinit var rsperm: IRSPerm

    @Mock
    lateinit var sonyManager: SonyManager

    @Mock
    lateinit var knoxManagerCompat: KnoxManagerCompat

    private val eventDispatcherFactory by inject(EventDispatcher.Factory::class.java)
    private lateinit var eventDispatcher: EventDispatcher<KeyPadEvent.Events>

    private val keyPadEvent = KeyPadEvent.from(ByteArray(5).apply {
        set(0, 1)
    }) as KeyPadEvent.Events

    override fun createModules(): List<Module> {
        return listOf(
                module(override = true) {
                    single { rspermService }
                    single { knoxManagerCompat }
                    single<EventDispatcher.Factory> { EngineEventDispatcherFactory(engineTypeCheck, sonyManager) }
                }
        )
    }

    // EngineType에 따라서 RSPermEventDispatcher 를 생성한다.
    @Test
    fun createRspermEventDispatcherTest() = runBlocking {
        Mockito.`when`(engineTypeCheck.getEngineType()).thenReturn(EngineType.ENGINE_TYPE_RSPERM)

        eventDispatcher = eventDispatcherFactory.create()

        MatcherAssert.assertThat("RSPermEventDispatcher 가 생성되지 않아서 실패 ", eventDispatcher, Matchers.instanceOf(RSPermEventDispatcher::class.java))
    }

    // EngineType에 따라서 KnoxEventDispatcher 를 생성한다.
    @Test
    fun createKnoxEventDispatcherTest() = runBlocking {
        Mockito.`when`(engineTypeCheck.getEngineType()).thenReturn(EngineType.ENGINE_TYPE_KNOX)

        eventDispatcher = eventDispatcherFactory.create()

        MatcherAssert.assertThat("KnoxEventDispatcher 가 생성되지 않아서 실패 ", eventDispatcher, Matchers.instanceOf(KnoxEventDispatcher::class.java))
    }

    // EngineType에 따라서 SonyEventDispatcher 를 생성한다.
    @Test
    fun createSonyEventDispatcherTest() = runBlocking {
        Mockito.`when`(engineTypeCheck.getEngineType()).thenReturn(EngineType.ENGINE_TYPE_SONY)

        eventDispatcher = eventDispatcherFactory.create()

        MatcherAssert.assertThat("SonyEventDispatcher 가 생성되지 않아서 실패 ", eventDispatcher, Matchers.instanceOf(SonyEventDispatcher::class.java))
    }


    // RSPermEventDispatcher에 event 를 전달하고 정상 동작하는지 확인한다.
    @Test
    fun rspermEventDispatchTest() = runBlocking<Unit> {
        Mockito.`when`(engineTypeCheck.getEngineType()).thenReturn(EngineType.ENGINE_TYPE_RSPERM)
        Mockito.`when`(rspermService.isBind()).thenReturn(true)
        Mockito.`when`(rspermService.getRsperm()).thenReturn(rsperm)

        eventDispatcher = eventDispatcherFactory.create()

        val eventResult = eventDispatcher.dispatch(keyPadEvent)
        MatcherAssert.assertThat("diapatch가 안되서 실패", eventResult, Matchers.`is`(true))
    }


    // RSPermEventDispatcher에 event 를 engine 이 binding 안되어이을때 확인한다.
    @Test
    fun rspermEventDispatchFailTest() = runBlocking<Unit> {
        Mockito.`when`(engineTypeCheck.getEngineType()).thenReturn(EngineType.ENGINE_TYPE_RSPERM)
        Mockito.`when`(rspermService.isBind()).thenReturn(false)

        eventDispatcher = eventDispatcherFactory.create()

        val eventResult = eventDispatcher.dispatch(keyPadEvent)
        MatcherAssert.assertThat("diapatch 가 성공해서 실패", eventResult, Matchers.`is`(false))
    }


    // KnoxEventDispatcher event 를 전달하고 정상 동작하는지 확인한다.
    @Test
    fun knoxEventDispatchTest() = runBlocking<Unit> {
        Mockito.`when`(engineTypeCheck.getEngineType()).thenReturn(EngineType.ENGINE_TYPE_KNOX)

        eventDispatcher = eventDispatcherFactory.create()

        val eventResult = eventDispatcher.dispatch(keyPadEvent)
        MatcherAssert.assertThat("diapatch가 안되서 실패", eventResult, Matchers.`is`(true))
    }


    // SonyEventDispatcher event 를 전달하고 정상 동작하는지 확인한다.
    @Test
    fun sonyEventDispatchTest() = runBlocking<Unit> {
        Mockito.`when`(engineTypeCheck.getEngineType()).thenReturn(EngineType.ENGINE_TYPE_SONY)
        Mockito.`when`(sonyManager.bindGet(any(), any())).thenReturn(true)

        eventDispatcher = eventDispatcherFactory.create()

        val eventResult = eventDispatcher.dispatch(keyPadEvent)

        delay(100)
        MatcherAssert.assertThat("diapatch가 안되서 실패", eventResult, Matchers.`is`(true))
    }
}