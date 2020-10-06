package com.rsupport.mobile.agent.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import junit.framework.Assert.assertEquals
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
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
class WebConnectionLogoutTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val guid = "guid-1234"


    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun logoutFailWhenStreamIsNull() {
        val apiService = createApiService(context)
        val logoutResult = apiService.agentLogout(guid)
        assertThat(logoutResult, Matchers.instanceOf(Result.Failure::class.java))

        val errorCode = ((logoutResult as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `Network stream 생성시 IOException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IOException("IOException"))
        val logoutResult = apiService.agentLogout(guid)
        Assert.assertFalse(logoutResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 InvalidKeyException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidKeyException("InvalidKeyException"))
        val logoutResult = apiService.agentLogout(guid)
        Assert.assertFalse(logoutResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 BadPaddingException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, BadPaddingException("BadPaddingException"))
        val logoutResult = apiService.agentLogout(guid)
        Assert.assertFalse(logoutResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 InvalidAlgorithmParameterException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidAlgorithmParameterException("InvalidAlgorithmParameterException"))
        val logoutResult = apiService.agentLogout(guid)
        Assert.assertFalse(logoutResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 IllegalBlockSizeException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IllegalBlockSizeException("IllegalBlockSizeException"))
        val logoutResult = apiService.agentLogout(guid)
        Assert.assertFalse(logoutResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 알수 없는 오류 발생하면 실패한다`() {
        val apiService = createApiService(context, RuntimeException("RuntimeException"))
        val logoutResult = apiService.agentLogout(guid)
        Assert.assertFalse(logoutResult.isSuccess)
        assertEquals(RSErrorCode.UNKNOWN, GlobalStatic.g_errNumber)
    }

    @Test
    fun logoutFailWhenStreamIsRSException() {
        val apiService = createApiService(context, IOException("IOException"))
        val logoutResult = apiService.agentLogout(guid)
        assertThat(logoutResult, Matchers.instanceOf(Result.Failure::class.java))

        val errorCode = ((logoutResult as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun logoutFailWhenEmptyData() {
        val apiService = createApiService(context, "empty_data.xml")
        assertThat(apiService.agentLogout(guid), Matchers.instanceOf(Result.Failure::class.java))
    }

    @Test
    fun logoutFailWhenResultIsError900() {
        val apiService = createApiService(context, "result_900.xml")

        assertThat(apiService.agentLogout(guid), Matchers.instanceOf(Result.Failure::class.java))
        assertEquals(GlobalStatic.g_errNumber, 900)
    }

    @Test
    fun logoutSuccessWhenResultIsSuccess() {
        val apiService = createApiService(context, "result_success_empty.xml")
        assertThat(apiService.agentLogout(guid), Matchers.instanceOf(Result.Success::class.java))
    }
}