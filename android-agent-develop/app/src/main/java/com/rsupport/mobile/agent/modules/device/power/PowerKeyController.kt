package com.rsupport.mobile.agent.modules.device.power

import android.os.Build
import android.view.KeyEvent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.distinctUntilChanged
import com.rsupport.mobile.agent.modules.device.inject.EventDispatcher
import com.rsupport.mobile.agent.modules.device.inject.KeyPadEvent
import com.rsupport.mobile.agent.utils.ScreenUtils
import kotlinx.coroutines.*
import org.koin.java.KoinJavaComponent.get

/**
 * Power KeyEvent를 dispatch 하여 폰의 Screen OFF 상태에서 Screen ON 으로 하거나 Screen ON 상태에서 Screen OFF 상태로 변화 시킬수 있다.
 * Screen ON 상태에서 [on] 을 연속으로 호출하여도 Screen ON 상태를 유지하며
 * Screen OFF 상태에서 [off]를 연속으로 호출하여도 Screen OFF 상태를 유지 시킬 수 있다.
 */
class PowerKeyController(private val eventDispatcherFactory: EventDispatcher.Factory) : Observer<PowerKeyEvent> {

    private val _powerKeyLiveData = MutableLiveData<PowerKeyEvent>()
    private val powerKeyLiveData = _powerKeyLiveData.distinctUntilChanged()
    private val screenUtils: ScreenUtils = get(ScreenUtils::class.java)
    private val uiScope = MainScope()
    private val workerScope = CoroutineScope(Dispatchers.IO)

    private val screenStateObserver = Observer<Boolean> { isOn ->
        isOn?.let {
            when (it) {
                true -> {
                    if (_powerKeyLiveData.value is PowerOffKeyEvent) {
                        _powerKeyLiveData.value = null
                    }
                }
                false -> {
                    if (_powerKeyLiveData.value is PowerOnKeyEvent) {
                        _powerKeyLiveData.value = null
                    }
                }
            }
        }
    }

    init {
        uiScope.launch {
            powerKeyLiveData.observeForever(this@PowerKeyController)
            screenUtils.isScreenState.observeForever(screenStateObserver)
        }
    }

    fun on() {
        uiScope.launch {
            if (screenUtils.isOn()) {
                return@launch
            }
            _powerKeyLiveData.value = PowerOnKeyEvent()
        }
    }

    fun off() {
        uiScope.launch {
            if (!screenUtils.isOn()) return@launch
            _powerKeyLiveData.value = PowerOffKeyEvent()
        }
    }

    override fun onChanged(powerOnKeyEvent: PowerKeyEvent?) {
        powerOnKeyEvent?.let {

            workerScope.launch {
                val eventDispatcher = eventDispatcherFactory.create()

                it.events.forEach { keyPadEvent ->
                    if (keyPadEvent is KeyPadEvent.Events) {
                        eventDispatcher.dispatch(event = keyPadEvent)
                        // AKA 폰에서 Down event 후 다시 Up Event 를 dispatch 하면 전원이 켜진후 다시 꺼진다.
                        // Down 이벤트후 화면이 켜졌으면 Up Event 를 dispatch 하지 않도록한다.
                        if (Build.MODEL?.toUpperCase()?.contains("LG-F520") == true) {
                            delay(200)
                            if (powerOnKeyEvent is PowerOnKeyEvent) {
                                val isScreenOn = withContext(Dispatchers.Main) {
                                    screenUtils.isOn()
                                }
                                if (isScreenOn) return@launch
                            }
                        }
                    }
                }
            }
        }
    }

    fun release() {
        uiScope.launch {
            powerKeyLiveData.removeObserver(this@PowerKeyController)
            screenUtils.isScreenState.removeObserver(screenStateObserver)
            screenUtils.release()
        }
        workerScope.cancel()
    }
}

interface PowerKeyEvent {
    val events: List<KeyPadEvent>
}


data class PowerOffKeyEvent(override val events: List<KeyPadEvent> = listOf(
        KeyPadEvent.from(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_POWER),
        KeyPadEvent.from(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_POWER)
)) : PowerKeyEvent

data class PowerOnKeyEvent(override val events: List<KeyPadEvent> = listOf(
        KeyPadEvent.from(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_POWER),
        KeyPadEvent.from(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_POWER)
)) : PowerKeyEvent