package com.rsupport.mobile.agent.modules.engine

import android.app.Activity
import android.content.Context
import android.view.WindowManager
import androidx.annotation.WorkerThread
import com.rsupport.knox.KnoxManagerCompat
import com.rsupport.knox.Updatable
import com.rsupport.media.mediaprojection.utils.DisplayUtils
import com.rsupport.mobile.agent.BuildConfig
import com.rsupport.mobile.agent.api.ApiService
import com.rsupport.mobile.agent.constant.Global
import com.rsupport.mobile.agent.utils.OpenClass
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.sony.SonyManager
import config.EngineConfigSetting
import org.koin.java.KoinJavaComponent

@OpenClass
class EngineTypeCheck(private val context: Context, private val knoxManager: KnoxManagerCompat, private val sonyManager: SonyManager) {
    //Default Rsperm
    private var engineType = EngineType.ENGINE_TYPE_RSPERM

    /**
     * 사용 가능한 Engine 종류를 설정한다.
     * [getEngineType]
     *
     * [EngineType.ENGINE_TYPE_KNOX],
     * [EngineType.ENGINE_TYPE_RSPERM],
     * [EngineType.ENGINE_TYPE_SONY]
     */
    @WorkerThread
    fun checkEngineType() {
        val type = knoxManager.isKnoxAvailable(context)
        if (type == KnoxManagerCompat.KNOX_SUPPORTED_DEVICE) {
            engineType = EngineType.ENGINE_TYPE_KNOX
            EngineConfigSetting.setIsKnox(true)
            updateTouchPoint()
        } else if (isSonyAvailable(context)) {
            engineType = EngineType.ENGINE_TYPE_SONY
        } else {
            engineType = EngineType.ENGINE_TYPE_RSPERM
        }
    }

    fun getEngineType(): Int {
        return engineType
    }

    /**
     * Engine 상태를 확인하여 활성화 시킨다.
     * @param activationCallback Engine을 사용할 수 없는 상태면 false 사용할 수 있는 상태면 true
     */
    @WorkerThread
    fun activateEngineState(activationCallback: (Boolean) -> Unit) { //STEP 1  KNOX 가능한지 체크.
        checkEngineType()
        if (engineType == EngineType.ENGINE_TYPE_KNOX) {
            knoxManager.activation(context, activationCallback)
        } else {
            // knox 외에는 모두 활성화상태로 응답한다.
            activationCallback.invoke(true)
        }
    }

    @WorkerThread
    fun activateEngineState(activity: Activity, activationCallback: (Boolean) -> Unit) { //STEP 1  KNOX 가능한지 체크.
        checkEngineType()
        if (engineType == EngineType.ENGINE_TYPE_KNOX) {
            knoxManager.activation(context) { licenseActivate ->
                if (licenseActivate) {
                    knoxManager.adminActivation(activity) {
                        activationCallback.invoke(it)
                    }
                } else {
                    activationCallback.invoke(false)
                }
            }
        } else {
            // knox 외에는 모두 활성화상태로 응답한다.
            activationCallback.invoke(true)
        }
    }

    private fun updateTouchPoint() {
        (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.let { windowManager ->
            val encodingScreenSize = DisplayUtils.detectVirtualDisplaySize(windowManager.defaultDisplay, 480)
            knoxManager.checkInvalidTouchPoint(
                    context,
                    DisplayUtils.getKnoxMaxDisplay(windowManager.defaultDisplay, encodingScreenSize)
            )
        }
    }

    private fun isSonyAvailable(context: Context): Boolean {
        return sonyManager.isServiceAvailable(context)
    }

    @WorkerThread
    fun isActivated(): Boolean {
        checkEngineType()
        return if (engineType == EngineType.ENGINE_TYPE_KNOX) {
            knoxManager.isActivated(context)
        }
        // Knox 외에는 모두 Active 상태이다
        else {
            return true
        }
    }
}


class KnoxKeyUpdate : Updatable {
    override fun update(): String {
        Global.getInstance().webConnection.setNetworkInfo()
        val knoxKey = KoinJavaComponent.get(ApiService::class.java).requestKnoxEnterpriseKey(BuildConfig.VERSION_NAME)
        return (knoxKey as? Result.Success)?.value?.knoxKey ?: ""
    }
}