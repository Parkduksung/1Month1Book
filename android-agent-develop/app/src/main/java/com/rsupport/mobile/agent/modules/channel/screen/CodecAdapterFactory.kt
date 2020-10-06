package com.rsupport.mobile.agent.modules.channel.screen

import com.rsupport.media.ICodecAdapter
import com.rsupport.mobile.agent.constant.ChannelInfo
import com.rsupport.mobile.agent.constant.ViewerType
import com.rsupport.mobile.agent.modules.channel.CRCAgentScreenChannel

class CodecAdapterFactory(private val channel: CRCAgentScreenChannel) {
    fun create(channelInfo: ChannelInfo): ICodecAdapter = when (channelInfo.viewerType) {
        ViewerType.SCAP -> RC45CodecAdapter(channel)
        ViewerType.XENC -> RSNetCodecAdapter(channel)
    }
}