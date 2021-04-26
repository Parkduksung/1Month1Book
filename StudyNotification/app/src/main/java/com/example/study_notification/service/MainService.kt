package com.example.study_notification.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.study_notification.App
import com.example.study_notification.R


class ServiceContext {


    private var serviceSession: ServiceSession? = null

    fun getServiceSession(): ServiceSession {

        return serviceSession ?: ServiceSession(App.instance.context()).apply {
            serviceSession = this
        }

    }

}

class ServiceLocalBinder(private val serviceContext: ServiceContext) : Binder() {
    fun getService(): ServiceContext {
        return serviceContext
    }
}

class MainService : Service(), LifecycleObserver {

    companion object {
        private const val TAG = "ServiceExam"

    }

    private var serviceSession: ServiceSession? = null

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "MainService onBind")
        return ServiceLocalBinder(ServiceContext()).apply {
            getService().apply {
                getServiceSession().registerSessionListener(sessionCallback)
            }
        }
    }

    private val sessionCallback = object : ServiceSession.OnSessionListener {

        override fun onChangedStatus(status: ServiceSession.Status) {
            Log.d(TAG, "MainService onChangedStatus")
            when (status) {

                ServiceSession.Status.Connecting -> {
                    Log.d(TAG, "MainService ServiceSession.Status.Connecting")
                    releaseServiceSession()
                    startForegroundService()
                }

                ServiceSession.Status.Disconnecting -> {
                    Log.d(TAG, "MainService ServiceSession.Status.Disconnecting")
                    stopForegroundService()
                }

                else -> {
                }
            }
        }
    }

    private fun releaseServiceSession() {
        serviceSession?.release()
        serviceSession = null
    }

    override fun unbindService(conn: ServiceConnection) {
        Log.d(TAG, "MainService unbindService")
        super.unbindService(conn)
    }

    //서비스가 메모리 관련해서 끊어져도 다시 실행 안하게 설정.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MainService onDestroy")
        serviceSession?.unregisterSessionListener(sessionCallback)

        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun inServiceBackground() {
        Log.d(TAG, "MainService inServiceBackground")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun inServiceForeground() {
        Log.d(TAG, "MainService inServiceForeground")
    }


    private fun startForegroundService() {
        startService(Intent(this@MainService, MainService::class.java))

        val resultIntent = Intent(this, ServiceActivity::class.java).apply {
            action = "android.intent.action.MAIN"
            addCategory("android.intent.category.LAUNCHER")
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, "Service")
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Sample Service")
            .setContentText("Service is in progress.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)


        val notification = builder.build()
        startForeground(10, notification)
    }

    private fun stopForegroundService() {
        stopForeground(true)
        stopSelf()
    }
}


class ServiceSession(private val context: Context) {

    companion object {
        private const val TAG = "ServiceExam"
    }

    private var sessionStatus: ServiceSession.Status = Status.Connecting

    private val sessionSet = mutableSetOf<OnSessionListener>()

    fun registerSessionListener(onSessionListener: OnSessionListener) {
        sessionSet.add(onSessionListener)
    }

    fun unregisterSessionListener(onSessionListener: OnSessionListener) {
        sessionSet.remove(onSessionListener)
    }


    fun release() {
        Log.d(TAG, "ServiceSession release")
        onSessionListener.onChangedStatus(Status.Disconnecting)
    }

    fun getStatus(): Status {
        return sessionStatus
    }


    private val onSessionListener: OnSessionListener = object : OnSessionListener {
        override fun onChangedStatus(status: Status) {
            sessionStatus = status

            when (status) {
                Status.Connecting ->onConnected()
                Status.Disconnecting -> onDisconnecting()
            }

            sessionSet.forEach { it.onChangedStatus(status) }
        }
    }

    private fun onConnected() {
        Log.d(TAG, "ServiceSession onConnected")
    }

    private fun onDisconnecting() {
        Log.d(TAG, "ServiceSession onDisconnecting")
    }


    sealed class Status {

        object Idle : Status()
        object Connecting : Status()
        object Connected : Status()

        object Disconnecting : Status()
        object Disconnected : Status()
    }


    interface OnSessionListener {

        fun onChangedStatus(status: Status)
    }

}