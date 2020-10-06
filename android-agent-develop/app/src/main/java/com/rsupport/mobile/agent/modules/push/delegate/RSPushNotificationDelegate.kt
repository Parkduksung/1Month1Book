package com.rsupport.mobile.agent.modules.push.delegate

import android.app.Service

interface RSPushNotificationDelegate {
    fun setForeground(service: Service);
    fun stopForeground(service: Service);
}