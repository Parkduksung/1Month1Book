package com.rsupport.mobile.agent.api

import com.rsupport.rscommon.exception.RSException
import java.io.IOException
import java.io.InputStream
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException

interface WebReadStream {
    @Throws(
            RSException::class,
            IOException::class,
            IllegalBlockSizeException::class,
            BadPaddingException::class,
            InvalidAlgorithmParameterException::class,
            InvalidKeyException::class
    )
    fun getStream(): InputStream
}

