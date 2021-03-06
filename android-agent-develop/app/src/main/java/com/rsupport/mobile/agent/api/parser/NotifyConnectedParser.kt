package com.rsupport.mobile.agent.api.parser

import com.rsupport.mobile.agent.api.model.NotifyConnectedResult
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.XMLParser
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import com.rsupport.util.log.RLog
import java.io.InputStream
import java.util.*

class NotifyConnectedParser : StreamParser<NotifyConnectedResult> {
    override fun parse(inputStream: InputStream): Result<NotifyConnectedResult> {

        return inputStream.use {
            var isSuccess = false
            var errorCode = RSErrorCode.UNKNOWN

            val map: HashMap<String, String> = XMLParser().parse(it)

            if (map.isEmpty()) {
                return@use Result.failure(RSException(RSErrorCode.Parser.XML_IO_ERROR))
            }

            val keys: Set<String> = map.keys
            val it = keys.iterator()

            while (it.hasNext()) {
                val key = it.next().toUpperCase()
                val value = map[key]
                if (key == "RESULT") {
                    if (value == "0") {
                        isSuccess = true
                    } else {
                        errorCode = value?.toDoubleOrNull()?.toInt() ?: RSErrorCode.UNKNOWN
                    }
                }
                RLog.d("agentSessionResult : $key=$value")
            }

            if (isSuccess) {
                Result.success(NotifyConnectedResult())
            } else {
                Result.failure(RSException(errorCode))
            }
        }
    }
}