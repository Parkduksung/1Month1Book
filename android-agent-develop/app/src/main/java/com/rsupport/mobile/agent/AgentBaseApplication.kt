package com.rsupport.mobile.agent

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.text.TextUtils
import com.rsupport.litecam.binder.Binder
import com.rsupport.mobile.agent.constant.*
import com.rsupport.mobile.agent.koin.AppKoinSetup
import com.rsupport.mobile.agent.koin.KoinBaseSetup
import com.rsupport.mobile.agent.modules.function.Clipboard
import com.rsupport.mobile.agent.modules.push.RSPushMessaging
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.service.AgentLoginManager
import com.rsupport.mobile.agent.status.AgentStatus
import com.rsupport.mobile.agent.utils.Collector
import com.rsupport.mobile.agent.utils.ErrorData
import com.rsupport.mobile.agent.utils.Utility
import com.rsupport.rscommon.ILogger
import com.rsupport.rscommon.Logger
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.errorlog.ELAppType
import com.rsupport.rscommon.errorlog.ELProduct
import com.rsupport.rscommon.errorlog.RSErrorLog
import com.rsupport.util.log.RLog
import org.koin.java.KoinJavaComponent.inject

class AgentBaseApplication {

    private val koinSetup: KoinBaseSetup = AppKoinSetup()
    private val configRepository by inject(ConfigRepository::class.java)
    private val agentStatus by inject(AgentStatus::class.java)

    fun onCreate(application: Application) {
        RLog.setTag("RVAgent")
        koinSetup.setup(application)
        initLogCollector(application)
        globalInit(application)

        Thread.setDefaultUncaughtExceptionHandler(CustomExceptionHandler())
    }

    private fun initLogCollector(application: Application) {
        val applicationContext = application.applicationContext
        if (BuildConfig.DEBUG) {
            Logger.setLoggerInstance(object : ILogger {
                override fun i(p0: String?) {
                    p0?.let { RLog.i(it) }
                }

                override fun w(p0: String?) {
                    p0?.let { RLog.w(it) }
                }

                override fun d(p0: String?) {
                    p0?.let { RLog.d(it) }
                }

            })
        }

        RSErrorLog.createInstance(applicationContext, ELProduct("RemoteView", "ASP"), ELAppType.Agent, String.format("%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE), object : RSErrorLog.IELConfiguration {
            override fun getAPIURL(): String {
                return configRepository.getServerInfo().url + "/collector/el"
            }

            override fun getUploadJobServiceImplClassName(): String {
                return "com.rsupport.mobile.agent.service.ErrorLogUploadJobService"
            }

            override fun getUploadServiceImplClassName(): String {
                return "com.rsupport.mobile.agent.service.ErrorLogUploadService"
            }
        })
    }

    private fun globalInit(application: Application) {
        createNotificationChannel(application)
        Clipboard.getInstance(application)
        Utility.mainContext = application
        Global.getInstance().setAppContext(application)
        GlobalStatic.loadDeviceInfo()
        GlobalStatic.loadLibrary()
        loadSettingInfo(application)

        GlobalStatic.loadAppInfo(application)
        GlobalStatic.loadResource(application)
        GlobalStatic.loadSettingURLInfo(application)
        Global.getInstance()

        // 접속중 강제 종료되었을때 앱이 다시 시작되면 로그인 상태로 전환한다.
        if (agentStatus.get() == AgentStatus.AGENT_STATUS_REMOTING && !TextUtils.isEmpty(AgentBasicInfo.getAgentGuid(application))) {
            agentStatus.setLoggedIn()
        }

        RSPushMessaging.getInstance().setContext(application)
        AgentLoginManager.getInstence().setContext(application)
        Binder.getInstance().setContext(application)
        Global.getInstance().webConnection
    }


    private fun loadSettingInfo(application: Application) {
        var pref = application.getSharedPreferences(PreferenceConstant.RV_PREF_SETTING_INIT, Activity.MODE_PRIVATE)
        pref = application.getSharedPreferences(PreferenceConstant.RV_PREF_GUIDE_VIEW, Activity.MODE_PRIVATE)
        GlobalStatic.ISTOUCHVIEWGUIDE = pref.getBoolean("istouchviewguide", true)
        GlobalStatic.ISCURSORVIEWGUIDE = pref.getBoolean("iscursorviewguide", true)
        GlobalStatic.g_setinfoLanguage = GlobalStatic.getSystemLanguage(application)
    }

    private fun createNotificationChannel(application: Application) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName: CharSequence = application.getString(R.string.notification_channel_connect)
            val notificationChannelDescription = application.getString(R.string.notification_channel_connect_desc)
            val notificationChannel = NotificationChannel(AgentNotificationBar.AGENT_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE).apply {
                description = notificationChannelDescription
            }
            val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private class CustomExceptionHandler : Thread.UncaughtExceptionHandler {
        private val defaultUEH: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

        override fun uncaughtException(thread: Thread, throwable: Throwable) {
            val errorData = ErrorData(RSErrorCode.UNKNOWN.toString(), "App crashed: " + throwable.message, "UncaughtException")
            Collector.push(errorData)
            defaultUEH?.uncaughtException(thread, throwable)
        }
    }

}