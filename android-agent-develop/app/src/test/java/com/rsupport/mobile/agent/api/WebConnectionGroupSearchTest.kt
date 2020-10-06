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
class WebConnectionGroupSearchTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `Network stream을 생성하지 못하면 실패한다`() {
        val apiService = createApiService(context)
        val groupSearchResult = apiService.getGroupSearch(", ", "", "", "")
        assertFalse(groupSearchResult.isSuccess)
    }

    @Test
    fun `Network stream 생성시 IOException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IOException("IOException"))
        val groupSearchResult = apiService.getGroupSearch(", ", "", "", "")
        assertFalse(groupSearchResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 InvalidKeyException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidKeyException("InvalidKeyException"))
        val groupSearchResult = apiService.getGroupSearch(", ", "", "", "")
        assertFalse(groupSearchResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 BadPaddingException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, BadPaddingException("BadPaddingException"))
        val groupSearchResult = apiService.getGroupSearch(", ", "", "", "")
        assertFalse(groupSearchResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 InvalidAlgorithmParameterException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, InvalidAlgorithmParameterException("InvalidAlgorithmParameterException"))
        val groupSearchResult = apiService.getGroupSearch(", ", "", "", "")
        assertFalse(groupSearchResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 IllegalBlockSizeException 이 발생하면 실패한다`() {
        val apiService = createApiService(context, IllegalBlockSizeException("IllegalBlockSizeException"))
        val groupSearchResult = apiService.getGroupSearch(", ", "", "", "")
        assertFalse(groupSearchResult.isSuccess)
        assertEquals(RSErrorCode.Network.IO_ERROR, GlobalStatic.g_errNumber)
    }

    @Test
    fun `Network stream 생성시 알수 없는 오류 발생하면 실패한다`() {
        val apiService = createApiService(context, RuntimeException("RuntimeException"))
        val groupSearchResult = apiService.getGroupSearch(", ", "", "", "")
        assertFalse(groupSearchResult.isSuccess)
        assertEquals(RSErrorCode.UNKNOWN, GlobalStatic.g_errNumber)
    }

    @Test
    fun `xml parsing 오류 발생시 실패한다`() {
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
        val errorCode = ((apiService.getGroupSearch(", ", "", "", "") as? Result.Failure)?.throwable as? RSException)?.errorCode
        assertEquals(RSErrorCode.Parser.XML_SAX_ERROR, errorCode)
    }

    @Test
    fun `서버로부터 비어있는데이터를 받으면 조회 XML_IO_ERROR 로 실패한다`() {
        val apiService = createApiService(context, "empty_data.xml")
        val groupSearchResult = apiService.getGroupSearch(", ", "", "", "")

        val errorCode = ((groupSearchResult as? Result.Failure)?.throwable as? RSException)?.errorCode

        assertFalse(groupSearchResult.isSuccess)
        assertEquals(RSErrorCode.Parser.XML_IO_ERROR, errorCode)
    }


    @Test
    fun `RETCODE 가 100이 아니면 조회 실패한다`() {
        val apiService = createApiService(context, "retcode_900.xml")
        val groupSearchResult = apiService.getGroupSearch(", ", "", "", "")
        assertFalse(groupSearchResult.isSuccess)
        assertEquals(900, GlobalStatic.g_errNumber)
    }

    @Test
    fun `RETCODE 가 100이고 데이터가 없으면 조회 사이즈가 0어이야한다`() {
        val apiService = createApiService(context, "retcode_success100_empty.xml")
        val groupSearchResult = apiService.getGroupSearch(", ", "", "", "")
        assertTrue(groupSearchResult.isSuccess)
        (groupSearchResult as Result.Success).apply {
            assertEquals(0, value.count())
        }
    }

    @Test
    fun `조회에 성공하고 Group 정보가 1개일때 조회 사이즈는 1이어야한다`() {
        val apiService = createApiService(context, "/groupsearch/retcode_success_groupsearch.xml")
        val groupSearchResult = apiService.getGroupSearch(", ", "", "", "")
        assertTrue(groupSearchResult.isSuccess)
        (groupSearchResult as Result.Success).apply {
            assertEquals(1, value.count())
            assertEquals("4d56f858-7719-47a2-b470-b3d3aafdbb37", value[0].grpid)
            assertEquals("100000000g00000000", value[0].pgrpid)
            assertEquals("mac_test", value[0].groupName)
            assertEquals("1", value[0].grpCount)
        }
    }

    @Test
    fun `조회에 성공하고 RootGroup 1개, Group 정보가 1개일때 조회 사이즈는 1이고 Group 만 조회 되어야한다`() {
        val apiService = createApiService(context, "/groupsearch/retcode_success_groupsearch_rootgroup.xml")
        val groupSearchResult = apiService.getGroupSearch(", ", "", "", "")
        assertTrue(groupSearchResult.isSuccess)
        (groupSearchResult as Result.Success).apply {
            assertEquals(1, value.count())
            assertEquals("4d56f858-7719-47a2-b470-b3d3aafdbb37", value[0].grpid)
            assertEquals("100000000g00000000", value[0].pgrpid)
            assertEquals("mac_test", value[0].groupName)
            assertEquals("1", value[0].grpCount)
        }
    }
}