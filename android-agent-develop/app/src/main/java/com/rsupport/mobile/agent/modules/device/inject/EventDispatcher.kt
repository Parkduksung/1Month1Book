package com.rsupport.mobile.agent.modules.device.inject

import android.content.Context
import android.view.KeyEvent
import androidx.annotation.WorkerThread
import com.rsupport.knox.KnoxManagerCompat
import com.rsupport.mobile.agent.modules.engine.EngineType
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import com.rsupport.mobile.agent.service.RSPermService
import com.rsupport.sony.SonyManager
import com.rsupport.util.log.RLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent
import org.koin.java.KoinJavaComponent.inject

interface EventDispatcher<T : KeyPadEvent> {

    @WorkerThread
    fun dispatch(event: T): Boolean

    interface Factory {
        fun create(): EventDispatcher<KeyPadEvent.Events>
    }
}


class EngineEventDispatcherFactory(private val engineTypeCheck: EngineTypeCheck, private val sonyManager: SonyManager) : EventDispatcher.Factory {

    override fun create(): EventDispatcher<KeyPadEvent.Events> {
        return when (engineTypeCheck.getEngineType()) {
            EngineType.ENGINE_TYPE_RSPERM -> RSPermEventDispatcher()
            EngineType.ENGINE_TYPE_KNOX -> KnoxEventDispatcher()
            EngineType.ENGINE_TYPE_SONY -> SonyEventDispatcher(sonyManager)
            else -> LogPrintDispatcher()
        }
    }
}

class RSPermEventDispatcher : EventDispatcher<KeyPadEvent.Events> {
    private val rspermService: RSPermService by KoinJavaComponent.inject(RSPermService::class.java)

    override fun dispatch(event: KeyPadEvent.Events): Boolean {
        if (!rspermService.isBind()) {
            return false
        }

        try {
            event.events.forEach {
                rspermService.getRsperm()?.injectKeyEvent(it.action + 100, it.keyCode)
            }
        } catch (e: Exception) {
            RLog.e(e)
        }
        return true
    }
}

class KnoxEventDispatcher : EventDispatcher<KeyPadEvent.Events> {

    private val context by inject(Context::class.java)
    private val knoxManagerCompat by inject(KnoxManagerCompat::class.java)

    override fun dispatch(event: KeyPadEvent.Events): Boolean {
        event.events.forEach {
            knoxManagerCompat.injectKeyEvent(context, KeyEvent(it.action, it.keyCode))
        }
        return true
    }
}

class SonyEventDispatcher(private val sonyManager: SonyManager) : EventDispatcher<KeyPadEvent.Events> {
    private val context by inject(Context::class.java)

    override fun dispatch(event: KeyPadEvent.Events): Boolean {
        if (sonyManager.bindGet(context, 2000)) {
            event.events.forEach {
                sonyManager.injectKeyEvent(KeyEvent(it.action, it.keyCode))
            }
        }
        return true
    }
}

class LogPrintDispatcher() : EventDispatcher<KeyPadEvent.Events> {
    override fun dispatch(event: KeyPadEvent.Events): Boolean {
        CoroutineScope(Dispatchers.IO).launch {
            event.events.forEach {
                RLog.w("dispatch.keyPadEvent.$it")
            }
        }
        return true
    }
}