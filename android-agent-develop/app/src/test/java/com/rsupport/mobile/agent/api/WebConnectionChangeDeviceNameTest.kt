package com.rsupport.mobile.agent.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.rscommon.define.RSErrorCode
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException

@RunWith(RobolectricTestRunner::class)
class WebConnectionChangeDeviceNameTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `Network stream 생성이 안되면 실패한다`() {
        val apiService = createApiService(context)
        val changeDeviceNameResult = apiService.deviceNameChange("", "", "", "", "")
        Assert.assertFalse(changeDeviceNameResult.isSuccess)
    }

    @Test
    fun `Network stream 생성시 IOException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IOException("IOException"))
        val changeDeviceNameResult = apiService.deviceNameChange("", "", "", "", "")
        Assert.assertFalse(changeDeviceNameResult.isSuccess)
        Assert.assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
        Assert.assertEquals(RSErrorCode.Network.IO_ERROR, changeDeviceNameResult.getErrorCode())
    }

    @Test
    fun `Network stream 생성시 InvalidKeyException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidKeyException("InvalidKeyException"))
        val changeDeviceNameResult = apiService.deviceNameChange("", "", "", "", "")
        Assert.assertFalse(changeDeviceNameResult.isSuccess)
        Assert.assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
        Assert.assertEquals(RSErrorCode.Network.IO_ERROR, changeDeviceNameResult.getErrorCode())
    }

    @Test
    fun `Network stream 생성시 BadPaddingException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, BadPaddingException("BadPaddingException"))
        val changeDeviceNameResult = apiService.deviceNameChange("", "", "", "", "")
        Assert.assertFalse(changeDeviceNameResult.isSuccess)
        Assert.assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
        Assert.assertEquals(RSErrorCode.Network.IO_ERROR, changeDeviceNameResult.getErrorCode())
    }

    @Test
    fun `Network stream 생성시 InvalidAlgorithmParameterException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidAlgorithmParameterException("InvalidAlgorithmParameterException"))
        val changeDeviceNameResult = apiService.deviceNameChange("", "", "", "", "")
        Assert.assertFalse(changeDeviceNameResult.isSuccess)
        Assert.assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
        Assert.assertEquals(RSErrorCode.Network.IO_ERROR, changeDeviceNameResult.getErrorCode())
    }

    @Test
    fun `Network stream 생성시 IllegalBlockSizeException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IllegalBlockSizeException("IllegalBlockSizeException"))
        val changeDeviceNameResult = apiService.deviceNameChange("", "", "", "", "")
        Assert.assertFalse(changeDeviceNameResult.isSuccess)
        Assert.assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
        Assert.assertEquals(RSErrorCode.Network.IO_ERROR, changeDeviceNameResult.getErrorCode())
    }

    @Test
    fun `Network stream 생성시 알수없는 오류가 발생하면 실패한다`() {
        val apiService = createApiService(context, RuntimeException("RuntimeException"))
        val changeDeviceNameResult = apiService.deviceNameChange("", "", "", "", "")
        Assert.assertFalse(changeDeviceNameResult.isSuccess)
        Assert.assertEquals(RSErrorCode.UNKNOWN, GlobalStatic.g_errNumber)
        Assert.assertEquals(RSErrorCode.UNKNOWN, changeDeviceNameResult.getErrorCode())
    }

    @Test
    fun `서버로부터 받은데이터가 없으면 실패한다`() {
        val apiService = createApiService(context, "empty_data.xml")
        val changeDeviceNameResult = apiService.deviceNameChange("", "", "", "", "")
        Assert.assertFalse(changeDeviceNameResult.isSuccess)
        Assert.assertEquals(RSErrorCode.Parser.XML_IO_ERROR, GlobalStatic.g_errNumber)
        Assert.assertEquals(RSErrorCode.Parser.XML_IO_ERROR, changeDeviceNameResult.getErrorCode())
    }

    @Test
    fun `RETCODE 가 900이면 실패한다`() {
        val apiService = createApiService(context, "result_900.xml")
        val changeDeviceNameResult = apiService.deviceNameChange("", "", "", "", "")
        Assert.assertFalse(changeDeviceNameResult.isSuccess)
        Assert.assertEquals(900, GlobalStatic.g_errNumber)
        Assert.assertEquals(900, changeDeviceNameResult.getErrorCode())
    }

    @Test
    fun `RETCODE 가 0이면 성공한다`() {
        val apiService = createApiService(context, "result_success_empty.xml")
        val changeDeviceNameResult = apiService.deviceNameChange("", "", "", "", "")
        Assert.assertTrue(changeDeviceNameResult.isSuccess)
    }

    @Test
    fun `RETCODE 가 0 이지만 STOP 이 1이면 실패한다`() {
        val apiService = createApiService(context, "result_success_stop.xml")
        val changeDeviceNameResult = apiService.deviceNameChange("", "", "", "", "")
        Assert.assertFalse(changeDeviceNameResult.isSuccess)
        Assert.assertEquals(RSErrorCode.UNKNOWN, changeDeviceNameResult.getErrorCode())
    }
}