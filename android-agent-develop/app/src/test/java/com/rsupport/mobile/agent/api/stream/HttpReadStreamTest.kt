package com.rsupport.mobile.agent.api.stream

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import com.rsupport.mobile.agent.api.WebReadStream
import com.rsupport.mobile.agent.api.createFileGZIPStream
import com.rsupport.mobile.agent.api.createFileStream
import com.rsupport.mobile.agent.api.net.HttpURLConnectionFactory
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.ProtocolException

@RunWith(RobolectricTestRunner::class)
class HttpReadStreamTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var httpURLConnection: HttpURLConnection
    private lateinit var httpURLConnectionFactory: HttpURLConnectionFactory

    private val resultSuccessEmpty = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <DATA>
            <RESULT>0</RESULT>
            <ERRORMSG></ERRORMSG>
        </DATA>
    """.trimIndent()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Before
    fun setup() {
        httpURLConnectionFactory = Mockito.mock(HttpURLConnectionFactory::class.java)
        httpURLConnection = Mockito.mock(HttpURLConnection::class.java)
        whenever(httpURLConnectionFactory.create(any())).thenReturn(httpURLConnection)
    }

    @Test
    fun `잘못된 POST URL을 설정하면 실패한다`() {
        val webReadStream: WebReadStream = HttpReadStream.Builder()
                .setPost(true)
                .build()
        val errorCode = try {
            val inputStream = webReadStream.getStream()
        } catch (e: RSException) {
            e.errorCode
        }
        assertEquals(RSErrorCode.Network.MALFORMED_URL, errorCode)
    }

    @Test
    fun `잘못된 GET URL을 설정하면 실패한다`() {
        val webReadStream: WebReadStream = HttpReadStream.Builder().build()
        val errorCode = try {
            val inputStream = webReadStream.getStream()
        } catch (e: RSException) {
            e.errorCode
        }
        assertEquals(RSErrorCode.Network.MALFORMED_URL, errorCode)
    }

    @Test
    fun `Parameter encoding 이 안되면 실패한다`() {
        val parameterGenerator = Mockito.mock(ParameterGenerator::class.java)
        whenever(parameterGenerator.generate(any())).thenThrow(UnsupportedEncodingException())
        val webReadStream: WebReadStream = HttpReadStream.Builder()
                .setPost(true)
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
    fun `연결 상태에서 http post method 를 설정하면 실패한다`() {
        whenever(httpURLConnection.setRequestMethod(any())).thenThrow(ProtocolException("not support protocol"))
        val webReadStream: WebReadStream = HttpReadStream.Builder()
                .setServerURL("https://rview.com")
                .setPost(true)
                .setHttpURLConnectionFactory(httpURLConnectionFactory)
                .build()
        val errorCode = try {
            val inputStream = webReadStream.getStream()
        } catch (e: RSException) {
            e.errorCode
        }
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `연결 상태에서 http get method 를 설정하면 실패한다`() {
        whenever(httpURLConnection.setRequestMethod(any())).thenThrow(ProtocolException("not support protocol"))

        val webReadStream: WebReadStream = HttpReadStream.Builder()
                .setServerURL("https://rview.com")
                .setPost(false)
                .setHttpURLConnectionFactory(httpURLConnectionFactory)
                .build()
        val errorCode = try {
            val inputStream = webReadStream.getStream()
        } catch (e: RSException) {
            e.errorCode
        }
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }


    @Test
    fun `Post 로 parameter 전송시 Exception 이 발생하면 실패한다`() {
        whenever(httpURLConnection.outputStream).thenThrow(IOException("io exception"))
        val webReadStream: WebReadStream = HttpReadStream.Builder()
                .setServerURL("https://rview.com")
                .setPost(true)
                .setHttpURLConnectionFactory(httpURLConnectionFactory)
                .build()
        val errorCode = try {
            val inputStream = webReadStream.getStream()
        } catch (e: RSException) {
            e.errorCode
        }
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `http connection 에서 return code 가 -1 이면 실패한다`() {
        val outputStream = Mockito.mock(OutputStream::class.java)
        whenever(httpURLConnection.outputStream).thenReturn(outputStream)
        whenever(httpURLConnection.responseCode).thenReturn(-1)
        val webReadStream: WebReadStream = HttpReadStream.Builder()
                .setServerURL("https://rview.com")
                .setPost(true)
                .setHttpURLConnectionFactory(httpURLConnectionFactory)
                .build()
        val errorCode = try {
            val inputStream = webReadStream.getStream()
        } catch (e: RSException) {
            e.errorCode
        }
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Post InputStream 생성이되면 성공한다`() {
        val outputStream = Mockito.mock(OutputStream::class.java)
        val inputStream = createFileStream("result_success_empty.xml")
        whenever(httpURLConnection.outputStream).thenReturn(outputStream)
        whenever(httpURLConnection.inputStream).thenReturn(inputStream)
        whenever(httpURLConnection.responseCode).thenReturn(200)
        val webReadStream: WebReadStream = HttpReadStream.Builder()
                .setServerURL("https://rview.com")
                .setPost(true)
                .setHttpURLConnectionFactory(httpURLConnectionFactory)
                .build()
        val inputStreamResult = webReadStream.getStream()
        assertNotNull(inputStreamResult)
    }

    @Test
    fun `Get InputStream 생성이되면 성공한다`() {
        val outputStream = Mockito.mock(OutputStream::class.java)
        val inputStream = createFileStream("result_success_empty.xml")
        whenever(httpURLConnection.outputStream).thenReturn(outputStream)
        whenever(httpURLConnection.inputStream).thenReturn(inputStream)
        whenever(httpURLConnection.responseCode).thenReturn(200)
        val webReadStream: WebReadStream = HttpReadStream.Builder()
                .setServerURL("https://rview.com")
                .setHttpURLConnectionFactory(httpURLConnectionFactory)
                .build()
        val inputStreamResult = webReadStream.getStream()
        assertNotNull(inputStreamResult)
    }

    @Test
    fun `GZIPInputStream 생성이되면 성공한다`() {
        val outputStream = Mockito.mock(OutputStream::class.java)
        val inputStream = createFileGZIPStream("result_success_empty.xml")

        whenever(httpURLConnection.outputStream).thenReturn(outputStream)
        whenever(httpURLConnection.inputStream).thenReturn(inputStream)
        whenever(httpURLConnection.responseCode).thenReturn(200)
        whenever(httpURLConnection.getHeaderField("content-Encoding")).thenReturn("gzip")
        val webReadStream: WebReadStream = HttpReadStream.Builder()
                .setServerURL("https://rview.com")
                .setPost(true)
                .setHttpURLConnectionFactory(httpURLConnectionFactory)
                .build()

        val inputStreamResult = webReadStream.getStream()
        assertNotNull(inputStreamResult)
    }

    @Test
    fun `InputStream 에서 IOException이 발생하면 실패한다`() {
        val inputStream = Mockito.mock(InputStream::class.java)
        val outputStream = Mockito.mock(OutputStream::class.java)
        whenever(inputStream.read(any(), any(), any())).thenThrow(IOException("read io error"))
        whenever(httpURLConnection.outputStream).thenReturn(outputStream)
        whenever(httpURLConnection.inputStream).thenReturn(inputStream)
        whenever(httpURLConnection.responseCode).thenReturn(200)
        val webReadStream: WebReadStream = HttpReadStream.Builder()
                .setServerURL("https://rview.com")
                .setPost(true)
                .setHttpURLConnectionFactory(httpURLConnectionFactory)
                .build()

        val errorCode = try {
            val inputStreamResult = webReadStream.getStream()
        } catch (e: RSException) {
            e.errorCode
        }
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `InputStream 에서 데이터를 읽으면 성공한다`() {
        val inputStream = createFileStream("result_success_empty.xml")

        val outputStream = Mockito.mock(OutputStream::class.java)
        whenever(httpURLConnection.outputStream).thenReturn(outputStream)
        whenever(httpURLConnection.inputStream).thenReturn(inputStream)
        whenever(httpURLConnection.responseCode).thenReturn(200)
        val webReadStream: WebReadStream = HttpReadStream.Builder()
                .setServerURL("https://rview.com")
                .setPost(true)
                .setHttpURLConnectionFactory(httpURLConnectionFactory)
                .build()

        val inputStreamResult = webReadStream.getStream()
        assertNotNull(inputStreamResult)
        assertEquals(resultSuccessEmpty, String(inputStreamResult?.readBytes()!!))
    }

    @Test
    fun `GZIPInputStream 에서 데이터를 읽으면 성공한다`() {
        val inputStream = createFileGZIPStream("result_success_empty.xml")
        val outputStream = Mockito.mock(OutputStream::class.java)
        whenever(httpURLConnection.outputStream).thenReturn(outputStream)
        whenever(httpURLConnection.inputStream).thenReturn(inputStream)
        whenever(httpURLConnection.responseCode).thenReturn(200)
        whenever(httpURLConnection.getHeaderField("content-Encoding")).thenReturn("gzip")
        val webReadStream: WebReadStream = HttpReadStream.Builder()
                .setServerURL("https://rview.com")
                .setPost(true)
                .setHttpURLConnectionFactory(httpURLConnectionFactory)
                .build()
        val inputStreamResult = webReadStream.getStream()
        assertNotNull(inputStreamResult)
        assertEquals(resultSuccessEmpty, String(inputStreamResult?.readBytes()!!))
    }

}