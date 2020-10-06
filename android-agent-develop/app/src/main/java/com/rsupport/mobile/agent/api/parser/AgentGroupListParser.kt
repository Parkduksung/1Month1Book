package com.rsupport.mobile.agent.api.parser

import com.rsupport.mobile.agent.utils.XMLParser

class AgentGroupListParser(xmlParser: XMLParser = XMLParser()) : AgentGroupInfoParser(xmlParser) {
    override fun filter(key: String): Boolean = key.startsWith("ROOT_GROUP")
}