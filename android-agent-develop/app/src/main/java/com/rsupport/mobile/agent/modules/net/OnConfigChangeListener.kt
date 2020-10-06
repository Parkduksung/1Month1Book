package com.rsupport.mobile.agent.modules.net

import android.content.res.Configuration

interface OnConfigChangeListener {
    fun onConfigChanged(newConfig: Configuration)
}