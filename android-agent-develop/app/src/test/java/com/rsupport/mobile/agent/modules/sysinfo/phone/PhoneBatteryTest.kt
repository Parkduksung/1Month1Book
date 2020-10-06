package com.rsupport.mobile.agent.modules.sysinfo.phone

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import androidx.test.core.app.ApplicationProvider
import com.rsupport.mobile.agent.TestApplication
import com.rsupport.mobile.agent.utils.TestLogPrinter
import com.rsupport.util.log.RLog
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(
        application = TestApplication::class
)
@RunWith(RobolectricTestRunner::class)
class PhoneBatteryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var phoneBattery: PhoneBattery


    @Before
    fun setup() {
        context.sendStickyBroadcast(Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_SCALE, 100)
            putExtra(BatteryManager.EXTRA_LEVEL, 0)
        })

        phoneBattery = PhoneBattery(context)
        RLog.setLogPrinter(TestLogPrinter())
    }


    @Test
    fun battery_info() {
        val batteryInfo = phoneBattery.getBatteryInfo()
        assertNotNull(batteryInfo)
    }

    @Test
    fun battery_close() {
        phoneBattery.close()
    }

    @Test
    fun level_0_when_100_0() {
        context.sendBroadcast(Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_SCALE, 100)
            putExtra(BatteryManager.EXTRA_LEVEL, 0)
        })

        val batteryInfo = phoneBattery.getBatteryInfo()
        assertThat(batteryInfo.level, `is`("0%"))
    }

    @Test
    fun level_50_when_1000_500() {
        context.sendBroadcast(Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_SCALE, 1000)
            putExtra(BatteryManager.EXTRA_LEVEL, 500)
        })

        val batteryInfo = phoneBattery.getBatteryInfo()
        assertThat(batteryInfo.level, `is`("50%"))
    }

    @Test
    fun status_charging_when_battery_charging() {
        context.sendBroadcast(Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_CHARGING)
        })

        val batteryInfo = phoneBattery.getBatteryInfo()
        assertThat(batteryInfo.status, `is`("CHARGING"))
    }

    @Test
    fun status_discharging_when_battery_discharging() {
        context.sendBroadcast(Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_DISCHARGING)
        })

        val batteryInfo = phoneBattery.getBatteryInfo()
        assertThat(batteryInfo.status, `is`("DISCHARGING"))
    }

    @Test
    fun status_full_when_battery_full() {
        context.sendBroadcast(Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_FULL)
        })

        val batteryInfo = phoneBattery.getBatteryInfo()
        assertThat(batteryInfo.status, `is`("FULL"))
    }


    @Test
    fun status_no_charging_when_battery_no_charging() {
        context.sendBroadcast(Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_NOT_CHARGING)
        })

        val batteryInfo = phoneBattery.getBatteryInfo()
        assertThat(batteryInfo.status, `is`("NOT_CHARGING"))
    }

    @Test
    fun status_unknown_when_battery_unknown() {
        context.sendBroadcast(Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_STATUS, -1)
        })

        val batteryInfo = phoneBattery.getBatteryInfo()
        assertThat(batteryInfo.status, `is`("Unknown"))
    }

    @Test
    fun status_tech_when_battery_tech_rsup() {
        context.sendBroadcast(Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_TECHNOLOGY, "rsup")
        })

        val batteryInfo = phoneBattery.getBatteryInfo()
        assertThat(batteryInfo.tech, `is`("rsup"))
    }

    @Test
    fun status_volt_0_when_battery_0() {
        context.sendBroadcast(Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        })

        val batteryInfo = phoneBattery.getBatteryInfo()
        assertThat(batteryInfo.volt, `is`("0"))
    }

    @Test
    fun status_volt_when_battery_45() {
        context.sendBroadcast(Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_VOLTAGE, 45)
        })

        val batteryInfo = phoneBattery.getBatteryInfo()
        assertThat(batteryInfo.volt, `is`("4.5"))
    }

    @Test
    fun status_temp_0_when_battery_0() {
        context.sendBroadcast(Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        })

        val batteryInfo = phoneBattery.getBatteryInfo()
        assertThat(batteryInfo.temperature, `is`("0"))
    }

    @Test
    fun status_temp_60_when_battery_600() {
        context.sendBroadcast(Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_TEMPERATURE, 600)
        })

        val batteryInfo = phoneBattery.getBatteryInfo()
        assertThat(batteryInfo.temperature, `is`("60.0"))
    }


    @Test
    fun status_plugged_ac_when_battery_ac() {
        context.sendBroadcast(Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_PLUGGED, BatteryManager.BATTERY_PLUGGED_AC)
        })

        val batteryInfo = phoneBattery.getBatteryInfo()
        assertThat(batteryInfo.plug, `is`("PLUGGED_AC"))
    }

    @Test
    fun status_plugged_usb_when_battery_usb() {
        context.sendBroadcast(Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_PLUGGED, BatteryManager.BATTERY_PLUGGED_USB)
        })

        val batteryInfo = phoneBattery.getBatteryInfo()
        assertThat(batteryInfo.plug, `is`("PLUGGED_USB"))
    }

    @Test
    fun status_plugged_else_when_battery_unpluged() {
        context.sendBroadcast(Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_PLUGGED, -1)
        })

        val batteryInfo = phoneBattery.getBatteryInfo()
        assertThat(batteryInfo.plug, `is`("UNPLUGGED"))
    }


}