package com.rsupport.mobile.agent

import android.app.Application
import com.rsupport.mobile.agent.utils.TestLogPrinter
import com.rsupport.util.log.RLog

class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RLog.setLogPrinter(TestLogPrinter())
    }
}