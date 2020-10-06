package com.rsupport.mobile.agent.api.net

import com.rsupport.mobile.agent.constant.ComConstant
import com.rsupport.mobile.agent.repo.config.ProxyInfo
import com.rsupport.rscommon.exception.RSException
import com.rsupport.util.log.RLog
import org.apache.http.HttpHost
import org.apache.http.auth.AuthSchemeProvider
import org.apache.http.auth.AuthScope
import org.apache.http.auth.NTCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.HttpClient
import org.apache.http.client.config.AuthSchemes
import org.apache.http.config.Lookup
import org.apache.http.config.RegistryBuilder
import org.apache.http.impl.auth.BasicSchemeFactoryHC4
import org.apache.http.impl.auth.DigestSchemeFactoryHC4
import org.apache.http.impl.auth.NTLMSchemeFactory
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClients
import java.lang.Exception


class ProxyHttpClientFactory(private val proxyInfo: ProxyInfo) : HttpClientFactory {

    @Throws(RSException::class)
    override fun create(): HttpClient {
        try {
            val proxyAddr: String = proxyInfo.address
            val proxyPort: Int = proxyInfo.port.toInt()
            val proxyUsername: String = proxyInfo.id
            val proxyPasswd: String = proxyInfo.pwd

            val authSchemeRegistryLookup: Lookup<AuthSchemeProvider> = RegistryBuilder.create<AuthSchemeProvider>()
                    .register(AuthSchemes.NTLM, NTLMSchemeFactory())
                    .register(AuthSchemes.DIGEST, DigestSchemeFactoryHC4())
                    .register(AuthSchemes.BASIC, BasicSchemeFactoryHC4())
                    .build()

            val ntCreds = NTCredentials(proxyUsername, proxyPasswd, "", "")
            val credsProvider: CredentialsProvider = BasicCredentialsProvider()
            credsProvider.setCredentials(AuthScope(proxyAddr, proxyPort), ntCreds)
            val proxy = HttpHost(proxyAddr, proxyPort)
            return HttpClients.custom()
                    .setDefaultCredentialsProvider(credsProvider)
                    .setDefaultAuthSchemeRegistry(authSchemeRegistryLookup)
                    .setProxy(proxy)
                    .build()
        } catch (e: Exception) {
            RLog.e(e)
            throw RSException(ComConstant.NET_ERR_PROXY_VERIFY.toInt())
        }

    }
}

interface HttpClientFactory {
    @Throws(RSException::class)
    fun create(): HttpClient
}