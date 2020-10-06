package com.rsupport.mobile.agent.ui.settings.basic

import com.rsupport.mobile.agent.constant.Global
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.repo.config.ProxyInfo
import com.rsupport.mobile.agent.repo.config.ServerInfo
import com.rsupport.mobile.agent.utils.OpenClass
import org.koin.java.KoinJavaComponent.inject

@OpenClass
class ServerSettingInteractor {

    private val configRepository by inject(ConfigRepository::class.java)

    var useProxy: Boolean
        get() {
            return configRepository.isProxyUse()
        }
        set(value) {
            configRepository.setUseProxy(value)
        }

    fun setStandardServer(): ServerInfo {
        configRepository.setProductType(GlobalStatic.PRODUCT_CORP)
        Global.getInstance().webConnection.setNetworkInfo()
        return configRepository.getServerInfo()
    }

    fun setCustomServer(): ServerInfo {
        configRepository.setProductType(GlobalStatic.PRODUCT_SERVER)
        Global.getInstance().webConnection.setNetworkInfo()
        return configRepository.getServerInfo()
    }

    fun updateCustomServer(customServerURL: String) {
        configRepository.setCustomServerURL(customServerURL)
        Global.getInstance().webConnection.setNetworkInfo()
    }

    fun getServerInfo(): ServerInfo {
        return configRepository.getServerInfo()
    }

    fun getProductType(): Int {
        return configRepository.getProductType()
    }

    fun getProxyInfo(): ProxyInfo {
        return configRepository.getProxyInfo()
    }

    fun setProxyInfo(proxyInfo: ProxyInfo) {
        configRepository.setProxyInfo(proxyInfo)
    }

    fun getCustomServerInfo(): ServerInfo {
        return configRepository.getCustomServerURL()
    }
}