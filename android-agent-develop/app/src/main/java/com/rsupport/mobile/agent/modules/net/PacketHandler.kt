package com.rsupport.mobile.agent.modules.net

import com.rsupport.mobile.agent.modules.net.model.Packet

interface PacketHandler<T : Packet> {
    fun onReceive(payload: Int, packet: T)
}