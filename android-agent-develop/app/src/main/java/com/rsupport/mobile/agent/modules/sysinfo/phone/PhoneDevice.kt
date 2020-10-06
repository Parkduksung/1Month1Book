package com.rsupport.mobile.agent.modules.sysinfo.phone

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager

class PhoneDevice(private val context: Context) {

    private val INFO_EMPTY = "EMPTY"
    private val telephonyManager: TelephonyManager by lazy {
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    fun getModelName(): String? {
        return Build.MODEL
    }

    fun getManufacturer(): String? {
        return Build.MANUFACTURER
    }

    fun getFirmwareVersion(): String? {
        return Build.VERSION.RELEASE + "(" + Build.ID + ")"
    }

    fun getOSVersion(): String? {
        return System.getProperty("os.version")
    }

    fun getBuildNumber(): String? {
        return Build.DISPLAY
    }

    @SuppressLint("MissingPermission")
    fun getSIMSerialNumber(): String? {
        return telephonyManager.simSerialNumber ?: INFO_EMPTY
    }

    @SuppressLint("MissingPermission")
    fun getImei(): String? {
        val deviceID = telephonyManager.deviceId
        return if (deviceID != null && deviceID.isNotEmpty() && deviceID != "0") {
            deviceID
        } else {
            INFO_EMPTY
        }
    }
}