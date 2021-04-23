package com.example.study_notification.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.study_notification.R


class ServiceContext {

}

class ServiceLocalBinder(private val serviceContext: ServiceContext) : Binder() {
    fun getService(): ServiceContext {
        return serviceContext
    }
}

class MainService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return ServiceLocalBinder(ServiceContext())
    }


    //서비스가 메모리 관련해서 끊어져도 다시 실행 안하게 설정.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
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