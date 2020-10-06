package com.rsupport.mobile.agent.modules.net

import android.content.Context
import com.rsupport.mobile.agent.modules.channel.screen.StreamController
import com.rsupport.mobile.agent.modules.net.channel.DataChannel
import com.rsupport.mobile.agent.modules.net.channel.DataChannelImpl
import com.rsupport.mobile.agent.service.HxdecThread

class DataChannelFactory(
        private val context: Context,
        private val mouseEventListener: DataChannelImpl.OnMouseEventListener,
        private val screenStreamController: StreamController
) : DataChannel.Factory {

    override fun create(): DataChannel {
        return DataChannelImpl(context, screenStreamController).apply {
            setMouseEventListener(mouseEventListener)
        }
    }
}