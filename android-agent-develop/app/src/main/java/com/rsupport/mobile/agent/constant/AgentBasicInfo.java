package com.rsupport.mobile.agent.constant;

import android.content.Context;
import android.content.SharedPreferences;

import com.rsupport.mobile.agent.api.model.AgentLoginResult;
import com.rsupport.mobile.agent.utils.Utility;

public class AgentBasicInfo {

    public static final String RV_AGENT_SHARD_PARAM = "REMOTE_VIEW_AGENT";
    public static final String RV_AGENT_SHARD_PARAM_PUBLIC = "RV_AGENT_SHARD_PARAM_PUBLIC";

    public static final String RV_AGENT_SHARD_PARAM_RV_ID = "RV_AGENT_SHARD_PARAM_RV_ID";
    public static final String RV_AGENT_SHARD_PARAM_URL_LOGIN = "RV_AGENT_SHARD_PARAM_URL_LOGIN";
    public static final String RV_AGENT_SHARD_PARAM_URL_LOGOUT = "RV_AGENT_SHARD_PARAM_URL_LOGOUT";
    public static final String RV_AGENT_SHARD_PARAM_AGENT_GUID = "RV_AGENT_SHARD_PARAM_AGENT_GUID";
    public static final String RV_AGENT_SHARD_PARAM_AGENT_BIZID = "RV_AGENT_SHARD_PARAM_AGENT_BIZID";
    public static final String RV_AGENT_SHARD_PARAM_AGENT_NAME = "RV_AGENT_SHARD_PARAM_AGENT_NAME";
    public static final String RV_AGENT_SHARD_PARAM_AGENT_WEB_SERVER = "RV_AGENT_SHARD_PARAM_AGENT_WEB_SERVER";
    public static final String RV_AGENT_SHARD_PARAM_AGENT_WEB_PORT = "RV_AGENT_SHARD_PARAM_AGENT_WEB_PORT";
    public static final String RV_AGENT_SHARD_PARAM_AGENT_WEB_PROTOCOL = "RV_AGENT_SHARD_PARAM_AGENT_WEB_PROTOCOL";
    public static final String RV_AGENT_SHARD_PARAM_IS_AGENT_START = "RV_AGENT_SHARD_PARAM_IS_AGENT_START";
    public static final String RV_AGENT_SHARD_PARAM_FCM_REGIST_CHANGE = "RV_AGENT_SHARD_PARAM_FCM_REGIST_CHANGE";
    public static final String RV_AGENT_SHARD_PARAM_ACCESS_TOKEN = "RV_AGENT_SHARD_PARAM_ACCESS_TOKEN";
    public static final String RV_AGENT_SHARD_PARAM_REFRESH_TOKEN = "RV_AGENT_SHARD_PARAM_REFRESH_TOKEN";
    public static final String RV_AGENT_SHARD_PARAM_API_VERSION = "RV_AGENT_SHARD_PARAM_API_VERSION";
    public static final String RV_AGENT_SHARD_PARAM_REFRESH_TOKEN_URL = "RV_AGENT_SHARD_PARAM_REFRESH_TOKEN_URL";

    public static String RV_AGENT_URL_LOGIN = "/RemoteView/Command/Agent/Agent_Login.aspx";
    public static final String RV_AGENT_URL_LOGOUT = "/RemoteView/Command/Agent/Agent_Logout.aspx";
    public static String RV_AGENT_URL_INSTALL = "/RemoteView/Command/Agent/Agent_Install.aspx";
    public static String RV_AGENT_URL_ACCOUNT_CHANGE = "/Agent/Agent_Account_Change.aspx";
    public static String RV_AGENT_URL_VALIDATE = "/RemoteView/Command/Agent/Agent_Validate.aspx";
    public static String RV_AGENT_DEVICE_NAME_CHANGE = "/Agent/Agent_Config_Modify.aspx";
    public static String RV_AGENT_URL_FCM_RESIST_CHANGE = "/agent/fcm_register";

