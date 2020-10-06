package com.rsupport.mobile.agent.modules.sysinfo.app

import com.rsupport.mobile.agent.modules.engine.EngineType
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import com.rsupport.mobile.agent.service.RSPermService
import com.rsupport.mobile.agent.utils.SdkVersion
import org.koin.java.KoinJavaComponent.inject

interface RunningApplication {
    fun getRunningAppInfos(): List<RunningAppInfo>

    interface Factory {
        fun create(): RunningApplication
    }
}

class RunningAppFactory(private val sdkVersion: SdkVersion, private val engineTypeCheck: EngineTypeCheck, private val rspermService: RSPermService) : RunningApplication.Factory {
    override fun create(): RunningApplication {
        if (sdkVersion.lessThanOrEqual19()) {
            return RunningApplicationKitkat()
        } else {
            when {
                engineTypeCheck.getEngineType() == EngineType.ENGINE_TYPE_KNOX -> {
                    return RunningApplicationKnox()
                }
                engineTypeCheck.getEngineType() == EngineType.ENGINE_TYPE_RSPERM -> {
                    return if (rspermService.isBind()) {
                        rspermService.getRsperm()?.let {
                            return@let RunningApplicationRsperm(it)
                        } ?: RunningApplicationKitkat()
                    } else RunningApplicationKitkat()
                }
                else -> {
                    return RunningApplicationKitkat()
                }
            }
        }
    }
}