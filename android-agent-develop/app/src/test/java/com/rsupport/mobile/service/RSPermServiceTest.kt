package com.rsupport.mobile.service

import base.BaseTest
import com.rsupport.litecam.binder.Binder
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.service.OnRspermBindListener
import com.rsupport.mobile.agent.service.RSPermService
import com.rsupport.rsperm.IRSPerm
import kotlinx.coroutines.*
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class RSPermServiceTest : BaseTest() {


    @Mock
    lateinit var rspermBinder: Binder

    @Mock
    lateinit var rsperm: IRSPerm

    private val bindResult = BindResult()

    override fun createModules(): List<Module> {
        return listOf(
                module {
                    factory {
                        rspermBinder
                    }
                    factory { RSPermService() }
                }
        )
    }

    // 1. Rsperm 이 설치되어 있지 않을때 Binding 실패한다.
    @Test
    fun rspermFailTest() = runBlocking<Unit> {
        Mockito.`when`(rspermBinder.bind()).thenReturn(Binder.RSPERM_BIND_FAIL)
        val rspermService by inject(RSPermService::class.java)
        rspermService.bindRsperm(bindResult)
        delay(100)
        MatcherAssert.assertThat("binding 이 성공해서 실패", bindResult.isSuccess(), Matchers.`is`(false))
    }

    // 2. Rsperm 이 설치되어 있을때 Binding 을 성공한다.
    @Test
    fun rspermBindTest() = runBlocking<Unit> {
        Mockito.`when`(rspermBinder.bind()).thenReturn(Binder.RSPERM_BIND_SUCCESS)
        Mockito.`when`(rspermBinder.binder).thenReturn(rsperm)
        val rspermService by inject(RSPermService::class.java)
        rspermService.bindRsperm(bindResult)
        delay(100)
        MatcherAssert.assertThat("binding 이 안되서 실패", bindResult.isSuccess(), Matchers.`is`(true))
    }

    // 3. Unbind 후에 bind 이 잘되었는지 확인한다.
    @Test
    fun rspermUnBindTest() = runBlocking<Unit> {
        Mockito.`when`(rspermBinder.bind()).thenReturn(Binder.RSPERM_BIND_SUCCESS)
        Mockito.`when`(rspermBinder.binder).thenReturn(rsperm)
        Mockito.`when`(rspermBinder.isBinderAlive).thenReturn(false)
        val rspermService by inject(RSPermService::class.java)
        rspermService.bindRsperm(bindResult)
        delay(100)
        MatcherAssert.assertThat("binding 이 안되서 실패", bindResult.isSuccess(), Matchers.`is`(true))


        rspermService.unbindRsperm()
        delay(100)
        MatcherAssert.assertThat("Rsperm 과 binding 해지가 안되서 실패", rspermService.isBind(), Matchers.`is`(false))
    }

    class BindResult : OnRspermBindListener {
        private var isSuccess: Boolean = false

        fun isSuccess(): Boolean {
            return isSuccess
        }

        override fun onResult(result: Result<IRSPerm>) {
            isSuccess = result.isSuccess
        }
    }
}


