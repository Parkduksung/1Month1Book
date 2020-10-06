package com.rsupport.mobile.agent.modules.sysinfo.phone

import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import com.rsupport.util.log.RLog

class PhoneNetwork(
        private val context: Context,
        private val phoneNumber: PhoneNumber = PhoneNumber(context),
        private val phoneStateProvider: PhoneStateProvider = PhoneStateProvider(context)
) {
    /** Anything worse than or equal to this will show 0 bars.  */
    private val MIN_RSSI = -100

    /** Anything better than or equal to this will show the max bars.  */
    private val MAX_RSSI = -55
    private val INFO_EMPTY = "EMPTY"

    private val telephonyManager: TelephonyManager by lazy {
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }
    private val wifiManager: WifiManager by lazy {
        context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    fun close() {
        phoneStateProvider.close()
    }

    fun getOperatorName(): String? {
        var operatorName: String? = getSimOperatorName()
        if (operatorName == null || operatorName.trim() == "" || operatorName == INFO_EMPTY) {
            operatorName = getServiceStateOperatorName()
        }
        return operatorName
    }

    fun getMobileSpeed(): String {
        return if (INFO_EMPTY != phoneNumber.getPhoneNumber() || checkSimMounted()) {
            phoneStateProvider.getMobileSpeed()
        } else {
            INFO_EMPTY
        }
    }

    fun getWifiSpeed(): String? {
        return try {
            getLevel(wifiManager.connectionInfo.rssi).toString() + "%"
        } catch (e: java.lang.Exception) {
            RLog.v(e)
            ""
        }
    }

    fun getLevel(mRssi: Int): Int {
        return if (mRssi == Int.MAX_VALUE) {
            -1
        } else calculateSignalLevel(mRssi, 100)
    }

    fun getConnectedWifiSSID(): String {
        if (!wifiManager.isWifiEnabled) {
            return INFO_EMPTY
        }
        return wifiManager.connectionInfo?.ssid ?: INFO_EMPTY
    }

    fun getAccessPointInfo(): AccessPoint {
        try {
            if (!wifiManager.isWifiEnabled) {
                return AccessPoint()
            }
            val wi = wifiManager.connectionInfo
            val di = wifiManager.dhcpInfo

            return AccessPoint(
                    bssid = wi.bssid,
                    macAddress = null,
                    linkSpeed = wi.linkSpeed.toString(),
                    signalStrength = getWifiStrength(wi),
                    serverAddress = intToIp(di.serverAddress),
                    gateway = intToIp(di.gateway),
                    ipAddress = intToIp(di.ipAddress),
                    netmask = intToIp(di.netmask),
                    dns1 = intToIp(di.dns1),
                    dns2 = intToIp(di.dns2),
                    leaseDuration = di.leaseDuration.toString()
            )
        } catch (e: java.lang.Exception) {
            return AccessPoint()
        }
    }

    private fun calculateSignalLevel(rssi: Int, numLevels: Int): Int {
        return if (rssi <= MIN_RSSI || numLevels < 2) {
            0
        } else if (rssi >= MAX_RSSI) {
            numLevels - 1
        } else {
            val partitionSize = (MAX_RSSI - MIN_RSSI).toDouble() / (numLevels - 1).toDouble()
            val level = (rssi - MIN_RSSI).toDouble() / partitionSize
            Math.round(level).toInt()
        }
    }


    private fun checkSimMounted(): Boolean {
        return telephonyManager.simState != TelephonyManager.SIM_STATE_ABSENT
    }


    private fun getSimOperatorName(): String? {
        return if (telephonyManager.simOperatorName == null || telephonyManager.simOperatorName == "") {
            INFO_EMPTY
        } else {
            telephonyManager.simOperatorName
        }
    }

    private fun getServiceStateOperatorName(): String? {
        val operatorName: String? = getOperatorAlphaLong()
        return if (operatorName == null || operatorName == "") {
            INFO_EMPTY
        } else {
            operatorName
        }
    }

    private fun getOperatorAlphaLong(): String? {
        return phoneStateProvider.getOperatorName()
    }

    private fun removeQuotation(target: String): String {
        return target.replace("\"".toRegex(), "")
    }

    private fun getWifiStrength(wi: WifiInfo): String {
        var signalStrength = "Unknown"
        getWifiScanResult().let { scanResult ->
            for (sr in scanResult) {
                if (removeQuotation(wi.ssid) == removeQuotation(sr.SSID ?: "")) {
                    signalStrength = sr.level.toString() ?: "Unknown"
                    break
                }
            }
        }
        return signalStrength
    }

    fun getWifiScanResult(): List<ScanResult> {
        if (!wifiManager.isWifiEnabled) return emptyList()
        wifiManager.startScan()
        return wifiManager.scanResults ?: return emptyList()
    }

    private fun intToIp(i: Int): String? {
        return (i and 0xFF).toString() + "." +
                (i shr 8 and 0xFF) + "." +
                (i shr 16 and 0xFF) + "." +
                (i shr 24 and 0xFF)
    }
}

data class AccessPoint(
        val bssid: String? = null,
        val macAddress: String? = null,
        val linkSpeed: String? = null,
        val signalStrength: String? = null,
        val serverAddress: String? = null,
        val gateway: String? = null,
        val ipAddress: String? = null,
        val netmask: String? = null,
        val dns1: String? = null,
        val dns2: String? = null,
        val leaseDuration: String? = null

) {
    val empty: Boolean
        get() {
            return bssid.isNullOrEmpty() and
                    macAddress.isNullOrEmpty() and
                    linkSpeed.isNullOrEmpty() and
                    signalStrength.isNullOrEmpty() and
                    serverAddress.isNullOrEmpty() and
                    gateway.isNullOrEmpty() and
                    ipAddress.isNullOrEmpty() and
                    netmask.isNullOrEmpty() and
                    dns1.isNullOrEmpty() and
                    dns2.isNullOrEmpty() and
                    leaseDuration.isNullOrEmpty()
        }
}