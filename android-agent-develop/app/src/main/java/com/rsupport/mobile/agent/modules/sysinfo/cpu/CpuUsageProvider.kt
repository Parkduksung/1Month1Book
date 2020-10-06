package com.rsupport.mobile.agent.modules.sysinfo.cpu

import com.rsupport.mobile.agent.modules.engine.EngineType
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import com.rsupport.mobile.agent.modules.memory.UsageContainer
import com.rsupport.mobile.agent.modules.memory.dumpsys.DumpsysReader
import com.rsupport.mobile.agent.modules.sysinfo.cpu.kitkat.CpuUsageKitkatFactory
import com.rsupport.mobile.agent.modules.sysinfo.cpu.knox.CpuUsageKnoxFactory
import com.rsupport.mobile.agent.modules.sysinfo.cpu.rsperm.CpuUsageRspermFactory
import com.rsupport.mobile.agent.utils.SdkVersion
import org.koin.java.KoinJavaComponent.inject

/**
 * Cpu 사용량을 조회 할 수있는 factory 를 생성한다.
 * @see [CpuUsage.Factory]
 */
class CpuUsageProvider {
    private val sdkVersion by inject(SdkVersion::class.java)
    private val engineTypeCheck by inject(EngineTypeCheck::class.java)

    fun create(): CpuUsage.Factory {
        return if (sdkVersion.lessThanOrEqual19()) {
            CpuUsageKitkatFactory()
        } else {
            when (engineTypeCheck.getEngineType()) {
                EngineType.ENGINE_TYPE_KNOX -> CpuUsageKnoxFactory()
                EngineType.ENGINE_TYPE_RSPERM -> CpuUsageRspermFactory(DumpsysReader.createCpuInfo())
                else -> {
                    object : CpuUsage.Factory {
                        override fun get(): UsageContainer<CpuUsage> {
                            return UsageContainer(emptyList())
                        }

                        override fun close() {
                        }
                    }
                }
            }
        }
    }
}