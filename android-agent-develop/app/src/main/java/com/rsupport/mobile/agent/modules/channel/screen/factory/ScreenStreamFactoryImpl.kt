package com.rsupport.mobile.agent.modules.channel.screen.factory

import android.content.Context
import com.rsupport.media.stream.*
import com.rsupport.mobile.agent.modules.channel.screen.ScreenStreamFactory
import com.rsupport.mobile.agent.modules.engine.EngineType
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import com.rsupport.mobile.agent.service.RSPermService
import com.rsupport.mobile.agent.utils.SdkVersion
import com.rsupport.rsperm.IRSPerm

class ScreenStreamFactoryImpl(
        private val context: Context,
        private val engineTypeCheck: EngineTypeCheck,
        private val rspermService: RSPermService,
        private val version: SdkVersion
) : ScreenStreamFactory {

    override fun create(): ScreenStream? {
        return when (engineTypeCheck.getEngineType()) {
            EngineType.ENGINE_TYPE_RSPERM -> createScreenStreamByRSPerm()
            EngineType.ENGINE_TYPE_KNOX -> createKnoxScreenStream()
            EngineType.ENGINE_TYPE_SONY -> createSonyScreenStream()
            else -> createScreenStream()
        }
    }

    private fun createScreenStream(): RsMediaProjectionStreamVD? {
        return if (version.greaterThan21()) {
            RsMediaProjectionStreamVD(context)
        } else {
            null
        }
    }

    private fun createSonyScreenStream(): ScreenStream {
        return if (version.greaterThan21()) {
            RsMediaProjectionStreamVD(context)
        } else {
            RsSonyStream(context)
        }
    }

    private fun createKnoxScreenStream(): ScreenStream {
        return if (version.greaterThan21()) {
            RsMediaProjectionStreamVD(context)
        } else {
            RsKNOXStream(context)
        }
    }

    private fun createScreenStreamByRSPerm(): ScreenStream? {
        return if (version.greaterThan21()) {
            val rsperm = rspermService.getRsperm()
            if (rsperm != null && rsperm.isBinded) {
                RsMediaProjectionStream(context, rsperm) // 5.0 MediaProjection 처리
            } else {
                RsMediaProjectionStreamVD(context)
            }
        } else {
            val rsperm: IRSPerm? = rspermService.getRsperm()
            if (rsperm != null && rsperm.isBinded) {
                RsScreenCaptureStream(context, rsperm) // RS capture
            } else {
                null
            }
        }
    }
}