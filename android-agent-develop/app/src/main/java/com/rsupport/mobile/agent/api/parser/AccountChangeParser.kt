package com.rsupport.mobile.agent.api.parser

import com.rsupport.mobile.agent.api.model.AccountChangeResult
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.XMLParser
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import java.io.InputStream
import java.util.*

class AccountChangeParser : StreamParser<AccountChangeResult> {
    override fun parse(inputStream: InputStream): Result<AccountChangeResult> {
        return inputStream.use {
            var errorCode = RSErrorCode.UNKNOWN
            var errorMsg = ""
            var isSuccess = false
            val map: HashMap<String, String> = XMLParser().parse(it)

            if (map.isEmpty()) {
                return Result.failure(RSException(RSErrorCode.Parser.XML_IO_ERROR))
            }

            val keys: Set<String> = map.keys
            val iterator = keys.iterator()

            while (iterator.hasNext()) {
                val key = iterator.next().toUpperCase()
                val value = map[key] ?: continue

                if (key == "RESULT") {
                    if (value == "0") {
                        isSuccess = true
                    } else {
                        errorCode = value.toDouble().toInt()
                    }
                } else if (key == "ERRORMSG") {
                    errorMsg = value
                }
            }

            if (isSuccess) {
                Result.success(AccountChangeResult())
            } else {
                Result.failure(RSException(errorCode, errorMsg))
            }

        }
    }
}