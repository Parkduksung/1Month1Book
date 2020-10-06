package com.rsupport.mobile.agent.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import junit.framework.Assert.*
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.lang.RuntimeException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException

@RunWith(RobolectricTestRunner::class)
class WebConnectionCheckAccessIDValidateTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `Network stream 이 null 이면 실패 해야한다`() {
        val apiService = createApiService(context)
        val checkAccessIDValidateResult = apiService.checkAccessIDValidate("", "", "")
        assertFalse(checkAccessIDValidateResult.isSuccess)
    }

    @Test
    fun `Network stream 생서시 RSException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, RSException(100))
        val errorCode = ((apiService.checkAccessIDValidate("", "", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(100, errorCode)
    }

    @Test
    fun `Network stream 생서시 IOException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IOException("io exception"))

        val errorCode = ((apiService.checkAccessIDValidate("", "", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생서시 InvalidKeyException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidKeyException("io exception"))
        val errorCode = ((apiService.checkAccessIDValidate("", "", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생서시 BadPaddingException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, BadPaddingException("io exception"))
        val errorCode = ((apiService.checkAccessIDValidate("", "", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생서시 InvalidAlgorithmParameterException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidAlgorithmParameterException("io exception"))

        val errorCode = ((apiService.checkAccessIDValidate("", "", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }


    @Test
    fun `Network stream 생서시 IllegalBlockSizeException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IllegalBlockSizeException("io exception"))

        val errorCode = ((apiService.checkAccessIDValidate("", "", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생서시 정의하지 않은 오류가 발생하면 실패한다`() {
        val apiService = createApiService(context, RuntimeException("io exception"))

        val errorCode = ((apiService.checkAccessIDValidate("", "", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.UNKNOWN, errorCode)
    }

    @Test
    fun `Stream 에서 데이터를 읽지 못하면 실패한다`() {
        val apiService = createApiService(context, "empty_data.xml")
        val checkAccessIDValidateResult = apiService.checkAccessIDValidate("", "", "")
        val errorCode = ((checkAccessIDValidateResult as? Result.Failure)?.throwable as? RSException)?.errorCode

        assertFalse(checkAccessIDValidateResult.isSuccess)
        assertEquals(RSErrorCode.Parser.XML_IO_ERROR, errorCode)
    }

    @Test
    fun `RESULT 코드가 0이 아니면 실패해야한다`() {
        val apiService = createApiService(context, "result_900.xml")
        val checkAccessIDValidateResult = apiService.checkAccessIDValidate("", "", "")
        assertFalse(checkAccessIDValidateResult.isSuccess)
        assertEquals(900, GlobalStatic.g_errNumber)
        assertEquals("test 900 error", GlobalStatic.g_err)
    }

    @Test
    fun `RESULT 코드가 0이 이면 성공해야한다`() {
        val apiService = createApiService(context, "result_success_empty.xml")
        val checkAccessIDValidateResult = apiService.checkAccessIDValidate("", "", "")
        assertTrue(checkAccessIDValidateResult.isSuccess)
    }
}