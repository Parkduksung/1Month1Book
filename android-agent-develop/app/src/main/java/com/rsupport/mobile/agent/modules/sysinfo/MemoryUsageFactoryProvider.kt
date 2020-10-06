package com.rsupport.mobile.agent.modules.sysinfo

import android.app.ActivityManager
import android.content.Context
import com.rsupport.knox.KnoxManagerCompat
import com.rsupport.mobile.agent.modules.engine.EngineType
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import com.rsupport.mobile.agent.modules.memory.MemoryUsage
import com.rsupport.mobile.agent.modules.memory.UsageContainer
import com.rsupport.mobile.agent.modules.memory.dumpsys.DumpsysMemoryUsageFactory
import com.rsupport.mobile.agent.modules.memory.dumpsys.DumpsysReader
import com.rsupport.mobile.agent.modules.memory.knox.KnoxMemoryUsageFactory
import com.rsupport.mobile.agent.modules.memory.shell.ProcStatMemoryUsageFactory
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppFactory
import com.rsupport.mobile.agent.utils.SdkVersion
import com.rsupport.util.log.RLog
import org.koin.java.KoinJavaComponent.inject

class MemoryUsageFactoryProvider {


    companion object {
        @JvmStatic
        fun get(): MemoryUsage.Factory {
            val engineTypeCheck by inject(EngineTypeCheck::class.java)
            return when (engineTypeCheck.getEngineType()) {
                EngineType.ENGINE_TYPE_RSPERM -> {
                    val sdkVersion by inject(SdkVersion::class.java)
                    if (sdkVersion.lessThanOrEqual19()) {
                        // Kitkat 이하에서는 proc에 직접 접근해서 사용한다.
                        val activityManager by inject(ActivityManager::class.java)
                        ProcStatMemoryUsageFactory(activityManager)
                    } else {
                        DumpsysMemoryUsageFactory(DumpsysReader.createMemInfo())
                    }
                }
                EngineType.ENGINE_TYPE_KNOX -> {
                    val context by inject(Context::class.java)
                    val knoxManagerCompat by inject(KnoxManagerCompat::class.java)
                    val runningAppFactory by inject(RunningAppFactory::class.java)
                    KnoxMemoryUsageFactory(context, runningAppFactory, knoxManagerCompat)
                }
                else -> {
                    RLog.w("EmptyFactory created")
                    EmptyFactory()
                }
            }
        }
    }

    class EmptyFactory : MemoryUsage.Factory {
        override fun get(): UsageContainer<MemoryUsage> {
            return UsageContainer(emptyList())
        }

        override fun close() {
        }
    }
}