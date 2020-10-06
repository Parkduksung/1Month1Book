package com.rsupport.mobile.agent.api.parser

import com.rsupport.mobile.agent.api.model.AgentInstallResult
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.XMLParser
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import com.rsupport.util.log.RLog
import java.io.InputStream
import java.util.*

class AgentInstallParser : StreamParser<AgentInstallResult> {

    override fun parse(inputStream: InputStream): Result<AgentInstallResult> {
        return inputStream.use {
            val map: HashMap<String, String> = XMLParser().parse(inputStream)
            if (map.isEmpty()) {
                return@use Result.failure(RSException(RSErrorCode.Parser.XML_SAX_ERROR))
            }


            var isSuccess = false
            var errorCode = ErrorCode.UNKNOWN_ERROR
            var errorMessage = ""
            val keys: Set<String> = map.keys

            val it = keys.iterator()

            while (it.hasNext()) {
                val key = it.next().toUpperCase()
                val value = map[key]
                if (key == "RESULT") {
                    if (value == "0") {
                        isSuccess = true
                    } else {
                        errorCode = value?.toDouble()?.toInt() ?: ErrorCode.UNKNOWN_ERROR
                    }
                } else if (key == "ERRORMSG") {
                    errorMessage = value ?: ""
                }
                RLog.d("agentInstall : $key=$value")
            }
            if (isSuccess) {
                Result.success(AgentInstallResult())
            } else {
                Result.failure(RSException(errorCode, errorMessage))
            }
        }
    }
}