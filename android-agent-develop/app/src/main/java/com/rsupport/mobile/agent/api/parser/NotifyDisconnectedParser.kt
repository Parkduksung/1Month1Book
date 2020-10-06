package com.rsupport.mobile.agent.api.parser

import com.rsupport.mobile.agent.api.model.NotifyDisconnectedResult
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.XMLParser
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import java.io.InputStream
import java.util.*

class NotifyDisconnectedParser : StreamParser<NotifyDisconnectedResult> {
    override fun parse(inputStream: InputStream): Result<NotifyDisconnectedResult> {

        return inputStream.use {
            var isSuccess = false
            var errorCode = RSErrorCode.UNKNOWN
            val map: HashMap<String, String> = XMLParser().parse(it)

            if (map.isEmpty()) {
                return@use Result.failure(RSException(RSErrorCode.Parser.XML_IO_ERROR))
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
                Result.success(NotifyDisconnectedResult())
            } else {
                Result.failure(RSException(errorCode))
            }
        }
    }
}