package com.rsupport.mobile.agent.api.stream

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import com.rsupport.mobile.agent.api.net.HttpClientFactory
import com.rsupport.mobile.agent.api.net.HttpRequestFactory
import com.rsupport.mobile.agent.constant.ComConstant
import com.rsupport.mobile.agent.repo.config.ProxyInfo
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import org.apache.http.*
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPostHC4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import java.io.*

@RunWith(RobolectricTestRunner::class)
class ProxyHttpReadStreamTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val availableProxyInfo = ProxyInfo("https://rview.com", "443", "id", "pwd")

    @Before
    fun setup() {
        stopKoin()
    }

    @Test
    fun `Proxy 설정을 하지않으면 실패한다`() {
        val webReadStream = ProxyHttpReadStream.Builder()
                .build()
        val errorCode = try {
            val inputStream = webReadStream.getStream()
        } catch (e: RSException) {
            e.errorCode
        }
        assertEquals(ComConstant.NET_ERR_PROXYINFO_NULL.toInt(), errorCode)
    }

    @Test
    fun `Parameter 생성이 안되면 실패한다`() {
        val parameterGenerator = Mockito.mock(ParameterGenerator::class.java)
        whenever(parameterGenerator.generate(any())).thenThrow(UnsupportedEncodingException("UnsupportedEncodingException"))
        val webReadStream = ProxyHttpReadStream.Builder()
                .setProxyInfo(availableProxyInfo)
                .setParameterGenerator(parameterGenerator)
                .build()
        val errorCode = try {
            val inputStream = webReadStream.getStream()
        } catch (e: RSException) {
            e.errorCode
        }
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Proxy를 통해 호출이 안되면 실패한다`() {
        val httpClientFactory = Mockito.mock(HttpClientFactory::class.java)
        whenever(httpClientFactory.create()).thenThrow(RSException(ComConstant.NET_ERR_PROXY_VERIFY.toInt()))
        val webReadStream = ProxyHttpReadStream.Builder()
                .setProxyInfo(availableProxyInfo)
                .setHttpClientFactory(httpClientFactory)
                .build()
        val errorCode = try {
            val inputStream = webReadStream.getStream()
        } catch (e: RSException) {
            e.errorCode
        }
        assertEquals(ComConstant.NET_ERR_PROXY_VERIFY.toInt(), errorCode)
    }

    @Test
    fun `응답코드가 HttpStatus SC_OK 가 아니면 실패한다`() {
        val httpClientFactory = Mockito.mock(HttpClientFactory::class.java)
        val httpRequestFactory = Mockito.mock(HttpRequestFactory::class.java)
        val httpClient = Mockito.mock(HttpClient::class.java)
        val httpResponse = Mockito.mock(HttpResponse::class.java)
        val statusLine = Mockito.mock(StatusLine::class.java)
        val httpGet = Mockito.mock(HttpGet::class.java)
        whenever(httpRequestFactory.create(any())).thenReturn(httpGet)
        whenever(httpClientFactory.create()).thenReturn(httpClient)
        whenever(httpResponse.getStatusLine()).thenReturn(statusLine)
        whenever(statusLine.statusCode).thenReturn(HttpStatus.SC_BAD_GATEWAY)
        whenever(httpClient.execute(any())).thenReturn(httpResponse)
        val webReadStream = ProxyHttpReadStream.Builder()
                .setProxyInfo(availableProxyInfo)
                .setHttpClientFactory(httpClientFactory)
                .setHttpRequestFactory(httpRequestFactory)
                .build()
        val errorCode = try {
            val inputStream = webReadStream.getStream()
        } catch (e: RSException) {
            e.errorCode
        }
        assertEquals(ComConstant.NET_ERR_PROXY_VERIFY.toInt(), errorCode)
    }

    @Test
    fun `응답으로부터 데이터를 읽지 못하면 실패한다`() {
        val httpClientFactory = Mockito.mock(HttpClientFactory::class.java)
        val httpRequestFactory = Mockito.mock(HttpRequestFactory::class.java)
        val httpClient = Mockito.mock(HttpClient::class.java)
        val httpResponse = Mockito.mock(HttpResponse::class.java)
        val statusLine = Mockito.mock(StatusLine::class.java)
        val httpGet = Mockito.mock(HttpGet::class.java)

        whenever(httpRequestFactory.create(any())).thenReturn(httpGet)
        whenever(httpClientFactory.create()).thenReturn(httpClient)
        whenever(httpResponse.getStatusLine()).thenReturn(statusLine)
        whenever(statusLine.statusCode).thenReturn(HttpStatus.SC_OK)
        whenever(httpClient.execute(any())).thenReturn(httpResponse)
        val webReadStream = ProxyHttpReadStream.Builder()
                .setProxyInfo(availableProxyInfo)
                .setHttpClientFactory(httpClientFactory)
                .setHttpRequestFactory(httpRequestFactory)
                .build()
        val errorCode = try {
            val inputStream = webReadStream.getStream()
        } catch (e: RSException) {
            e.errorCode
        }
        assertEquals(ComConstant.NET_ERR_PROXY_VERIFY.toInt(), errorCode)
    }

    @Test
    fun `InputStream 으로부터 데이터를 읽지 못하면 실패한다`() {
        val httpClientFactory = Mockito.mock(HttpClientFactory::class.java)
        val httpRequestFactory = Mockito.mock(HttpRequestFactory::class.java)
        val httpClient = Mockito.mock(HttpClient::class.java)
        val httpResponse = Mockito.mock(HttpResponse::class.java)
        val statusLine = Mockito.mock(StatusLine::class.java)
        val httpGet = Mockito.mock(HttpGet::class.java)
        val httpEntity = Mockito.mock(HttpEntity::class.java)

        whenever(httpRequestFactory.create(any())).thenReturn(httpGet)
        whenever(httpClientFactory.create()).thenReturn(httpClient)
        whenever(httpResponse.getStatusLine()).thenReturn(statusLine)
        whenever(httpResponse.getEntity()).thenReturn(httpEntity)
        whenever(statusLine.statusCode).thenReturn(HttpStatus.SC_OK)
        whenever(httpClient.execute(any())).thenReturn(httpResponse)

        val webReadStream = ProxyHttpReadStream.Builder()
                .setProxyInfo(availableProxyInfo)
                .setHttpClientFactory(httpClientFactory)
                .setHttpRequestFactory(httpRequestFactory)
                .build()
        val errorCode = try {
            val inputStream = webReadStream.getStream()
        } catch (e: RSException) {
            e.errorCode
        }
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Get Proxy 를 통해 inputStream이 생성되면 성공한다`() {
        val httpClientFactory = Mockito.mock(HttpClientFactory::class.java)
        val httpRequestFactory = Mockito.mock(HttpRequestFactory::class.java)
        val httpClient = Mockito.mock(HttpClient::class.java)
        val httpResponse = Mockito.mock(HttpResponse::class.java)
        val statusLine = Mockito.mock(StatusLine::class.java)
        val httpGet = Mockito.mock(HttpGet::class.java)

        whenever(httpRequestFactory.create(any())).thenReturn(httpGet)
        whenever(httpClientFactory.create()).thenReturn(httpClient)
        whenever(httpResponse.getStatusLine()).thenReturn(statusLine)
        whenever(httpResponse.getEntity()).thenReturn(ByteArrayHttpEntity("hello".toByteArray()))
        whenever(statusLine.statusCode).thenReturn(HttpStatus.SC_OK)
        whenever(httpClient.execute(any())).thenReturn(httpResponse)
        val webReadStream = ProxyHttpReadStream.Builder()
                .setProxyInfo(availableProxyInfo)
                .setParameter(hashMapOf("key" to "value"))
                .setHttpClientFactory(httpClientFactory)
                .setHttpRequestFactory(httpRequestFactory)
                .setServerURL("https://rview.com")
                .build()
        val inputStream = webReadStream.getStream()
        assertNotNull(inputStream)
        assertEquals("hello", String(inputStream!!.readBytes()))
    }

    @Test
    fun `Post Proxy 를 통해 inputStream이 생성되면 성공한다`() {
        val httpClientFactory = Mockito.mock(HttpClientFactory::class.java)
        val httpRequestFactory = Mockito.mock(HttpRequestFactory::class.java)
        val httpClient = Mockito.mock(HttpClient::class.java)
        val httpResponse = Mockito.mock(HttpResponse::class.java)
        val statusLine = Mockito.mock(StatusLine::class.java)
        val httpPost = Mockito.mock(HttpPost::class.java)

        whenever(httpRequestFactory.create(any())).thenReturn(httpPost)
        whenever(httpClientFactory.create()).thenReturn(httpClient)
        whenever(httpResponse.getStatusLine()).thenReturn(statusLine)
        whenever(httpResponse.getEntity()).thenReturn(ByteArrayHttpEntity("hello".toByteArray()))
        whenever(statusLine.statusCode).thenReturn(HttpStatus.SC_OK)
        whenever(httpClient.execute(any())).thenReturn(httpResponse)
        val webReadStream = ProxyHttpReadStream.Builder()
                .setProxyInfo(availableProxyInfo)
                .setHttpClientFactory(httpClientFactory)
                .setHttpRequestFactory(httpRequestFactory)
                .setServerURL("https://rview.com")
                .setPost(true)
                .build()
        val inputStream = webReadStream.getStream()
        assertNotNull(inputStream)
        assertEquals("hello", String(inputStream!!.readBytes()))
    }

}

class ByteArrayHttpEntity(private val data: ByteArray) : HttpEntity {
    override fun getContentEncoding(): Header? {
        return null
    }

    override fun isRepeatable(): Boolean {
        return false
    }

    override fun consumeContent() {
    }

    override fun getContent(): InputStream {
        return ByteArrayInputStream(data)
    }

    override fun isChunked(): Boolean {
        return false
    }

    override fun isStreaming(): Boolean {
        return false
    }

    override fun writeTo(p0: OutputStream?) {
    }

    override fun getContentLength(): Long {
        return data.count().toLong()
    }

    override fun getContentType(): Header? {
        return null
    }
}
