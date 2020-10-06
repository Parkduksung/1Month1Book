package com.rsupport.knox

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Point
import android.os.Build
import android.text.TextUtils
import android.view.KeyEvent
import android.view.WindowManager
import com.rsupport.engine.PermissionResultCallback
import com.rsupport.knox.permission.AdminActivateCallback
import com.rsupport.util.log.RLog

class KnoxManagerCompat {

    companion object {
        // Knox 를 지원한다.
        const val KNOX_SUPPORTED_DEVICE = 1

        // Knox 를 지원하는 삼성 폰이지만 knox 버전이 낮아서 지원하지 않는다.
        const val KNOX_LOW_VERSION_DEVICE = 2

        // Knox 를 지원하지 않는다.
        const val KNOX_NOT_SUPPORT = 3

        // kitkat version 부터 knox 지원한다.
        private const val MIN_KNOX_API_LEVEL = 11
        private const val RV_AGENT_SHARD_PARAM = "REMOTE_VIEW_AGENT"
        private const val touchPointXKey = "Xkey"
        private const val touchPointYKey = "Ykey"

        private var correctionXPoint = 1f
        private var correctionYPoint = 1f

        const val INVALID_MEMORY = -1L
        const val INVALID_CPU = -1L
    }

    private var key: String? = null
    private var knoxEngine: KnoxEngine? = null
    private var isTouchPointRatioUpdated = false

    /**
     * 녹스 key 가 없으면 녹스 key 를 업데이트할 수 있는 interface 이다.
     * 해당 interface 를 설정하지 않으면 녹스를 사용할 수 없는 폰으로 판단한다.
     */
    var knoxKeyUpdatable: Updatable? = null

    /**
     * Knox 가능한지 체크.
     * @param context
     * @return
     */
    fun isKnoxAvailable(context: Context): Int { //Knox 버전 체크.
        val type = checkKnoxVersion(context)
        if (type == KNOX_NOT_SUPPORT || type == KNOX_LOW_VERSION_DEVICE) {
            return type
        }
        //Knox manager 생성.
        return if (getKnoxEngine(context)?.isInputSupported != true) {
            KNOX_NOT_SUPPORT
        } else type
    }

    /**
     * Knox 사용 권한획득을 요청한다.
     */
    fun activation(context: Context, activationCallback: (Boolean) -> Unit) {
        getKnoxEngine(context)?.run {
            this.requestPermission(object : PermissionResultCallback {
                override fun onSuccess() {
                    activationCallback.invoke(true)
                }

                override fun onFail() {
                    activationCallback.invoke(false)
                }
            })
        } ?: activationCallback.invoke(false)
    }

    /**
     * Knox Manager 값 설정.
     * @param context
     * @param key
     * @param packageName
     * @return false 시 Knox 지원 안되는것으로 판단.
     */
    private fun getKnoxEngine(context: Context): KnoxEngine? {
        if (TextUtils.isEmpty(key)) {
            key = knoxKeyUpdatable?.update()
        }
        if (knoxEngine == null && !TextUtils.isEmpty(key)) {
            knoxEngine = KnoxEngine(key ?: "").apply {
                initialize(context)
            }
        }
        return knoxEngine
    }

