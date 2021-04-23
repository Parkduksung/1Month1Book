package com.example.study_notification.service

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
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
        app.unbindService(serviceConnection)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStartScreen() {
        app.bindService(
            Intent(app, MainService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        Log.d("ServiceViewModel", "onStartScreen")
    }


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }
}