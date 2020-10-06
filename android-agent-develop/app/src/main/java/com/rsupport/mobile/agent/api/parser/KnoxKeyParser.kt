package com.rsupport.mobile.agent.api.parser

import android.text.TextUtils
import com.rsupport.mobile.agent.api.DefaultWebCryptoFactory
import com.rsupport.mobile.agent.api.WebCryptoFactory
import com.rsupport.mobile.agent.api.model.KnoxKeyResult
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.XMLParser
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import java.io.InputStream
import java.util.*

class KnoxKeyParser(private val webCryptoFactory: WebCryptoFactory = DefaultWebCryptoFactory()) : StreamParser<KnoxKeyResult> {
    override fun parse(inputStream: InputStream): Result<KnoxKeyResult> {
        return inputStream.use {
            var isSuccess = false
            var errorCode = RSErrorCode.UNKNOWN
            val map: HashMap<String, String> = XMLParser().parse(it)
            if (map.isEmpty()) {
                return@use Result.failure(RSException(RSErrorCode.Parser.XML_IO_ERROR))
            }
            val keys: Set<String> = map.keys
            val iterator = keys.iterator()

            var knoxKey: String? = null
            while (iterator.hasNext()) {
                val key = iterator.next().toUpperCase()
                val value = map[key]
                if (key == "RETCODE") {
                    if (value == "100") {
                        isSuccess = true
                    } else {
                        errorCode = value?.toDoubleOrNull()?.toInt() ?: RSErrorCode.UNKNOWN
                    }
                } else if (key == "APIKEY") {
                    knoxKey = webCryptoFactory.create().decrypt(value)
                }
            }
            if (isSuccess && !knoxKey.isNullOrEmpty()) {
                Result.success(
                        KnoxKeyResult().apply {
                            this.knoxKey = knoxKey
                        }
                )
            } else {
                Result.failure(RSException(errorCode))
            }
        }
    }
}