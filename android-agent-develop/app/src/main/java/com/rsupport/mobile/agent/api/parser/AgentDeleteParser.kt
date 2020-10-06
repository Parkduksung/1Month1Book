package com.rsupport.mobile.agent.api.parser

import com.rsupport.mobile.agent.api.model.AgentDeleteResult
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.Result.Companion.failure
import com.rsupport.mobile.agent.utils.XMLParser
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import java.io.InputStream
import java.util.*

class AgentDeleteParser : StreamParser<AgentDeleteResult> {
    override fun parse(inputStream: InputStream): Result<AgentDeleteResult> {
        return inputStream.use {
            var errorCode = RSErrorCode.UNKNOWN
            var isSuccess = false
            val map: HashMap<String, String> = XMLParser().parse(it)
            if (map.isEmpty()) {
                return failure(RSException(RSErrorCode.Parser.XML_IO_ERROR))
            }

            val keys: Set<String> = map.keys
            val iterator = keys.iterator()

            while (iterator.hasNext()) {
                val key = iterator.next().toUpperCase()
                val value = map[key]
                if (key == "RESULT") {
                    if (value == "0") {
                        isSuccess = true
                    } else {
                        errorCode = value?.toDoubleOrNull()?.toInt() ?: RSErrorCode.UNKNOWN
                    }
                }
            }

            if (isSuccess) {
                Result.success(AgentDeleteResult())
            } else {
                Result.failure(RSException(errorCode))
            }
        }
    }
}