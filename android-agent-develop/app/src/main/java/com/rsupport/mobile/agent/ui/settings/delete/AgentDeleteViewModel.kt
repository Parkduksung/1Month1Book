package com.rsupport.mobile.agent.ui.settings.delete

import android.app.Application
import androidx.databinding.ObservableField
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.ui.base.BaseViewModel
import com.rsupport.mobile.agent.ui.base.ViewState
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.rscommon.exception.RSException
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class AgentDeleteViewModel(app: Application) : BaseViewModel(app) {
    val userId = ObservableField<String>("")
    val userPwd = ObservableField<String>("")

    private val agentDeleteInteractor by inject(AgentDeleteInteractor::class.java)

    fun deleteAgent() {
        viewStateChange(AgentDeleteViewState.ShowProgress)
        viewModelScope.launch {
            try {
                val userId = requireNotNull(userId.get()).requireNotEmpty()
                val userPwd = requireNotNull(userPwd.get()).requireNotEmpty()

                when (val deleteResult = agentDeleteInteractor.deleteAgent(userId, userPwd)) {
                    is Result.Success -> {
                        viewStateChange(AgentDeleteViewState.HideProgress)
                        viewStateChange(AgentDeleteViewState.DeletedSuccess)
                    }
                    is Result.Failure -> {
                        viewStateChange(AgentDeleteViewState.HideProgress)
                        if (deleteResult.throwable is RSException) {
                            when (deleteResult.throwable.errorCode) {
                                ErrorCode.SETTING_NET_ERR_PROXY_VERIFY -> viewStateChange(AgentDeleteViewState.InvalidProxy)
                                ErrorCode.SETTING_INVALID_ACCOUNT_OR_PWD -> viewStateChange(AgentDeleteViewState.InvalidAccount)
                                ErrorCode.SETTING_ALREADY_DELETE_AGENT -> {
                                    viewStateChange(AgentDeleteViewState.AlreadyDeletedAgent)
                                }
                                else -> viewStateChange(AgentDeleteViewState.NotDefinedError(deleteResult.throwable.errorCode))
                            }
                        } else {
                            viewStateChange(AgentDeleteViewState.NotDefinedError(ErrorCode.UNKNOWN_ERROR))
                        }
                    }
                }
            } catch (e: IllegalArgumentException) {
                viewStateChange(AgentDeleteViewState.HideProgress)
                if (userId.get().isNullOrEmpty() && userPwd.get().isNullOrEmpty()) {
                    viewStateChange(AgentDeleteViewState.EmptyIdAndPwd)
                } else if (userId.get().isNullOrEmpty()) {
                    viewStateChange(AgentDeleteViewState.EmptyId)
                } else if (userPwd.get().isNullOrEmpty()) {
                    viewStateChange(AgentDeleteViewState.EmptyPwd)
                }
            }
        }
    }

    override fun onCleared() {
        agentDeleteInteractor.release()
        super.onCleared()
    }

    private fun String.requireNotEmpty(): String {
        return if (trim().isNotEmpty()) trim()
        else throw java.lang.IllegalArgumentException("Require is not empty.")
    }
}


sealed class AgentDeleteViewState : ViewState {
    object ShowProgress : AgentDeleteViewState()
    object HideProgress : AgentDeleteViewState()
    object InvalidAccount : AgentDeleteViewState()
    object EmptyIdAndPwd : AgentDeleteViewState()
    object EmptyId : AgentDeleteViewState()
    object EmptyPwd : AgentDeleteViewState()
    object InvalidProxy : AgentDeleteViewState()

    object DeletedSuccess : AgentDeleteViewState()
    object AlreadyDeletedAgent : AgentDeleteViewState()
    data class NotDefinedError(val errorCode: Int) : AgentDeleteViewState()
}