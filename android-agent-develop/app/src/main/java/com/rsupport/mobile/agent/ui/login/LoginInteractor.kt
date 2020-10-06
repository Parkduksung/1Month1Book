package com.rsupport.mobile.agent.ui.login

import android.content.Context
import android.text.TextUtils
import com.rsupport.android.engine.install.EngineContextFactory
import com.rsupport.android.engine.install.IEngineContext
import com.rsupport.android.engine.install.exception.InstallException
import com.rsupport.android.engine.install.gson.dto.EngineGSon
import com.rsupport.mobile.agent.api.ApiService
import com.rsupport.mobile.agent.api.ApiService.Companion.KNOX_BLACK_LIST_MUST_UPDATE_ERROR
import com.rsupport.mobile.agent.api.ApiService.Companion.KNOX_BLACK_LIST_NOT_SUPPORT_ERROR
import com.rsupport.mobile.agent.api.ApiService.Companion.KNOX_BLACK_LIST_PARAM_ERROR
import com.rsupport.mobile.agent.api.model.ConsoleLoginResult
import com.rsupport.mobile.agent.api.model.GroupInfo
import com.rsupport.mobile.agent.constant.ComConstant
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.modules.engine.EngineType
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.utils.OpenClass
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.SdkVersion
import com.rsupport.mobile.agent.utils.Utility
import com.rsupport.rscommon.exception.RSException
import config.EngineConfigSetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject

@OpenClass
class LoginInteractor {
    private val context: Context by inject(Context::class.java)
    private val configRepository: ConfigRepository by inject(ConfigRepository::class.java)
    private val apiService: ApiService by inject(ApiService::class.java)
    private val sdkVersion by inject(SdkVersion::class.java)
    private val engineContext by inject(IEngineContext::class.java)
    private val engineTypeCheck by inject(EngineTypeCheck::class.java)

    /**
     * Tutorial 을 봤는지를 확인한다.
     * @return 튜토리얼을 봤으면 true, 그렇지 않으면 false
     */
    fun isShowTutorial(): Boolean {
        return configRepository.isShowTutorial()
    }

    /**
     * api 서버에 로그인을 시도한다.
     * Agent 정보등 원격접속에 필요한 데이터를 받아온다.
     * @see ConsoleLoginState
     * @return 성공하면 [Result.Success] 그렇지 않으면 [Result.Failure]
     */
    suspend fun consoleLogin(corpId: String?, userId: String, userPwd: String): Result<ConsoleLoginState> = withContext(Dispatchers.IO) {
        return@withContext consoleLogin(corpId, userId, userPwd, true)
    }

    private fun updateProductType() {
        if (configRepository.getProductType() != GlobalStatic.PRODUCT_SERVER) {
            if (TextUtils.isEmpty(GlobalStatic.g_loginInfo.corpid)) {
                configRepository.setProductType(GlobalStatic.PRODUCT_PERSONAL)
            }
        }
    }

    private fun consoleLogin(corpId: String?, userId: String, userPwd: String, isSecure: Boolean): Result<ConsoleLoginState> {
        updateProductType()
        when (val consoleLoginResult = apiService.consoleLogin(corpId, userId, userPwd, isSecure)) {
            is Result.Success -> {
                GlobalStatic.g_loginInfo.userid = userId
                GlobalStatic.g_loginInfo.userpasswd = userPwd
                configRepository.updateLoginGroupId(corpId ?: "")
                configRepository.setNewNoticeSeq(consoleLoginResult.value.newNoticeSeq?.toIntOrNull()
                        ?: 0)

                val loginProcResult = keepLoginProc(corpId ?: "")
                if (loginProcResult is Result.Failure) {
                    return Result.failure<ConsoleLoginState>(loginProcResult.throwable)
                }
                return Result.success(ConsoleLoginState.from(consoleLoginResult.value))
            }
            is Result.Failure -> {
                if (consoleLoginResult.throwable is RSException && isSecure) {
                    if (consoleLoginResult.throwable.errorCode == ComConstant.WEB_ERR_INVALID_PARAMETER.toInt()) {
                        return consoleLogin(corpId, userId, userPwd, false)
                    }
                }

                val errorCode = when (val exception = consoleLoginResult.throwable) {
                    is RSException -> {
                        when (exception.errorCode) {
                            ComConstant.NET_ERR_PROXYINFO_NULL.toInt(), ComConstant.NET_ERR_PROXY_VERIFY.toInt() -> ErrorCode.LOGIN_NET_ERR_PROXY_VERIFY
                            ComConstant.WEB_ERR_PASSWORD_EXPIRED.toInt() -> ErrorCode.LOGIN_EXPIRED_PWD
                            ComConstant.WEB_ERR_AES_NOT_FOUND_USERID.toInt() -> ErrorCode.LOGIN_INVALID_ID_AES
                            ComConstant.WEB_ERR_AES_INVALID_USER_ACCOUNT.toInt(), ComConstant.WEB_ERR_INVALID_USER_ACCOUNT.toInt() -> ErrorCode.LOGIN_INVALID_USER_ACCOUNT_OR_PWD
                            else -> exception.errorCode
                        }
                    }
                    else -> ErrorCode.UNKNOWN_ERROR
                }
                return Result.failure<ConsoleLoginState>(RSException(errorCode))
            }
        }
    }


