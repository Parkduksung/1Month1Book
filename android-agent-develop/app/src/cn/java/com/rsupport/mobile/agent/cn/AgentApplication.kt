package com.rsupport.mobile.agent.cn

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.rsupport.mobile.agent.AgentBaseApplication
import com.rsupport.mobile.agent.modules.push.RSPushMessaging
import com.rsupport.mobile.agent.modules.push.delegate.RSPushNotificationDelegateImpl

class AgentApplication : Application() {

    private val baseApplication = AgentBaseApplication()

    override fun onCreate() {
        baseApplication.onCreate(this)
        RSPushMessaging.getInstance().apply {
            setPushDelegate(RSPushNotificationDelegateImpl())
            setPublisherReconnect(true)
        }
        super.onCreate()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}