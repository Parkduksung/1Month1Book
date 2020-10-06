package com.rsupport.mobile.agent.utils

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.rsupport.mobile.agent.BuildConfig
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.rscommon.errorlog.RSErrorLog
import org.koin.java.KoinJavaComponent.inject
import com.rsupport.mobile.agent.constant.AgentBasicInfo
import com.rsupport.mobile.agent.constant.Global
import com.rsupport.mobile.agent.constant.GlobalStatic
import java.lang.reflect.Modifier


interface Collector<T> {

    companion object {

        @JvmStatic
        fun <T> push(data: T): Boolean {
            return push(data, DefaultCollectorFactory())
        }

        @JvmStatic
        fun <T> push(data: T, factory: CollectorFactory = DefaultCollectorFactory()): Boolean {
            return factory.create(data)?.collect(data) ?: false
        }
    }

    fun collect(data: T): Boolean
}

interface CollectorFactory {
    fun <T> create(data: T): Collector<T>?
}


class DefaultCollectorFactory : CollectorFactory {
    override fun <T> create(data: T): Collector<T>? {
        return when (data) {
            is ErrorData -> ErrorLogCollector() as Collector<T>
            else -> null
        }
    }
}

class ErrorLogCollector : Collector<ErrorData> {
    override fun collect(data: ErrorData): Boolean {
        if (!BuildConfig.DEBUG) {
            RSErrorLog.report(data.code, data.getDisplayedMessage(), data.detail)
        }
        return true
    }
}

data class ErrorData(val code: String, val message: String, val detail: String = ErrorDataDetail(Global.getInstance().appContext).toString()) {
    constructor(codeInt: Int, message: String) : this(codeInt.toString(), message)

    fun getDisplayedMessage(): String {
        return "$message\n[Error code: $code]"
    }
}

class ErrorDataDetail(context: Context) {

    private val configRepository by inject(ConfigRepository::class.java)

    @SerializedName("sessionId")
    var sessionId: String? = AgentBasicInfo.getAgentGuid(context)

    @SerializedName("agentId")
    var agentId: String? = AgentBasicInfo.RV_AGENT_ID

    @SerializedName("userId")
    var userId: String? = ""

    @SerializedName("mobileNo")
    var mobileNo: String? = ""

    @SerializedName("webServerIP")
    var webServerIP: String? = configRepository.getCustomServerURL().url

    @SerializedName("updateServerIP")
    var updateServerIP: String? = GlobalStatic.connectionInfo.agentupdateurl

    @SerializedName("sessionServerIP")
    var sessionServerIP: String? = ""

    @SerializedName("gatewayServerIP")
    var gatewayServerIP: String? = ""

    @SerializedName("proxyServerIP")
    var proxyServerIP: String? = configRepository.getProxyInfo().address

    @SerializedName("proxyServerPort")
    var proxyServerPort: String? = configRepository.getProxyInfo().port

    @SerializedName("proxyServerUserId")
    var proxyServerUserId: String? = configRepository.getProxyInfo().id

    @SerializedName("siteName")
    var siteName: String? = "ASP"

    override fun toString(): String {
        val gson = GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.PRIVATE)
                .create()
        return gson.toJson(this)
    }
}