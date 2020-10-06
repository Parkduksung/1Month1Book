package com.rsupport.mobile.agent.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rsupport.mobile.agent.api.parser.CheckSupportKnoxParser
import com.rsupport.mobile.agent.api.parser.StreamParser
import com.rsupport.mobile.agent.api.parser.StreamParserFactory
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.XMLParser
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.xml.sax.SAXException
import java.io.IOException
import java.io.InputStream
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.util.HashMap
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException

@RunWith(RobolectricTestRunner::class)
class WebConnectionCheckSupportKnoxResultTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `Network stream 생성이 안되면 실패한다`() {
        val apiService = createApiService(context)
        val errorCode = ((apiService.reqeustCheckSupportKnox() as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생성중 IOException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IOException("IOException"))
        val errorCode = ((apiService.reqeustCheckSupportKnox() as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생성중 RSException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, RSException(999))
        val errorCode = ((apiService.reqeustCheckSupportKnox() as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(999, errorCode)
    }

    @Test
    fun `Network stream 생성중 InvalidKeyException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidKeyException("InvalidKeyException"))
        val errorCode = ((apiService.reqeustCheckSupportKnox() as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생성중 InvalidAlgorithmParameterException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidAlgorithmParameterException("InvalidAlgorithmParameterException"))
        val errorCode = ((apiService.reqeustCheckSupportKnox() as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생성중 IllegalBlockSizeException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IllegalBlockSizeException("IllegalBlockSizeException"))
        val errorCode = ((apiService.reqeustCheckSupportKnox() as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생성중 BadPaddingException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, BadPaddingException("BadPaddingException"))
        val errorCode = ((apiService.reqeustCheckSupportKnox() as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `xml Parsing SAXException 이 발생하면 실패한다`() {
        val webConnection = createWebConnection(context, "empty_data.xml", streamParserFactory = object : StreamParserFactory {
            override fun <T, M> create(clazz: Class<T>): StreamParser<M> {
                return CheckSupportKnoxParser(
                        object : XMLParser() {
                            override fun parse(`is`: InputStream?): HashMap<String, String> {
                                throw SAXException()
                            }
                        }
                ) as StreamParser<M>
            }
        })

        val apiService = createApiService(context, webConnection)
        val errorCode = ((apiService.reqeustCheckSupportKnox() as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Parser.XML_SAX_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생성중 정의하지 않은 오류가 발생하면 실패한다`() {
        val apiService = createApiService(context, RuntimeException("RuntimeException"))
        val errorCode = ((apiService.reqeustCheckSupportKnox() as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.UNKNOWN, errorCode)
    }

    @Test
    fun `비어있는 데이터를 수신하면 실패한다`() {
        val apiService = createApiService(context, "empty_data.xml")
        val errorCode = ((apiService.reqeustCheckSupportKnox() as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Parser.XML_IO_ERROR, errorCode)
    }

    @Test
    fun `RETCODE 가 0또는 100이 아니면 실패한다`() {
        val apiService = createApiService(context, "retcode_900.xml")
        val checkSupportKnoxResult = apiService.reqeustCheckSupportKnox()
        assertFalse(checkSupportKnoxResult.isSuccess)
        assertEquals(900, GlobalStatic.g_errNumber)
    }

    @Test
    fun `RETCODE 가 0이면 성공한다`() {
        val apiService = createApiService(context, "retcode_success_empty.xml")
        val checkSupportKnoxResult = apiService.reqeustCheckSupportKnox()
        assertTrue(checkSupportKnoxResult.isSuccess)
    }

    @Test
    fun `RETCODE 가 100이면 성공한다`() {
        val apiService = createApiService(context, "retcode_success100_empty.xml")
        val checkSupportKnoxResult = apiService.reqeustCheckSupportKnox()
        assertTrue(checkSupportKnoxResult.isSuccess)
    }
}