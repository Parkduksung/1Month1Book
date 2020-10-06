package com.rsupport.mobile.agent.modules.channel

import android.content.Context
import android.content.res.Configuration
import android.os.Looper
import com.rsupport.commons.net.socket.SocketCompat
import com.rsupport.mobile.agent.modules.net.OnConfigChangeListener
import com.rsupport.mobile.agent.modules.net.PacketHandler
import com.rsupport.mobile.agent.modules.net.channel.DataChannel
import com.rsupport.mobile.agent.modules.net.model.MsgPacket
import com.rsupport.mobile.agent.utils.timer.DebounceTimer
import com.rsupport.mobile.agent.utils.timer.ThreadDebounceTimer

class CRCAgentDataChannel(context: Context, nopTimer: DebounceTimer = ThreadDebounceTimer()) : CRCChannel(context, nopTimer) {
    private var dataPacketHandler: PacketHandler<MsgPacket>? = null
    private var dataChannelFactory: DataChannel.Factory? = null
    private var configChangeListener: OnConfigChangeListener? = null
    private var dataChannel: DataChannel? = null

    override fun onPrepare() {
    }

    override fun onConnected(socketCompat: SocketCompat) {
        dataChannel = dataChannelFactory?.create()?.apply {
            setSocketCompat(socketCompat)
            dataPacketHandler = getPacketHandler()
            configChangeListener = getConfigChangedListener()
        }
    }

    override fun onConnectFail() {
    }

    override fun onDisconnected() {
        dataChannel?.close()
        dataChannel = null
    }

    override fun onReleased() {
    }

    override fun onReceivePacket(payloadType: Int, msg: MsgPacket) {
        dataPacketHandler?.onReceive(payloadType, msg)
    }

    override fun onConfigChanged(newConfig: Configuration) {
        if (isConnected()) {
            configChangeListener?.onConfigChanged(newConfig)
        }
    }

    fun setChannelFactory(dataChannelFactory: DataChannel.Factory) {
        this.dataChannelFactory = dataChannelFactory
    }
}
