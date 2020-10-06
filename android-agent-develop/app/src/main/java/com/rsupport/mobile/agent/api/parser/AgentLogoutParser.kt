package com.rsupport.mobile.agent.api.parser

import com.rsupport.mobile.agent.api.model.AgentLogoutResult
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.XMLParser
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import java.io.InputStream
import java.util.*

class AgentLogoutParser : StreamParser<AgentLogoutResult> {

    override fun parse(inputStream: InputStream): Result<AgentLogoutResult> {
        return inputStream.use {
            val map: HashMap<String, String> = XMLParser().parse(inputStream)
            if (map.isEmpty()) {
                return@use Result.failure(RSException(RSErrorCode.Parser.XML_IO_ERROR, ""))
            }

            val keys: Set<String> = map.keys
            val iterator = keys.iterator()

            var isSuccess = false
            var errorCode = ErrorCode.UNKNOWN_ERROR

            while (iterator.hasNext()) {
                val key = iterator.next().toUpperCase()
                val value = map[key]
                if (key == "RESULT") {
                    if (value == "0") {
                        isSuccess = true
                    } else {
                        errorCode = value!!.toDouble().toInt()
                    }
                }
            }
            if (isSuccess) {
                Result.success(AgentLogoutResult(true))
            } else {
                Result.failure(RSException(errorCode))
            }
        }
    }
}