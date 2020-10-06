package com.rsupport.mobile.agent.repo.config

import android.text.TextUtils

data class ServerInfo(val url: String) {
    fun isAvailable(): Boolean {
        return !TextUtils.isEmpty(url)
    }
}