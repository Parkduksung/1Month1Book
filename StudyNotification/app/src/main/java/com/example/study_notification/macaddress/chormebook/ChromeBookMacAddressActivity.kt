package com.example.study_notification.macaddress.chormebook

import android.annotation.SuppressLint
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.study_notification.R

@RequiresApi(Build.VERSION_CODES.M)
class ChromeBookMacAddressActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chrome_mac_address)

        Log.d("결과1", NetworkUtil.getWifiMacAddress())
        Log.d("결과2", getMacAddress())
        Log.d("결과3", NetworkUtil.getMacAddress(this))
        Log.d("결과4", NetworkUtil.getMacAddress1("wlan0"))

    }


    @SuppressLint("HardwareIds")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getMacAddress() : String {

        val managers = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        val info = managers.connectionInfo

        return info.macAddress
    }
}