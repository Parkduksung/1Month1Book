package com.rsupport.mobile.agent.api.parser

import com.rsupport.mobile.agent.utils.XMLParser

class AgentGroupSearchParser(xmlParser: XMLParser = XMLParser()) : AgentGroupInfoParser(xmlParser) {
    override fun filter(key: String): Boolean = key.startsWith("GROUP")
}