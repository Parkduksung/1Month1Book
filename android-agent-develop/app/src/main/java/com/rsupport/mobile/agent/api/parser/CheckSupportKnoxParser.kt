package com.rsupport.mobile.agent.api.parser

import com.rsupport.mobile.agent.api.model.CheckSupportKnoxResult
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.XMLParser
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import org.xml.sax.SAXException
import java.io.InputStream
import java.util.*

class CheckSupportKnoxParser(private val xmlParser: XMLParser = XMLParser()) : StreamParser<CheckSupportKnoxResult> {
    override fun parse(inputStream: InputStream): Result<CheckSupportKnoxResult> {
        return inputStream.use {
            try {
                var isSuccess = false
                var errorCode = RSErrorCode.UNKNOWN
                xmlParser.endPrefixMapping("&")
                val xmlMap: HashMap<String, String> = xmlParser.parse(it)
                if (xmlMap.isEmpty()) {
                    return@use Result.failure(RSException(RSErrorCode.Parser.XML_IO_ERROR))
                }

                val keys: Set<String> = xmlMap.keys
                val iterator = keys.iterator()

                while (iterator.hasNext()) {
                    val key = iterator.next().toUpperCase()
                    val value = xmlMap[key]
                    if (key == "RETCODE") {
                        if (value == "0" || value == "100") {
                            isSuccess = true
                        } else {
                            errorCode = value?.toDoubleOrNull()?.toInt() ?: RSErrorCode.UNKNOWN
                        }
                    }
                }
                if (isSuccess) {
                    Result.success(CheckSupportKnoxResult())
                } else {
                    Result.failure(RSException(errorCode))
                }
            } catch (sae: SAXException) {
                Result.failure(RSException(RSErrorCode.Parser.XML_SAX_ERROR))
            }
        }
    }
}