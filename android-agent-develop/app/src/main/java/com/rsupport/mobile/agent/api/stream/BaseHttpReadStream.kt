package com.rsupport.mobile.agent.api.stream

import com.rsupport.mobile.agent.api.WebReadStream
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import com.rsupport.util.log.RLog
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.InputStream

abstract class BaseHttpReadStream : WebReadStream {

    @Throws(RSException::class)
    protected abstract fun getInputStream(): InputStream

    @Throws(RSException::class)
    override fun getStream(): InputStream {
        return try {
            ByteArrayInputStream(
                    BufferedInputStream(getInputStream()).use {
                        it.readBytes()
                    }
            )
        } catch (e: RSException) {
            RLog.e(e)
            throw e
        } catch (e: Exception) {
            RLog.e(e)
            throw RSException(RSErrorCode.Network.IO_ERROR)
        }
    }
}