package com.rsupport.mobile.agent.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rsupport.mobile.agent.api.model.AgentInstallResult
import com.rsupport.mobile.agent.constant.GlobalStatic
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
class WebConnectionAgentInstallTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `inputStream 이 null 일경우 설치를 실패한다`() {
        val apiService = createApiService(context)
        val agentInstallResult = callAgentInstall(apiService)

        assertFalse(agentInstallResult.isSuccess)
    }

    @Test
    fun `Network stream 생성시 IOException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IOException("IOException"))
        val agentInstallResult = callAgentInstall(apiService)
        assertFalse(agentInstallResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 InvalidKeyException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidKeyException("InvalidKeyException"))
        val agentInstallResult = callAgentInstall(apiService)
        assertFalse(agentInstallResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 BadPaddingException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, BadPaddingException("BadPaddingException"))
        val agentInstallResult = callAgentInstall(apiService)
        assertFalse(agentInstallResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 InvalidAlgorithmParameterException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidAlgorithmParameterException("InvalidAlgorithmParameterException"))
        val agentInstallResult = callAgentInstall(apiService)
        assertFalse(agentInstallResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 IllegalBlockSizeException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IllegalBlockSizeException("IllegalBlockSizeException"))
        val agentInstallResult = callAgentInstall(apiService)
        assertFalse(agentInstallResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 알수 없는 오류 발생하면 실패한다`() {
        val apiService = createApiService(context, RuntimeException("RuntimeException"))
        val agentInstallResult = callAgentInstall(apiService)
        assertFalse(agentInstallResult.isSuccess)
        assertEquals(RSErrorCode.UNKNOWN, GlobalStatic.g_errNumber)
    }

    @Test
    fun `서버가 데이터를 빈값으로 내려줄경우 설치를 실패한다`() {
        val apiService = createApiService(context, "empty_data.xml")
        val agentInstallResult = callAgentInstall(apiService)

        assertFalse(agentInstallResult.isSuccess)
    }

    @Test
    fun `RESULT 가 0이 아니면 설치를 실패한다`() {
        val apiService = createApiService(context, "result_900.xml")
        val agentInstallResult = callAgentInstall(apiService)
        assertFalse(agentInstallResult.isSuccess)
        assertEquals(900, GlobalStatic.g_errNumber)
        assertEquals("test 900 error", GlobalStatic.g_err)
    }

    @Test
    fun `호출중 RSException 이 발생할경우 예외가 발생한다`() {
        val apiService = createApiService(context, RSException(999))
        val errorCode = ((callAgentInstall(apiService) as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(999, errorCode)
    }

    @Test
    fun `RESULT 가 0이면 설치 성공`() {
        val apiService = createApiService(context, "result_success_empty.xml")
        val agentInstallResult = callAgentInstall(apiService)
        assertTrue(agentInstallResult.isSuccess)
    }

    private fun callAgentInstall(webConnection: ApiService): Result<AgentInstallResult> {
        return webConnection.agentInstall(
                "guid-123",
                "accessid-123",
                "accessPass-123",
                "agentName-123",
                "bizid-123")

    }
}