package com.rsupport.mobile.agent.modules.sysinfo.phone

import android.content.Context
import android.os.HandlerThread
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

class PhoneStateProvider(context: Context) {
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private var isUnRegister: AtomicBoolean = AtomicBoolean(false)

    private var phoneServiceState: ServiceState? = null
    private var phoneSignalStrength: SignalStrength? = null
    private var mPhoneState = TelephonyManager.CALL_STATE_IDLE
    private var phoneStatePhoneStateListener: PhoneStateListener? = null

    private var mobileSignalStrength: String? = null
    private var mobileSignalStrengthLTE = "0%"
    private val handlerThread: HandlerThread

    init {
        val countDownLatch = CountDownLatch(1)
        handlerThread = object : HandlerThread("PhoneStateListenerImpl") {
            override fun onLooperPrepared() {
                try {
                    phoneStatePhoneStateListener = PhoneStateListenerImpl()
                    telephonyManager.listen(phoneStatePhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS or
                            PhoneStateListener.LISTEN_SERVICE_STATE or
                            PhoneStateListener.LISTEN_CALL_STATE)
                } catch (e: Exception) {
                }
                countDownLatch.countDown()
            }
        }
        handlerThread.start()
        countDownLatch.await()
    }

    fun getOperatorName(): String? {
        return phoneServiceState?.operatorAlphaLong
    }

    fun close() {
        handlerThread.quit()
        try {
            if (!isUnRegister.get()) {
                telephonyManager.listen(phoneStatePhoneStateListener, PhoneStateListener.LISTEN_NONE)
                isUnRegister.set(true)
            }
        } catch (e: java.lang.Exception) {
        }
    }

    private fun isLTE(): Boolean {
        return telephonyManager.networkType == 13
    }

    private fun isCdma(): Boolean {
        return phoneSignalStrength != null && !phoneSignalStrength!!.isGsm
    }

    private fun isEvdo(): Boolean {
        return phoneServiceState != null && phoneServiceState.toString().toLowerCase(Locale.getDefault()).contains("evdo")
    }

    private fun getEvdoLevel(signalStrength: SignalStrength): Int {
        val evdoDbm = signalStrength.evdoDbm
        val evdoSnr = signalStrength.evdoSnr
        var levelEvdoDbm = 0
        var levelEvdoSnr = 0
        levelEvdoDbm = if (evdoDbm >= -65) {
            4
        } else if (evdoDbm >= -75) {
            3
        } else if (evdoDbm >= -90) {
            2
        } else if (evdoDbm >= -105) {
            1
        } else {
            0
        }
        levelEvdoSnr = if (evdoSnr >= 7) {
            4
        } else if (evdoSnr >= 5) {
            3
        } else if (evdoSnr >= 3) {
            2
        } else if (evdoSnr >= 1) {
            1
        } else {
            0
        }
        return if (levelEvdoDbm < levelEvdoSnr) levelEvdoDbm else levelEvdoSnr
    }

    private fun getCdmaLevel(signalStrength: SignalStrength): Int {
        val cdmaDbm = signalStrength.cdmaDbm
        val cdmaEcio = signalStrength.cdmaEcio
        val levelDbm: Int
        val levelEcio: Int

        levelDbm = if (cdmaDbm >= -75) {
            4
        } else if (cdmaDbm >= -85) {
            3
        } else if (cdmaDbm >= -95) {
            2
        } else if (cdmaDbm >= -100) {
            1
        } else {
            0
        }

        // Ec/Io are in dB*10
        levelEcio = if (cdmaEcio >= -90) {
            4
        } else if (cdmaEcio >= -110) {
            3
        } else if (cdmaEcio >= -130) {
            2
        } else if (cdmaEcio >= -150) {
            1
        } else {
            0
        }
        return if (levelDbm < levelEcio) levelDbm else levelEcio
    }

    private fun getSignalLevel(signal: Int): String {
        return if (signal >= -75) {
            "100%"
        } else if (signal >= -85) {
            "75%"
        } else if (signal >= -95) {
            "50%"
        } else if (signal >= -100) {
            "25%"
        } else {
            "0%"
        }
    }

    private fun calculateMobileSignalLevel(): String? {
        if (phoneSignalStrength == null) return null
        var level: String? = null
        if (!isCdma()) {
            val asu = phoneSignalStrength!!.gsmSignalStrength
            level = if (asu <= 2 || asu == 99) {
                "0%"
            } else if (asu >= 12) {
                "100%"
            } else if (asu >= 8) {
                "75%"
            } else if (asu >= 5) {
                "50%"
            } else {
                "25%"
            }
        } else {
            var signalLevel = 0
            if (mPhoneState == TelephonyManager.CALL_STATE_IDLE && isEvdo()) {
                signalLevel = getEvdoLevel(phoneSignalStrength!!)
            } else {
                signalLevel = getCdmaLevel(phoneSignalStrength!!)
            }
            level = if (signalLevel == 0) {
                "0%"
            } else if (signalLevel == 4) {
                "100%"
            } else if (signalLevel == 3) {
                "75%"
            } else if (signalLevel == 2) {
                "50%"
            } else {
                "25%"
            }
        }
        return level
    }

    fun getMobileSpeed(): String {
        return when {
            isLTE() -> mobileSignalStrengthLTE
            mobileSignalStrength != null -> mobileSignalStrength!!
            else -> "0%"
        }
    }

    private inner class PhoneStateListenerImpl : PhoneStateListener() {
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
            phoneSignalStrength = signalStrength
            mobileSignalStrength = calculateMobileSignalLevel()
        }

        override fun onServiceStateChanged(state: ServiceState) {
            phoneServiceState = state
            mobileSignalStrength = calculateMobileSignalLevel()
            if (phoneSignalStrength != null) {
                mobileSignalStrengthLTE = getSignalLevel(phoneSignalStrength!!.getCdmaDbm())
            }
        }

        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            mPhoneState = state
            if (isCdma()) {
                mobileSignalStrength = calculateMobileSignalLevel()
            }
            if (isLTE()) {
                if (phoneSignalStrength != null) {
                    mobileSignalStrengthLTE = getSignalLevel(phoneSignalStrength!!.getCdmaDbm())
                }
            }
        }
    }
}