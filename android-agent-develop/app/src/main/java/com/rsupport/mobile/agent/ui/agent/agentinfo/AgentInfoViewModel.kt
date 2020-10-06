package com.rsupport.mobile.agent.ui.agent.agentinfo

import android.app.Application
import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.*
import com.rsupport.mobile.agent.api.model.AgentInfo
import com.rsupport.mobile.agent.status.AgentStatus
import com.rsupport.mobile.agent.ui.base.BaseViewModel
import com.rsupport.mobile.agent.ui.base.ViewState
import com.rsupport.mobile.agent.utils.NetworkUtils
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class AgentInfoViewModel(application: Application) : BaseViewModel(application), LifecycleObserver {

    private val context by inject(Context::class.java)
    private val networkUtils by inject(NetworkUtils::class.java)

    /**
     * Agent 의 상태 정보를 관리한다.
     */
    private val agentInfoInteractor by inject(AgentInfoInteractor::class.java)

    /**
     * 베터리 최적화를 사용하면 packageName, 그렇지 않으면 null
     */
    val batteryOptimized: LiveData<String> = agentInfoInteractor.ignoreBatteryOptimized

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading.distinctUntilChanged()


    /**
     * 로그인 되어 있으면 true, 그렇지 않으면 false
     */
    val isLoggedIn: LiveData<Boolean> = Transformations.map(agentInfoInteractor.agentInfo) {
        return@map !(it.status == AgentStatus.AGENT_STATUS_LOGOUT.toInt() || it.status == AgentStatus.AGENT_STATUS_NOLOGIN.toInt())
    }

    val agentInfo: LiveData<AgentInfo> = agentInfoInteractor.agentInfo

    override fun onCleared() {
        super.onCleared()
        agentInfoInteractor.release()
    }

    /**
     * 로그인 상태일때 정보업데이트를 위해 로그인을한다.
     */
    fun updateLoginIfLoginStatus() {
        _isLoading.value = true
        viewModelScope.launch {
            if (agentInfoInteractor.isRemoting()) {
                agentInfoInteractor.updateAgentInfo()
            } else if (agentInfoInteractor.isLoggedIn()) {
                agentInfoInteractor.loginProcess().run {
                    if (this == AgentInfoInteractor.OK) {
                        viewStateChange(AgentInfoViewState.LoginCompleted)
                        if (agentInfoInteractor.isFirstLaunch()) {
                            viewStateChange(AgentInfoViewState.FirstLaunched)
                        }
                        agentInfoInteractor.launched()
                        if (!agentInfoInteractor.isEngineActivated()) {
                            _isLoading.value = false
                            viewStateChange(AgentInfoViewState.NeedEngineActivate)
                            return@launch
                        }
                    } else {
                        viewStateChange(AgentInfoViewState.LoginFail(this))
                    }
                }
            }
            _isLoading.value = false
        }
    }

    /**
     * 로그인 또는 로그 아웃
     */
    fun toggleLogin() {
        _isLoading.value = true
        viewModelScope.launch {
            if (TextUtils.isEmpty(agentInfoInteractor.agentInfo.value?.guid)) {
                viewStateChange(AgentInfoViewState.EmptyGuid)
            } else {
                if (!networkUtils.isAvailableNetwork()) {
                    viewStateChange(AgentInfoViewState.OffLineState)
                } else {
                    when (isLoggedIn.value) {
                        // 로그아웃 시도
                        true -> {
                            agentInfoInteractor.logoutProcess().run {
                                if (this == AgentInfoInteractor.OK) {
                                    viewStateChange(AgentInfoViewState.LogoutCompleted)
                                } else {
                                    viewStateChange(AgentInfoViewState.LogoutFail(this))
                                }
                            }
                        }
                        // 로그인 시도
                        else -> {
                            agentInfoInteractor.loginProcess().run {
                                if (this == AgentInfoInteractor.OK) {
                                    viewStateChange(AgentInfoViewState.LoginCompleted)
                                    if (agentInfoInteractor.isFirstLaunch()) {
                                        viewStateChange(AgentInfoViewState.FirstLaunched)
                                    }
                                    agentInfoInteractor.launched()
                                } else {
                                    viewStateChange(AgentInfoViewState.LoginFail(this))
                                }
                            }
                        }
                    }
                }
                _isLoading.value = false
            }
        }
    }
}

sealed class AgentInfoViewState : ViewState {
    /**
     * Agent 미 등록 상태
     */
    object EmptyGuid : AgentInfoViewState()

    /**
     * 로그인 완료 상태
     */
    object LoginCompleted : AgentInfoViewState()

    /**
     * 로그아웃 완료 상태
     */
    object LogoutCompleted : AgentInfoViewState()

    /**
     * 로그인 오류
     * @param errorCode 오류 코드 [AgentInfoInteractor.REMOVED_AGENT], [AgentInfoInteractor.EXPIRED_AGENT], [AgentInfoInteractor.UNREGISTER_AGENT], [AgentInfoInteractor.UPDATE_FORCED], [AgentInfoInteractor.UPDATED] 외 알수 없음.
     */
    data class LoginFail(val errorCode: Int) : AgentInfoViewState()

    /**
     * 로그아웃 실패
     * @param errorCode 서버 정의 오류 코드
     */
    data class LogoutFail(val errorCode: Int) : AgentInfoViewState()

    /**
     * network 상태가 off line 일때
     */
    object OffLineState : AgentInfoViewState()

    /**
     * 앱이 최초 실행되었다
     */
    object FirstLaunched : AgentInfoViewState()

    /**
     * Engine 이 활성화 되지 않아서 권한요청 화면으로 이동해야한다.
     */
    object NeedEngineActivate : AgentInfoViewState()
}