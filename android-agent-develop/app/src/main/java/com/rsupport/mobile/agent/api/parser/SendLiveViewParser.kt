package com.rsupport.mobile.agent.api.parser

import com.rsupport.mobile.agent.api.model.SendLiveViewResult
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.Result.Companion.failure
import com.rsupport.mobile.agent.utils.XMLParser
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import java.io.InputStream
import java.util.*

class SendLiveViewParser : StreamParser<SendLiveViewResult> {
    override fun parse(inputStream: InputStream): Result<SendLiveViewResult> {
        return inputStream.use {
            var errorCode = RSErrorCode.UNKNOWN
            var isSuccess = false
            var isStop = false

            val map: HashMap<String, String> = XMLParser().parse(it)
            if (map.isEmpty()) {
                return@use failure(RSException(RSErrorCode.Parser.XML_IO_ERROR))
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
                } else if (key == "STOP") {
                    if (value == "1") {
                        isStop = true
                    }
                }
            }
            if (isStop) {
                isSuccess = false
            }

            if (isSuccess) {
                return@use Result.success(SendLiveViewResult())
            } else {
                return@use Result.failure(RSException(errorCode))
            }
        }
    }
}