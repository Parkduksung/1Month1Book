package com.example.study_notification.service

import android.app.Application
import android.util.Log
import androidx.lifecycle.*

class ServiceViewModel(private val app: Application) : AndroidViewModel(app), LifecycleObserver {

    companion object {
        private const val TAG = "ServiceViewModel"
    }

    override fun onCleared() {
        Log.d("ServiceViewModel", "onCleared")
        super.onCleared()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStopScreen() {
        Log.d("ServiceViewModel", "onStopScreen")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStartScreen() {
        Log.d("ServiceViewModel", "onStartScreen")
    }

}