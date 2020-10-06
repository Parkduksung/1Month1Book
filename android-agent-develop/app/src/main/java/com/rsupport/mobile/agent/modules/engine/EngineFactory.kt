package com.rsupport.mobile.agent.modules.engine

import android.content.Context
import com.rsupport.knox.KnoxManagerCompat
import com.rsupport.sony.SonyManager
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.get
import org.koin.java.KoinJavaComponent.inject

interface EngineFactory {
    fun create(): EngineTypeCheck
}

class DefaultEngineFactory(private val context: Context) : EngineFactory {

    private val knoxManagerCompat by inject(KnoxManagerCompat::class.java)

    override fun create(): EngineTypeCheck {
        knoxManagerCompat.knoxKeyUpdatable = get(qualifier = named("knox"), clazz = KnoxKeyUpdate::class.java)
        return EngineTypeCheck(context.applicationContext, knoxManagerCompat, SonyManager.getInstance())
    }
}