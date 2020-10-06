package com.rsupport.mobile.agent.api

import com.rsupport.mobile.agent.utils.crypto.WebCrypto

interface WebCryptoFactory {
    fun create(): WebCrypto
}

class DefaultWebCryptoFactory() : WebCryptoFactory {
    override fun create(): WebCrypto {
        return WebCrypto()
    }
}