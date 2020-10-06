package com.rsupport.mobile.agent.api.parser

import com.google.gson.Gson
import com.rsupport.mobile.agent.api.model.FcmRegisterResult
import com.rsupport.mobile.agent.api.model.GsonReposeDao
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.rscommon.exception.RSException
import com.rsupport.util.log.RLog
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class FcmRegisterParser : StreamParser<FcmRegisterResult> {

    override fun parse(inputStream: InputStream): Result<FcmRegisterResult> {
        var isSuccess = false
        var errorCode = ErrorCode.UNKNOWN_ERROR

        return inputStream.use {
            try {
                val reader = BufferedReader(InputStreamReader(it))
                val gsonReposeDao = Gson().fromJson(reader, GsonReposeDao::class.java)
                if ("0" == gsonReposeDao.result) {
                    isSuccess = true
                } else {
                    errorCode = gsonReposeDao.result?.toInt() ?: ErrorCode.UNKNOWN_ERROR
                    isSuccess = false
                }
            } catch (e: Exception) {
                RLog.e(e)
            }

            if (isSuccess) {
                Result.success(FcmRegisterResult())
            } else {
                Result.failure(RSException(errorCode))
            }
        }
    }
}