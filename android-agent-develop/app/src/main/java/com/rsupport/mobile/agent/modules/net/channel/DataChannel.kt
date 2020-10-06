package com.rsupport.mobile.agent.modules.net.channel

import com.rsupport.commons.net.socket.SocketCompat
import com.rsupport.mobile.agent.modules.net.OnConfigChangeListener
import com.rsupport.mobile.agent.modules.net.PacketHandler
import com.rsupport.mobile.agent.modules.net.model.MsgPacket

interface DataChannel {
    fun setSocketCompat(socketCompat: SocketCompat)
    fun setMouseEventListener(mouseEventListener: DataChannelImpl.OnMouseEventListener)
    fun getPacketHandler(): PacketHandler<MsgPacket>
    fun getConfigChangedListener(): OnConfigChangeListener
    fun close()

    interface Factory {
        fun create(): DataChannel
    }
}