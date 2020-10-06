package com.rsupport.mobile.agent.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rsupport.mobile.agent.api.parser.AgentGroupListParser
import com.rsupport.mobile.agent.api.parser.StreamParser
import com.rsupport.mobile.agent.api.parser.StreamParserFactory
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.repo.device.DeviceRepository
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.XMLParser
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import junit.framework.Assert.*
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.java.KoinJavaComponent
import org.robolectric.RobolectricTestRunner
import org.xml.sax.SAXException
import java.io.IOException
import java.io.InputStream
import java.lang.RuntimeException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.util.HashMap
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException

@RunWith(RobolectricTestRunner::class)
class WebConnectionAgentSubListTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `Network stream 을 생성하지 못하면 실패한다`() {
        val apiService = createApiService(context)
        val agentSubListResult = apiService.getAgentSubGroupList("", "")
        assertFalse(agentSubListResult.isSuccess)
    }


    @Test
    fun `Network stream 생성시 IOException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IOException("IOException"))
        val agentSubGroupListResult = apiService.getAgentSubGroupList("", "")
        Assert.assertFalse(agentSubGroupListResult.isSuccess)
        Assert.assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 InvalidKeyException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidKeyException("InvalidKeyException"))
        val agentSubGroupListResult = apiService.getAgentSubGroupList("", "")
        Assert.assertFalse(agentSubGroupListResult.isSuccess)
        Assert.assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 BadPaddingException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, BadPaddingException("BadPaddingException"))
        val agentSubGroupListResult = apiService.getAgentSubGroupList("", "")
        Assert.assertFalse(agentSubGroupListResult.isSuccess)
        Assert.assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 InvalidAlgorithmParameterException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidAlgorithmParameterException("InvalidAlgorithmParameterException"))
        val agentSubGroupListResult = apiService.getAgentSubGroupList("", "")
        Assert.assertFalse(agentSubGroupListResult.isSuccess)
        Assert.assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 IllegalBlockSizeException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IllegalBlockSizeException("IllegalBlockSizeException"))
        val agentSubGroupListResult = apiService.getAgentSubGroupList("", "")
        Assert.assertFalse(agentSubGroupListResult.isSuccess)
        Assert.assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 알수 없는 오류가 발생하면 실패한다`() {
        val apiService = createApiService(context, RuntimeException("RuntimeException"))
        val agentSubGroupListResult = apiService.getAgentSubGroupList("", "")
        Assert.assertFalse(agentSubGroupListResult.isSuccess)
        Assert.assertEquals(RSErrorCode.UNKNOWN, GlobalStatic.g_errNumber)
    }

    @Test
    fun `서버가 비어있는 데이터를 내려주면 실패한다`() {
        val apiService = createApiService(context, "empty_data.xml")
        val agentSubListResult = apiService.getAgentSubGroupList("", "")
        assertFalse(agentSubListResult.isSuccess)
    }

    @Test
    fun `Network stream 생성중 IO Exception 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IOException("io exception"))
        val errorCode = ((apiService.getAgentSubGroupList("", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Network.IO_ERROR, errorCode)
    }

    @Test
    fun `parsing 중 오류가 발생하면면 실패한다`() {
        val webConnection = createWebConnection(context, "empty_data.xml", streamParserFactory = object : StreamParserFactory {
            override fun <T, M> create(clazz: Class<T>): StreamParser<M> {
                return AgentGroupListParser(
                        object : XMLParser() {
                            override fun parse(`is`: InputStream?): HashMap<String, String> {
                                throw SAXException()
                            }
                        }
                ) as StreamParser<M>
            }
        });

        val apiService = createApiService(context, webConnection = webConnection)

        val errorCode = ((apiService.getAgentSubGroupList("", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Parser.XML_SAX_ERROR, errorCode)
    }

    @Test
    fun `RETCODE 가 100이 아니면 실패한다`() {
        val apiService = createApiService(context, "retcode_900.xml")
        val agentSubListResult = apiService.getAgentSubGroupList("", "")
        assertFalse(agentSubListResult.isSuccess)
        assertEquals(900, GlobalStatic.g_errNumber)
    }

    @Test
    fun `RETCODE 가 100 이지만 데이터가 없으면 빈값으로 반환한다`() {
        val apiService = createApiService(context, "retcode_success100_empty.xml")
        val agentSubListResult = apiService.getAgentSubGroupList("", "")
        assertTrue(agentSubListResult.isSuccess)
    }

    @Test
    fun `데이터가 있을경우 groupInfo 를 반환한다`() {
        val apiService = createApiService(context, "/agentsublist/retcode_success_group.xml")
        val agentSubListResult = apiService.getAgentSubGroupList("", "")
        assertTrue(agentSubListResult.isSuccess)

        (agentSubListResult as Result.Success).value[0].apply {
            assertEquals("4d56f858-7719-47a2-b470-b3d3aafdbb37", grpid)
            assertEquals("100000000g00000000", pgrpid)
            assertEquals("mac_test", grpname)
        }
    }

    @Test
    fun `조회에 성공하고 RootGroup 1개, Group 정보가 1개일때 조회 사이즈는 1이고 Group 만 조회 되어야한다`() {
        val apiService = createApiService(context, "/agentsublist/retcode_success_group_rootgroup.xml")
        val agentSubListResult = apiService.getAgentSubGroupList("", "")

        assertTrue(agentSubListResult.isSuccess)
        (agentSubListResult as Result.Success).value[0].apply {
            assertEquals("4d56f858-7719-47a2-b470-b3d3aafdbb37", grpid)
            assertEquals("100000000g00000000", pgrpid)
            assertEquals("mac_test", grpname)
        }
    }
}