    public static String RV_AGENT_ID = "";
    public static String RV_AGENT_SESSIONIP_LENGTH = "";
    public static String RV_AGENT_SESSIONIP = "";
    public static String RV_AGENT_SESSIONPORT = "";
    public static String RV_AGENT_SESSIONIP2 = "";
    public static String RV_AGENT_SESSIONPORT2 = "";
    public static String RV_AGENT_DISPLAYNAME = "";
    public static String RV_AGENT_SSL = "";
    public static String RV_AGENT_SHAREDFOLDER = "";
    public static String RV_AGENT_SESSIONRESULT_PAGE = "";
    public static String RV_AGENT_ACCOUNTCHANGE_PAGE = "";
    public static String RV_AGENT_CONFIGMODIFY_PAGE = "";
    public static String RV_AGENT_CONFIGQUERY_PAGE = "";
    public static String RV_AGENT_RMTCALLCONNECT_PAGE = "";
    public static String RV_AGENT_RMTCALLDISCONNECT_PAGE = "";
    public static String RV_AGENT_RMTFTPCONNECT_PAGE = "";
    public static String RV_AGENT_RMTFTPDISCONNECT_PAGE = "";
    public static String RV_AGENT_AGENTHELP_PAGE = "";
    public static String RV_AGENT_AUTHWEB_SERVER = "";
    public static String RV_AGENT_AUTHWEB_PORT = "";
    public static String RV_AGENT_AUTHWEB_SERVER2 = "";
    public static String RV_AGENT_UPDATECHECK_PAGE = "";
    public static String RV_AGENT_UPDATEADDR = "";
    public static String RV_AGENT_UPDATEPORT = "";
    public static String RV_AGENT_UPDATEADDR2 = "";
    public static String RV_AGENT_UPDATEDIR = "";
    public static String RV_AGENT_AUTOSCREENLOCK = "";
    public static String RV_AGENT_AUTOSYSTEMLOCK = "";
    public static String RV_AGENT_ENABLED_EXT = "";
    public static String RV_AGENT_CRASH_SERVER = "";
    public static String RV_AGENT_CRASH_PAGE = "";
    public static String RV_AGENT_RVOEMTYPE = "";
    public static String RV_AGENT_ASSET_TIMER = "";
    public static String RV_AGENT_RANDOMPROCESS = "";
    public static String RV_AGENT_CONNECT_SSL_TYPE = "";
    public static String RV_AGENT_CONNECT_SERVER_TYPE = "";
    public static String RV_AGENT_PUSHSERVER_ADDRESS = "";
    public static String RV_AGENT_PUSHSERVER_PORT = "";
    public static String RV_AGENT_RSPERM_DOWNLOAD_URL = "";
    public static String RV_AGENT_NEWVERSION = "";
    public static String RV_AGENT_BIZ_ID = "";
    public static String RV_AGENT_PUSHSERVER_WILLTOPIC = "remoteviewlogoutmanager";
    public static Boolean RV_AGENT_PUSH_SSL = false;

    public static String loginKey = null;


    public static SharedPreferences getPreperence(Context context) {
        return context.getSharedPreferences(RV_AGENT_SHARD_PARAM, context.MODE_PRIVATE);
    }

    public static String getAgentGuid(Context context) {
        return getPreperence(context).getString(RV_AGENT_SHARD_PARAM_AGENT_GUID, "");
    }

