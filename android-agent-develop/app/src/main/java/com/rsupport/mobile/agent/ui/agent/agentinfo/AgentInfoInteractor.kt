package com.rsupport.mobile.agent.ui.agent.agentinfo

import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.iid.FirebaseInstanceId
import com.rsupport.mobile.agent.BuildConfig
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.api.ApiService
import com.rsupport.mobile.agent.api.model.AgentInfo
import com.rsupport.mobile.agent.constant.AgentBasicInfo
import com.rsupport.mobile.agent.constant.ComConstant
import com.rsupport.mobile.agent.constant.Global
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.constant.legacy.AgentLoginResultUpdater
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.extension.toRSExceptionOrNull
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import com.rsupport.mobile.agent.modules.push.RSPushMessaging
import com.rsupport.mobile.agent.repo.agent.AgentRepository
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.status.AgentStatus
import com.rsupport.mobile.agent.utils.AgentLogManager
import com.rsupport.mobile.agent.utils.OpenClass
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.SdkVersion
import com.rsupport.rscommon.exception.RSException
import com.rsupport.util.log.RLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject


/**
 * Agent 정보와 상태
 */
@OpenClass
class AgentInfoInteractor {
    companion object {
        const val OK = 0

        //삭제된 컴퓨터
        const val REMOVED_AGENT = 212

        //등록되지 않은 컴퓨터
        const val UNREGISTER_AGENT = 113

        // 만료된 에이전트
        const val EXPIRED_AGENT = 215

        /**
         * client 정의 코드
         * 강제 업데이트
         */
        const val UPDATE_FORCED = -99999

        /**
         * 새로운 버전 업데이트
         */
        const val UPDATED = -99998
    }


    private val context by inject(Context::class.java)
    private val powerManager by inject(PowerManager::class.java)
    private val sdkVersion by inject(SdkVersion::class.java)
    private val agentRepository by inject(AgentRepository::class.java)
    private val configRepository by inject(ConfigRepository::class.java)
    private val agentStatus by inject(AgentStatus::class.java)
    private val agentLogManager by inject(AgentLogManager::class.java)
    private val engineTypeCheck by inject(EngineTypeCheck::class.java)
    private val apiService by inject(ApiService::class.java)

    /**
     * Koin 에서 Agent 생성시 1번호출된다.
     * 그외 호출하면 안된다.
     */
    fun initialized() {
        agentStatus.addListener(statusChangedListener)
    }

    /**
     * 자원을 해지 할때 호출한다.
     */
    fun release() {
        agentStatus.remoteListener(statusChangedListener)
    }

