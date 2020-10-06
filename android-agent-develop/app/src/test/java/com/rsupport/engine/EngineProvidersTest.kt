package com.rsupport.engine

import android.content.Context
import base.BaseTest
import com.rsupport.knox.KnoxManagerCompat
import com.rsupport.knox.Updatable
import com.rsupport.mobile.agent.modules.engine.EngineFactory
import com.rsupport.mobile.agent.modules.engine.EngineProvider
import com.rsupport.mobile.agent.modules.engine.EngineProviders
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class EngineProvidersTest : BaseTest() {

    @Mock
    lateinit var factory: EngineFactory

    @Mock
    lateinit var knoxManagerCompat: KnoxManagerCompat

    @Mock
    lateinit var updatable: Updatable

    override fun createModules(): List<Module> {
        return listOf(
                module {
                    single { knoxManagerCompat }
                    factory(qualifier = named("knox")) { updatable }
                }
        )
    }

    @Before
    override fun setup() {
        super.setup()
    }

    // 기본 Provider 를 확인한다.
    @Test
    fun defaultProviderTest() = runBlocking<Unit> {
        val engineProvider = EngineProviders.of(context)
        MatcherAssert.assertThat("EngineProvider 가 아니어서 실패", engineProvider, Matchers.instanceOf(EngineProvider::class.java))
    }

    // 기본 EngineChecker 를 확인한다.
    @Test
    fun defaultCheckerTest() = runBlocking<Unit> {
        val engineChecker = EngineProviders.of(context).get()
        MatcherAssert.assertThat("EngineChecker 가 아니어서 실패", engineChecker, Matchers.instanceOf(EngineTypeCheck::class.java))
    }

    // Custom EngineCheck 가 생성 되었는지를 확인한다.
    @Test
    fun customCheckerTest() = runBlocking<Unit> {
        val mockEngineTypeChecker = Mockito.mock(EngineTypeCheck::class.java)
        Mockito.`when`(factory.create()).thenReturn(mockEngineTypeChecker)
        val engineChecker = EngineProviders.of(context, factory).get()
        MatcherAssert.assertThat("EngineChecker 가 아니어서 실패", engineChecker, Matchers.`is`(mockEngineTypeChecker))
    }
}