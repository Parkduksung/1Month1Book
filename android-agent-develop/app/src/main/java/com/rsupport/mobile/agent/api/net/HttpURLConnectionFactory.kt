package com.rsupport.mobile.agent.api.net

import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

class HttpURLConnectionFactory {

    private val DO_NOT_VERIFY = HostnameVerifier { hostname, session -> true }

    @Throws(IOException::class, MalformedURLException::class)
    fun create(serverURL: String): HttpURLConnection {
        return URL(serverURL).let {
            if (serverURL.contains("https")) {
                (it.openConnection() as HttpsURLConnection).apply {
                    hostnameVerifier = DO_NOT_VERIFY
                }
            } else {
                it.openConnection() as HttpURLConnection
            }
        }
    }
}