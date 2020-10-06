package com.rsupport.mobile.agent.ui.settings.basic

import androidx.lifecycle.Observer
import androidx.lifecycle.asFlow
import base.BaseTest
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.repo.config.ProxyInfo
import com.rsupport.mobile.agent.repo.config.ServerInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.module.Module
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ServerSettingViewModelTest : BaseTest() {

    @Mock
    lateinit var productTypeObserver: Observer<ServerSettingViewModel.ProductType>

    @Mock
    lateinit var proxyModelObserver: Observer<ServerSettingViewModel.ProxyModel>

    @Mock
    lateinit var serverSettingInteractor: ServerSettingInteractor

    private lateinit var serverSettingViewModel: ServerSettingViewModel

    override fun createModules(): List<Module> {
        return emptyList();
    }

    @Before
    override fun setup() {
        super.setup()
        `when`(serverSettingInteractor.getCustomServerInfo()).thenReturn(ServerInfo(""))
        Mockito.`when`(serverSettingInteractor.getProxyInfo()).thenReturn(ProxyInfo())
        Mockito.`when`(serverSettingInteractor.useProxy).thenReturn(false)

        serverSettingViewModel = ServerSettingViewModel(application, serverSettingInteractor)
    }

    // Standard / Enterprise 제품을 선택 후 결과를 확인한다.
    @Test
    fun standardServerSelectStateChangedTest() {
        serverSettingViewModel.productType.observeForever(productTypeObserver)
        serverSettingViewModel.setStandardServer()
        Mockito.verify(productTypeObserver).onChanged(ServerSettingViewModel.ProductType.Standard)
    }

    private val proxyURL = "www.rview.com"
    private val customServerURL = "www.rview.com"

    // 서버 제품을 선택 후 결과를 확인한다.
    @Test
    fun customServerSelectStateChangedTest() {

        val customServerInfo = ServerInfo(customServerURL)
        Mockito.`when`(serverSettingInteractor.setCustomServer()).thenReturn(customServerInfo)

        serverSettingViewModel.productType.observeForever(productTypeObserver)
        serverSettingViewModel.setCustomServer()
        Mockito.verify(productTypeObserver).onChanged(ServerSettingViewModel.ProductType.Customer(customServerInfo))
    }

    // standard 제품 을 선택후 선택이 되었는지를 확인한다.(기업용)
    @Test
    fun standardGroupServerSelectStateTest() {
        Mockito.`when`(serverSettingInteractor.getProductType()).thenReturn(GlobalStatic.PRODUCT_CORP)
        serverSettingViewModel.setStandardServer()
        val productType = serverSettingViewModel.getProductType()
        assertThat("Standard 제품으로 설정했는데 설정이 안되서 실패", productType, Matchers.instanceOf(ServerSettingViewModel.ProductType.Standard::class.java))
    }

    // standard 제품 을 선택후 선택이 되었는지를 확인한다.(개인용)
    @Test
    fun standardPersonalServerSelectStateTest() {
        Mockito.`when`(serverSettingInteractor.getProductType()).thenReturn(GlobalStatic.PRODUCT_PERSONAL)
        serverSettingViewModel.setStandardServer()
        val productType = serverSettingViewModel.getProductType()
        assertThat("Standard 제품으로 설정했는데 설정이 안되서 실패", productType, Matchers.instanceOf(ServerSettingViewModel.ProductType.Standard::class.java))
    }

    // custom server 제품 을 선택후 선택이 되었는지를 확인한다.
    @Test
    fun customServerSelectStateTest() {
        Mockito.`when`(serverSettingInteractor.getProductType()).thenReturn(GlobalStatic.PRODUCT_SERVER)
        Mockito.`when`(serverSettingInteractor.getServerInfo()).thenReturn(ServerInfo(customServerURL))
        Mockito.`when`(serverSettingInteractor.setCustomServer()).thenReturn(ServerInfo(customServerURL))
        serverSettingViewModel.setCustomServer()
        val productType = serverSettingViewModel.getProductType()
        assertThat("custom 제품으로 설정했는데 설정이 안되서 실패", productType, Matchers.instanceOf(ServerSettingViewModel.ProductType.Customer::class.java))
    }

    // 서버정보를 설정후 설정된 서버 정보를 확인한다.
    @Test
    fun customServerSelectURLTest() {
        Mockito.`when`(serverSettingInteractor.getProductType()).thenReturn(GlobalStatic.PRODUCT_SERVER)
        Mockito.`when`(serverSettingInteractor.getServerInfo()).thenReturn(ServerInfo(customServerURL))
        Mockito.`when`(serverSettingInteractor.setCustomServer()).thenReturn(ServerInfo(customServerURL))
        serverSettingViewModel.setCustomServer()
        val productType = serverSettingViewModel.getProductType()
        assertThat("custom 제품으로 설정했는데 설정이 안되서 실패", (productType as ServerSettingViewModel.ProductType.Customer).serverInfo, Matchers.`is`(ServerInfo(customServerURL)))
    }

    // Proxy toggle 이 되는지 확인한다.
    @Test
    fun proxyToggleTest() = runBlocking {
        val beforeProxyState = serverSettingViewModel.isUseProxy.asFlow().first()
        serverSettingViewModel.toggleProxyUse()
        val afterProxyState = serverSettingViewModel.isUseProxy.asFlow().first()

        assertThat("proxy 설정 값이 바뀌지 않아서 실패", afterProxyState, Matchers.not(beforeProxyState))
    }

    // Proxy 사용으로 설정 후 결과를 확인한다.
    @Test
    fun proxyUseStateTest() {
        Mockito.`when`(serverSettingInteractor.useProxy).thenReturn(true)
        Mockito.`when`(serverSettingInteractor.getProxyInfo()).thenReturn(ProxyInfo())
        serverSettingViewModel.setUseProxy(true)
        val proxyUseState = serverSettingViewModel.getProxyModel()
        assertThat("proxy 사용설정을 했는데 설정되지 않아서 실패", proxyUseState.isUse, Matchers.`is`(true))
    }

    // Proxy 사용으로 설정되었을때 상태 변화를 확인한다.
    @Test
    fun proxyUseStateObserverTest() {
        Mockito.`when`(serverSettingInteractor.getProxyInfo()).thenReturn(ProxyInfo())
        serverSettingViewModel.setUseProxy(true)

        serverSettingViewModel.proxyModel.observeForever(proxyModelObserver)
        Mockito.verify(proxyModelObserver).onChanged(ServerSettingViewModel.ProxyModel(true))
    }

    // Proxy 사용안함으로 설정 후 결과를 확인한다.
    @Test
    fun proxyNotUseStateTest() {
        Mockito.`when`(serverSettingInteractor.useProxy).thenReturn(false)
        Mockito.`when`(serverSettingInteractor.getProxyInfo()).thenReturn(ProxyInfo())

        serverSettingViewModel.setUseProxy(false)
        val proxyUseState = serverSettingViewModel.getProxyModel()
        assertThat("proxy 사용설정을 안했는데 설정되어서 실패", proxyUseState.isUse, Matchers.`is`(false))
    }

    // Proxy 미사용으로 설정되었을때 상태 변화를 확인한다.
    @Test
    fun proxyNotUseStateObserverTest() {
        Mockito.`when`(serverSettingInteractor.getProxyInfo()).thenReturn(ProxyInfo())
        serverSettingViewModel.setUseProxy(false)
        serverSettingViewModel.proxyModel.observeForever(proxyModelObserver)
        Mockito.verify(proxyModelObserver).onChanged(ServerSettingViewModel.ProxyModel(false))
    }

    // Proxy 설정 상태에서 URL을 설정 후 결과를 확인한다.
    @Test
    fun proxyUseURLTest() {
        Mockito.`when`(serverSettingInteractor.getProxyInfo()).thenReturn(ProxyInfo(proxyURL))
        Mockito.`when`(serverSettingInteractor.useProxy).thenReturn(true)

        serverSettingViewModel.setUseProxy(true)
        serverSettingViewModel.setProxyURL(proxyURL)
        serverSettingViewModel.proxyModel.observeForever(proxyModelObserver)

        Mockito.verify(proxyModelObserver).onChanged(ServerSettingViewModel.ProxyModel(true, proxyURL))
        assertThat("proxy url 설정 값이 달라서 실패", serverSettingViewModel.getProxyModel().url, Matchers.`is`(proxyURL))
    }

    private val proxyPort = "8080"

    // Proxy 설정 상태에서 Port 를 설정 후 결과를 확인한다.
    @Test
    fun proxyUsePortTest() {
        Mockito.`when`(serverSettingInteractor.getProxyInfo()).thenReturn(ProxyInfo(port = proxyPort))
        Mockito.`when`(serverSettingInteractor.useProxy).thenReturn(true)

        serverSettingViewModel.setUseProxy(true)
        serverSettingViewModel.setProxyPort(proxyPort)
        serverSettingViewModel.proxyModel.observeForever(proxyModelObserver)

        Mockito.verify(proxyModelObserver).onChanged(ServerSettingViewModel.ProxyModel(true, port = proxyPort))
        assertThat("proxy port 설정 값이 달라서 실패", serverSettingViewModel.getProxyModel().port, Matchers.`is`(proxyPort))
    }


    private val userID = "userid"

    // Proxy 설정 상태에서 userid 를 설정 후 결과를 확인한다.
    @Test
    fun proxyUseUserIdTest() {
        Mockito.`when`(serverSettingInteractor.getProxyInfo()).thenReturn(ProxyInfo(id = userID))
        Mockito.`when`(serverSettingInteractor.useProxy).thenReturn(true)

        serverSettingViewModel.setUseProxy(true)
        serverSettingViewModel.setProxyUserId(userID)
        serverSettingViewModel.proxyModel.observeForever(proxyModelObserver)

        Mockito.verify(proxyModelObserver).onChanged(ServerSettingViewModel.ProxyModel(true, userId = userID))
        assertThat("proxy userId 설정 값이 달라서 실패", serverSettingViewModel.getProxyModel().userId, Matchers.`is`(userID))
    }

    private val userPWD = "userpwd"

    // Proxy 설정 상태에서 userPwd 를 설정 후 결괄ㄹ 확인한다.
    @Test
    fun proxyUseUserPwdTest() {
        Mockito.`when`(serverSettingInteractor.getProxyInfo()).thenReturn(ProxyInfo(id = userPWD))
        Mockito.`when`(serverSettingInteractor.useProxy).thenReturn(true)

        serverSettingViewModel.setUseProxy(true)
        serverSettingViewModel.setProxyUserPwd(userPWD)
        serverSettingViewModel.proxyModel.observeForever(proxyModelObserver)

        Mockito.verify(proxyModelObserver).onChanged(ServerSettingViewModel.ProxyModel(true, userId = userPWD))
        assertThat("proxy userpwd 설정 값이 달라서 실패", serverSettingViewModel.getProxyModel().userId, Matchers.`is`(userPWD))
    }

    // CustomServerURL 이 초기 설정되는지 확인한다.
    @Test
    fun customInitializeTest() {
        `when`(serverSettingInteractor.getCustomServerInfo()).thenReturn(ServerInfo("https://rview.com"))

        val customServer = serverSettingViewModel.customServerURL.get()
        assertThat("초기 서버 주소가 설정되지 않아서 실패", customServer, Matchers.`is`("https://rview.com"))
    }

    // http를 선택했을때 기존 주소가 https 로 시작하면 http 로 시작하는 주소로 설정되는 것을 확인한다.
    @Test
    fun customServerURLReplaceHttpWhenHttpsTest() {
        `when`(serverSettingInteractor.getCustomServerInfo()).thenReturn(ServerInfo("https://rview.com"))
        serverSettingViewModel.selectedHttp()
        val serverURL = serverSettingViewModel.customServerURL.get()
        assertThat("http 로 시작하지 않아서 실패", serverURL, Matchers.`is`("http://rview.com"))
    }

    // http를 선택했을때 기존 주소가 https 도 아니고 http 도 아니면 http 로 시작하는 주소로 설정되는 것을 확인한다.
    @Test
    fun customServerURLReplaceHttpWhenNoneTest() {
        `when`(serverSettingInteractor.getCustomServerInfo()).thenReturn(ServerInfo("rview.com"))

        serverSettingViewModel.selectedHttp()
        val serverURL = serverSettingViewModel.customServerURL.get()
        assertThat("http 로 시작하지 않아서 실패", serverURL, Matchers.`is`("http://rview.com"))
    }


    // http를 선택했을때 http로 시작하면 그대로 설정되는것을 확인한다.
    @Test
    fun customServerURLReplaceHttpWhenHttpTest() {
        `when`(serverSettingInteractor.getCustomServerInfo()).thenReturn(ServerInfo("http://rview.com"))

        serverSettingViewModel.selectedHttp()
        val serverURL = serverSettingViewModel.customServerURL.get()
        assertThat("http 로 시작하지 않아서 실패", serverURL, Matchers.`is`("http://rview.com"))
    }

    // https를 선택했을때 기존 주소가 http 로 시작하면 httpㄴ 로 시작하는 주소로 설정되는 것을 확인한다.
    @Test
    fun customServerURLReplaceHttpsWhenHttpTest() {
        `when`(serverSettingInteractor.getCustomServerInfo()).thenReturn(ServerInfo("http://rview.com"))

        serverSettingViewModel.selectedHttps()
        val serverURL = serverSettingViewModel.customServerURL.get()
        assertThat("https 로 시작하지 않아서 실패", serverURL, Matchers.`is`("https://rview.com"))
    }

    // https를 선택했을때 기존 주소가 https 도 아니고 http 도 아니면 https 로 시작하는 주소로 설정되는 것을 확인한다.
    @Test
    fun customServerURLReplaceHttpsWhenNoneTest() {
        `when`(serverSettingInteractor.getCustomServerInfo()).thenReturn(ServerInfo("rview.com"))

        serverSettingViewModel.selectedHttps()
        val serverURL = serverSettingViewModel.customServerURL.get()
        assertThat("https 로 시작하지 않아서 실패", serverURL, Matchers.`is`("https://rview.com"))
    }

    // http를 선택했을때 http로 시작하면 그대로 설정되는것을 확인한다.
    @Test
    fun customServerURLReplaceHttpsWhenHttpsTest() {
        `when`(serverSettingInteractor.getCustomServerInfo()).thenReturn(ServerInfo("https://rview.com"))

        serverSettingViewModel.selectedHttps()
        val serverURL = serverSettingViewModel.customServerURL.get()
        assertThat("https 로 시작하지 않아서 실패", serverURL, Matchers.`is`("https://rview.com"))
    }

    // 기본 Custom 서버가 https 인지 확인한다.
    @Test
    fun should_https_when_default() = runBlocking<Unit> {
        `when`(serverSettingInteractor.getCustomServerInfo()).thenReturn(ServerInfo(""))
        val isHttps = serverSettingViewModel.isHttpsServer.value
        assertThat(isHttps, `is`(true))
    }
}

