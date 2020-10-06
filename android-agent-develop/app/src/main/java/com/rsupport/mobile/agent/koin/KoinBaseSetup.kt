package com.rsupport.mobile.agent.koin

import android.app.ActivityManager
import android.content.Context
import android.os.PowerManager
import com.rsupport.mobile.agent.utils.Utility
import com.rsupport.sony.SonyManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

abstract class KoinBaseSetup {

    // android Module
    private val androidModule = module {
        single { androidContext().packageManager }
        single { androidContext().getSystemService(Context.POWER_SERVICE) as PowerManager }
        single { androidContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager }
    }

    private val baseModule = module {
        single {
            Utility()
            SonyManager.getInstance()
        }
    }

    protected abstract fun getModules(): List<Module>

    private fun createModules(): List<Module> {
        return mutableListOf(androidModule).apply {
            add(baseModule)
            addAll(getModules())
        }
    }

    fun setup(context: Context) {
        startKoin {
            androidContext(context.applicationContext)
            modules(createModules())
        }
    }
}