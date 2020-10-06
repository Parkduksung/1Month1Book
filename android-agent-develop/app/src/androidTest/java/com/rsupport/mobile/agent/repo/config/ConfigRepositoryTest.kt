package com.rsupport.mobile.agent.repo.config

import android.content.Context
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.constant.GlobalStatic
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.java.KoinJavaComponent.inject
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ConfigRepositoryTest {

    private val configRepository by inject(ConfigRepository::class.java)
    private val context by inject(Context::class.java)

    // 기업용 서버 주소를 확인한다.
    @Test
    fun corpServerURLTest() = runBlocking {
        val corpServerInfo = configRepository.getCorpServerURL()
        MatcherAssert.assertThat("서버주소가 CORP 가 아니라서 실패", corpServerInfo.url, Matchers.`is`(context.getString(R.string.serverip_biz)))
    }

    // 개인용 서버 주소를 확인하다.
    @Test
    fun persnalServerURLTest() = runBlocking {
        val corpServerInfo = configRepository.getPersonalServerURL()
        MatcherAssert.assertThat("서버주소가 Personal 가 아니라서 실패", corpServerInfo.url, Matchers.`is`(context.getString(R.string.serverip_personal)))
    }

    // Custom 서버주소에 http를 포함하지 않고 설정된 서버주소에 http 를 포함하는지를 확인한다.
    @Test
    fun customServerExcludeHttpURLTest() = runBlocking {
        configRepository.setCustomServerURL("rview.com")
        val customServerInfo = configRepository.getCustomServerURL()
        MatcherAssert.assertThat("서버주소가 http 가 없어서 실패", customServerInfo.url, Matchers.`is`("http://rview.com"))
    }

    // Custom 서버 주소를 http 를 포함하고 설정된 서버주소를 확인한다.
    @Test
    fun customServerIncludeHttpURLTest() = runBlocking {
        configRepository.setCustomServerURL("http://rview.com")
        val customServerInfo = configRepository.getCustomServerURL()
        MatcherAssert.assertThat("설정된 서버 주소가 달라서 실패", customServerInfo.url, Matchers.`is`("http://rview.com"))
    }

    // ProductType 을 CORP 로 설정하고 값을 확인한다.
    @Test
    fun productTypeCorpURLTest() = runBlocking {
        configRepository.setProductType(GlobalStatic.PRODUCT_CORP)
        val serverInfo = configRepository.getServerInfo()
        MatcherAssert.assertThat("설정된 서버 주소가 corp URL 이 아니어서 실패", serverInfo.url, Matchers.`is`(context.getString(R.string.serverip_biz)))
    }

    // ProductType 을 PERSONAL 로 설정하고 값을 확인한다.
    @Test
    fun productTypePersonalURLTest() = runBlocking {
        configRepository.setProductType(GlobalStatic.PRODUCT_PERSONAL)
        val serverInfo = configRepository.getServerInfo()
        MatcherAssert.assertThat("설정된 서버 주소가 Personal URL이 아니어서 실패", serverInfo.url, Matchers.`is`(context.getString(R.string.serverip_personal)))
    }

    // ProductType 을 CUSTOM 로 설정하고 값을 확인한다.
    @Test
    fun productTypeCustomURLTest() = runBlocking {
        configRepository.setProductType(GlobalStatic.PRODUCT_SERVER)
        configRepository.setCustomServerURL("rview.cn")
        val serverInfo = configRepository.getServerInfo()
        MatcherAssert.assertThat("설정된 서버 주소가 customServerURL 이 아니라서 실패", serverInfo.url, Matchers.`is`("http://rview.cn"))
    }

    // 튜토리얼 설정후 값을 확인한다.
    @Test
    fun tutorialShowTest() = runBlocking {
        configRepository.setShowTutorial(true)
        val isShowTutorial = configRepository.isShowTutorial()
        MatcherAssert.assertThat("tutorial 값이 달라서 실패", isShowTutorial, Matchers.`is`(true))
    }

    // AppStart 설정후 값을 확인한다.
    @Test
    fun startAppTest() = runBlocking {
        configRepository.setStartApp(true)
        val isStartApp = configRepository.getStartApp()
        MatcherAssert.assertThat("startApp 값이 달라서 실패", isStartApp, Matchers.`is`(true))
    }

    // Proxy 사용여부를 toggle 후 값을 확인한다.
    @Test
    fun togglePrioxyUsedTest() = runBlocking {
        configRepository.setUseProxy(false)
        configRepository.toggleProxyUse()
        val toggleResult = configRepository.isProxyUse()
        MatcherAssert.assertThat("toggle 했는데 값이 바뀌지 않아서 실패", toggleResult, Matchers.`is`(true))
    }

    // ProxyInfo 를 설정하고 값을 확인한다.
    @Test
    fun changeProxyInfoTest() = runBlocking {
        val expectedProxyInfo = ProxyInfo(address = "rview.cn")
        configRepository.setProxyInfo(expectedProxyInfo)
        val proxyInfo = configRepository.getProxyInfo()
        MatcherAssert.assertThat("proxy address 가 달라서 실패", proxyInfo, Matchers.`is`(expectedProxyInfo))
    }

    // 최초 실행시 값을 확인한다.
    @Test
    fun firstStartTest() = runBlocking {
        configRepository.delete()
        val isFirstLaunch = configRepository.isFirstLaunch()
        MatcherAssert.assertThat("첫번째 실행 값이 달라서 실패", isFirstLaunch, Matchers.`is`(true))
    }

    // 공지 번호를 증가시키고 새로운 공지가 있는지 확인한다.
    @Test
    fun noticeSeqIncreaseTest() = runBlocking {
        configRepository.delete()
        configRepository.setNewNoticeSeq(1)
        val hasNewNotice = configRepository.hasNewNotice()
        MatcherAssert.assertThat("새로운 공지가 표시 되지 않아서 실패", hasNewNotice, Matchers.`is`(true))
    }

    // 공지 번호를 증가시키고 새로운 공지를 확인했을때 새로운 공지가 없는지를 확인한다.
    @Test
    fun noticeSeqUpdateTest() = runBlocking {
        configRepository.delete()
        configRepository.setNewNoticeSeq(1)
        configRepository.syncNoticeSeq()
        val hasNewNotice = configRepository.hasNewNotice()
        MatcherAssert.assertThat("새로운 공지를 확인했지만 새로운 공지가 있어서 실패", hasNewNotice, Matchers.`is`(false))
    }
}