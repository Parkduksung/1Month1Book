package com.rsupport.mobile.agent.modules.net

import android.content.Context
import com.rsupport.commons.net.socket.SocketCompat

interface SocketCompatFactory {
    fun create(context: Context): SocketCompat
}