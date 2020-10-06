package com.rsupport.mobile.agent.repo.device

import android.content.Context
import org.koin.java.KoinJavaComponent.inject
import com.rsupport.mobile.agent.constant.GlobalStatic

class DeviceRepository {
    private val context: Context by inject(Context::class.java)

    val macAddress: String by lazy {
        GlobalStatic.getMacAddress(context)
    }

    val localIP: String
        get() = GlobalStatic.getLocalIP()


}