    public static void setAgentGuid(Context context, String guid) {
        SharedPreferences mPref = getPreperence(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(RV_AGENT_SHARD_PARAM_AGENT_GUID, guid);
        editor.commit();

        //뷰어쪽으로 공유해줄 필요가 있음
        setAgentGuidPublic(context, guid);
    }

    public static String getFCMRegistChangeURL(Context context) {
        return getPreperence(context).getString(RV_AGENT_SHARD_PARAM_FCM_REGIST_CHANGE, RV_AGENT_URL_FCM_RESIST_CHANGE);
    }

    public static void setFCMRegistChangeURL(Context context, String url) {
        SharedPreferences mPref = getPreperence(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(RV_AGENT_SHARD_PARAM_FCM_REGIST_CHANGE, url);
        editor.commit();

    }

    public static void setAgentGuidPublic(Context context, String guid) {
        SharedPreferences mPref = context.getSharedPreferences(RV_AGENT_SHARD_PARAM_PUBLIC, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(RV_AGENT_SHARD_PARAM_AGENT_GUID, guid);
        editor.commit();

//		Log.d("hyu","guid : " + guid);

    }

    public static void removeAgentGuid(Context context) {
        SharedPreferences.Editor editor = getPreperence(context).edit();
        editor.remove(RV_AGENT_SHARD_PARAM_AGENT_GUID);
        editor.commit();

        //뷰어쪽으로 공유해줄 필요가 있음
        removeAgentGuidPublic(context);
    }

    public static void removeAgentGuidPublic(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(RV_AGENT_SHARD_PARAM_PUBLIC, context.MODE_PRIVATE).edit();
        editor.remove(RV_AGENT_SHARD_PARAM_AGENT_GUID);
        editor.commit();
    }

    public static void setIsAgentStart(Context context, boolean isAgentStart) {
        if (Utility.isSamsungPreint3Th()) {
            return;
        }
        SharedPreferences mPref = getPreperence(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(RV_AGENT_SHARD_PARAM_IS_AGENT_START, isAgentStart);
        editor.commit();
    }

    public static boolean getIsAgentStart(Context context) {
        if (Utility.isSamsungPreint3Th()) {
            return GlobalStatic.isFirstAgentStart;
        }
        return getPreperence(context).getBoolean(RV_AGENT_SHARD_PARAM_IS_AGENT_START, false);
    }

    public static String getLoginURL(Context context) {
        return getPreperence(context).getString(RV_AGENT_SHARD_PARAM_URL_LOGIN, RV_AGENT_URL_LOGIN);
    }

    public static void setLoginURL(Context context, String url) {
        SharedPreferences mPref = getPreperence(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(RV_AGENT_SHARD_PARAM_URL_LOGIN, url);
        editor.commit();
    }

    public static String getLogoutURL(Context context) {
        return getPreperence(context).getString(RV_AGENT_SHARD_PARAM_URL_LOGOUT, RV_AGENT_URL_LOGOUT);
    }

    public static void setLogoutURL(Context context, String url) {
        SharedPreferences mPref = getPreperence(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(RV_AGENT_SHARD_PARAM_URL_LOGOUT, url);
        editor.commit();
    }

    public static String getAgentName(Context context) {
        return getPreperence(context).getString(RV_AGENT_SHARD_PARAM_AGENT_NAME, "");
    }

    public static void setAgentName(Context context, String name) {
        SharedPreferences mPref = getPreperence(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(RV_AGENT_SHARD_PARAM_AGENT_NAME, name);
        editor.commit();
    }

    public static String getAgentWebServer(Context context) {
        return getPreperence(context).getString(RV_AGENT_SHARD_PARAM_AGENT_WEB_SERVER, "");
    }

    public static void setAgentWebServer(Context context, String server) {
        SharedPreferences mPref = getPreperence(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(RV_AGENT_SHARD_PARAM_AGENT_WEB_SERVER, server);
        editor.commit();
    }

    public static String getAgentWebPort(Context context) {
        return getPreperence(context).getString(RV_AGENT_SHARD_PARAM_AGENT_WEB_PORT, "");
    }

    public static void setAgentWebPort(Context context, String port) {
        SharedPreferences mPref = getPreperence(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(RV_AGENT_SHARD_PARAM_AGENT_WEB_PORT, port);
        editor.commit();
    }

    public static String getAgentWebProtocol(Context context) {
        return getPreperence(context).getString(RV_AGENT_SHARD_PARAM_AGENT_WEB_PROTOCOL, "");
    }

    public static void setAgentWebProtocol(Context context, String protocol) {
        SharedPreferences mPref = getPreperence(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(RV_AGENT_SHARD_PARAM_AGENT_WEB_PROTOCOL, protocol);
        editor.commit();
    }

    public static String getAgentBizID(Context context) {
        return getPreperence(context).getString(RV_AGENT_SHARD_PARAM_AGENT_BIZID, "");
    }

    public static void setAgentBizID(Context context, String bizId) {
        SharedPreferences mPref = getPreperence(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(RV_AGENT_SHARD_PARAM_AGENT_BIZID, bizId);
        editor.commit();
    }

    public static void removeAllAgentInfos(Context context) {
        SharedPreferences.Editor editor = getPreperence(context).edit();
        editor.clear().apply();
    }

    public static void setAccessToken(Context context, String value) {
        SharedPreferences mPref = getPreperence(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(RV_AGENT_SHARD_PARAM_ACCESS_TOKEN, value);
        editor.commit();
    }

    public static String getAccessToken(Context context) {
        return getPreperence(context).getString(RV_AGENT_SHARD_PARAM_ACCESS_TOKEN, "");
    }

    public static void setRefreshToken(Context context, String value) {
        SharedPreferences mPref = getPreperence(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(RV_AGENT_SHARD_PARAM_REFRESH_TOKEN, value);
        editor.commit();
    }

    public static String getRefreshToken(Context context) {
        return getPreperence(context).getString(RV_AGENT_SHARD_PARAM_REFRESH_TOKEN, "");
    }

    public static void setApiVersion(Context context, String value) {
        SharedPreferences mPref = getPreperence(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(RV_AGENT_SHARD_PARAM_API_VERSION, value);
        editor.commit();
    }

    public static String getApiVersion(Context context) {
        return getPreperence(context).getString(RV_AGENT_SHARD_PARAM_API_VERSION, "");
    }

    public static void setRefreshTokenURL(Context context, String value) {
        SharedPreferences mPref = getPreperence(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(RV_AGENT_SHARD_PARAM_REFRESH_TOKEN_URL, value);
        editor.commit();
    }

    public static String getRefreshTokenURL(Context context) {
        return getPreperence(context).getString(RV_AGENT_SHARD_PARAM_REFRESH_TOKEN_URL, "");
    }
}
