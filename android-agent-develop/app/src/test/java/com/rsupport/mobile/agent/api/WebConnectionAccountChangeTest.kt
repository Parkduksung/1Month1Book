package com.rsupport.mobile.agent.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.rscommon.define.RSErrorCode
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
class WebConnectionAccountChangeTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `Network stream 생성이 안되면 실패한다`() {
        val apiService = createApiService(context)
        val accountChangeResult = apiService.agentAccountChange("", "", "", "", "")
        assertFalse(accountChangeResult.isSuccess)
    }

    @Test
    fun `Network stream 생성시 IOException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IOException("IOException"))
        val accountChangeResult = apiService.agentAccountChange("", "", "", "", "")
        assertFalse(accountChangeResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 InvalidKeyException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidKeyException("InvalidKeyException"))
        val accountChangeResult = apiService.agentAccountChange("", "", "", "", "")
        assertFalse(accountChangeResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 BadPaddingException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, BadPaddingException("BadPaddingException"))
        val accountChangeResult = apiService.agentAccountChange("", "", "", "", "")
        assertFalse(accountChangeResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 InvalidAlgorithmParameterException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidAlgorithmParameterException("InvalidAlgorithmParameterException"))
        val accountChangeResult = apiService.agentAccountChange("", "", "", "", "")
        assertFalse(accountChangeResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 IllegalBlockSizeException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IllegalBlockSizeException("IllegalBlockSizeException"))
        val accountChangeResult = apiService.agentAccountChange("", "", "", "", "")
        assertFalse(accountChangeResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 알수 없는 오류 발생하면 실패한다`() {
        val apiService = createApiService(context, RuntimeException("RuntimeException"))
        val accountChangeResult = apiService.agentAccountChange("", "", "", "", "")
        assertFalse(accountChangeResult.isSuccess)
        assertEquals(RSErrorCode.UNKNOWN, GlobalStatic.g_errNumber)
    }

    @Test
    fun `서버로부터 빈값을 받을경우 실패한다`() {
        val apiService = createApiService(context, "empty_data.xml")
        val accountChangeResult = apiService.agentAccountChange("", "", "", "", "")
        assertFalse(accountChangeResult.isSuccess)
        assertEquals(RSErrorCode.Parser.XML_IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `RESULT 가 0이 아니면 실패한다`() {
        val apiService = createApiService(context, "result_900.xml")
        val accountChangeResult = apiService.agentAccountChange("", "", "", "", "")
        assertFalse(accountChangeResult.isSuccess)
        assertEquals(900, GlobalStatic.g_errNumber)
        assertEquals("test 900 error", GlobalStatic.g_err)
    }

    @Test
    fun `RESULT 가 0이면 성공한다`() {
        val apiService = createApiService(context, "result_success_empty.xml")
        val accountChangeResult = apiService.agentAccountChange("", "", "", "", "")
        assertTrue(accountChangeResult.isSuccess)
    }

}