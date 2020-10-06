package com.rsupport.mobile.agent.api

import android.content.Context
import com.rsupport.mobile.agent.api.model.*
import com.rsupport.mobile.agent.constant.legacy.ConsoleLoginResultUpdater
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.repo.device.DeviceRepository
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.rscommon.exception.RSException


class WebConnectionApiService(
        private val context: Context,
        private val webConnection: WebConnection,
        private val deviceRepository: DeviceRepository,
        private val configRepository: ConfigRepository
) : ApiService {
    override fun consoleLogin(corpID: String?, userID: String, userPwd: String, isSecure: Boolean): Result<ConsoleLoginResult> {
        webConnection.setAESEnable(isSecure)
        val connInfo = GlobalStatic.connectionInfo
        connInfo.clear()

        return when (val loginResult = webConnection.consoleLogin(userID, userPwd, corpID
                ?: "", deviceRepository.macAddress, deviceRepository.localIP)) {
            is Result.Success -> {
                // ApiVersion이 다르면 ApiVersion 을 Prefix 로 추가하여 다시 호출한다.
                if (webConnection.apiVersion != loginResult.value.apiVersion) {
                    webConnection.apiVersion = loginResult.value.apiVersion
                    consoleLogin(userID, userPwd, corpID ?: "", isSecure)
                } else {
                    ConsoleLoginResultUpdater().update(loginResult.value)
                    loginResult
                }
            }
            is Result.Failure -> {
                if (loginResult.throwable is RSException) {
                    GlobalStatic.g_errNumber = loginResult.throwable.errorCode
                    GlobalStatic.g_err = loginResult.throwable.userMessage
                    loginResult
                } else {
                    GlobalStatic.g_errNumber = ErrorCode.UNKNOWN_ERROR
                    Result.failure(RSException(ErrorCode.UNKNOWN_ERROR, loginResult.throwable.message
                            ?: ""))
                }
            }
        }
    }

    override fun reqeustCheckSupportKnox(): Result<CheckSupportKnoxResult> {
        return when (val checkSupportKnoxResult = webConnection.reqeustCheckSupportKnox()) {
            is Result.Success -> checkSupportKnoxResult
            is Result.Failure -> {
                if (checkSupportKnoxResult.throwable is RSException) {
                    GlobalStatic.g_errNumber = checkSupportKnoxResult.throwable.errorCode
                    GlobalStatic.g_err = checkSupportKnoxResult.throwable.userMessage
                    checkSupportKnoxResult
                } else {
                    GlobalStatic.g_errNumber = ErrorCode.UNKNOWN_ERROR
                    Result.failure(RSException(ErrorCode.UNKNOWN_ERROR, checkSupportKnoxResult.throwable.message
                            ?: ""))
                }
            }
        }
    }

    override fun getAgentGroupList(lType: String, bizID: String): Result<List<GroupInfo>> {
        GlobalStatic.g_vecGroups.clear()
        return when (val agentGroupListResult = webConnection.getAgentGroupList(lType, bizID)) {
            is Result.Success -> {
                GlobalStatic.g_vecGroups.addAll(agentGroupListResult.value)
                agentGroupListResult
            }
            is Result.Failure -> {
                if (agentGroupListResult.throwable is RSException) {
                    GlobalStatic.g_errNumber = agentGroupListResult.throwable.errorCode
                    GlobalStatic.g_err = agentGroupListResult.throwable.userMessage
                    agentGroupListResult
                } else {
                    GlobalStatic.g_errNumber = ErrorCode.UNKNOWN_ERROR
                    Result.failure(RSException(ErrorCode.UNKNOWN_ERROR, agentGroupListResult.throwable.message
                            ?: ""))
                }
            }
        }
    }

    override fun getAgentSubGroupList(lType: String, bizID: String): Result<List<GroupInfo>> {
        return when (val agentSubGroupListResult = webConnection.getAgentGroupSubList(lType, bizID)) {
            is Result.Success -> agentSubGroupListResult
            is Result.Failure -> {
                if (agentSubGroupListResult.throwable is RSException) {
                    GlobalStatic.g_errNumber = agentSubGroupListResult.throwable.errorCode
                    GlobalStatic.g_err = agentSubGroupListResult.throwable.userMessage
                    agentSubGroupListResult
                } else {
                    GlobalStatic.g_errNumber = ErrorCode.UNKNOWN_ERROR
                    Result.failure(RSException(ErrorCode.UNKNOWN_ERROR, agentSubGroupListResult.throwable.message
                            ?: ""))
                }
            }
        }
    }

    override fun getGroupSearch(lType: String, cType: String, searchText: String, parentGroupId: String): Result<List<GroupInfo>> {
        return when (val groupSearchResult = webConnection.getGroupSearch(lType, cType, searchText, parentGroupId)) {
            is Result.Success -> groupSearchResult
            is Result.Failure -> {
                if (groupSearchResult.throwable is RSException) {
                    GlobalStatic.g_errNumber = groupSearchResult.throwable.errorCode
                    GlobalStatic.g_err = groupSearchResult.throwable.userMessage
                    groupSearchResult
                } else {
                    GlobalStatic.g_errNumber = ErrorCode.UNKNOWN_ERROR
                    Result.failure(RSException(ErrorCode.UNKNOWN_ERROR, groupSearchResult.throwable.message
                            ?: ""))
                }
            }
        }
    }

    override fun deleteAgent(guid: String, userId: String, userPwd: String, bizId: String): Result<AgentDeleteResult> {
        return when (val agentDeleteResult = webConnection.requestAgentDelete(guid, userId, userPwd, bizId)) {
            is Result.Success -> agentDeleteResult
            is Result.Failure -> {
                if (agentDeleteResult.throwable is RSException) {
                    GlobalStatic.g_errNumber = agentDeleteResult.throwable.errorCode
                    GlobalStatic.g_err = agentDeleteResult.throwable.userMessage
                    agentDeleteResult
                } else {
                    GlobalStatic.g_errNumber = ErrorCode.UNKNOWN_ERROR
                    Result.failure(RSException(ErrorCode.UNKNOWN_ERROR, agentDeleteResult.throwable.message
                            ?: ""))
                }
            }
        }

    }

    override fun agentLogin(guid: String): Result<AgentLoginResult> {
        return when (val agnetLoginResult = webConnection.agentLogin(guid)) {
            is Result.Success -> agnetLoginResult
            is Result.Failure -> {
                if (agnetLoginResult.throwable is RSException) {
                    GlobalStatic.g_errNumber = agnetLoginResult.throwable.errorCode
                    GlobalStatic.g_err = agnetLoginResult.throwable.userMessage
                    agnetLoginResult
                } else {
                    GlobalStatic.g_errNumber = ErrorCode.UNKNOWN_ERROR
                    Result.failure(RSException(ErrorCode.UNKNOWN_ERROR, agnetLoginResult.throwable.message
                            ?: ""))
                }
            }
        }
    }

    override fun agentLogout(guid: String): Result<AgentLogoutResult> {
        return when (val agnetLogoutResult = webConnection.agentLogout(guid)) {
            is Result.Success -> agnetLogoutResult
            is Result.Failure -> {
                if (agnetLogoutResult.throwable is RSException) {
                    GlobalStatic.g_errNumber = agnetLogoutResult.throwable.errorCode
                    GlobalStatic.g_err = agnetLogoutResult.throwable.userMessage
                    agnetLogoutResult
                } else {
                    GlobalStatic.g_errNumber = ErrorCode.UNKNOWN_ERROR
                    Result.failure(RSException(ErrorCode.UNKNOWN_ERROR, agnetLogoutResult.throwable.message
                            ?: ""))
                }
            }
        }
    }

    override fun notifyConnected(guid: String, loginKey: String, engineType: EncoderType): Result<NotifyConnectedResult> {
        return when (val notifyConnectedResult = webConnection.notifyConnected(guid, loginKey, engineType.type)) {
            is Result.Success -> notifyConnectedResult
            is Result.Failure -> {
                if (notifyConnectedResult.throwable is RSException) {
                    GlobalStatic.g_errNumber = notifyConnectedResult.throwable.errorCode
                    GlobalStatic.g_err = notifyConnectedResult.throwable.userMessage
                    notifyConnectedResult
                } else {
                    GlobalStatic.g_errNumber = ErrorCode.UNKNOWN_ERROR
                    Result.failure(RSException(ErrorCode.UNKNOWN_ERROR, notifyConnectedResult.throwable.message
                            ?: ""))
                }
            }
        }
    }

    override fun notifyDisconnected(guid: String, logKey: String): Result<NotifyDisconnectedResult> {
        return when (val notifyDisconnectResult = webConnection.notifyDisconnected(guid, logKey)) {
            is Result.Success -> notifyDisconnectResult
            is Result.Failure -> {
                if (notifyDisconnectResult.throwable is RSException) {
                    GlobalStatic.g_errNumber = notifyDisconnectResult.throwable.errorCode
                    GlobalStatic.g_err = notifyDisconnectResult.throwable.userMessage
                    notifyDisconnectResult
                } else {
                    GlobalStatic.g_errNumber = ErrorCode.UNKNOWN_ERROR
                    Result.failure(RSException(ErrorCode.UNKNOWN_ERROR, notifyDisconnectResult.throwable.message
                            ?: ""))
                }
            }
        }
    }

    override fun registerFcmId(guid: String, registerID: String): Result<FcmRegisterResult> {
        return when (val registerResult = webConnection.sendFcmResistID(guid, registerID)) {
            is Result.Success -> registerResult
            is Result.Failure -> {
                if (registerResult.throwable is RSException) {
                    GlobalStatic.g_errNumber = registerResult.throwable.errorCode
                    GlobalStatic.g_err = registerResult.throwable.userMessage
                    registerResult
                } else {
                    GlobalStatic.g_errNumber = ErrorCode.UNKNOWN_ERROR
                    Result.failure(RSException(ErrorCode.UNKNOWN_ERROR, registerResult.throwable.message
                            ?: ""))
                }
            }
        }
    }


    override fun agentInstall(guid: String, id: String, pass: String, agentName: String, agentBizID: String?): Result<AgentInstallResult> {
        return when (val agentInstallResult = webConnection.agentInstall(guid, id, pass, agentName, agentBizID)) {
            is Result.Success -> agentInstallResult
            is Result.Failure -> {
                if (agentInstallResult.throwable is RSException) {
                    GlobalStatic.g_errNumber = agentInstallResult.throwable.errorCode
                    GlobalStatic.g_err = agentInstallResult.throwable.userMessage
                    agentInstallResult
                } else {
                    GlobalStatic.g_errNumber = ErrorCode.UNKNOWN_ERROR
                    Result.failure(RSException(ErrorCode.UNKNOWN_ERROR, agentInstallResult.throwable.message
                            ?: ""))
                }
            }
        }
    }

    override fun agentConnectAgreeResult(guid: String, logKey: String, result: String, url: String): Result<ConnectAgreeResult> {
        return when (val connectAgreeResult = webConnection.agentConnectAgreeResult(guid, logKey, result, url)) {
            is Result.Success -> connectAgreeResult
            is Result.Failure -> {
                if (connectAgreeResult.throwable is RSException) {
                    GlobalStatic.g_errNumber = connectAgreeResult.throwable.errorCode
                    GlobalStatic.g_err = connectAgreeResult.throwable.userMessage
                    connectAgreeResult
                } else {
                    GlobalStatic.g_errNumber = ErrorCode.UNKNOWN_ERROR
                    Result.failure(RSException(ErrorCode.UNKNOWN_ERROR, connectAgreeResult.throwable.message
                            ?: ""))
                }
            }
        }
    }

    override fun checkAccessIDValidate(accessId: String, accessPass: String, bizId: String): Result<CheckAccessIDValidateResult> {
        return when (val checkAccessIDValidateResult = webConnection.checkAccessIDValidate(accessId, accessPass, bizId)) {
            is Result.Success -> checkAccessIDValidateResult
            is Result.Failure -> {
                if (checkAccessIDValidateResult.throwable is RSException) {
                    GlobalStatic.g_errNumber = checkAccessIDValidateResult.throwable.errorCode
                    GlobalStatic.g_err = checkAccessIDValidateResult.throwable.userMessage
                    checkAccessIDValidateResult
                } else {
                    GlobalStatic.g_errNumber = ErrorCode.UNKNOWN_ERROR
                    Result.failure(RSException(ErrorCode.UNKNOWN_ERROR, checkAccessIDValidateResult.throwable.message
                            ?: ""))
                }
            }
        }
    }

    override fun agentSessionResult(guid: String, result: String, sessionIP: String, sessionPort: String): Result<AgentSessionResult> {
        return when (val agentSessionResult = webConnection.agentSessionResult(guid, result, sessionIP, sessionPort)) {
            is Result.Success -> agentSessionResult
            is Result.Failure -> {
                if (agentSessionResult.throwable is RSException) {
                    GlobalStatic.g_errNumber = agentSessionResult.throwable.errorCode
                    GlobalStatic.g_err = agentSessionResult.throwable.userMessage
                    agentSessionResult
                } else {
                    GlobalStatic.g_errNumber = ErrorCode.UNKNOWN_ERROR
                    Result.failure(RSException(ErrorCode.UNKNOWN_ERROR, agentSessionResult.throwable.message
                            ?: ""))
                }
            }
        }
    }

    override fun requestKnoxEnterpriseKey(appVersion: String): Result<KnoxKeyResult> {
        return when (val knoxKeyResult = webConnection.requestKnoxEnterpriseKey(appVersion)) {
            is Result.Success -> knoxKeyResult
            is Result.Failure -> {
                if (knoxKeyResult.throwable is RSException) {
                    GlobalStatic.g_errNumber = knoxKeyResult.throwable.errorCode
                    GlobalStatic.g_err = knoxKeyResult.throwable.userMessage
                    knoxKeyResult
                } else {
                    GlobalStatic.g_errNumber = ErrorCode.UNKNOWN_ERROR
                    Result.failure(RSException(ErrorCode.UNKNOWN_ERROR, knoxKeyResult.throwable.message
                            ?: ""))
                }
            }
        }
    }

    override fun deviceNameChange(guid: String, newDeviceName: String, ssl: String, localIp: String, macAddress: String): Result<ChangeDeviceNameResult> {
        return when (val changeDeviceNameResult = webConnection.deviceNameChange(guid, newDeviceName, ssl, localIp, macAddress)) {
            is Result.Success -> changeDeviceNameResult
            is Result.Failure -> {
                if (changeDeviceNameResult.throwable is RSException) {
                    GlobalStatic.g_errNumber = changeDeviceNameResult.throwable.errorCode
                    GlobalStatic.g_err = changeDeviceNameResult.throwable.userMessage
                    changeDeviceNameResult
                } else {
                    GlobalStatic.g_errNumber = ErrorCode.UNKNOWN_ERROR
                    Result.failure(RSException(ErrorCode.UNKNOWN_ERROR, changeDeviceNameResult.throwable.message
                            ?: ""))
                }
            }
        }
    }

    override fun agentAccountChange(guid: String, oldId: String, oldPasswd: String, newId: String, newPasswd: String): Result<AccountChangeResult> {
        return when (val accountChangeResult = webConnection.agentAccountChange(guid, oldId, oldPasswd, newId, newPasswd)) {
            is Result.Success -> accountChangeResult
            is Result.Failure -> {
                if (accountChangeResult.throwable is RSException) {
                    GlobalStatic.g_errNumber = accountChangeResult.throwable.errorCode
                    GlobalStatic.g_err = accountChangeResult.throwable.userMessage
                    accountChangeResult
                } else {
                    GlobalStatic.g_errNumber = ErrorCode.UNKNOWN_ERROR
                    Result.failure(RSException(ErrorCode.UNKNOWN_ERROR, accountChangeResult.throwable.message
                            ?: ""))
                }
            }
        }
    }

    override fun sendLiveView(server: String, page: String, port: Int, guid: String, width: String, height: String, imagePath: String, filePath: String): Result<SendLiveViewResult> {
        return when (val sendLiveViewResult = webConnection.sendLiveView(server, page, port, guid, width, height, imagePath, filePath)) {
            is Result.Success -> sendLiveViewResult
            is Result.Failure -> {
                if (sendLiveViewResult.throwable is RSException) {
                    GlobalStatic.g_errNumber = sendLiveViewResult.throwable.errorCode
                    GlobalStatic.g_err = sendLiveViewResult.throwable.userMessage
                    sendLiveViewResult
                } else {
                    GlobalStatic.g_errNumber = ErrorCode.UNKNOWN_ERROR
                    Result.failure(RSException(ErrorCode.UNKNOWN_ERROR, sendLiveViewResult.throwable.message
                            ?: ""))
                }
            }
        }
    }
}
