package com.rsupport.mobile.agent.extension

import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.rscommon.exception.RSException


fun Throwable.toRSExceptionOrNull(): RSException? {
    return this as? RSException
}

fun Throwable.getErrorCode(defaultErrorCode: Int = ErrorCode.UNKNOWN_ERROR): Int {
    return (this as? RSException)?.errorCode ?: defaultErrorCode
}

fun Throwable.getErrorCode(): Int {
    return (this as? RSException)?.errorCode ?: ErrorCode.UNKNOWN_ERROR
}