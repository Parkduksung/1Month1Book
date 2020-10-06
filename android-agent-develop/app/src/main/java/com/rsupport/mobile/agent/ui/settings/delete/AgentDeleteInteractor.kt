package com.rsupport.mobile.agent.ui.settings.delete

import androidx.lifecycle.asFlow
import com.rsupport.mobile.agent.constant.ComConstant
import com.rsupport.mobile.agent.api.ApiService
import com.rsupport.mobile.agent.api.model.AgentDeleteResult
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.repo.agent.AgentRepository
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.utils.OpenClass
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.rscommon.exception.RSException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject
import com.rsupport.mobile.agent.constant.Global
import com.rsupport.mobile.agent.modules.push.RSPushMessaging

@OpenClass
class AgentDeleteInteractor {

    private val apiService by inject(ApiService::class.java)
    private val agentRepository by inject(AgentRepository::class.java)
    private val configRepository by inject(ConfigRepository::class.java)

    suspend fun deleteAgent(userId: String, userPwd: String): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext agentRepository.getAgentInfo().asFlow().map {
            if (it.guid.isNullOrEmpty()) null
            else Pair(it.guid, it.bizId)
        }.first()?.let {
            when (val agentDeleteResult = apiService.deleteAgent(it.first, userId, userPwd, it.second)) {
                is Result.Success -> {
                    removeAgent()
                    configRepository.setShowTutorial(true)
                    Result.success(true)
                }
                is Result.Failure -> convertErrorCode(agentDeleteResult)
            }
        } ?: run {
            Result.failure<Boolean>(RSException(ErrorCode.SETTING_ALREADY_DELETE_AGENT))
        }
    }

    private fun removeAgent() {
        configRepository.delete()
        agentRepository.clearAll()
        Global.getInstance().agentThread?.releaseAll()
        Global.getInstance().agentThread = null
        RSPushMessaging.getInstance().clear()
    }

    private fun convertErrorCode(result: Result.Failure<AgentDeleteResult>): Result<Boolean> {
        return when (val rs = result.throwable) {
            is RSException -> {
                when (rs.errorCode) {
                    ComConstant.WEB_ERR_INVALID_PARAMETER.toInt(),
                    ComConstant.WEB_ERR_NOT_FOUND_USERID.toInt(),
                    ComConstant.WEB_ERR_INVALID_USER_ACCOUNT.toInt(),
                    ComConstant.WEB_ERR_AES_INVALID_USER_ACCOUNT.toInt() -> Result.failure(RSException(ErrorCode.SETTING_INVALID_ACCOUNT_OR_PWD))
                    ComConstant.WEB_ERR_ALREADY_DELETE_AGENTID.toInt() -> {
                        Result.failure(RSException(ErrorCode.SETTING_ALREADY_DELETE_AGENT))
                    }
                    ComConstant.NET_ERR_PROXYINFO_NULL.toInt(), ComConstant.NET_ERR_PROXY_VERIFY.toInt() -> Result.failure(RSException(ErrorCode.SETTING_NET_ERR_PROXY_VERIFY))
                    else -> Result.failure(rs)
                }
            }
            else -> {
                Result.failure<Boolean>(RSException(ErrorCode.UNKNOWN_ERROR))
            }
        }
    }

    fun release() {

    }
}