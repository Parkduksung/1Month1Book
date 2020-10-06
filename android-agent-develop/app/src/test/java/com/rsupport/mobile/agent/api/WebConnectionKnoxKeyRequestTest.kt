package com.rsupport.mobile.agent.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rsupport.mobile.agent.BuildConfig
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
class WebConnectionKnoxKeyRequestTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `Network stream 생성이 안되면 실패한다`() {
        val apiService = createApiService(context)
        val knoxResult = apiService.requestKnoxEnterpriseKey(BuildConfig.VERSION_NAME)
        assertFalse(knoxResult.isSuccess)
    }

    @Test
    fun `Network stream 생성시 IOException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IOException("IOException"))
        val knoxKeyResult = apiService.requestKnoxEnterpriseKey(BuildConfig.VERSION_NAME)
        assertFalse(knoxKeyResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
        assertEquals(RSErrorCode.Network.IO_ERROR, knoxKeyResult.getErrorCode())
    }

    @Test
    fun `Network stream 생성시 InvalidKeyException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidKeyException("InvalidKeyException"))
        val knoxKeyResult = apiService.requestKnoxEnterpriseKey(BuildConfig.VERSION_NAME)
        assertFalse(knoxKeyResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
        assertEquals(RSErrorCode.Network.IO_ERROR, knoxKeyResult.getErrorCode())
    }

    @Test
    fun `Network stream 생성시 BadPaddingException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, BadPaddingException("BadPaddingException"))
        val knoxKeyResult = apiService.requestKnoxEnterpriseKey(BuildConfig.VERSION_NAME)
        assertFalse(knoxKeyResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
        assertEquals(RSErrorCode.Network.IO_ERROR, knoxKeyResult.getErrorCode())
    }

    @Test
    fun `Network stream 생성시 InvalidAlgorithmParameterException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidAlgorithmParameterException("InvalidAlgorithmParameterException"))
        val knoxKeyResult = apiService.requestKnoxEnterpriseKey(BuildConfig.VERSION_NAME)
        assertFalse(knoxKeyResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
        assertEquals(RSErrorCode.Network.IO_ERROR, knoxKeyResult.getErrorCode())
    }

    @Test
    fun `Network stream 생성시 IllegalBlockSizeException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IllegalBlockSizeException("IllegalBlockSizeException"))
        val knoxKeyResult = apiService.requestKnoxEnterpriseKey(BuildConfig.VERSION_NAME)
        assertFalse(knoxKeyResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
        assertEquals(RSErrorCode.Network.IO_ERROR, knoxKeyResult.getErrorCode())
    }

    @Test
    fun `Network stream 생성시 알수없는 오류가 발생하면 실패한다`() {
        val apiService = createApiService(context, RuntimeException("RuntimeException"))
        val knoxKeyResult = apiService.requestKnoxEnterpriseKey(BuildConfig.VERSION_NAME)
        assertFalse(knoxKeyResult.isSuccess)
        assertEquals(RSErrorCode.UNKNOWN, GlobalStatic.g_errNumber)
        assertEquals(RSErrorCode.UNKNOWN, knoxKeyResult.getErrorCode())
    }

    @Test
    fun `서버로부터 받은데이터가 없으면 실패한다`() {
        val apiService = createApiService(context, "empty_data.xml")
        val knoxKeyResult = apiService.requestKnoxEnterpriseKey(BuildConfig.VERSION_NAME)
        assertFalse(knoxKeyResult.isSuccess)
        assertEquals(RSErrorCode.Parser.XML_IO_ERROR, GlobalStatic.g_errNumber)
        assertEquals(RSErrorCode.Parser.XML_IO_ERROR, knoxKeyResult.getErrorCode())
    }

    @Test
    fun `RETCODE 가 900이면 실패한다`() {
        val apiService = createApiService(context, "retcode_900.xml")
        val knoxResult = apiService.requestKnoxEnterpriseKey(BuildConfig.VERSION_NAME)
        assertFalse(knoxResult.isSuccess)
        assertEquals(900, GlobalStatic.g_errNumber)
        assertEquals(900, knoxResult.getErrorCode())
    }

    @Test
    fun `RETCODE 가 100이면 성공한다`() {
        val webConnection = createWebConnection(context, xmlFile = "knoxkey/knox_key_success.xml", cryptoFactory = DefaultWebCryptoFactory())
        val apiService = createApiService(context, webConnection)
        val knoxResult = apiService.requestKnoxEnterpriseKey(BuildConfig.VERSION_NAME)
        assertTrue(knoxResult.isSuccess)
        assertEquals("knox_key_1234", (knoxResult as Result.Success).value.knoxKey)
    }
}