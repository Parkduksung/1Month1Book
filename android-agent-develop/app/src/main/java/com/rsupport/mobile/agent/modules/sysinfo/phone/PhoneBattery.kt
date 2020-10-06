package com.rsupport.mobile.agent.modules.sysinfo.phone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.rsupport.util.log.RLog

class PhoneBattery(private val context: Context) {
    private var isUnregist: Boolean = false
    private val batteryInfoReceiver: BatteryReceiver
    private val batteryInfo = BatteryInfo()

    private var batteryLevelResult = "EMPTY"
    private var statusResult = "Unknown"
    private var techResult = "EMPTY"
    private var voltResult = "0"
    private var temperatureResult = "0"
    private var plugResult = "UNPLUGGED"


    init {
        batteryInfoReceiver = BatteryReceiver()
        context.registerReceiver(batteryInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))?.apply {
            setBatteryInfo(this)
        }
    }

    fun close() {
        try {
            if (!isUnregist) {
                context.unregisterReceiver(batteryInfoReceiver)
                isUnregist = true
            }
        } catch (e: java.lang.Exception) {
            RLog.e(e)
        }
    }

    fun getBatteryInfo(): BatteryInfo {
        return batteryInfo.copy(
                level = batteryLevelResult,
                status = statusResult,
                tech = techResult,
                volt = voltResult,
                temperature = temperatureResult,
                plug = plugResult
        )
    }

    private fun setBatteryInfo(intent: Intent) {
        val action = intent.action
        if (Intent.ACTION_BATTERY_CHANGED == action) {
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)

            // level
            batteryLevelResult = (level * 100 / scale).toString() + "%"
            statusResult = when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0)) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "CHARGING"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "DISCHARGING"
                BatteryManager.BATTERY_STATUS_FULL -> "FULL"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "NOT_CHARGING"
                else -> "Unknown"
            }

            // tech
            intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)?.apply {
                techResult = this
            }

            // volt
            var sb = StringBuffer(intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0).toString())
            if (sb.toString() != "0") sb.insert(1, ".")
            voltResult = sb.toString()

            // temperature
            sb = StringBuffer(intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0).toString())
            if (sb.toString() != "0") sb.insert(sb.length - 1, ".")
            temperatureResult = sb.toString()
            plugResult = when (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)) {
                BatteryManager.BATTERY_PLUGGED_AC -> "PLUGGED_AC"
                BatteryManager.BATTERY_PLUGGED_USB -> "PLUGGED_USB"
                else -> "UNPLUGGED"
            }
        }
    }

    data class BatteryInfo(
            val level: String? = null,
            val status: String? = null,
            val tech: String? = null,
            val volt: String? = null,
            val temperature: String? = null,
            val plug: String? = null
    )

    inner class BatteryReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            setBatteryInfo(intent)
        }
    }
}