    /**
     * 베터리 절약 모드 무시 설정이 되어있는지에 대한 property
     * @return livedata value 베터리 절약모드 무시 설정이 안되어있으면 app package name, 그렇지 않으면 null. sdk version 이 23미만일때는 null
     */
    val ignoreBatteryOptimized: LiveData<String> by lazy {
        MutableLiveData<String>().apply {
            value = if (sdkVersion.greaterThan23()) {
                if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                    context.packageName
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    val agentInfo: LiveData<AgentInfo> by lazy {
        agentRepository.getAgentInfo()
    }


    /**
     * 로그인을 시도한다.
     * @return 로그인 성공하면 0, 그렇지 않으면 오류코드
     */
    suspend fun loginProcess(): Int = withContext(Dispatchers.IO) {
        val guid = AgentBasicInfo.getAgentGuid(context)
        GlobalStatic.loadSettingInfo(context)
        agentLogManager.addAgentLog(context, String.format(context.getString(R.string.agent_log_agent_login), configRepository.getServerInfo().url))

        return@withContext when (val agentLoginResult = callLoginApis(guid)) {
            is Result.Success -> loginSuccess(guid)
            is Result.Failure -> loginFailure(agentLoginResult)
        }
    }

    private fun callLoginApis(guid: String): Result<Boolean> {
        return when (val agentLoginResult = agentLogin(guid)) {
            is Result.Success -> updateSessionResult(guid)
            is Result.Failure -> agentLoginResult
        }
    }

    private fun agentLogin(guid: String): Result<Boolean> {
        return when (val loginResult = apiService.agentLogin(guid)) {
            is Result.Success -> {
                AgentLoginResultUpdater().update(loginResult.value)
                Result.success(true)
            }
            is Result.Failure -> Result.failure(loginResult.throwable)
        }
    }

    private fun updateSessionResult(guid: String): Result<Boolean> {
        return when (val sessionResult = apiService.agentSessionResult(guid, "0", AgentBasicInfo.RV_AGENT_PUSHSERVER_ADDRESS, AgentBasicInfo.RV_AGENT_PUSHSERVER_PORT)) {
            is Result.Success -> Result.success(true)
            is Result.Failure -> Result.failure(sessionResult.throwable)
        }
    }

    private fun loginFailure(agentLoginResult: Result.Failure<Boolean>): Int {
        val errorCode = agentLoginResult.throwable.toRSExceptionOrNull()?.errorCode
                ?: ErrorCode.UNKNOWN_ERROR

        agentLogManager.addAgentLog(context, String.format(context.getString(R.string.agent_log_agent_login_faile), "false", errorCode.toString()))

        // TODO EXPIRED_AGENT 의 경우 처리 추가했으나 확인해야한다.
        if (errorCode == REMOVED_AGENT || errorCode == UNREGISTER_AGENT || errorCode == EXPIRED_AGENT) {
            configRepository.delete()
            RSPushMessaging.getInstance().clear()
            if (Global.getInstance().agentThread != null) {
                Global.getInstance().agentThread.releaseAll()
                Global.getInstance().agentThread = null
            }
        }

        // 강제 업데이트
        return if (errorCode == ComConstant.WEB_ERR_APP_VERSION.toInt()) {
            UPDATE_FORCED
        } else {
            errorCode
        }
    }

    private suspend fun loginSuccess(guid: String): Int {
        agentStatus.setLoggedIn()
        updateFcmToken(guid)
        updateAgentInfo()

        if (!TextUtils.isEmpty(AgentBasicInfo.RV_AGENT_PUSHSERVER_PORT)) {
            RSPushMessaging.getInstance().apply {
                try {
                    setServerInfo(AgentBasicInfo.RV_AGENT_PUSHSERVER_ADDRESS, AgentBasicInfo.RV_AGENT_PUSHSERVER_PORT.toInt())
                    register(BuildConfig.VERSION_NAME)
                    register("$guid/agent")
                } catch (e: Exception) {
                    RLog.e(e)
                }
            }
        }
        return if (AgentBasicInfo.RV_AGENT_NEWVERSION == "1") {
            UPDATED
        } else {
            OK
        }
    }

    private fun updateFcmToken(guid: String) {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            Thread { apiService.registerFcmId(guid, it.token) }.start()
        }
    }

    /**
     * 로그아웃 한다.
     */
    suspend fun logoutProcess(): Int = withContext(Dispatchers.IO) {
        val guid = AgentBasicInfo.getAgentGuid(context)

        return@withContext when (val logoutResult = apiService.agentLogout(guid)) {
            is Result.Success -> {
                val messaging = RSPushMessaging.getInstance()
                messaging.unregister(BuildConfig.VERSION_NAME)
                messaging.unregister("$guid/agent")

                Global.getInstance().agentThread?.releaseAll()
                Global.getInstance().agentThread = null
                agentStatus.setLogOut()
                RSPushMessaging.getInstance().clear();
                updateAgentInfo()
                OK
            }
            is Result.Failure -> {
                if (logoutResult.throwable is RSException) {
                    logoutResult.throwable.errorCode
                } else {
                    ErrorCode.UNKNOWN_ERROR
                }
            }
        }
    }

    /**
     * agent 정보를 update 한다.
     */
    suspend fun updateAgentInfo() = withContext(Dispatchers.IO) {
        agentRepository.insert(createAgentInfo())
    }

    /**
     * 로그인 상태를 확인한다.
     */
    fun isLoggedIn(): Boolean {
        return createAgentInfo().let {
            it.status == AgentStatus.AGENT_STATUS_LOGIN.toInt() || it.status == AgentStatus.AGENT_STATUS_REMOTING.toInt()
        }
    }

    /**
     * 원격 제어중
     */
    fun isRemoting(): Boolean {
        return createAgentInfo().let {
            return@let it.status == AgentStatus.AGENT_STATUS_REMOTING.toInt()
        }
    }

    private fun createAgentInfo(): AgentInfo {
        return AgentInfo().apply {
            guid = AgentBasicInfo.getAgentGuid(context)
            name = AgentBasicInfo.getAgentName(context)
            status = agentStatus.get().toInt()
            macaddr = GlobalStatic.getMacAddress(context)
            localip = GlobalStatic.getLocalIP()
            devicetype = "3"
            extend = ComConstant.RVFLAG_KEY_ANDROID.toString()
            osname = "Android" + Build.VERSION.RELEASE
            GlobalStatic.g_agentInfo = this
            bizId = AgentBasicInfo.getAgentBizID(context)
        }
    }

    /**
     * Agent 가 install 되어 있는지를 확인한다.
     * @return install 되어있으면 true, 그렇지 않으면 false
     */
    fun isAgentInstalled(): Boolean {
        return !TextUtils.isEmpty(AgentBasicInfo.getAgentGuid(context))
    }

    suspend fun removeAgent() = withContext(Dispatchers.IO) {
        configRepository.delete()
        agentRepository.clearAll()
        Global.getInstance().agentThread?.releaseAll()
        Global.getInstance().agentThread = null
        updateAgentInfo()
    }

    fun isFirstLaunch(): Boolean {
        return configRepository.isFirstLaunch()
    }

    fun launched() {
        configRepository.setFirstLaunch(false)
    }

    suspend fun isEngineActivated(): Boolean = withContext(Dispatchers.IO) {
        engineTypeCheck.isActivated()
    }

    private val statusChangedListener = object : AgentStatus.OnStatusChangedListener {
        override fun onChanged(status: Int) {
            CoroutineScope(Dispatchers.Main).launch {
                updateAgentInfo()
            }
        }
    }
}