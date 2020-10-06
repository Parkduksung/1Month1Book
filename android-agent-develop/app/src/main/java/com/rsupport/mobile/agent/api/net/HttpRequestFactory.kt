package com.rsupport.mobile.agent.api.net

import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpRequestBase

class PostHttpRequestFactory : HttpRequestFactory {
    override fun create(url: String): HttpRequestBase {
        return HttpPost(url)
    }
}

class GetHttpRequestFactory : HttpRequestFactory {
    override fun create(url: String): HttpRequestBase {
        return HttpGet(url)
    }
}

interface HttpRequestFactory {
    fun create(url: String): HttpRequestBase
}