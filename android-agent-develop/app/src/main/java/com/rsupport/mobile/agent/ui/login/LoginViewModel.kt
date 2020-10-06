package com.rsupport.mobile.agent.ui.login

import android.app.Application
import android.text.TextUtils
import androidx.databinding.ObservableField
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.ui.agent.agentinfo.AgentInfoInteractor
import com.rsupport.mobile.agent.ui.base.BaseViewModel
import com.rsupport.mobile.agent.ui.base.ViewState
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.rscommon.exception.RSException
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class LoginViewModel(application: Application) : BaseViewModel(application) {
    val corpIdObservable = ObservableField<String>("")
    val userIdObservable = ObservableField<String>("")
    val userPwdObservable = ObservableField<String>("")

    private val agentInfoInteractor: AgentInfoInteractor by inject(AgentInfoInteractor::class.java)
    private val loginInteractor: LoginInteractor by inject(LoginInteractor::class.java)

    override fun onCleared() {
        agentInfoInteractor.release()
        loginInteractor.release()
        super.onCleared()
    }

    fun updateViewState() {
        viewModelScope.launch {
            if (!isShowTutorial()) {
                viewStateChange(MainViewState.ShowTutorialViewState)
            } else if (checkAgentInstalled()) {
                viewStateChange(MainViewState.StartAgentInfoViewState)
            }
        }
    }

    private fun checkAgentInstalled(): Boolean {
        return agentInfoInteractor.isAgentInstalled();
    }

    private fun isShowTutorial(): Boolean {
        return loginInteractor.isShowTutorial()
    }

    fun performLogin() {
        viewStateChange(MainViewState.HideSoftKeyboard)

        createLoginInfo()?.apply {
            viewStateChange(MainViewState.ShowProgress)
            viewModelScope.launch {
                when (val engineResult = loginInteractor.isSupportEngine()) {
                    is Result.Success -> consoleLogin(corpId, userId, userPwd)
                    is Result.Failure -> notSupportEngine(engineResult)
                }
            }
        }
    }

    private suspend fun notSupportEngine(engineResult: Result.Failure<SupportEngine>) {
        viewStateChange(MainViewState.HideProgress)
        if (engineResult.throwable is RSException) {
            when (engineResult.throwable.errorCode) {
                ErrorCode.ENGINE_KNOX_BLACK_LIST_WEB_854 -> viewStateChange(MainViewState.NeedDeviceUpdate)
                ErrorCode.ENGINE_KNOX_BLACK_LIST_WEB_855 -> viewStateChange(MainViewState.BlackListDevice)
                ErrorCode.ENGINE_KNOX_BLACK_LIST_WEB_111 -> viewStateChange(MainViewState.BlackListQueryError)
                ErrorCode.ENGINE_NOT_SUPPORTED -> findInstallableEngine()
                else -> viewStateChange(MainViewState.NotSupportDevice)
            }
        } else {
            viewStateChange(MainViewState.NotSupportDevice)
        }
    }

    private suspend fun findInstallableEngine() {
        viewStateChange(MainViewState.ShowProgress)
        loginInteractor.findInstallableEngine().let {
            if (TextUtils.isEmpty(it)) {
                viewStateChange(MainViewState.HideProgress)
                viewStateChange(MainViewState.NotSupportDevice)
            } else {
                viewStateChange(MainViewState.HideProgress)
                viewStateChange(MainViewState.RspermInstallRequest(it))
            }
        }
    }

    private suspend fun consoleLogin(corpId: String, id: String, pwd: String) {
        when (val consoleLoginState = loginInteractor.consoleLogin(corpId, id, pwd)) {
            is Result.Success -> consoleLoginSuccess(consoleLoginState)
            is Result.Failure -> consolLoginFail(consoleLoginState)
        }
    }

    private fun consolLoginFail(consoleLoginState: Result.Failure<ConsoleLoginState>) {
        viewStateChange(MainViewState.HideProgress)
        if (consoleLoginState.throwable is RSException) {
            when (consoleLoginState.throwable.errorCode) {
                ErrorCode.LOGIN_FORCE_UPDATE -> viewStateChange(MainViewState.AppForceUpdate)
                ErrorCode.LOGIN_UPGRADE_MEMBER -> viewStateChange(MainViewState.UpgradeMember)
                ErrorCode.LOGIN_NET_ERR_PROXY_VERIFY -> viewStateChange(MainViewState.ProxyVerify)
                else -> viewStateChange(MainViewState.LoginFailure(consoleLoginState.throwable))
            }
        } else {
            viewStateChange(MainViewState.LoginFailure(consoleLoginState.throwable))
        }
    }

    private fun consoleLoginSuccess(consoleLoginState: Result.Success<ConsoleLoginState>) {
        viewStateChange(MainViewState.HideProgress)
        when (consoleLoginState.value) {
            ConsoleLoginState.UpdateAvailable -> viewStateChange(MainViewState.UpdataAvailable)
            is ConsoleLoginState.PasswordExpireDay -> viewStateChange(MainViewState.PassExpireDay(consoleLoginState.value.expireDay, consoleLoginState.value.serverURL))
            else -> {
                viewStateChange(MainViewState.LoginSuccess)
            }
        }
    }

    private fun createLoginInfo(): LoginInfo? {
        if (!loginInteractor.checkServerURL()) {
            viewStateChange(MainViewState.InvalidServerURL)
            return null
        } else if (TextUtils.isEmpty(userIdObservable.get())) {
            viewStateChange(MainViewState.EmptyUserId)
            return null
        } else if (TextUtils.isEmpty(userPwdObservable.get())) {
            viewStateChange(MainViewState.EmptyUserPwd)
            return null
        }
        return LoginInfo(
                corpIdObservable.get() ?: "",
                userIdObservable.get()!!,
                userPwdObservable.get()!!
        )
    }

    private data class LoginInfo(val corpId: String, val userId: String, val userPwd: String)
}

sealed class MainViewState : ViewState {
    object ShowTutorialViewState : MainViewState()
    object StartAgentInfoViewState : MainViewState()
    object ShowProgress : MainViewState()
    object HideProgress : MainViewState()
    object HideSoftKeyboard : MainViewState()
    object InvalidServerURL : MainViewState()
    object EmptyUserId : MainViewState()
    object EmptyUserPwd : MainViewState()
    object LoginSuccess : MainViewState()
    object UpdataAvailable : MainViewState()

    object NotSupportDevice : MainViewState()
    object BlackListQueryError : MainViewState()
    object BlackListDevice : MainViewState()
    object NeedDeviceUpdate : MainViewState()
    object AppForceUpdate : MainViewState()
    object UpgradeMember : MainViewState()
    object ProxyVerify : MainViewState()
    data class LoginFailure(val exception: Throwable) : MainViewState()
    data class PassExpireDay(val day: String, val serverURL: String) : MainViewState()


    data class RspermInstallRequest(val packageName: String) : MainViewState()
}