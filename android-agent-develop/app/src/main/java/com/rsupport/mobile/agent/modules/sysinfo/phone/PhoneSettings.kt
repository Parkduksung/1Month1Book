package com.rsupport.mobile.agent.modules.sysinfo.phone

import android.content.ContentResolver
import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import com.rsupport.util.log.RLog
import java.util.*

class PhoneSettings(private val context: Context) {

    private val OFF = 0
    private val ON = 1

    fun getAirplaneSetState(): Int {
        return Settings.System.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, OFF)
    }

    fun getWifiSetState(): Int {
        return Settings.System.getInt(context.contentResolver, Settings.Global.WIFI_ON, OFF)
    }

    fun getBluetoothSetState(): Int {
        return Settings.System.getInt(context.contentResolver, Settings.Global.BLUETOOTH_ON, OFF)
    }

    fun getLanguageSetState(): String {
        return context.resources.configuration.locale.language.toUpperCase(Locale.getDefault())
    }


    fun getSoundSetState(): Int {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return if (am.ringerMode == 0 || am.ringerMode == 1) {
            OFF
        } else {
            ON
        }
    }

    fun getPlaneMode(): Int {
        var mode = 0
        try {
            mode = Settings.System.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON)
        } catch (e: SettingNotFoundException) {
            RLog.e(e)
        }
        return if (mode > 0) ON else OFF
    }

    fun getAutoSyncMode(): Int {
        return if (ContentResolver.getMasterSyncAutomatically()) ON else OFF
    }

    fun getDataRoamSetting(): Int {
        var mode = 0
        try {
            mode = Settings.Secure.getInt(context.contentResolver, Settings.Global.DATA_ROAMING)
        } catch (e: SettingNotFoundException) {
            RLog.e(e)
        }
        return if (mode > 0) ON else OFF
    }


    fun getShowingTextPassword(): Int {
        var mode = 0
        try {
            mode = Settings.System.getInt(context.contentResolver, Settings.System.TEXT_SHOW_PASSWORD)
        } catch (e: SettingNotFoundException) {
            RLog.e(e)
        }
        return if (mode > 0) ON else OFF
    }

    fun getRingerVolume(): Int {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return am.getStreamVolume(AudioManager.STREAM_RING) * 100 / am.getStreamMaxVolume(AudioManager.STREAM_RING)
    }

    fun getMediaVolume(): Int {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return am.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

    fun getRingerMode(): String? {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (am.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> return "Normal mode"
            AudioManager.RINGER_MODE_SILENT -> return "Silent mode"
            AudioManager.RINGER_MODE_VIBRATE -> return "Vibrate mode"
        }
        return "Unknown"
    }


}