package com.rsupport.mobile.agent.ui.agent

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.*
import com.rsupport.knox.KnoxParam
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import com.rsupport.mobile.agent.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent

class EngineActivationViewModel(app: Application) : BaseViewModel(app), LifecycleObserver {
    private val ACTIVATE_STATE_INIT = 0
    private val ACTIVATE_STATE_REQUEST = 1
    private val ACTIVATE_STATE_FALURE = 2
    private val ACTIVATE_STATE_SUCCESS = 3

    private val engineTypeCheck by KoinJavaComponent.inject(EngineTypeCheck::class.java)

    private val activateState: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>(ACTIVATE_STATE_INIT)
    }

    val isLoading: LiveData<Boolean>
        get() = Transformations.map(activateState) {
            it == ACTIVATE_STATE_REQUEST
        }.distinctUntilChanged()

    val isActivated: LiveData<Boolean>
        get() = Transformations.map(activateState) {
            it == ACTIVATE_STATE_SUCCESS
        }.distinctUntilChanged()

    fun activateAdmin(activity: Activity) {
        activateState.value = ACTIVATE_STATE_REQUEST
        viewModelScope.launch(Dispatchers.IO) {
            engineTypeCheck.checkEngineType()
            // 디바이스 관리자 권한 호출
            engineTypeCheck.activateEngineState(activity) { isActivateAdmin ->
                // onActivityResult 에서 처리된다....
                // knox 취소가 동작하지 않음.
                if (!isActivateAdmin) {
                    viewModelScope.launch { activateState.value = ACTIVATE_STATE_FALURE }
                } else {
                    viewModelScope.launch { activateState.value = ACTIVATE_STATE_SUCCESS }
                }
            }
        }
    }

    fun activate(activity: Activity) {
        activateState.value = ACTIVATE_STATE_REQUEST
        viewModelScope.launch(Dispatchers.IO) {
            engineTypeCheck.checkEngineType()

            engineTypeCheck.activateEngineState { isActivateLicense ->
                if (!isActivateLicense) {
                    viewModelScope.launch { activateState.value = ACTIVATE_STATE_FALURE }
                } else {
                    // 디바이스 관리자 권한 호출
                    engineTypeCheck.activateEngineState(activity) { isActivateAdmin ->
                        // onActivityResult 에서 처리된다....
                        // knox 취소가 동작하지 않음.
                        if (!isActivateAdmin) {
                            viewModelScope.launch { activateState.value = ACTIVATE_STATE_FALURE }
                        } else {
                            viewModelScope.launch { activateState.value = ACTIVATE_STATE_SUCCESS }
                        }
                    }
                }
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == KnoxParam.DEVICE_ADMIN_ADD_RESULT_ENABLE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                activateState.value = ACTIVATE_STATE_FALURE
            } else if (resultCode == Activity.RESULT_OK) {
                activateState.value = ACTIVATE_STATE_SUCCESS
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        if (activateState.value == ACTIVATE_STATE_REQUEST) {
            activateState.value = null
            activateState.value = ACTIVATE_STATE_REQUEST
        }
    }

}