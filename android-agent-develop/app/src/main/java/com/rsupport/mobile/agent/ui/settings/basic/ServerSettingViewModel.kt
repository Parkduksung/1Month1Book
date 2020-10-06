package com.rsupport.mobile.agent.ui.settings.basic

import android.app.Application
import android.text.TextUtils
import androidx.arch.core.util.Function
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.repo.config.ProxyInfo
import com.rsupport.mobile.agent.repo.config.ServerInfo
import com.rsupport.mobile.agent.ui.base.BaseViewModel

class ServerSettingViewModel(app: Application, private val serverSettingInteractor: ServerSettingInteractor) : BaseViewModel(application = app), LifecycleObserver {

    private val _productType by lazy { MutableLiveData<ProductType>(getProductType()) }
    val productType = _productType.distinctUntilChanged()

    private val _proxyModel by lazy { MutableLiveData<ProxyModel>(getProxyModel()) }
    val proxyModel = _proxyModel.distinctUntilChanged()

    val isOpenProductType = ObservableBoolean(false)

    val isStandard: LiveData<Boolean>
        get() = Transformations.map(productType, Function {
            return@Function it is ProductType.Standard
        })

    val isCustomServer: LiveData<Boolean>
        get() = Transformations.map(productType, Function {
            return@Function (it is ProductType.Customer)
        })

    val customServerURL by lazy {
        ObservableField<String>(serverSettingInteractor.getCustomServerInfo().url)
    }

    val isHttpsServer by lazy {
        val isHttps = customServerURL.get()?.let {
            if (TextUtils.isEmpty(it)) true
            else it.contains("https://")
        } ?: true
        MutableLiveData<Boolean>(isHttps)
    }

    val isUseProxy: LiveData<Boolean>
        get() = Transformations.map(proxyModel, Function {
            return@Function it.isUse
        })


    val proxyAddress by lazy { ObservableField<String>(getProxyModel().url) }
    val proxyPort by lazy { ObservableField<String>(getProxyModel().port) }
    val proxyUser by lazy { ObservableField<String>(getProxyModel().userId) }
    val proxyPassword by lazy { ObservableField<String>(getProxyModel().userPwd) }


    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        updateCustomServerURL(customServerURL.get() ?: "")
    }

    fun selectedHttp() {
        isHttpsServer.value = false
        updateCustomServerURL(customServerURL.get() ?: "")
        _productType.value = ProductType.Customer(serverSettingInteractor.getCustomServerInfo())
    }

    fun selectedHttps() {
        isHttpsServer.value = true
        updateCustomServerURL(customServerURL.get() ?: "")
        _productType.value = ProductType.Customer(serverSettingInteractor.getCustomServerInfo())
    }

    private fun replacePrefixHttpURL(serverURL: String): String {
        return when {
            serverURL.startsWith("https://") -> serverURL.replace("https", "http")
            serverURL.startsWith("http://") -> serverURL
            else -> "http://".plus(serverURL)
        }
    }

    private fun replacePrefixHttpsURL(serverURL: String): String {
        return when {
            serverURL.startsWith("https://") -> serverURL
            serverURL.startsWith("http://") -> serverURL.replace("http", "https")
            else -> "https://".plus(serverURL)
        }
    }

    fun toggleProductTypeExpand() {
        isOpenProductType.set(!isOpenProductType.get())
    }

    fun setStandardServer() {
        isOpenProductType.set(false)
        updateCustomServerURL(customServerURL.get() ?: "")
        serverSettingInteractor.setStandardServer()
        _productType.value = ProductType.Standard
    }

    fun setCustomServer() {
        isOpenProductType.set(false)
        updateCustomServerURL(customServerURL.get() ?: "")
        val serverInfo = serverSettingInteractor.setCustomServer()
        _productType.value = ProductType.Customer(serverInfo)
    }

    private fun updateCustomServerURL(serverURL: String) {
        val fixedServerURL = if (isHttpsServer.value == true) replacePrefixHttpsURL(serverURL) else replacePrefixHttpURL(serverURL)
        customServerURL.set(fixedServerURL)
        serverSettingInteractor.updateCustomServer(fixedServerURL)
    }

    fun getProductType(): ProductType = when (serverSettingInteractor.getProductType()) {
        GlobalStatic.PRODUCT_SERVER -> ProductType.Customer(serverSettingInteractor.getServerInfo())
        else -> ProductType.Standard
    }

    fun getProxyModel(): ProxyModel {
        return serverSettingInteractor.getProxyInfo().let {
            ProxyModel(serverSettingInteractor.useProxy, it.address, it.port, it.id, it.pwd)
        }
    }

    fun toggleProxyUse() {
        setUseProxy(!serverSettingInteractor.useProxy)
    }

    fun onProxyInfoChanged() {
        updateProxyInfo(ProxyInfo(proxyAddress.get() ?: "", proxyPort.get() ?: "", proxyUser.get()
                ?: "", proxyPassword.get() ?: ""))
    }

    fun setUseProxy(isUse: Boolean) {
        serverSettingInteractor.useProxy = isUse
        _proxyModel.value = getProxyModel().copy(isUse = isUse)
    }

    fun setProxyURL(proxyURL: String) {
        val proxyInfo = serverSettingInteractor.getProxyInfo().copy(address = proxyURL)
        updateProxyInfo(proxyInfo)
    }

    fun setProxyPort(proxyPort: String) {
        val proxyInfo = serverSettingInteractor.getProxyInfo().copy(port = proxyPort)
        updateProxyInfo(proxyInfo)
    }

    fun setProxyUserId(userId: String) {
        val proxyInfo = serverSettingInteractor.getProxyInfo().copy(id = userId)
        updateProxyInfo(proxyInfo)
    }

    fun setProxyUserPwd(userPwd: String) {
        val proxyInfo = serverSettingInteractor.getProxyInfo().copy(pwd = userPwd)
        updateProxyInfo(proxyInfo)
    }

    private fun updateProxyInfo(proxyInfo: ProxyInfo) {
        serverSettingInteractor.setProxyInfo(proxyInfo)
        _proxyModel.value = getProxyModel()
    }

    sealed class ProductType {
        object Standard : ProductType()
        data class Customer(val serverInfo: ServerInfo) : ProductType()
    }

    data class ProxyModel(val isUse: Boolean, val url: String = "", val port: String = "", val userId: String = "", val userPwd: String = "")
}