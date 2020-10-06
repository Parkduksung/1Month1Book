package com.rsupport.mobile.agent.modules.net

import android.content.Context
import com.rsupport.commons.net.socket.SocketCompat
import com.rsupport.commons.net.socket.compat.RSNetSocketCompat
import com.rsupport.commons.net.socket.compat.policy.RSNetConnectPolicy
import com.rsupport.commons.net.socket.compat.policy.RSNetDisconnectPolicy
import com.rsupport.commons.net.socket.rsnetcl.protocol.RSNetConnectionParams
import com.rsupport.commons.net.socket.rsnetcl.protocol.RSNetProxyInfo
import com.rsupport.jni.RC45SocketCompat
import com.rsupport.mobile.agent.constant.ChannelInfo
import com.rsupport.mobile.agent.constant.ViewerType

class BasicSocketCompatFactory(
        private val channelInfo: ChannelInfo,
        private val useEncrypt: Boolean = false
) : SocketCompatFactory {
    override fun create(context: Context): SocketCompat {
        return when (channelInfo.viewerType) {
            ViewerType.SCAP -> {
                RC45SocketCompat(context, RC45SocketCompat.BUFFER_SIZE, RC45SocketCompat.REMOTECALL4_ACTIVEX, channelInfo.channelRequest.channelId, channelInfo.channelRequest.connectGuid, channelInfo.vhubip, channelInfo.vhubport, channelInfo.vhubinfoArr).apply {
                    if (useEncrypt) {
                        enableEncrypt()
                    }
                }
            }
            ViewerType.XENC -> {
                RSNetSocketCompat(
                        channelInfo.channelRequest.connectGuid,
                        channelInfo.channelRequest.channelId,
                        RSNetConnectionParams().apply {
                            uri = channelInfo.vhubip
                            port = channelInfo.vhubport.toString()
                            roomId = channelInfo.channelRequest.connectGuid.plus("_").plus(channelInfo.channelRequest.channelId)
                            userId = channelInfo.channelRequest.userGuid
                            userInfo = "{}"
                            proxyInfo = channelInfo.proxyInfo?.let {
                                RSNetProxyInfo(it.address, it.port.toIntOrNull() ?: 8080, it.id, it.pwd)
                            }
                        }
                        , RSNetConnectPolicy.create(RSNetConnectPolicy.PolicyType.AnyOne)
                        , RSNetDisconnectPolicy.create(RSNetDisconnectPolicy.PolicyType.EmptyUser)
                ).apply {
                    if (useEncrypt) {
                        enableEncrypt()
                    }
                }
            }
        }
    }

}

