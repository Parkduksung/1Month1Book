package com.example.study_notification.macaddress.chormebook

import android.annotation.SuppressLint
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.study_notification.R

@RequiresApi(Build.VERSION_CODES.M)
class ChromeBookMacAddressActivity : AppCompatActivity() {

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chrome_mac_address)

        Log.d("결과1", NetworkUtil.getWifiMacAddress())
        Log.d("결과2", getMacAddress())
        Log.d("결과3", NetworkUtil.getMacAddress(this))
//        Log.d("결과4", NetworkUtil.getMacAddress1("wlan0"))
        Log.d("결과5", NetworkUtil.getMac())
        Log.d("결과6", NetworkUtil.getMacAddress2())


        NetworkUtil.getValidEthernetInterface().forEach {
            Log.d("결과7", it.hardwareAddress.toString())
            Log.d("결과7", it.hardwareAddress.size.toString())
        }

        val androidId: String =
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        findViewById<TextView>(R.id.android_text).text =androidId

        Log.d("결과8", androidId)

    }


    @SuppressLint("HardwareIds")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getMacAddress() : String {

        val managers = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        val info = managers.connectionInfo

        return info.macAddress
    }
}