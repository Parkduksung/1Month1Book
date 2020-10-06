package com.rsupport.mobile.agent.constant.legacy

import android.content.Context
import com.rsupport.mobile.agent.api.model.AgentLoginResult
import com.rsupport.mobile.agent.constant.AgentBasicInfo
import org.koin.java.KoinJavaComponent

class AgentLoginResultUpdater {

    private val context by KoinJavaComponent.inject(Context::class.java)

    public fun update(agentLoginResult: AgentLoginResult) {
        AgentBasicInfo.setLoginURL(context, agentLoginResult.RV_AGENT_LOGIN_URL)
        AgentBasicInfo.setLogoutURL(context, agentLoginResult.RV_AGENT_LOGOUT_URL)
        AgentBasicInfo.setAgentName(context, agentLoginResult.RV_AGENT_DISPLAYNAME)
        AgentBasicInfo.setFCMRegistChangeURL(context, agentLoginResult.FCM_REGISTRATION_URL)

        AgentBasicInfo.RV_AGENT_ID = agentLoginResult.RV_AGENT_ID
        AgentBasicInfo.RV_AGENT_SESSIONIP_LENGTH = agentLoginResult.RV_AGENT_SESSIONIP_LENGTH
        AgentBasicInfo.RV_AGENT_SESSIONIP = agentLoginResult.RV_AGENT_SESSIONIP
        AgentBasicInfo.RV_AGENT_SESSIONPORT = agentLoginResult.RV_AGENT_SESSIONPORT
        AgentBasicInfo.RV_AGENT_SESSIONIP2 = agentLoginResult.RV_AGENT_SESSIONIP2
        AgentBasicInfo.RV_AGENT_SESSIONPORT2 = agentLoginResult.RV_AGENT_SESSIONPORT2
        AgentBasicInfo.RV_AGENT_DISPLAYNAME = agentLoginResult.RV_AGENT_DISPLAYNAME
        AgentBasicInfo.RV_AGENT_SSL = agentLoginResult.RV_AGENT_SSL
        AgentBasicInfo.RV_AGENT_SHAREDFOLDER = agentLoginResult.RV_AGENT_SHAREDFOLDER
        AgentBasicInfo.RV_AGENT_SESSIONRESULT_PAGE = agentLoginResult.RV_AGENT_SESSIONRESULT_PAGE
        AgentBasicInfo.RV_AGENT_ACCOUNTCHANGE_PAGE = agentLoginResult.RV_AGENT_ACCOUNTCHANGE_PAGE
        AgentBasicInfo.RV_AGENT_CONFIGMODIFY_PAGE = agentLoginResult.RV_AGENT_CONFIGMODIFY_PAGE
        AgentBasicInfo.RV_AGENT_CONFIGQUERY_PAGE = agentLoginResult.RV_AGENT_CONFIGQUERY_PAGE
        AgentBasicInfo.RV_AGENT_RMTCALLCONNECT_PAGE = agentLoginResult.RV_AGENT_RMTCALLCONNECT_PAGE
        AgentBasicInfo.RV_AGENT_RMTCALLDISCONNECT_PAGE = agentLoginResult.RV_AGENT_RMTCALLDISCONNECT_PAGE
        AgentBasicInfo.RV_AGENT_RMTFTPCONNECT_PAGE = agentLoginResult.RV_AGENT_RMTFTPCONNECT_PAGE
        AgentBasicInfo.RV_AGENT_RMTFTPDISCONNECT_PAGE = agentLoginResult.RV_AGENT_RMTFTPDISCONNECT_PAGE
        AgentBasicInfo.RV_AGENT_AGENTHELP_PAGE = agentLoginResult.RV_AGENT_AGENTHELP_PAGE
        AgentBasicInfo.RV_AGENT_AUTHWEB_SERVER = agentLoginResult.RV_AGENT_AUTHWEB_SERVER
        AgentBasicInfo.RV_AGENT_AUTHWEB_PORT = agentLoginResult.RV_AGENT_AUTHWEB_PORT
        AgentBasicInfo.RV_AGENT_AUTHWEB_SERVER2 = agentLoginResult.RV_AGENT_AUTHWEB_SERVER2
        AgentBasicInfo.RV_AGENT_UPDATECHECK_PAGE = agentLoginResult.RV_AGENT_UPDATECHECK_PAGE
        AgentBasicInfo.RV_AGENT_UPDATEADDR = agentLoginResult.RV_AGENT_UPDATEADDR
        AgentBasicInfo.RV_AGENT_UPDATEPORT = agentLoginResult.RV_AGENT_UPDATEPORT
        AgentBasicInfo.RV_AGENT_UPDATEADDR2 = agentLoginResult.RV_AGENT_UPDATEADDR2
        AgentBasicInfo.RV_AGENT_UPDATEDIR = agentLoginResult.RV_AGENT_UPDATEDIR
        AgentBasicInfo.RV_AGENT_AUTOSCREENLOCK = agentLoginResult.RV_AGENT_AUTOSCREENLOCK
        AgentBasicInfo.RV_AGENT_AUTOSYSTEMLOCK = agentLoginResult.RV_AGENT_AUTOSYSTEMLOCK
        AgentBasicInfo.RV_AGENT_ENABLED_EXT = agentLoginResult.RV_AGENT_ENABLED_EXT
        AgentBasicInfo.RV_AGENT_CRASH_SERVER = agentLoginResult.RV_AGENT_CRASH_SERVER
        AgentBasicInfo.RV_AGENT_CRASH_PAGE = agentLoginResult.RV_AGENT_CRASH_PAGE
        AgentBasicInfo.RV_AGENT_RVOEMTYPE = agentLoginResult.RV_AGENT_RVOEMTYPE
        AgentBasicInfo.RV_AGENT_ASSET_TIMER = agentLoginResult.RV_AGENT_ASSET_TIMER
        AgentBasicInfo.RV_AGENT_RANDOMPROCESS = agentLoginResult.RV_AGENT_RANDOMPROCESS
        AgentBasicInfo.RV_AGENT_CONNECT_SSL_TYPE = agentLoginResult.RV_AGENT_CONNECT_SSL_TYPE
        AgentBasicInfo.RV_AGENT_CONNECT_SERVER_TYPE = agentLoginResult.RV_AGENT_CONNECT_SERVER_TYPE
        AgentBasicInfo.RV_AGENT_PUSHSERVER_ADDRESS = agentLoginResult.RV_AGENT_PUSHSERVER_ADDRESS
        AgentBasicInfo.RV_AGENT_PUSHSERVER_PORT = agentLoginResult.RV_AGENT_PUSHSERVER_PORT
        AgentBasicInfo.RV_AGENT_RSPERM_DOWNLOAD_URL = agentLoginResult.RV_AGENT_RSPERM_DOWNLOAD_URL
        AgentBasicInfo.RV_AGENT_PUSHSERVER_WILLTOPIC = agentLoginResult.RV_AGENT_PUSHSERVER_WILLTOPIC
        AgentBasicInfo.RV_AGENT_NEWVERSION = agentLoginResult.RV_AGENT_NEWVERSION
        AgentBasicInfo.RV_AGENT_PUSH_SSL = agentLoginResult.RV_AGENT_PUSH_SSL
    }
}