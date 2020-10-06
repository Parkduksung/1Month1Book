package com.rsupport.mobile.agent.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import org.junit.After
import org.junit.Assert.*
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
class WebConnectionNotifyDisconnectTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `Network stream 생성이 안되면 실패한다`() {
        val apiService = createApiService(context)
        val disconnectResult = apiService.notifyDisconnected("", "")
        assertFalse(disconnectResult.isSuccess)
    }

    @Test
    fun `Network stream 생성시 IOException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IOException("IOException"))
        val errorCode = ((apiService.notifyDisconnected("", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생성시 InvalidKeyException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidKeyException("InvalidKeyException"))
        val errorCode = ((apiService.notifyDisconnected("", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생성시 BadPaddingException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, BadPaddingException("BadPaddingException"))
        val errorCode = ((apiService.notifyDisconnected("", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생성시 InvalidAlgorithmParameterException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidAlgorithmParameterException("InvalidAlgorithmParameterException"))
        val errorCode = ((apiService.notifyDisconnected("", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생성시 IllegalBlockSizeException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IllegalBlockSizeException("IllegalBlockSizeException"))
        val errorCode = ((apiService.notifyDisconnected("", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생성시 RuntimeException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, RuntimeException("RuntimeException"))
        val errorCode = ((apiService.notifyDisconnected("", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.UNKNOWN, errorCode)
    }

    @Test
    fun `서버로부터 비어있는 데이터를 받을경우 실패한다`() {
        val apiService = createApiService(context, "empty_data.xml")
        val errorCode = ((apiService.notifyDisconnected("", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Parser.XML_IO_ERROR, errorCode)
    }

    @Test
    fun `RESULT 가 0이 아니면 실패한다`() {
        val apiService = createApiService(context, "result_900.xml")
        val disconnectResult = apiService.notifyDisconnected("", "")
        assertFalse(disconnectResult.isSuccess)
        assertEquals(900, GlobalStatic.g_errNumber)
    }

    @Test
    fun `RESULT 가 0이면 성공한다`() {
        val apiService = createApiService(context, "result_success_empty.xml")
        val disconnectResult = apiService.notifyDisconnected("", "")
        assertTrue(disconnectResult.isSuccess)
    }

}