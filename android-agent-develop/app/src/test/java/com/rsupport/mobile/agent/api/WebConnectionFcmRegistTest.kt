package com.rsupport.mobile.agent.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import junit.framework.Assert.*
import org.junit.After
import org.junit.Assert
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
class WebConnectionFcmRegistTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun returnFalseWhenStreamIsNull() {
        val apiService = createApiService(context)
        assertFalse(apiService.registerFcmId("guid-123", "registerid-123").isSuccess)
    }

    @Test
    fun `Network stream 생성시 IOException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IOException("IOException"))
        val fcmRegisterResult = apiService.registerFcmId("guid-123", "registerid-123")
        assertFalse(fcmRegisterResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 InvalidKeyException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidKeyException("InvalidKeyException"))
        val fcmRegisterResult = apiService.registerFcmId("guid-123", "registerid-123")
        assertFalse(fcmRegisterResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 BadPaddingException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, BadPaddingException("BadPaddingException"))
        val fcmRegisterResult = apiService.registerFcmId("guid-123", "registerid-123")
        assertFalse(fcmRegisterResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 InvalidAlgorithmParameterException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidAlgorithmParameterException("InvalidAlgorithmParameterException"))
        val fcmRegisterResult = apiService.registerFcmId("guid-123", "registerid-123")
        assertFalse(fcmRegisterResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 IllegalBlockSizeException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IllegalBlockSizeException("IllegalBlockSizeException"))
        val fcmRegisterResult = apiService.registerFcmId("guid-123", "registerid-123")
        assertFalse(fcmRegisterResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 RuntimeException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, RuntimeException("RuntimeException"))
        val fcmRegisterResult = apiService.registerFcmId("guid-123", "registerid-123")
        assertFalse(fcmRegisterResult.isSuccess)
        assertEquals(RSErrorCode.UNKNOWN, GlobalStatic.g_errNumber)
    }

    @Test
    fun returnFalseWhenStreamIsException() {
        val apiService = createApiService(context, IOException("io exception"))
        assertFalse(apiService.registerFcmId("guid-123", "registerid-123").isSuccess)
    }


    @Test
    fun returnFalseWhenResultIsNotJson() {
        val apiService = createApiService(context, "empty_data.xml")
        assertFalse(apiService.registerFcmId("guid-123", "registerid-123").isSuccess)
    }

    @Test
    fun returnFalseWhenResultIsEmpty() {
        val apiService = createApiService(context, "empty_data.json")
        assertFalse(apiService.registerFcmId("guid-123", "registerid-123").isSuccess)
    }

    @Test
    fun returnFalseWhenResultIsNotZero() {
        val apiService = createApiService(context, "result_900.json")
        assertFalse(apiService.registerFcmId("guid-123", "registerid-123").isSuccess)
        assertEquals(900, GlobalStatic.g_errNumber)
    }

    @Test
    fun returnTrueWhenResultIsZero() {
        val apiService = createApiService(context, "result_success_empty.json")
        assertTrue(apiService.registerFcmId("guid-123", "registerid-123").isSuccess)
    }

}