package com.rsupport.mobile.agent.modules.push.delegate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WifiLock
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.cn.ui.dialog.PushNotificationDismissDialog
import com.rsupport.mobile.agent.constant.AgentNotificationBar
import com.rsupport.mobile.agent.utils.Utility

class RSPushNotificationDelegateImpl : RSPushNotificationDelegate {

    private var cpuWakeLock: PowerManager.WakeLock? = null
    private var wifiWakeLock: WifiLock? = null

    override fun setForeground(service: Service) {
        wakeLock(service)
        createNotificationChannel(service)
        createNotification(service)
    }

    override fun stopForeground(service: Service) {
        wifiWakeLock?.release()
        cpuWakeLock?.release()
        cpuWakeLock = null
        wifiWakeLock = null
        service.stopForeground(true)
    }

    private fun createPendingIntent(service: Service): PendingIntent {
        return PendingIntent.getActivity(
                service,
                0,
                Intent(service, PushNotificationDismissDialog::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
                PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun wakeLock(service: Service) {
        val powerManager = service.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (cpuWakeLock == null) {
            cpuWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "rvagent.cn:RSPushCPUWakeLock")
            cpuWakeLock?.acquire()
        }

        if (!Utility.isEtherNet(service.applicationContext)) {
            if (wifiWakeLock == null) {
                val wifiManager = service.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                wifiWakeLock = wifiManager.createWifiLock("rvagent.cn:RSPushWiFiWakeLock")
                wifiWakeLock?.setReferenceCounted(true)
                wifiWakeLock?.acquire()
            }
        }
    }


    private fun createNotification(service: Service) {
        service.startForeground(
                getNotificationId(),
                NotificationCompat.Builder(service, getNotificationChannelId())
                        .setContentTitle(service.getString(R.string.push_notification_title))
                        .setContentText(service.getString(R.string.push_notification_desc))
                        .setSmallIcon(R.drawable.icon_cn)
                        .setContentIntent(createPendingIntent(service))
                        .build()
        )
    }

    private fun getNotificationChannelId(): String {
        return AgentNotificationBar.AGENT_CHANNEL_ID
    }

    private fun getNotificationId(): Int {
        return 4400
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName: CharSequence = context.getString(R.string.notification_channel_wait)
            val notificationChannelDescription = context.getString(R.string.notification_channel_wait_desc)
            val notificationChannel = NotificationChannel(getNotificationChannelId(), channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = notificationChannelDescription
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}