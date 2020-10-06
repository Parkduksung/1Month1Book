package com.rsupport.mobile.agent

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex

class AgentApplication : Application() {
    private val baseApplication = AgentBaseApplication();
    override fun onCreate() {
        baseApplication.onCreate(this)
        super.onCreate()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}