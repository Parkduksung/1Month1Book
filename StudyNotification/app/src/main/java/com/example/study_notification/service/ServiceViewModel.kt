package com.example.study_notification.service

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class ServiceViewModel(private val app: Application) : AndroidViewModel(app), LifecycleObserver {

    companion object {
        private const val TAG = "ServiceExam"
    }

    override fun onCleared() {
        Log.d(TAG, "ServiceViewModel onCleared")
        super.onCleared()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStopScreen() {
        Log.d(TAG, "ServiceViewModel onStopScreen")
        serviceSession?.release()
        app.unbindService(serviceConnection)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStartScreen() {
        app.bindService(
            Intent(app, MainService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        Log.d(TAG, "ServiceViewModel onStartScreen")
    }

    private var serviceSession: ServiceSession? = null

    private val sessionListener = object : ServiceSession.OnSessionListener {
        override fun onChangedStatus(status: ServiceSession.Status) {

            when(status) {

                ServiceSession.Status.Connecting -> {
                    Log.d(TAG , "ServiceViewModel ServiceSession.Status.Connecting")
                }

                ServiceSession.Status.Disconnecting -> {
                    Log.d(TAG , "ServiceViewModel ServiceSession.Status.Disconnecting")
                }
            }

        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            val service  = (service as ServiceLocalBinder).getService()
            serviceSession = service.getServiceSession().apply {
                registerSessionListener(sessionListener)

                val serviceSessionStatus = getStatus()

                when(serviceSessionStatus) {

                    ServiceSession.Status.Idle -> {
                        Log.d(TAG , "ServiceViewModel ServiceSession.Status.Idle")
                    }

                    ServiceSession.Status.Connecting -> {
                        Log.d(TAG , "ServiceViewModel ServiceSession.Status.Connecting")
                    }

                    ServiceSession.Status.Disconnecting -> {
                        Log.d(TAG , "ServiceViewModel ServiceSession.Status.Disconnecting")
                    }
                }

            }

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceSession = null
            Log.d(TAG, "ServiceViewModel onServiceDisconnected")
        }
    }
}