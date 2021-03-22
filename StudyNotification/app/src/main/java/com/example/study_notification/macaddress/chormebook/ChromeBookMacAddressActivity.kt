package com.example.study_notification.macaddress.chormebook

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.study_notification.R

class ChromeBookMacAddressActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chrome_mac_address)

        Log.d("결과", NetworkUtil.getWifiMacAddress())

    }
}