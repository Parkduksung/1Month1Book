package com.example.a1month1book.doit

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.example.a1month1book.R
import com.example.a1month1book.base.BaseActivity
import com.example.a1month1book.databinding.DoitMission11Binding
import com.example.a1month1book.kotlin200.Part3
import java.lang.Exception

class DoitMission11 : BaseActivity<DoitMission11Binding>(R.layout.doit_mission_11) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.sendServiceMessageButton.setOnClickListener {
            Intent(this, DoitMission11Service::class.java).apply {
                putExtra("message1", binding.inputServiceMessage.text.toString())
            }.also { intent ->
                startService(intent)
            }
            Log.d("영역:메세지보냄", binding.inputServiceMessage.text.toString())
        }


        try {
            Log.d("결과", "try")
            throw Exception()
        }catch (e : Exception){
            Log.d("결과", "catch")
        }finally {
            Log.d("결과", "finally")
        }

    }

    override fun onNewIntent(intent: Intent?) {
        if (intent != null) {
            val message = intent.getStringExtra("message2").orEmpty()
            Log.d("영역:메세지받음", message)
            binding.getServiceMessage.text = message
        }
        super.onNewIntent(intent)
    }

}


class DoitMission11Service : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val message = intent.getStringExtra("message1").orEmpty()
            Log.d("영역:Service", message)

            Intent(this, DoitMission11::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("message2", message)
            }.also { intent ->
                startActivity(intent)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
}