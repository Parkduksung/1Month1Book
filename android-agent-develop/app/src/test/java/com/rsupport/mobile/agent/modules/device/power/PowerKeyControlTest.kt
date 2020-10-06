package com.rsupport.mobile.agent.modules.device.power

import androidx.lifecycle.MutableLiveData
import base.BaseTest
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.whenever
import com.rsupport.mobile.agent.modules.device.inject.EventDispatcher
import com.rsupport.mobile.agent.modules.device.inject.KeyPadEvent
import com.rsupport.mobile.agent.utils.ScreenUtils
import kotlinx.coroutines.runBlocking
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
class PowerKeyControlTest : BaseTest() {

    @Mock
    lateinit var eventDispatcherFactory: EventDispatcher.Factory

    @Mock
    lateinit var eventDispatcher: EventDispatcher<KeyPadEvent.Events>


    @Mock
    lateinit var screenUtils: ScreenUtils


    private val powerKeyController by inject(PowerKeyController::class.java)

    override fun createModules(): List<Module> {
        return listOf(
                module {
                    single { screenUtils }
                    single { eventDispatcherFactory }
                    factory { PowerKeyController(get()) }
                }
        )
    }

    @Before
    override fun setup() {
        super.setup()

        Mockito.`when`(eventDispatcherFactory.create()).thenReturn(eventDispatcher)
    }

    // 같은 powerOn 키가 들어왔을때 한가지 키만 처리하는지를 확인한다.
    @Test
    fun repeatPowerOnKeyTest() = runBlocking<Unit> {
        Mockito.`when`(screenUtils.isOn()).thenReturn(false)
        val screenLiveData = MutableLiveData<Boolean>(false)
        Mockito.`when`(screenUtils.isScreenState).thenReturn(screenLiveData)

        powerKeyController.on()
        powerKeyController.on()
        powerKeyController.on()
        powerKeyController.on()

        powerKeyController.release()

        Mockito.verify(eventDispatcher, times(2)).dispatch(any())
    }

    // 같은 powerOff 키가 들어왔을때 한가지 키만 처리하는지를 확인한다.
    @Test
    fun repeatPowerOffKeyTest() = runBlocking<Unit> {
        Mockito.`when`(screenUtils.isOn()).thenReturn(true, false)
        val screenLiveData = MutableLiveData<Boolean>(true)
        Mockito.`when`(screenUtils.isScreenState).thenReturn(screenLiveData)


        powerKeyController.off()
        powerKeyController.off()
        powerKeyController.off()
        powerKeyController.off()

        powerKeyController.release()

        Mockito.verify(eventDispatcher, times(2)).dispatch(any())
    }

    // 화면을 OFF 상태에서 ON 으로 변경할때 변경 동작이 느릴경우 ON 이 한번만 동작하는지를 확인한다.
    @Test
    fun slowOnTest() = runBlocking<Unit> {
        Mockito.`when`(screenUtils.isOn()).thenReturn(false)

        val screenLiveData = MutableLiveData<Boolean>(false)
        Mockito.`when`(screenUtils.isScreenState).thenReturn(screenLiveData)

        powerKeyController.on()
        powerKeyController.on()

        screenLiveData.value = true

        powerKeyController.on()
        powerKeyController.on()

        Mockito.verify(eventDispatcher, times(2)).dispatch(any())
    }
}
