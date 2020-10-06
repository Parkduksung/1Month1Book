package com.rsupport.mobile.agent.modules.channel

interface OnChannelListener {
    fun onConnecting()
    fun onConnectFail()
    fun onConnected()
    fun onDisconnected()
}