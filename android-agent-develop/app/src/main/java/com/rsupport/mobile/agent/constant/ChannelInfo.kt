package com.rsupport.mobile.agent.constant

import com.rsupport.mobile.agent.modules.channel.ChannelRequest
import com.rsupport.mobile.agent.repo.config.ProxyInfo
import java.util.*

class ChannelInfo {
    var vhubip: String? = null
    var vhubport: Int = 0
    var vhubinfoArr = ArrayList<Array<String>>()
    var viewerType: ViewerType = ViewerType.SCAP
    var channelRequest: ChannelRequest = ChannelRequest(0, 0, "", "")
    var proxyInfo: ProxyInfo? = null

    override fun toString(): String {
        return "ChannelInfo(vhubip=$vhubip, vhubport=$vhubport, vhubinfoArr=$vhubinfoArr, channelRequest=$channelRequest, viewerType=$viewerType)"
    }


}

enum class ViewerType {
    SCAP, XENC
}