    private fun keepLoginProc(corpId: String): Result<List<GroupInfo>> {
        return apiService.getAgentGroupList("1", corpId)
    }

    /**
     * 사용 가능한 Engine 을 찾는다.
     * @see SupportEngine
     * @return 사용 가능한 Engine 종류를 반환한다 [Result.Success]. 그렇지 않으면 [Result.Failure]
     */
    suspend fun isSupportEngine(): Result<SupportEngine> = withContext(Dispatchers.IO) {
        engineTypeCheck.checkEngineType()
        return@withContext when (val engineType = engineTypeCheck.getEngineType()) {
            EngineType.ENGINE_TYPE_KNOX -> {
                when (val result = apiService.reqeustCheckSupportKnox()) {
                    is Result.Success -> Result.success(SupportEngine.from(EngineType.ENGINE_TYPE_KNOX))
                    is Result.Failure -> {
                        if (result.throwable is RSException) {
                            if (result.throwable.errorCode == KNOX_BLACK_LIST_PARAM_ERROR) {
                                Result.failure(RSException(ErrorCode.ENGINE_KNOX_BLACK_LIST_WEB_111))
                            } else if (result.throwable.errorCode == KNOX_BLACK_LIST_MUST_UPDATE_ERROR) {
                                Result.failure(RSException(ErrorCode.ENGINE_KNOX_BLACK_LIST_WEB_854))
                            } else if (result.throwable.errorCode == KNOX_BLACK_LIST_NOT_SUPPORT_ERROR) {
                                Result.failure(RSException(ErrorCode.ENGINE_KNOX_BLACK_LIST_WEB_855))
                            }
                            // 단순 web 호출 오류는 무시한다.
                            else {
                                Result.success(SupportEngine.from(EngineType.ENGINE_TYPE_KNOX))
                            }
                        }
                        // 단순 web 호출 오류는 무시한다.
                        else {
                            Result.success(SupportEngine.from(EngineType.ENGINE_TYPE_KNOX))
                        }
                    }
                }
            }
            EngineType.ENGINE_TYPE_RSPERM -> {
                if (isInstalledRspermEngine()) {
                    Result.success(SupportEngine.from(EngineType.ENGINE_TYPE_RSPERM))
                } else {
                    if (findInstallableEngine().isEmpty() && sdkVersion.greaterThan21()) {
                        Result.success(SupportEngine.from(EngineType.ENGINE_TYPE_RSPERM))
                    } else {
                        Result.failure(RSException(ErrorCode.ENGINE_NOT_SUPPORTED))
                    }
                }
            }
            else -> Result.success(SupportEngine.from(engineType))
        }
    }

    private fun isInstalledRspermEngine(): Boolean {
        try {
            val gson = engineContext.requestFindRsperm()
            if (gson.installFiles.isEmpty() && !EngineConfigSetting.isSoftEncoding) {
                return Utility.isExistRsperm(context)
            }
        } catch (e: InstallException) {
            return false
        }
        return true
    }

    /**
     * 설치 가능한 Rsperm package name 을 찾는다.
     * @return 설치가능한 rsperm 이 있으면 pkg name, 그렇지 않으면 ""
     */
    suspend fun findInstallableEngine(): String = withContext(Dispatchers.IO) {
        val engineGson = engineContext.requestInstallableRsperm()
        if (engineGson.returnCode == EngineGSon.OK && engineGson.installFiles.isNotEmpty()) {
            return@withContext engineGson.installFiles[0].packageName ?: ""
        }
        return@withContext ""
    }

    /**
     * 개인용/기업용/서버납품용 설정상태에 따른 서버 설정이 정상인지를 확인한다.
     * @return 서버설정이 정상작으로 되어있으면 true, 그렇지 않으면 false
     */
    fun checkServerURL(): Boolean {
        return !TextUtils.isEmpty(configRepository.getServerInfo().url)
    }

    fun release() {
        EngineContextFactory.release(engineContext)
    }
}


sealed class SupportEngine {
    object KnoxEngine : SupportEngine()
    object SonyEngine : SupportEngine()
    object RspermEngine : SupportEngine()

    companion object {
        fun from(engineType: Int) = when (engineType) {
            EngineType.ENGINE_TYPE_KNOX -> KnoxEngine
            EngineType.ENGINE_TYPE_SONY -> SonyEngine
            else -> RspermEngine
        }
    }
}


sealed class ConsoleLoginState {
    /**
     * 로그인이 정상적으로 되었을때의 상태
     */
    object SuccessState : ConsoleLoginState()

    /**
     * 비밀번호 변경 날짜가있어서 비밀번호 변경이 필요한 상태
     */
    data class PasswordExpireDay(val expireDay: String, val serverURL: String) : ConsoleLoginState()

    /**
     * 업데이트 가능 버전이 있는 상태
     */
    object UpdateAvailable : ConsoleLoginState()

    companion object {
        private const val NEW_VERSION = "1"

        /**
         * ConsoleLoginState factory
         */
        fun from(consoleLoginResult: ConsoleLoginResult): ConsoleLoginState {
            return if (!TextUtils.isEmpty(consoleLoginResult.passwordLimitDays)) PasswordExpireDay(consoleLoginResult.passwordLimitDays!!, consoleLoginResult.webServer)
            else if (consoleLoginResult.newVersion == NEW_VERSION) UpdateAvailable
            else SuccessState
        }
    }

}