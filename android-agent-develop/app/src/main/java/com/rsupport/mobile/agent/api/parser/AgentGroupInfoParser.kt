package com.rsupport.mobile.agent.api.parser

import com.rsupport.mobile.agent.api.model.GroupInfo
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.XMLParser
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import com.rsupport.util.log.RLog
import org.xml.sax.SAXException
import java.io.InputStream
import java.util.*
import java.util.regex.Pattern

abstract class AgentGroupInfoParser(private val xmlParser: XMLParser = XMLParser()) : StreamParser<List<GroupInfo>> {

    protected abstract fun filter(key: String): Boolean

    override fun parse(inputStream: InputStream): Result<List<GroupInfo>> {
        return inputStream.use {
            try {
                var isSuccess = false
                var errorCode = ErrorCode.UNKNOWN_ERROR

                xmlParser.endPrefixMapping("&")
                val xmlMap: HashMap<String, String> = xmlParser.parse(it)

                if (xmlMap.isEmpty()) {
                    return@use Result.failure(RSException(RSErrorCode.Parser.XML_IO_ERROR, ""))
                }

                val keys: Set<String> = xmlMap.keys
                val iterator = keys.iterator()

                val agentGroupInfoList = mutableListOf<GroupInfo>()

                while (iterator.hasNext()) {
                    val key = iterator.next().toUpperCase()
                    val value = xmlMap[key]
                    if (key == "RETCODE") {
                        if (value == "100") {
                            isSuccess = true
                        } else {
                            errorCode = value?.toDouble()?.toInt() ?: ErrorCode.UNKNOWN_ERROR
                        }
                    } else if (filter(key) && key.startsWith("ROOT_GROUP")) {
                        value?.let {
                            agentGroupInfoList.add(getNewGroup(value))
                        }
                    } else if (filter(key) && key.startsWith("GROUP")) {
                        value?.let {
                            agentGroupInfoList.add(getNewGroup(value).apply {
                                this.key = key
                            })
                        }
                    }
                    RLog.d("getAgentList : key : $key : value : $value")
                }

                if (isSuccess) {
                    Result.success(agentGroupInfoList)
                } else {
                    Result.failure(RSException(errorCode))
                }

            } catch (sax: SAXException) {
                Result.failure(RSException(RSErrorCode.Parser.XML_SAX_ERROR))
            }
        }
    }


    private fun getNewGroup(line: String): GroupInfo {
        val groupInfo = GroupInfo()
        val st = StringTokenizer(line, ";")
        val p = Pattern.compile("<!\\[CDATA\\[(.*)]]>")
        while (st.hasMoreTokens()) {
            val token = st.nextToken()
            val pos = token.indexOf('=')
            if (pos != -1) {
                val key = token.substring(0, pos).toLowerCase()
                val value = token.substring(pos + 1)
                if (key.equals("grpid", ignoreCase = true)) {
                    groupInfo.grpid = value
                } else if (key.equals("pgrpid", ignoreCase = true)) {
                    groupInfo.pgrpid = value
                } else if (key.equals("grpname", ignoreCase = true)) {
                    val m = p.matcher(value)
                    if (m.matches()) {
                        //strip CDATA tags
                        groupInfo.grpname = m.group(1)
                    } else {
                        groupInfo.grpname = value
                    }
                } else if (key.equals("agent_group_count", ignoreCase = true)) {
                    groupInfo.grpCount = value
                }
            }
        }
        return groupInfo
    }
}