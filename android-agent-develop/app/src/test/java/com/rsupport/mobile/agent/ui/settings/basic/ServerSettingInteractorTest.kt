package com.rsupport.mobile.agent.ui.settings.basic

import base.BaseTest
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.repo.config.ProxyInfo
import com.rsupport.mobile.agent.repo.config.ServerInfo
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.Mockito

class ServerSettingInteractorTest : BaseTest() {

    private val basicSettingInteractor = ServerSettingInteractor()

    @Mock
    private lateinit var configRepository: ConfigRepository

    override fun createModules(): List<Module> {
        return listOf(module {
            single { configRepository }
        })
    }

    override fun setup() {
        super.setup()
        Mockito.`when`(configRepository.getServerInfo()).thenReturn(ServerInfo("https://www.rview.com"))
    }

    // standard, Enterprise 선택시 서버 주소를 확인한다.
    @Test
    fun selectStandardServerURLTest() {
        Mockito.`when`(configRepository.getServerInfo()).thenReturn(ServerInfo("https://www.rview.com"))

        val serverInfo = basicSettingInteractor.setStandardServer()
        val savedServerInfo = basicSettingInteractor.getServerInfo()
        MatcherAssert.assertThat("설정한 서버 주소가 달라서 실패", serverInfo, Matchers.`is`(savedServerInfo))
    }

    // standard, Enterprise 선택시 ProductType 을 확인한다.
    @Test
    fun selectStandardServerProductTypeTest() {
        Mockito.`when`(configRepository.getProductType()).thenReturn(GlobalStatic.PRODUCT_CORP)

        basicSettingInteractor.setStandardServer()
        val productType = basicSettingInteractor.getProductType()
        MatcherAssert.assertThat("Standard ProductType 이 달라서 실패", productType, Matchers.`is`(GlobalStatic.PRODUCT_CORP))
    }

    // customServer 를 선택시 서버 주소를 확인한다.
    @Test
    fun selectCustomServerURLTest() {
        Mockito.`when`(basicSettingInteractor.getServerInfo()).thenReturn(ServerInfo("http://rview.cn"))
        basicSettingInteractor.setCustomServer()
        basicSettingInteractor.updateCustomServer("rview.cn")


        val serverInfo = basicSettingInteractor.getServerInfo()
        MatcherAssert.assertThat("설정한 서버 주소가 달라서 실패", serverInfo.url, Matchers.`is`("http://rview.cn"))
    }


    // customServer 를 선택시 ProductType 을 확인한다.
    @Test
    fun selectCustomServerProductTypeTest() {
        Mockito.`when`(basicSettingInteractor.getServerInfo()).thenReturn(ServerInfo("https://rview.cn"))
        basicSettingInteractor.setCustomServer()
        basicSettingInteractor.updateCustomServer("https://rview.cn")
        val serverInfo = basicSettingInteractor.getServerInfo()
        MatcherAssert.assertThat("설정한 서버 주소가 달라서 실패", serverInfo.url, Matchers.`is`("https://rview.cn"))
    }

    // Proxy 사용 설정하고 결과를 확인한다.
    @Test
    fun selectProxyUseTest() {
        Mockito.`when`(configRepository.isProxyUse()).thenReturn(true)
        basicSettingInteractor.useProxy = true
        MatcherAssert.assertThat("proxy true 설정했는데 값이 달라서 실패", true, Matchers.`is`(basicSettingInteractor.useProxy))
    }

    // ProxyInfo 빈값을 설정하고 결과를 확인한다.
    @Test
    fun selectProxyTest() {
        Mockito.`when`(configRepository.getProxyInfo()).thenReturn(ProxyInfo())
        basicSettingInteractor.setProxyInfo(ProxyInfo())
        val proxyInfo = basicSettingInteractor.getProxyInfo()
        MatcherAssert.assertThat("ProxyInfo 를 설정했는데 값이 달라서 실패", ProxyInfo(), Matchers.`is`(proxyInfo))
    }

    // ProxyInfo url을 설정하고 결과를 확인한다.
    @Test
    fun selectProxyAddressTest() {
        Mockito.`when`(configRepository.getProxyInfo()).thenReturn(ProxyInfo(address = "rview.com"))
        basicSettingInteractor.setProxyInfo(ProxyInfo(address = "rview.com"))
        val proxyInfo = basicSettingInteractor.getProxyInfo()
        MatcherAssert.assertThat("ProxyInfo Address 를 설정했는데 값이 달라서 실패", "rview.com", Matchers.`is`(proxyInfo.address))
    }
}