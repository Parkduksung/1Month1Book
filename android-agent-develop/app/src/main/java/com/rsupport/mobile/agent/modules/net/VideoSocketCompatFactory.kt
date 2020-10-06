package com.rsupport.mobile.agent.modules.net

import android.content.Context
import com.rsupport.commons.net.socket.SocketCompat
import com.rsupport.commons.net.socket.compat.RSNetVideoSocketCompat
import com.rsupport.commons.net.socket.compat.policy.OnReconnectListener
import com.rsupport.commons.net.socket.compat.policy.RSNetConnectPolicy
import com.rsupport.commons.net.socket.compat.policy.RSNetDisconnectPolicy
import com.rsupport.commons.net.socket.compat.policy.RSNetReConnectPolicy
import com.rsupport.commons.net.socket.rsnetcl.callback.OnFPSChangedCallback
import com.rsupport.commons.net.socket.rsnetcl.protocol.RSNetConnectionParams
import com.rsupport.commons.net.socket.rsnetcl.protocol.RSNetProxyInfo
import com.rsupport.mobile.agent.constant.ChannelInfo
import com.rsupport.mobile.agent.constant.ViewerType

class VideoSocketCompatFactory(
        private val channelInfo: ChannelInfo,
        private val useEncrypt: Boolean = false,
        private val onFPSChangedCallback: OnFPSChangedCallback,
        private val onReconnectListener: OnReconnectListener
) : SocketCompatFactory {

    override fun create(context: Context): SocketCompat {
        return when (channelInfo.viewerType) {
            ViewerType.SCAP -> BasicSocketCompatFactory(channelInfo, useEncrypt).create(context)
            ViewerType.XENC -> {
                RSNetVideoSocketCompat(channelInfo.channelRequest.connectGuid, channelInfo.channelRequest.channelId, RSNetConnectionParams().apply {
                    uri = channelInfo.vhubip
                    port = channelInfo.vhubport.toString()
                    roomId = channelInfo.channelRequest.connectGuid.plus("_").plus(channelInfo.channelRequest.channelId)
                    userId = channelInfo.channelRequest.userGuid
                    userInfo = "{}"
                    proxyInfo = channelInfo.proxyInfo?.let {
                        RSNetProxyInfo(it.address, it.port.toIntOrNull() ?: 8080, it.id, it.pwd)
                    }
                }
                        , onFPSChangedCallback
                        , RSNetConnectPolicy.create(RSNetConnectPolicy.PolicyType.AnyOne)
                        , RSNetDisconnectPolicy.create(RSNetDisconnectPolicy.PolicyType.DeferredEmptyUser())
                        , RSNetReConnectPolicy.create(RSNetReConnectPolicy.PolicyType.Retry(onReconnectListener = onReconnectListener))
                ).apply {
                    if (useEncrypt) {
                        enableEncrypt()
                    }
                }
            }
        }
    }
}