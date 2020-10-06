package com.rsupport.mobile.agent.constant.legacy

import android.content.Context
import com.rsupport.mobile.agent.api.model.ConsoleLoginResult
import com.rsupport.mobile.agent.constant.AgentBasicInfo
import com.rsupport.mobile.agent.constant.GlobalStatic
import org.koin.java.KoinJavaComponent.inject

class ConsoleLoginResultUpdater {

    private val context by inject(Context::class.java)


    fun update(consoleLoginResult: ConsoleLoginResult) {
        with(consoleLoginResult) {
            if (webServer.contains("https://")) {
                GlobalStatic.connectionInfo.webProtocol = "https"
            } else {
                GlobalStatic.connectionInfo.webProtocol = "http"
            }
            GlobalStatic.connectionInfo.webServer = webServer
            GlobalStatic.connectionInfo.webServerPort = webServerPort
            GlobalStatic.connectionInfo.agentlisturl = agentlisturl
            GlobalStatic.connectionInfo.agentupdateurl = agentupdateurl
            GlobalStatic.connectionInfo.userKey = userKey
            GlobalStatic.connectionInfo.newVersion = newVersion
            GlobalStatic.connectionInfo.accountLock = accountLock
            GlobalStatic.connectionInfo.waitLockTime = waitLockTime
            GlobalStatic.connectionInfo.rvoemtype = rvOemType
            GlobalStatic.connectionInfo.loginFailCount = loginFailCount
            GlobalStatic.connectionInfo.newnoticeseq = newNoticeSeq
            GlobalStatic.connectionInfo.passwordlimitdays = passwordLimitDays


            AgentBasicInfo.RV_AGENT_URL_VALIDATE = agentURLValidate
            AgentBasicInfo.RV_AGENT_URL_LOGIN = agentLoginURL
            AgentBasicInfo.RV_AGENT_URL_INSTALL = agentInstallURL
            AgentBasicInfo.setAgentBizID(context, bizID)
            AgentBasicInfo.setAccessToken(context, accessToken)
            AgentBasicInfo.setRefreshTokenURL(context, refreshTokenURL)
            AgentBasicInfo.setRefreshToken(context, refreshToken)
            AgentBasicInfo.setApiVersion(context, apiVersion)
        }
    }
}