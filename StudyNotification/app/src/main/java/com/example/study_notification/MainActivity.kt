package com.example.study_notification

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

//
//    private val notificationHandler: NotificationHandler by lazy {
//        NotificationHandler(applicationContext)
//    }

    private lateinit var click: Click

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


//        val notificationManager = NotificationManager()


        btn_notification.setOnClickListener {

            click = object : Click {
                override fun click(isClick: Boolean) {

                }
            }
            click.click(true)


//            createNotificationChannel(application)
//            notificationHandler.createNotificationChannel("ran", "ran", "ran")
//            val resultIntent = Intent(this, this@MainActivity::class.java)
//            notificationHandler.showNotification("이건 연습이에요", resultIntent)
        }
    }

//    private fun createNotificationChannel(application: Application) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channelName: CharSequence =
//                application.getString(R.string.notification_channel_connect)
//            val notificationChannelDescription =
//                application.getString(R.string.notification_channel_connect_desc)
//            val notificationChannel = NotificationChannel(
//                AgentNotificationBar.AGENT_CHANNEL_ID,
//                channelName,
//                NotificationManager.IMPORTANCE_NONE
//            ).apply {
//                description = notificationChannelDescription
//            }
//            val notificationManager =
//                application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(notificationChannel)
//
//
//            val intent = Intent(this, LaunchActivity::class.java)
//
//            val pendingIntent = PendingIntent.getActivities(this,0,intent, PendingIntent.FLAG_UPDATE_CURRENT)
//
//            val builder = Notification.Builder(this, channelName.toString())
//                .setContentTitle("Android Agent")
//                .setContentText(notificationChannelDescription)
//                .setSmallIcon(R.drawable.notification_icon)
//                .setLargeIcon(
//                    BitmapFactory.decodeResource(
//                        this.resources,
//                        R.drawable.notification_icon
//                    )
//                )
//                .setContentIntent()
//            notificationManager.notify(1234, builder.build())
//
//        }
//    }


//
//    @SuppressLint("ResourceType")
//    private fun createNotificationChannel() {
//        // Create the NotificationChannel, but only on API 26+ because
//        // the NotificationChannel class is new and not in the support library
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = getString(1)
//            val descriptionText = getString(R.string.channel_description)
//            val importance = NotificationManager.IMPORTANCE_DEFAULT
//            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
//                description = descriptionText
//            }
//            // Register the channel with the system
//            val notificationManager: NotificationManager =
//                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
//
    companion object {


        private const val CHANNEL_ID = "channel"
    }
}

interface Click {
    fun click(isClick: Boolean)
}

