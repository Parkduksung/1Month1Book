package com.rsupport.mobile.agent.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rsupport.mobile.agent.api.parser.AgentGroupListParser
import com.rsupport.mobile.agent.api.parser.StreamParser
import com.rsupport.mobile.agent.api.parser.StreamParserFactory
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.repo.device.DeviceRepository
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.XMLParser
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import junit.framework.Assert.*
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.java.KoinJavaComponent
import org.robolectric.RobolectricTestRunner
import org.xml.sax.SAXException
import java.io.IOException
import java.io.InputStream
import java.lang.RuntimeException
import java.security.GeneralSecurityException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.util.HashMap
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException

@RunWith(RobolectricTestRunner::class)
class WebConnectionAgentListTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `Network stream 을 생성하지 못하면 실패한다`() {
        val apiService = createApiService(context)
        val agentListResult = apiService.getAgentGroupList("", "")

        assertFalse(agentListResult.isSuccess)
    }

    @Test
    fun `Network 연결시 IO Exception 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IOException("io exception"))
        val agentListResult = apiService.getAgentGroupList("", "")
        assertFalse(agentListResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network 연결시 InvalidKeyException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidKeyException("InvalidKeyException"))
        val agentListResult = apiService.getAgentGroupList("", "")
        assertFalse(agentListResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network 연결시 BadPaddingException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, BadPaddingException("BadPaddingException"))
        val agentListResult = apiService.getAgentGroupList("", "")
        assertFalse(agentListResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network 연결시 InvalidAlgorithmParameterException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidAlgorithmParameterException("InvalidAlgorithmParameterException"))
        val agentListResult = apiService.getAgentGroupList("", "")
        assertFalse(agentListResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network 연결시 IllegalBlockSizeException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IllegalBlockSizeException("IllegalBlockSizeException"))
        val agentListResult = apiService.getAgentGroupList("", "")
        assertFalse(agentListResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network 연결시 알수 없는 오류가 발생하면 실패한다`() {
        val apiService = createApiService(context, RuntimeException("RuntimeException"))
        val agentListResult = apiService.getAgentGroupList("", "")
        assertFalse(agentListResult.isSuccess)
        assertEquals(RSErrorCode.UNKNOWN, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network 연결시 GeneralSecurityException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, GeneralSecurityException("GeneralSecurityException"))
        val agentListResult = apiService.getAgentGroupList("", "")
        assertFalse(agentListResult.isSuccess)
        assertEquals(RSErrorCode.UNKNOWN, GlobalStatic.g_errNumber)
    }

    @Test
    fun `데이터 parsing 중 SAXException 이 발생하면 실패한다`() {
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

        val apiService = createApiService(context, webConnection)
        val errorCode = ((apiService.getAgentGroupList("", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Parser.XML_SAX_ERROR, errorCode)
    }

    @Test
    fun `서버로부터 받은 데이터가 비어있을때 실패한다`() {
        val apiService = createApiService(context, "empty_data.xml")
        val errorCode = ((apiService.getAgentGroupList("", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Parser.XML_IO_ERROR, errorCode)
    }

    @Test
    fun `RETCODE 가 100이 아니면 실패한다`() {
        val apiService = createApiService(context, "retcode_900.xml")
        val agentListResult = apiService.getAgentGroupList("", "")
        assertFalse(agentListResult.isSuccess)
        assertEquals(900, GlobalStatic.g_errNumber)
    }


    @Test
    fun `RETCODE 가 100이면 성공한다`() {
        val apiService = createApiService(context, "/agentlist/retcode_success.xml")
        val agentListResult = apiService.getAgentGroupList("", "")
        assertTrue(agentListResult.isSuccess)
    }

    @Test
    fun `root gourp 이 1개 있어야한다`() {
        val apiService = createApiService(context, "/agentlist/retcode_success_group.xml")
        val agentListResult = apiService.getAgentGroupList("", "")
        assertTrue(agentListResult.isSuccess)

        assertEquals(1, GlobalStatic.g_vecGroups.size)
        assertEquals("root", GlobalStatic.g_vecGroups[0].groupName)
        assertEquals("100000000g00000000", GlobalStatic.g_vecGroups[0].grpid)
        assertEquals("0", GlobalStatic.g_vecGroups[0].pgrpid)
    }


    @Test
    fun `group name 이 CDATA 일경우 이름만 가져와야한다`() {
        val apiService = createApiService(context, "/agentlist/retcode_success_group_cdata.xml")
        val agentListResult = apiService.getAgentGroupList("", "")
        assertTrue(agentListResult.isSuccess)
        assertEquals(1, GlobalStatic.g_vecGroups.size)
        assertEquals("mac_test", GlobalStatic.g_vecGroups[0].groupName)
        assertEquals("4d56f858-7719-47a2-b470-b3d3aafdbb37", GlobalStatic.g_vecGroups[0].grpid)
        assertEquals("100000000g00000000", GlobalStatic.g_vecGroups[0].pgrpid)
        assertEquals("0", GlobalStatic.g_vecGroups[0].grpCount)
    }

    @Test
    fun `조회에 성공하고 RootGroup 1개, Group 정보가 1개일때 조회 사이즈는 1이고 RootGroup 만 조회 되어야한다`() {
        val apiService = createApiService(context, "/agentlist/retcode_success_group_rootgroup.xml")
        val agentListResult = apiService.getAgentGroupList("", "")

        assertTrue(agentListResult.isSuccess)
        (agentListResult as Result.Success).apply {
            assertEquals(1, value.count())
            assertEquals("100000000g00000000", value[0].grpid)
            assertEquals("0", value[0].pgrpid)
        }
    }
}