package com.example.a1month1book.sort.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class MyService : Service() {


    // 액티비티와 연결될 수 있도록 만드는  것이 바인딩이라고 하고 그걸 여기서 재정의 한다.
    override fun onBind(intent: Intent): IBinder? {
        return null
//        TODO("Return the communication channel to the service.")
    }

    override fun onCreate() {
        super.onCreate()

    }


    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // null 체크를 하는 이유가 서비스는 시스템에 의해 자동으로 다시 시작될 수 있기 때문에 null 체크를 해야한다.
        if (intent == null) {
            return START_STICKY
        } else {
            processCommand(intent)
        }
        return super.onStartCommand(intent, flags, startId)
    }


    private fun processCommand(intent: Intent) {

        val command = intent.getStringExtra("command").orEmpty()
        val name = intent.getStringExtra("name").orEmpty()

        val intent = Intent(this, ServiceMainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("command", "show")
            putExtra("name", "$name 다시보내드림..")
        }

        startActivity(intent)

    }
}