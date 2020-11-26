package com.example.a1month1book.sort.service

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.a1month1book.R
import com.example.a1month1book.base.BaseActivity
import com.example.a1month1book.databinding.ActivityServiceBinding

class ServiceMainActivity : BaseActivity<ActivityServiceBinding>(R.layout.activity_service) {

    // 서비스란 백그라운드에서 실행되는 앱의 구성 요소를 말한다.
    // 구성요소이기 때문에 시스템에서 관리하고 액티비티같이 추가할때 manifest에 추가하여 사용하여야 한다.
    // 서비스를 시작하는 목적 이외에 intent 전달하는 목적도 있음.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("여기", "여기")

        binding.sendServiceButton.setOnClickListener {
            val intent = Intent(this, MyService::class.java).apply {
                putExtra("command", "show")
                putExtra("name", binding.getService.text.toString())
            }
            startService(intent) // 이게 onStartCommand 로 전달됨.
        }

        processIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        processIntent(intent)
        super.onNewIntent(intent)
    }

    private fun processIntent(intent: Intent) {
        val getName = intent.getStringExtra("name").orEmpty()

        //toast 안찍히는데요..?
        Toast.makeText(this, getName, Toast.LENGTH_LONG).show()
    }
}