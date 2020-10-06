package com.rsupport.mobile.agent.utils

import android.content.Context
import org.koin.java.KoinJavaComponent.inject

class NetworkUtils {

    private val context by inject(Context::class.java)

    /**
     * 네트워크를 사용 가능한지를 확인한다.
     * @return 네트워크를 사용 가능하면 true, 그렇지 않으면 false
     */
    fun isAvailableNetwork(): Boolean {
        return Utility.isOnline(context)
    }
}