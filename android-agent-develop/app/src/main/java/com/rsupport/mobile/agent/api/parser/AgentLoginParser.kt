package com.rsupport.mobile.agent.api.parser

import com.rsupport.mobile.agent.api.model.AgentLoginResult
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.XMLParser
import com.rsupport.rscommon.define.RSErrorCode
import com.rsupport.rscommon.exception.RSException
import com.rsupport.util.log.RLog
import java.io.InputStream
import java.util.*

class AgentLoginParser : StreamParser<AgentLoginResult> {

    override fun parse(inputStream: InputStream): Result<AgentLoginResult> {
        return inputStream.use {
            val map: HashMap<String, String> = XMLParser().parse(inputStream)
            if (map.isEmpty()) {
                return@use Result.failure(RSException(RSErrorCode.Parser.XML_IO_ERROR, ""))
            }

            var isSuccess = false
            var errorCode = ErrorCode.UNKNOWN_ERROR
            var errorMessage = ""
            val keys: Set<String> = map.keys
            val iterator = keys.iterator()

            val agentLoginResult = AgentLoginResult()

            while (iterator.hasNext()) {
                val key = iterator.next().toUpperCase()
                val value = map[key]
                if (key == "RESULT") {
                    if (value == "0") {
                        isSuccess = true
                    } else {
                        errorCode = value!!.toDouble().toInt()
                        isSuccess = false
                    }
                } else if (key == "ERRORMSG") {
                    errorMessage = value ?: ""
                } else if (key == "ID") {
                    agentLoginResult.RV_AGENT_ID = value ?: ""
                } else if (key == "SESSIONIP_LENGTH") {
                    agentLoginResult.RV_AGENT_SESSIONIP_LENGTH = value ?: ""
                } else if (key == "SESSIONIP") {
                    agentLoginResult.RV_AGENT_SESSIONIP = value ?: ""
                } else if (key == "SESSIONPORT") {
                    agentLoginResult.RV_AGENT_SESSIONPORT = value ?: ""
                } else if (key == "SESSIONIP2") {
                    agentLoginResult.RV_AGENT_SESSIONIP2 = value ?: ""
                } else if (key == "SESSIONPORT2") {
                    agentLoginResult.RV_AGENT_SESSIONPORT2 = value ?: ""
                } else if (key == "DISPLAYNAME") {
                    agentLoginResult.RV_AGENT_DISPLAYNAME = value ?: ""
                } else if (key == "SSL") {
                    agentLoginResult.RV_AGENT_SSL = value ?: ""
                } else if (key == "SHAREDFOLDER") {
                    agentLoginResult.RV_AGENT_SHAREDFOLDER = value ?: ""
                } else if (key == "LOGOUT_PAGE") {
                    agentLoginResult.RV_AGENT_LOGOUT_URL = value ?: ""
                } else if (key == "SESSIONRESULT_PAGE") {
                    agentLoginResult.RV_AGENT_SESSIONRESULT_PAGE = value ?: ""
                } else if (key == "ACCOUNTCHANGE_PAGE") {
                    agentLoginResult.RV_AGENT_ACCOUNTCHANGE_PAGE = value ?: ""
                } else if (key == "CONFIGMODIFY_PAGE") {
                    agentLoginResult.RV_AGENT_CONFIGMODIFY_PAGE = value ?: ""
                } else if (key == "CONFIGQUERY_PAGE") {
                    agentLoginResult.RV_AGENT_CONFIGQUERY_PAGE = value ?: ""
                } else if (key == "RMTCALLCONNECT_PAGE") {
                    agentLoginResult.RV_AGENT_RMTCALLCONNECT_PAGE = value ?: ""
                } else if (key == "RMTCALLDISCONNECT_PAGE") {
                    agentLoginResult.RV_AGENT_RMTCALLDISCONNECT_PAGE = value ?: ""
                } else if (key == "RMTFTPCONNECT_PAGE") {
                    agentLoginResult.RV_AGENT_RMTFTPCONNECT_PAGE = value ?: ""
                } else if (key == "RMTFTPDISCONNECT_PAGE") {
                    agentLoginResult.RV_AGENT_RMTFTPDISCONNECT_PAGE = value ?: ""
                } else if (key == "AGENTHELP_PAGE") {
                    agentLoginResult.RV_AGENT_AGENTHELP_PAGE = value ?: ""
                } else if (key == "LOGIN_PAGE") {
                    agentLoginResult.RV_AGENT_LOGIN_URL = value ?: ""
                } else if (key == "FCM_REGISTRATION_URL") {
                    agentLoginResult.FCM_REGISTRATION_URL = value ?: ""
                } else if (key == "AUTHWEB_SERVER") {
                    agentLoginResult.RV_AGENT_AUTHWEB_SERVER = value ?: ""
                } else if (key == "AUTHWEB_PORT") {
                    agentLoginResult.RV_AGENT_AUTHWEB_PORT = value ?: ""
                } else if (key == "AUTHWEB_SERVER2") {
                    agentLoginResult.RV_AGENT_AUTHWEB_SERVER2 = value ?: ""
                } else if (key == "UPDATECHECK_PAGE") {
                    agentLoginResult.RV_AGENT_UPDATECHECK_PAGE = value ?: ""
                } else if (key == "UPDATEADDR") {
                    agentLoginResult.RV_AGENT_UPDATEADDR = value ?: ""
                } else if (key == "UPDATEPORT") {
                    agentLoginResult.RV_AGENT_UPDATEPORT = value ?: ""
                } else if (key == "UPDATEADDR2") {
                    agentLoginResult.RV_AGENT_UPDATEADDR2 = value ?: ""
                } else if (key == "UPDATEDIR") {
                    agentLoginResult.RV_AGENT_UPDATEDIR = value ?: ""
                } else if (key == "AUTOSCREENLOCK") {
                    agentLoginResult.RV_AGENT_AUTOSCREENLOCK = value ?: ""
                } else if (key == "AUTOSYSTEMLOCK") {
                    agentLoginResult.RV_AGENT_AUTOSYSTEMLOCK = value ?: ""
                } else if (key == "ENABLED_EXT") {
                    agentLoginResult.RV_AGENT_ENABLED_EXT = value ?: ""
                } else if (key == "CRASH_SERVER") {
                    agentLoginResult.RV_AGENT_CRASH_SERVER = value ?: ""
                } else if (key == "CRASH_PAGE") {
                    agentLoginResult.RV_AGENT_CRASH_PAGE = value ?: ""
                } else if (key == "RVOEMTYPE") {
                    agentLoginResult.RV_AGENT_RVOEMTYPE = value ?: ""
                } else if (key == "ASSET_TIMER") {
                    agentLoginResult.RV_AGENT_ASSET_TIMER = value ?: ""
                } else if (key == "RANDOMPROCESS") {
                    agentLoginResult.RV_AGENT_RANDOMPROCESS = value ?: ""
                } else if (key == "CONNECT_SSL_TYPE") {
                    agentLoginResult.RV_AGENT_CONNECT_SSL_TYPE = value ?: ""
                } else if (key == "CONNECT_SERVER_TYPE") {
                    agentLoginResult.RV_AGENT_CONNECT_SERVER_TYPE = value ?: ""
                } else if (key == "PUSHSERVER_ADDRESS") {
                    agentLoginResult.RV_AGENT_PUSHSERVER_ADDRESS = value ?: ""
                } else if (key == "PUSHSERVER_PORT") {
                    agentLoginResult.RV_AGENT_PUSHSERVER_PORT = value ?: ""
                } else if (key == "RSPERM_DOWNLOAD_URL") {
                    agentLoginResult.RV_AGENT_RSPERM_DOWNLOAD_URL = value ?: ""
                } else if (key == "LOGOUT_TOPIC") {
                    agentLoginResult.RV_AGENT_PUSHSERVER_WILLTOPIC = value ?: ""
                } else if (key == "NEWVERSION") {
                    agentLoginResult.RV_AGENT_NEWVERSION = value ?: ""
                } else if (key == "MQTT_CONNECT_URI_TYPE") {
                    agentLoginResult.RV_AGENT_PUSH_SSL = value == "1"
                }
                RLog.d("agentLogin : $key=$value")
            }

            if (isSuccess) {
                Result.success(agentLoginResult)
            } else {
                Result.failure(RSException(errorCode, errorMessage))
            }
        }
    }
}