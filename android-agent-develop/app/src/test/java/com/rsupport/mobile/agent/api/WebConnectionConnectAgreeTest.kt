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
import java.security.InvalidKeyException

@RunWith(RobolectricTestRunner::class)
class WebConnectionConnectAgreeTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `Network strem 이 null 이면 실패한다`() {
        val apiService = createApiService(context)
        val connectAgreeResult = apiService.agentConnectAgreeResult("", "", "", "")
        assertFalse(connectAgreeResult.isSuccess)
    }

    @Test
    fun `Network stream 생성중 IO Exception 발생시 IO_ERROR 가 발생한다`() {
        val apiService = createApiService(context, IOException("io exception"))
        val errorCode = ((apiService.agentConnectAgreeResult("", "", "", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생성중 InvalidKeyException 발생시 IO_ERROR 가 발생한다`() {
        val apiService = createApiService(context, InvalidKeyException("InvalidKeyException"))
        val errorCode = ((apiService.agentConnectAgreeResult("", "", "", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생성중 BadPaddingException 발생시 IO_ERROR 가 발생한다`() {
        val apiService = createApiService(context, InvalidKeyException("BadPaddingException"))
        val errorCode = ((apiService.agentConnectAgreeResult("", "", "", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생성중 InvalidAlgorithmParameterException 발생시 IO_ERROR 가 발생한다`() {
        val apiService = createApiService(context, InvalidKeyException("InvalidAlgorithmParameterException"))
        val errorCode = ((apiService.agentConnectAgreeResult("", "", "", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생성중 IllegalBlockSizeException 발생시 IO_ERROR 가 발생한다`() {
        val apiService = createApiService(context, InvalidKeyException("IllegalBlockSizeException"))
        val errorCode = ((apiService.agentConnectAgreeResult("", "", "", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생성중 정의하지 않은 오류 발생시 발생시 Exception이  발생한다`() {
        val apiService = createApiService(context, RuntimeException("custom RuntimeException"))
        val errorCode = ((apiService.agentConnectAgreeResult("", "", "", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.UNKNOWN, errorCode)
    }


    @Test
    fun `RESULT 가 0이 아니면 실패한다`() {
        val apiService = createApiService(context, "result_900.xml")
        val connectAgreeResult = apiService.agentConnectAgreeResult("", "", "", "")
        assertFalse(connectAgreeResult.isSuccess)
        assertEquals(900, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Stream 으로부터 읽은데이터가 비었을때 실패한다`() {
        val apiService = createApiService(context, "empty_data.xml")
        val connectAgreeResult = apiService.agentConnectAgreeResult("", "", "", "")
        assertFalse(connectAgreeResult.isSuccess)

        val errorCode = ((connectAgreeResult as Result.Failure).throwable as RSException).errorCode
        assertEquals(RSErrorCode.Parser.XML_IO_ERROR, errorCode)
    }

    @Test
    fun `RESULT 가 0일때 성공한다`() {
        val apiService = createApiService(context, "result_success_empty.xml")
        val connectAgreeResult = apiService.agentConnectAgreeResult("", "", "", "")
        assertTrue(connectAgreeResult.isSuccess)
    }

}