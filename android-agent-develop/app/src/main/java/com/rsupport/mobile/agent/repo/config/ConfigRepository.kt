package com.rsupport.mobile.agent.repo.config

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import androidx.core.content.edit
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.constant.AgentBasicInfo
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.constant.PreferenceConstant
import com.rsupport.mobile.agent.status.AgentStatus
import com.rsupport.mobile.agent.utils.AgentSQLiteHelper
import org.koin.java.KoinJavaComponent.inject
import java.util.*

class ConfigRepository {

    companion object {
        private const val PREF_TUTORIAL = "tutorial"
        private const val TUTORIAL_KEY_OCCURED = "tutorial_first"

        private const val PREF_SETTING = "rssetting.init"
        private const val SETTING_KEY_PROXY_USE = "proxyuse"
        private const val SETTING_KEY_PROXY_ADDRESS = "proxyaddr"
        private const val SETTING_KEY_PROXY_PORT = "proxyport"
        private const val SETTING_KEY_PROXY_ID = "proxyid"
        private const val SETTING_KEY_PROXY_PWD = "proxypasswd"

        private const val PREF_LOGIN = "pref.login"
        private const val LOGIN_KEY_AGENT_INFO_FIRST = "agenet_info_first"

        private const val PREF_START = "rsflag.isstartapp"
        private const val START_KEY_IS_STARTED = "isstart"

        private const val PREF_NOTICE = "notice"
        private val NOTICE_KEY_NEW_SEQ = "newNoticeSeq"
        private val NOTICE_KEY_OLD_SEQ = "oldNoticeSeq"
    }


    private val context: Context by inject(Context::class.java)
    private val agentStatus by inject(AgentStatus::class.java)

    fun delete() {
        val sqlhelper = AgentSQLiteHelper(context)
        sqlhelper.initDB()
        sqlhelper.deleteAllTable()
        AgentBasicInfo.removeAgentGuid(context)
        AgentBasicInfo.removeAllAgentInfos(context)
        agentStatus.setLogOut()

        getTutorialPref().edit { clear() }
        getSettingPref().edit { clear() }
        getStartAppPref().edit { clear() }
        getLoginPref().edit { clear() }
        getNoticePref().edit { clear() }
    }

    fun updateLoginGroupId(groupId: String) {
        GlobalStatic.g_loginInfo.corpid = groupId
        getSettingPref().edit {
            putString(PreferenceConstant.RV_SERVER_CORP_ID, groupId)
        }
    }

    /**
     * 설정된 기업용 서버 주소를 반환한다.
     */
    fun getCorpServerURL(): ServerInfo {
        return (getSettingPref().getString(PreferenceConstant.RV_SERVER_IP_CORP, context.getString(R.string.serverip_biz))
                ?: context.getString(R.string.serverip_biz)).let {
            ServerInfo(it)
        }
    }

    /**
     * 설정된 개인용 서버 주소를 반환한다.
     */
    fun getPersonalServerURL(): ServerInfo {
        return (getSettingPref().getString(PreferenceConstant.RV_SERVER_IP_PERSONAL, context.getString(R.string.serverip_biz))
                ?: context.getString(R.string.serverip_personal)).let {
            ServerInfo(it)
        }
    }


    /**
     * 사용자 지정 서버 정보를 반환한다.
     */
    fun getCustomServerURL(): ServerInfo {
        return ServerInfo((getSettingPref().getString(PreferenceConstant.RV_SERVER_IP_SERVER, "")
                ?: ""))
    }

    /**
     * 사용자 지정 서버 정보를 저장한다.
     */
    fun setCustomServerURL(serverURL: String, isSSL: Boolean = false) {
        getSettingPref().edit {
            if (serverURL.toLowerCase(Locale.ENGLISH).startsWith("http")) {
                putString(PreferenceConstant.RV_SERVER_IP_SERVER, serverURL)
            } else {
                if (isSSL) {
                    putString(PreferenceConstant.RV_SERVER_IP_SERVER, "https://$serverURL")
                } else {
                    putString(PreferenceConstant.RV_SERVER_IP_SERVER, "http://$serverURL")
                }
            }
        }
    }

    /**
     * 설정된 서버 정보를 반환한다.
     * @return 서버 정보 standard/ enterprise 제품일 경우 [getCorpServerURL], 그렇지 않을 경우 [getCustomServerURL]
     */
    fun getServerInfo(): ServerInfo {
        return when (getProductType()) {
            GlobalStatic.PRODUCT_PERSONAL -> getPersonalServerURL()
            GlobalStatic.PRODUCT_CORP -> getCorpServerURL()
            GlobalStatic.PRODUCT_SERVER -> getCustomServerURL()
            else -> getCorpServerURL()
        }
    }

    /**
     * 제품종류를 반혼한다.
     * [GlobalStatic.PRODUCT_CORP], [GlobalStatic.PRODUCT_PERSONAL], [GlobalStatic.PRODUCT_SERVER]
     * 설정에서 Standard/Enterprise 를 선택하면 [GlobalStatic.PRODUCT_CORP]
     * 설정에서 Standard/Enterprise 를 선택후 GroupID 를 입력하지 않으면 [GlobalStatic.PRODUCT_PERSONAL] 로 로그인 단계에서 변경된다.
     * 설정에서 서버 정보를 설정하면 [GlobalStatic.PRODUCT_SERVER] 로 로그인 단계에서 변경된다.
     * @return 제품의 종류
     */
    fun getProductType(): Int {
        return getSettingPref().getInt(PreferenceConstant.RV_SERVER_TYPE, GlobalStatic.PRODUCT_CORP)
    }

