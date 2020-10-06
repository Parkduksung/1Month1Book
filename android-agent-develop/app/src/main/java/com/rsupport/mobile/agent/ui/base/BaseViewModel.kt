package com.rsupport.mobile.agent.ui.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * View 에 상태가 변경되었음을 알린다.
     */
    private val _viewStateLiveData = MutableLiveData<ViewState>()
    val viewState: LiveData<ViewState> = _viewStateLiveData

    protected val viewModelScope = CoroutineScope(Dispatchers.Main)

    protected fun viewStateChange(viewState: ViewState) {
        _viewStateLiveData.value = viewState
        _viewStateLiveData.value = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

}

interface ViewState