    /**
     * Knox 터치 제어 API
     *
     * @param action
     * @param x
     * @param y
     */
    fun injectPointerEvent(context: Context, action: Int, x: Float, y: Float, x2: Float, y2: Float) {
        try {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && !isTouchPointRatioUpdated) {
                isTouchPointRatioUpdated = true
                setTouchPointRatio(context)
            }
            val injectX = (x / correctionXPoint).toShort()
            val injectY = (y / correctionYPoint).toShort()

            getKnoxEngine(context)?.injectMouseEvent(action, 0, injectX, injectY, x2.toShort(), y2.toShort())
        } catch (se: java.lang.Exception) {
            se.printStackTrace()
        }
    }

    private fun setTouchPointRatio(context: Context) {
        val pre: SharedPreferences = context.getSharedPreferences(RV_AGENT_SHARD_PARAM, Context.MODE_PRIVATE)
        correctionXPoint = pre.getFloat(touchPointXKey, 1.0f)
        correctionYPoint = pre.getFloat(touchPointYKey, 1.0f)
    }


    /**
     * Konx 키 제어 API
     *
     * @param keyEvent
     */
    fun injectKeyEvent(context: Context, keyEvent: KeyEvent?) {
        try {
            keyEvent?.run {
                getKnoxEngine(context)?.injectKeyEvent(action, keyCode)
            }
        } catch (se: SecurityException) {
            se.printStackTrace()
        }
    }

    /**
     *
     * @param encodingScreenSize knox initScreenSize
     */
    fun checkInvalidTouchPoint(context: Context, encodingScreenSize: Point) {
        val displaySize = getDisplaySize(context)
        val pre = context.getSharedPreferences(RV_AGENT_SHARD_PARAM, Context.MODE_PRIVATE)
        val editor = pre.edit()
        editor.putFloat(touchPointXKey, displaySize.x.toFloat() / encodingScreenSize.x.toFloat())
        editor.putFloat(touchPointYKey, displaySize.y.toFloat() / encodingScreenSize.y.toFloat())
        editor.apply()
    }

    /**
     * Knox version 이 String 에서 int 로 변경되어 서버에 apilevel을 String 으로 변경하여 보냄
     */
    fun getKnoxSdkVersion(context: Context): String {
        return try {
            return getKnoxEngine(context)?.getAPILevel()?.let {
                if (it != KnoxParam.NOT_SUPPORTED_API) it.toString() else throw IllegalStateException("not support knox.")
            } ?: run {
                throw IllegalStateException("not support knox")
            }
        } catch (e: Throwable) {
            ""
        }
    }


    /**
     * Knox 버전 sdk 버전 5 이상일경우 동작하도록 버전 체크.
     * 비삼성 단말 or Knox 없을 경우 Exception 발생.
     * Exception 발생시 Knox 미지원 단말로 판별.
     * ---sample source---
     * Check SDK Version using getEnterpriseSdkVer() of EnterpriseDeviceManager class
     * switch (eDM.getEnterpriseSdkVer()) {
     * case ENTERPRISE_SDK_ENTERPRISE_2:
     * case ENTERPRISE_SDK_ENTERPRISE_2_1:
     * case ENTERPRISE_SDK_ENTERPRISE_2_2:
     * case ENTERPRISE_SDK_ENTERPRISE_3:
     * // Permission granted by APK Signing
     * break;
     *
     * case ENTERPRISE_SDK_ENTERPRISE_4:
     * case ENTERPRISE_SDK_ENTERPRISE_4_0_1:
     * case ENTERPRISE_SDK_ENTERPRISE_4_1:
     * case ENTERPRISE_SDK_ENTERPRISE_5:
     *
     * // Enterprise License Activation starts here
     *
     * Knox 버전 맵핑
     * https://docs.samsungknox.com/dev/common/knox-version-mapping.htm
     *
     * @param context
     * @return
     */
    private fun checkKnoxVersion(context: Context): Int {
        try {
            val apiLevel = getKnoxEngine(context)?.getAPILevel()?.let {
                if (it == KnoxParam.NOT_SUPPORTED_API) {
                    throw IllegalStateException("not support device")
                } else {
                    return@let it
                }
            } ?: run { throw IllegalStateException("not support device") }

            if (apiLevel < MIN_KNOX_API_LEVEL) {
                return KNOX_LOW_VERSION_DEVICE
            }
        } catch (e: Throwable) {
            return KNOX_NOT_SUPPORT
        }
        return KNOX_SUPPORTED_DEVICE
    }

    private fun getDisplaySize(context: Context): Point {
        val size = Point()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        display.getRealSize(size)
        val width = size.x
        val height = size.y
        RLog.i("Display getRealsize : x : $width  Y : $height")
        return size
    }

    fun isRunningPkg(context: Context, pkgName: String): Boolean {
        return getKnoxEngine(context = context)?.isRunningApp(pkgName) ?: false
    }

    fun getMemoryUsage(context: Context, pkgName: String): Long {
        return getKnoxEngine(context = context)?.let {
            if (!it.isMemoryUsageSupported()) INVALID_MEMORY
            else it.getMemoryUsage(pkgName)
        } ?: INVALID_MEMORY
    }

    fun adminActivation(activity: Activity, activateCallBack: (Boolean) -> Unit) {
        getKnoxEngine(activity)?.activateDeviceAdministrator(activity, object : AdminActivateCallback {
            override fun onDisabled() {
                activateCallBack.invoke(false)
            }

            override fun onEnabled() {
                activateCallBack.invoke(true)
            }
        })
    }

    fun isActivated(context: Context): Boolean {
        return getKnoxEngine(context)?.isDeviceAdministratorActive() ?: false
    }

    fun getCpuUsage(context: Context, pkgName: String): Long {
        return getKnoxEngine(context)?.let {
            if (!it.isCpuUsageSupported()) INVALID_CPU
            else it.getCpuUsage(pkgName)
        } ?: INVALID_CPU
    }
}

interface Updatable {
    fun update(): String
}