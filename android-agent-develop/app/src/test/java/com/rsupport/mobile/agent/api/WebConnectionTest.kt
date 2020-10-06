package com.rsupport.mobile.agent.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rsupport.mobile.agent.api.parser.DefaultStreamParserFactory
import com.rsupport.mobile.agent.api.parser.StreamParserFactory
import com.rsupport.mobile.agent.constant.AgentBasicInfo
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.repo.device.DeviceRepository
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.crypto.WebCrypto
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import junit.framework.TestCase.assertTrue
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.java.KoinJavaComponent
import org.robolectric.RobolectricTestRunner
import java.io.*
import java.util.zip.GZIPOutputStream

@RunWith(RobolectricTestRunner::class)
class WebConnectionTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setup() {
        AgentBasicInfo.setApiVersion(context, "")
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun checkTokensWhenCalledConsoleLogin() {
        val apiServie = createApiService(context, "console/console_login_v2_result.xml")

        val consoleLoginResult = apiServie.consoleLogin("id", "pwd", "bizid")

        assertTrue(consoleLoginResult.isSuccess)
        assertThat("access_toke_123", `is`(AgentBasicInfo.getAccessToken(context)))
        assertThat("refresh_token_123", `is`(AgentBasicInfo.getRefreshToken(context)))
        assertThat("v2", `is`(AgentBasicInfo.getApiVersion(context)))
        assertThat("/refresh_token/url", `is`(AgentBasicInfo.getRefreshTokenURL(context)))
    }
}

class DummyWebStreamFactory(context: Context, private val xmlFile: String? = null) : WebStreamFactory(context) {
    override fun create(): WebReadStream {
        return object : WebReadStream {
            override fun getStream(): InputStream {
                return xmlFile?.let { createFileStream(xmlFile) }
                        ?: throw RSException(RSErrorCode.Network.IO_ERROR)
            }
        }
    }
}

class ExceptionWebStreamFactory(context: Context, val exception: Exception) : WebStreamFactory(context) {
    override fun create(): WebReadStream {
        return object : WebReadStream {
            override fun getStream(): InputStream {
                throw exception
            }
        }
    }
}


class PassedWebCryptoFactory : WebCryptoFactory {
    override fun create(): WebCrypto {
        return object : WebCrypto() {
            override fun encrypt(plainText: String?): String {
                return plainText ?: ""
            }

            override fun decrypt(encryptText: String?): String {
                return encryptText ?: ""
            }
        }
    }
}

fun createFileStream(path: String, parent: String = "./src/test/res/resources"): InputStream {
    val file = File("$parent/$path")
    val byteArray = ByteArray(file.length().toInt())
    DataInputStream(FileInputStream(file)).use {
        it.readFully(byteArray)
    }

    return ByteArrayInputStream(byteArray)
}

fun createFileGZIPStream(path: String, parent: String = "./src/test/res/resources"): InputStream {
    val file = File("$parent/$path")
    val byteArray = ByteArray(file.length().toInt())
    DataInputStream(FileInputStream(file)).use {
        it.readFully(byteArray)
    }

    val baos = ByteArrayOutputStream()
    val outputStream = GZIPOutputStream(baos)
    outputStream.write(byteArray)
    outputStream.close()
    return ByteArrayInputStream(baos.toByteArray())
}


fun createWebConnection(context: Context, xmlFile: String? = null, cryptoFactory: WebCryptoFactory = PassedWebCryptoFactory(), streamParserFactory: StreamParserFactory = DefaultStreamParserFactory()): WebConnection {
    return WebConnection(cryptoFactory, DummyWebStreamFactory(context, xmlFile), streamParserFactory).apply {
        this.context = context
        this.setAESEnable(true)
        setNetworkInfo()
    }
}


fun createWebConnection(context: Context, exception: Exception, cryptoFactory: WebCryptoFactory = PassedWebCryptoFactory(), streamParserFactory: StreamParserFactory = DefaultStreamParserFactory()): WebConnection {
    return WebConnection(cryptoFactory, ExceptionWebStreamFactory(context, exception), streamParserFactory).apply {
        this.context = context
        this.setAESEnable(true)
        setNetworkInfo()
    }
}


fun createApiService(context: Context = KoinJavaComponent.get(Context::class.java), xmlFile: String? = null): ApiService {
    return WebConnectionApiService(
            context,
            createWebConnection(context, xmlFile),
            KoinJavaComponent.get(DeviceRepository::class.java),
            KoinJavaComponent.get(ConfigRepository::class.java)
    )
}

fun createApiService(context: Context = KoinJavaComponent.get(Context::class.java), webConnection: WebConnection): ApiService {
    return WebConnectionApiService(
            context,
            webConnection,
            KoinJavaComponent.get(DeviceRepository::class.java),
            KoinJavaComponent.get(ConfigRepository::class.java)
    )
}


fun createApiService(context: Context = KoinJavaComponent.get(Context::class.java), exception: Exception): ApiService {
    return WebConnectionApiService(
            context,
            createWebConnection(context, exception),
            KoinJavaComponent.get(DeviceRepository::class.java),
            KoinJavaComponent.get(ConfigRepository::class.java)
    )
}

fun Result<*>.getErrorCode(): Int? {
    return ((this as? Result.Failure)?.throwable as? RSException)?.errorCode
}