    /**
     * 제품의 종류를 설정한다.
     * [GlobalStatic.PRODUCT_CORP], [GlobalStatic.PRODUCT_PERSONAL], [GlobalStatic.PRODUCT_SERVER]
     * @param type 제품의 종류
     */
    fun setProductType(type: Int) {
        getSettingPref().edit {
            putInt(PreferenceConstant.RV_SERVER_TYPE, type)
        }
    }

    private fun getSettingPref() =
            context.getSharedPreferences(PREF_SETTING, Activity.MODE_PRIVATE)


    fun isShowTutorial(): Boolean {
        return getTutorialPref().getBoolean(TUTORIAL_KEY_OCCURED, false)
    }

    private fun getTutorialPref(): SharedPreferences {
        return context.getSharedPreferences(PREF_TUTORIAL, Context.MODE_PRIVATE)
    }

    fun setShowTutorial(isShow: Boolean) {
        getTutorialPref().edit {
            putBoolean(TUTORIAL_KEY_OCCURED, isShow)
        }
    }

    private fun getStartAppPref() = context.getSharedPreferences(PREF_START, Activity.MODE_PRIVATE)

    fun setStartApp(started: Boolean) {
        getStartAppPref().edit {
            putBoolean(START_KEY_IS_STARTED, started)
        }
    }

    fun getStartApp(): Boolean {
        return getStartAppPref().getBoolean(START_KEY_IS_STARTED, false)
    }


    fun toggleProxyUse() {
        setUseProxy(!isProxyUse())
    }

    fun setUseProxy(use: Boolean) {
        getSettingPref().edit {
            putBoolean(SETTING_KEY_PROXY_USE, use)
        }
    }

    fun isProxyUse(): Boolean {
        return getSettingPref().getBoolean(SETTING_KEY_PROXY_USE, false)
    }

    fun setProxyInfo(proxyInfo: ProxyInfo) {
        getSettingPref().edit {
            putString(SETTING_KEY_PROXY_ADDRESS, proxyInfo.address)
            putString(SETTING_KEY_PROXY_PORT, proxyInfo.port)
            putString(SETTING_KEY_PROXY_ID, proxyInfo.id)
            putString(SETTING_KEY_PROXY_PWD, proxyInfo.pwd)
        }
    }

    fun getProxyInfo(): ProxyInfo {
        return getSettingPref().let {
            ProxyInfo(
                    it.getString(SETTING_KEY_PROXY_ADDRESS, "") ?: "",
                    it.getString(SETTING_KEY_PROXY_PORT, "") ?: "",
                    it.getString(SETTING_KEY_PROXY_ID, "") ?: "",
                    it.getString(SETTING_KEY_PROXY_PWD, "") ?: ""
            )
        }
    }


    fun isFirstLaunch(): Boolean {
        return getLoginPref().getBoolean(LOGIN_KEY_AGENT_INFO_FIRST, true)
    }

    private fun getLoginPref(): SharedPreferences {
        return context.getSharedPreferences(PREF_LOGIN, Activity.MODE_PRIVATE)
    }

    fun setFirstLaunch(isFirst: Boolean) {
        getLoginPref().edit {
            putBoolean(LOGIN_KEY_AGENT_INFO_FIRST, isFirst)
        }
    }

    private fun getNoticePref(): SharedPreferences {
        return context.getSharedPreferences(PREF_NOTICE, Context.MODE_PRIVATE)
    }

    /**
     * 새로운 공지가 있는지 확인한다.
     */
    fun hasNewNotice(): Boolean {
        return getNoticePref().let {
            val newNoticeSeq = it.getInt(NOTICE_KEY_NEW_SEQ, 0)
            val oleNoticeSeq = it.getInt(NOTICE_KEY_OLD_SEQ, 0)
            return@let (newNoticeSeq > oleNoticeSeq && getProductType() != GlobalStatic.PRODUCT_SERVER)
        }
    }

    /**
     * 새로운 공지 번호를 저장한다.
     */
    fun setNewNoticeSeq(seq: Int) {
        getNoticePref().edit {
            putInt(NOTICE_KEY_NEW_SEQ, seq)
        }
    }

    /**
     * 새로운 공지를 사용자가 확인했음으로 동기화한다.
     */
    fun syncNoticeSeq() {
        getNoticePref().apply {
            edit {
                putInt(NOTICE_KEY_OLD_SEQ, getInt(NOTICE_KEY_NEW_SEQ, 0))
            }
        }
    }
}


data class ProxyInfo(
        val address: String = "",
        val port: String = "",
        val id: String = "",
        val pwd: String = ""
) {
    fun isValidate(): Boolean {
        if (TextUtils.isEmpty(address) || TextUtils.isEmpty(port)) {
            return false
        }

        try {
            Integer.parseInt(port)
        } catch (e: Exception) {
            return false
        }

        return true
    }
}