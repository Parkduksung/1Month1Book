package com.rsupport.mobile.agent.api.parser

import com.rsupport.mobile.agent.api.model.ConsoleLoginResult
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.XMLParser
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import com.rsupport.util.log.RLog
import java.io.InputStream
import java.util.*

class ConsoleLoginParser : StreamParser<ConsoleLoginResult> {


    override fun parse(inputStream: InputStream): Result<ConsoleLoginResult> {
        return inputStream.use {
            val map: HashMap<String, String> = XMLParser().parse(inputStream)
            if (map.isEmpty()) {
                return@use Result.failure(RSException(RSErrorCode.Parser.XML_IO_ERROR, ""))
            }

            var isSuccess = false
            var errorCode = ErrorCode.UNKNOWN_ERROR
            var errorMessage = ""
            val keys: Set<String> = map.keys
            val keyIteractor = keys.iterator()

            val consoleLoginResult = ConsoleLoginResult()

            while (keyIteractor.hasNext()) {
                val key = keyIteractor.next().toUpperCase()
                var value = map[key]
                if (key == "RETCODE") {
                    if (value == "100") {
                        isSuccess = true
                    } else {
                        errorCode = value?.toDouble()?.toInt() ?: ErrorCode.UNKNOWN_ERROR
                        isSuccess = false
                    }
                } else if (key == "RESULT") {
                    if (value == "100") {
                        isSuccess = true
                    } else {
                        errorCode = value?.toDouble()?.toInt() ?: ErrorCode.UNKNOWN_ERROR
                        isSuccess = false
                    }
                } else if (key == "WEBSERVER") {
                    consoleLoginResult.webServer = value ?: ""
                } else if (key == "WEBSERVERPORT") {
                    consoleLoginResult.webServerPort = value ?: ""
                } else if (key == "AGENT_GROUP_LIST_URL") {
                    consoleLoginResult.agentlisturl = value ?: ""
                } else if (key == "AGENTUPDATEURL") {
                    consoleLoginResult.agentupdateurl = value ?: ""
                } else if (key == "USERKEY") {
                    consoleLoginResult.userKey = value ?: ""
                } else if (key == "NEWVERSION") {
                    consoleLoginResult.newVersion = value ?: ""
                } else if (key == "ACCOUNTLOCK") {
                    consoleLoginResult.accountLock = value ?: ""
                } else if (key == "WAITLOCKTIME") {
                    if (value == null || value == "") {
                        value = "00:00"
                    }
                    consoleLoginResult.waitLockTime = value
                } else if (key == "RVOEMTYPE") {    //원격탐색기 옵션 (web.config 설정) (서버옵션)
                    consoleLoginResult.rvOemType = value ?: ""
                } else if (key == "LOGINFAILCOUNT") {
                    consoleLoginResult.loginFailCount = value ?: ""
                } else if (key == "NEWNOTICESEQ") {
                    consoleLoginResult.newNoticeSeq = value ?: ""
                } else if (key == "PASSWORDLIMITDAYS") {
                    // 비밀번호 만료기간
                    consoleLoginResult.passwordLimitDays = value ?: ""
                } else if (key == "AGENT_INSTALL_VALIDATE_URL") {
                    consoleLoginResult.agentURLValidate = value ?: ""
                } else if (key == "AGENT_LOGIN_URL") {
                    consoleLoginResult.agentLoginURL = value ?: ""
                } else if (key == "AGENT_INSTALL_OK_URL") {
                    consoleLoginResult.agentInstallURL = value ?: ""
                } else if (key == "BIZID") {
                    consoleLoginResult.bizID = value ?: ""
                } else if (key == "ACCESS_TOKEN") {
                    consoleLoginResult.accessToken = value ?: ""
                } else if (key == "REFRESH_TOKEN") {
                    consoleLoginResult.refreshToken = value ?: ""
                } else if (key == "API_VERSION") {
                    consoleLoginResult.apiVersion = value ?: ""
                } else if (key == "REFRESH_TOKEN_URL") {
                    consoleLoginResult.refreshTokenURL = value ?: ""
                }
                RLog.d("consoleLogin : $key=$value")
            }

            if (isSuccess) {
                Result.success(consoleLoginResult)
            } else {
                Result.failure(RSException(errorCode))
            }
        }
    }
}